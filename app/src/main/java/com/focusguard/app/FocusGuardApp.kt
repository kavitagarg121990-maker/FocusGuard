package com.focusguard.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.focusguard.app.data.local.FocusGuardDatabase
import com.focusguard.app.data.repository.AppBlockingRepository
import com.focusguard.app.data.repository.FocusSessionRepository
import com.focusguard.app.data.repository.PreferencesRepository
import com.focusguard.app.data.repository.UsageStatsRepository
import com.focusguard.app.service.AppBlockerForegroundService
import com.focusguard.app.service.FocusTimerService
import com.focusguard.app.worker.DailySnapshotWorker

class FocusGuardApp : Application() {

    val database: FocusGuardDatabase by lazy {
        FocusGuardDatabase.getInstance(this)
    }

    val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepository(this)
    }

    val appBlockingRepository: AppBlockingRepository by lazy {
        AppBlockingRepository(
            context = this,
            appRuleDao = database.appRuleDao(),
            shortContentRuleDao = database.shortContentRuleDao(),
            blockScheduleDao = database.blockScheduleDao(),
            preferencesRepository = preferencesRepository
        )
    }

    val focusSessionRepository: FocusSessionRepository by lazy {
        FocusSessionRepository(
            focusSessionDao = database.focusSessionDao(),
            preferencesRepository = preferencesRepository
        )
    }

    val usageStatsRepository: UsageStatsRepository by lazy {
        UsageStatsRepository(
            context = this,
            usageHistoryDao = database.usageHistoryDao(),
            appRuleDao = database.appRuleDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        // Schedule daily background snapshots to persist long-term historical stats
        DailySnapshotWorker.schedule(this)
        // Start foreground app blocker service
        AppBlockerForegroundService.start(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java) ?: return

            // 1. Focus Timer Channel
            val timerChannel = NotificationChannel(
                FocusTimerService.CHANNEL_ID,
                "Active Focus Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live ongoing countdown and stopwatch timers"
                setShowBadge(false)
            }

            // 2. App Blocker Channel
            val blockerChannel = NotificationChannel(
                AppBlockerForegroundService.CHANNEL_ID,
                "FocusGuard Background Blocker",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background protection and screen time monitoring"
                setShowBadge(false)
            }

            // 3. Alerts Channel
            val alertsChannel = NotificationChannel(
                "focus_alerts_channel",
                "Focus & App Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Distraction alerts and session completion chimes"
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(listOf(timerChannel, blockerChannel, alertsChannel))
        }
    }
}
