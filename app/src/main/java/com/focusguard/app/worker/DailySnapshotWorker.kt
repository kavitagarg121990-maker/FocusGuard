package com.focusguard.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.focusguard.app.FocusGuardApp
import com.focusguard.app.data.local.entity.UsageHistoryEntity
import java.util.concurrent.TimeUnit

/**
 * Background worker scheduled every 24 hours to snapshot today's screen time
 * into the local Room database, ensuring complete historical records survive OS roll-overs.
 */
class DailySnapshotWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? FocusGuardApp ?: return Result.failure()
        return try {
            val usageRepo = app.usageStatsRepository
            val historyDao = app.database.usageHistoryDao()
            val todayApps = usageRepo.getTodayAppUsageList()
            val todayEpoch = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())

            val entities = todayApps.map { appUsage ->
                UsageHistoryEntity(
                    epochDay = todayEpoch,
                    packageName = appUsage.packageName,
                    appName = appUsage.appName,
                    totalTimeInForegroundMillis = appUsage.totalTimeInForegroundMillis,
                    blockedAttemptsCount = 0,
                    estimatedTimeSavedMinutes = 0L
                )
            }

            if (entities.isNotEmpty()) {
                historyDao.upsertDayUsage(entities)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "focusguard_daily_snapshot"

        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<DailySnapshotWorker>(
                24, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
