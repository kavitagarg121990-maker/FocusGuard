package com.focusguard.app.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.focusguard.app.data.local.dao.AppRuleDao
import com.focusguard.app.data.local.dao.BlockScheduleDao
import com.focusguard.app.data.local.dao.ShortContentRuleDao
import com.focusguard.app.data.local.entity.AppRuleEntity
import com.focusguard.app.data.local.entity.BlockScheduleEntity
import com.focusguard.app.data.local.entity.ShortContentRuleEntity
import com.focusguard.app.data.model.InstalledAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar

class AppBlockingRepository(
    private val context: Context,
    private val appRuleDao: AppRuleDao,
    private val shortContentRuleDao: ShortContentRuleDao,
    private val blockScheduleDao: BlockScheduleDao,
    private val preferencesRepository: PreferencesRepository
) {
    private val packageManager: PackageManager = context.packageManager

    val allRules: Flow<List<AppRuleEntity>> = appRuleDao.getAllRules()
    val shortContentRules: Flow<List<ShortContentRuleEntity>> = shortContentRuleDao.getAllRules()
    val blockSchedules: Flow<List<BlockScheduleEntity>> = blockScheduleDao.getAllSchedules()

    /**
     * Loads list of installed apps merged with their local blocking rules.
     */
    suspend fun getInstalledApps(showSystemApps: Boolean = false): List<InstalledAppInfo> = withContext(Dispatchers.IO) {
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val launchablePackages = try {
            packageManager.queryIntentActivities(launcherIntent, 0)
                .mapNotNull { it.activityInfo?.packageName }
                .toSet()
        } catch (e: Exception) {
            emptySet()
        }

        val packages = try {
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            emptyList()
        }

        val currentRules = appRuleDao.getAllRules().first().associateBy { it.packageName }
        val ownPackage = context.packageName

        packages.mapNotNull { appInfo ->
            if (appInfo.packageName == ownPackage) return@mapNotNull null

            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isLaunchable = launchablePackages.contains(appInfo.packageName)

            // When NOT showing system apps:
            // 1. Keep all non-system user-installed apps
            // 2. Keep launchable apps (like YouTube, Chrome) that user interacts with
            // 3. Drop invisible low-level background system services
            if (!showSystemApps && isSystem && !isLaunchable) {
                return@mapNotNull null
            }

            val label = try {
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                appInfo.packageName
            }
            if (label.isBlank()) return@mapNotNull null

            val rule = currentRules[appInfo.packageName]

            InstalledAppInfo(
                packageName = appInfo.packageName,
                appName = label,
                icon = try { packageManager.getApplicationIcon(appInfo) } catch (e: Exception) { null },
                isSystemApp = isSystem,
                isBlocked = rule?.isBlocked ?: false,
                dailyLimitMinutes = rule?.dailyLimitMinutes,
                hasActiveSchedule = false
            )
        }.sortedWith(
            compareByDescending<InstalledAppInfo> { it.isBlocked || it.dailyLimitMinutes != null }
                .thenBy { it.appName.lowercase() }
        )
    }

    suspend fun setAppBlocked(packageName: String, appName: String, isBlocked: Boolean) = withContext(Dispatchers.IO) {
        val existing = appRuleDao.getRuleForPackage(packageName)
        if (existing == null) {
            appRuleDao.upsertRule(
                AppRuleEntity(
                    packageName = packageName,
                    appName = appName,
                    isBlocked = isBlocked
                )
            )
        } else {
            appRuleDao.updateBlockedStatus(packageName, isBlocked)
        }
    }

    suspend fun setAppDailyLimit(
        packageName: String,
        appName: String,
        limitMinutes: Int?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val isPremium = preferencesRepository.isPremium.first()
        if (limitMinutes != null && !isPremium) {
            val count = appRuleDao.countLimitedApps()
            val existing = appRuleDao.getRuleForPackage(packageName)
            // If already at 1 or more and not updating the existing limited app
            if (count >= 1 && (existing?.dailyLimitMinutes == null)) {
                return@withContext Result.failure(
                    IllegalStateException("Free tier allows 1 app daily limit. Upgrade to Premium for unlimited app limits.")
                )
            }
        }

        val existing = appRuleDao.getRuleForPackage(packageName)
        if (existing == null) {
            appRuleDao.upsertRule(
                AppRuleEntity(
                    packageName = packageName,
                    appName = appName,
                    dailyLimitMinutes = limitMinutes
                )
            )
        } else {
            appRuleDao.updateDailyLimit(packageName, limitMinutes)
        }
        Result.success(Unit)
    }

    suspend fun setShortContentRuleEnabled(id: String, isEnabled: Boolean) = withContext(Dispatchers.IO) {
        shortContentRuleDao.setEnabled(id, isEnabled)
    }

    suspend fun addBlockSchedule(schedule: BlockScheduleEntity): Result<Long> = withContext(Dispatchers.IO) {
        val isPremium = preferencesRepository.isPremium.first()
        if (!isPremium) {
            val currentCount = blockScheduleDao.countSchedules()
            if (currentCount >= 1) {
                return@withContext Result.failure(
                    IllegalStateException("Free tier includes 1 active schedule. Unlock Premium for unlimited recurring schedules.")
                )
            }
        }
        val id = blockScheduleDao.insertSchedule(schedule)
        Result.success(id)
    }

    suspend fun deleteBlockSchedule(schedule: BlockScheduleEntity) = withContext(Dispatchers.IO) {
        blockScheduleDao.deleteSchedule(schedule)
    }

    suspend fun toggleScheduleEnabled(id: Long, isEnabled: Boolean) = withContext(Dispatchers.IO) {
        blockScheduleDao.setScheduleEnabled(id, isEnabled)
    }

    /**
     * Determines whether the given package should be blocked right now based on:
     * 1. Direct block toggle
     * 2. Reached daily usage limit
     * 3. An active recurring schedule matching current day/time
     */
    suspend fun isPackageBlockedNow(
        packageName: String,
        todayUsageMinutes: Long
    ): Pair<Boolean, String?> = withContext(Dispatchers.IO) {
        val whitelist = preferencesRepository.whitelistPackages.first()
        if (whitelist.contains(packageName)) return@withContext false to null

        val rule = appRuleDao.getRuleForPackage(packageName)

        // 1. Direct block
        if (rule?.isBlocked == true) {
            return@withContext true to "Blocked completely"
        }

        // 2. Daily limit exceeded
        val limit = rule?.dailyLimitMinutes
        if (limit != null && todayUsageMinutes >= limit) {
            return@withContext true to "Daily limit of ${limit}m reached"
        }

        // 3. Active schedule
        val activeSchedules = blockScheduleDao.getActiveSchedulesSync()
        if (activeSchedules.isNotEmpty()) {
            val cal = Calendar.getInstance()
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)

            for (schedule in activeSchedules) {
                if (schedule.matchesTime(dayOfWeek, hour, minute)) {
                    val targets = schedule.targetPackagesCsv
                    if (targets.isBlank() || targets.split(",").contains(packageName)) {
                        return@withContext true to "Schedule: ${schedule.title}"
                    }
                }
            }
        }

        false to null
    }
}
