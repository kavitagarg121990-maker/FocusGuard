package com.focusguard.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.focusguard.app.FocusGuardApp
import com.focusguard.app.MainActivity
import com.focusguard.app.R
import com.focusguard.app.ui.overlay.BlockOverlayActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Foreground monitoring service running an active notification.
 * Performs periodic foreground-app polling and enforces daily limits and recurring schedules.
 */
class AppBlockerForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var monitorJob: Job? = null
    private var lastForegroundPackage: String? = null
    private val isOverlayShowing = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundServiceWithNotification()
        startMonitoringLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun startForegroundServiceWithNotification() {
        val notification = buildServiceNotification("FocusGuard is guarding your time", "Distraction blocking active")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startMonitoringLoop() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            val app = application as? FocusGuardApp ?: return@launch
            val blockingRepo = app.appBlockingRepository
            val usageRepo = app.usageStatsRepository
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager

            while (isActive) {
                try {
                    val currentForegroundPkg = getForegroundPackage(usageStatsManager)
                    if (currentForegroundPkg != null && currentForegroundPkg != packageName) {
                        if (currentForegroundPkg != lastForegroundPackage) {
                            lastForegroundPackage = currentForegroundPkg

                            // Check usage time today for this app
                            val todayApps = usageRepo.getTodayAppUsageList()
                            val appUsage = todayApps.find { it.packageName == currentForegroundPkg }
                            val todayMinutes = appUsage?.totalMinutes ?: 0L

                            val (isBlocked, reason) = blockingRepo.isPackageBlockedNow(
                                currentForegroundPkg,
                                todayMinutes
                            )

                            if (isBlocked && !isOverlayShowing.get()) {
                                isOverlayShowing.set(true)
                                usageRepo.recordBlockEvent(currentForegroundPkg)

                                val appName = appUsage?.appName ?: currentForegroundPkg.substringAfterLast('.')
                                launchBlockOverlay(currentForegroundPkg, appName, reason ?: "App is blocked by FocusGuard")

                                delay(2000)
                                isOverlayShowing.set(false)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Fail-safe: do not crash the service on any device-specific exception
                }
                delay(1200) // Poll every 1.2s
            }
        }
    }

    private fun getForegroundPackage(usageStatsManager: UsageStatsManager?): String? {
        if (usageStatsManager == null) return null
        val now = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(now - 3000, now)
        val event = UsageEvents.Event()
        var lastPkg: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
            ) {
                lastPkg = event.packageName
            }
        }
        return lastPkg
    }

    private fun launchBlockOverlay(pkg: String, name: String, reason: String) {
        val intent = Intent(this, BlockOverlayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            putExtra(BlockOverlayActivity.EXTRA_PACKAGE_NAME, pkg)
            putExtra(BlockOverlayActivity.EXTRA_APP_NAME, name)
            putExtra(BlockOverlayActivity.EXTRA_BLOCK_REASON, reason)
        }
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "FocusGuard Shield Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active distraction blocking and focus protection status."
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildServiceNotification(title: String, content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        monitorJob?.cancel()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "focusguard_monitor_channel"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, AppBlockerForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AppBlockerForegroundService::class.java))
        }
    }
}
