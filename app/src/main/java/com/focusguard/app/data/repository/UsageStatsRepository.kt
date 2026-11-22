package com.focusguard.app.data.repository

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Process
import android.provider.MediaStore
import com.focusguard.app.data.local.dao.AppRuleDao
import com.focusguard.app.data.local.dao.UsageHistoryDao
import com.focusguard.app.data.local.entity.UsageHistoryEntity
import com.focusguard.app.data.model.AppUsageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class UsageStatsRepository(
    private val context: Context,
    private val usageHistoryDao: UsageHistoryDao,
    private val appRuleDao: AppRuleDao
) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    private val packageManager: PackageManager = context.packageManager

    /**
     * Checks if PACKAGE_USAGE_STATS permission has been granted by user in System Settings.
     */
    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Retrieves today's total screen time in milliseconds (from midnight to now).
     */
    suspend fun getTodayTotalScreenTimeMillis(): Long = withContext(Dispatchers.IO) {
        val startOfDay = getStartOfTodayMillis()
        val now = System.currentTimeMillis()
        val stats = queryAggregateUsageStats(startOfDay, now)
        stats.values.sumOf { it.totalTimeInForeground }
    }

    /**
     * Retrieves today's app usage breakdown, sorted by most used.
     */
    suspend fun getTodayAppUsageList(): List<AppUsageInfo> = withContext(Dispatchers.IO) {
        val startOfDay = getStartOfTodayMillis()
        val now = System.currentTimeMillis()
        val statsMap = queryAggregateUsageStats(startOfDay, now)
        val rulesMap = appRuleDao.getAllRules().first().associateBy { it.packageName }
        val ownPkg = context.packageName

        statsMap.values
            .filter { it.totalTimeInForeground > 30_000L && it.packageName != ownPkg } // >30 seconds
            .map { usage ->
                val appLabel = try {
                    val appInfo = packageManager.getApplicationInfo(usage.packageName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    usage.packageName.substringAfterLast('.')
                }

                val icon = try {
                    packageManager.getApplicationIcon(usage.packageName)
                } catch (e: Exception) {
                    null
                }

                val rule = rulesMap[usage.packageName]

                AppUsageInfo(
                    packageName = usage.packageName,
                    appName = appLabel,
                    icon = icon,
                    totalTimeInForegroundMillis = usage.totalTimeInForeground,
                    lastTimeUsed = usage.lastTimeUsed,
                    dailyLimitMinutes = rule?.dailyLimitMinutes,
                    isBlocked = rule?.isBlocked ?: false
                )
            }
            .sortedByDescending { it.totalTimeInForegroundMillis }
    }

    /**
     * Calculates an automated smart insight delta:
     * e.g., compares today's usage on the top app vs yesterday's usage.
     */
    suspend fun getSmartInsight(): String? = withContext(Dispatchers.IO) {
        if (!hasUsageStatsPermission()) return@withContext null

        val startOfToday = getStartOfTodayMillis()
        val now = System.currentTimeMillis()
        val startOfYesterday = startOfToday - TimeUnit.DAYS.toMillis(1)

        val todayStats = queryAggregateUsageStats(startOfToday, now)
        val yesterdayStats = queryAggregateUsageStats(startOfYesterday, startOfToday)

        if (todayStats.isEmpty()) return@withContext null

        // Find the top used app today
        val topToday = todayStats.values.maxByOrNull { it.totalTimeInForeground } ?: return@withContext null
        val topPkg = topToday.packageName
        val todayTime = topToday.totalTimeInForeground
        val yesterdayTime = yesterdayStats[topPkg]?.totalTimeInForeground ?: 0L

        val appName = try {
            val appInfo = packageManager.getApplicationInfo(topPkg, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            topPkg.substringAfterLast('.')
        }

        val deltaMillis = todayTime - yesterdayTime
        val deltaMins = Math.abs(deltaMillis) / (60 * 1000)

        if (deltaMins < 10) {
            val totalTodayMins = todayStats.values.sumOf { it.totalTimeInForeground } / (60 * 1000)
            val totalYesterdayMins = yesterdayStats.values.sumOf { it.totalTimeInForeground } / (60 * 1000)
            return@withContext if (totalTodayMins < totalYesterdayMins) {
                "Great job! Total screen time is trending down compared to yesterday."
            } else {
                "Stay mindful today — take a 5-minute break whenever you feel fatigued."
            }
        }

        val hours = deltaMins / 60
        val mins = deltaMins % 60
        val formattedTime = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

        if (deltaMillis > 0) {
            "You've spent $formattedTime more on $appName than yesterday."
        } else {
            "You've saved $formattedTime on $appName compared to yesterday. Keep it up!"
        }
    }

    /**
     * Gets daily screen time for the past 7 days (day label to total minutes).
     */
    suspend fun getWeeklyDailyMinutes(): List<Pair<String, Float>> = withContext(Dispatchers.IO) {
        val result = mutableListOf<Pair<String, Float>>()
        val cal = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        for (i in 6 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            c.set(Calendar.HOUR_OF_DAY, 0)
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)

            val start = c.timeInMillis
            val end = start + TimeUnit.DAYS.toMillis(1) - 1
            val dayStats = queryAggregateUsageStats(start, end)
            val totalMins = (dayStats.values.sumOf { it.totalTimeInForeground } / (1000 * 60)).toFloat()
            val label = dayFormat.format(Date(start))
            result.add(label to totalMins)
        }
        result
    }

    /**
     * Exports all local screen-time and blocked records to a CSV file in Downloads directory.
     */
    suspend fun exportUsageDataToCsv(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val history = usageHistoryDao.getAllUsageSync()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "FocusGuard_ScreenTime_Export_$timestamp.csv"

            val csvContent = buildString {
                appendLine("Date,Package Name,App Name,Foreground Minutes,Blocked Attempts,Time Saved (Minutes)")
                if (history.isNotEmpty()) {
                    for (item in history) {
                        val dateStr = dateFormat.format(Date(item.epochDay * 86400000L))
                        val mins = item.totalTimeInForegroundMillis / (60 * 1000)
                        appendLine("\"$dateStr\",\"${item.packageName}\",\"${item.appName}\",$mins,${item.blockedAttemptsCount},${item.estimatedTimeSavedMinutes}")
                    }
                } else {
                    // Export today's real-time snapshot if historical table is empty
                    val todayApps = getTodayAppUsageList()
                    val todayDate = dateFormat.format(Date())
                    for (app in todayApps) {
                        appendLine("\"$todayDate\",\"${app.packageName}\",\"${app.appName}\",${app.totalMinutes},0,0")
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return@withContext Result.failure(Exception("Failed to create download file"))

                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvContent.toByteArray())
                }
                Result.success("Exported to Downloads/$fileName")
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetFile = File(downloadsDir, fileName)
                FileOutputStream(targetFile).use { outputStream ->
                    outputStream.write(csvContent.toByteArray())
                }
                Result.success("Saved to ${targetFile.absolutePath}")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getTotalBlockedAttempts(sinceEpochDays: Long = 0L): Flow<Int?> {
        return usageHistoryDao.getTotalBlockedAttemptsSince(sinceEpochDays)
    }

    fun getTotalEstimatedSavedMinutes(sinceEpochDays: Long = 0L): Flow<Long?> {
        return usageHistoryDao.getTotalEstimatedSavedMinutesSince(sinceEpochDays)
    }

    suspend fun recordBlockEvent(packageName: String) = withContext(Dispatchers.IO) {
        val todayEpoch = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        val defaultSavedPerBlock = 10L // Average 10 minutes saved per avoided distraction doomloop
        usageHistoryDao.incrementBlockedAttempt(todayEpoch, packageName, defaultSavedPerBlock)
    }

    private fun queryAggregateUsageStats(startTime: Long, endTime: Long): Map<String, UsageStats> {
        return usageStatsManager?.queryAndAggregateUsageStats(startTime, endTime) ?: emptyMap()
    }

    private fun getStartOfTodayMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
