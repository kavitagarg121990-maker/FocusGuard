package com.focusguard.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.focusguard.app.FocusGuardApp
import com.focusguard.app.MainActivity
import com.focusguard.app.R
import com.focusguard.app.ui.focus.FocusTimerMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FocusTimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var tickerJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private var mode = FocusTimerMode.STOPWATCH
    private var elapsedSeconds = 0
    private var remainingSeconds = 25 * 60
    private var totalDurationSeconds = 25 * 60
    private var isPaused = false

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        when (action) {
            ACTION_START -> {
                val modeStr = intent?.getStringExtra(EXTRA_MODE) ?: FocusTimerMode.STOPWATCH.name
                mode = try { FocusTimerMode.valueOf(modeStr) } catch (e: Exception) { FocusTimerMode.STOPWATCH }
                totalDurationSeconds = intent?.getIntExtra(EXTRA_DURATION_SECONDS, 25 * 60) ?: (25 * 60)
                remainingSeconds = totalDurationSeconds
                elapsedSeconds = 0
                isPaused = false
                startForegroundServiceWithNotification()
                startTicker()
            }
            ACTION_PAUSE -> {
                isPaused = true
                updateNotification()
            }
            ACTION_RESUME -> {
                isPaused = false
                updateNotification()
            }
            ACTION_ADD_FIVE_MIN -> {
                remainingSeconds += 300
                totalDurationSeconds += 300
                elapsedSeconds += 300
                updateNotification()
            }
            ACTION_FINISH -> {
                finishSessionAndStop()
            }
        }

        return START_STICKY
    }

    private fun acquireWakeLock() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FocusGuard:FocusTimerWakeLock")
            wakeLock?.acquire(4 * 60 * 60 * 1000L) // 4 hours maximum safety timeout
        } catch (e: Exception) {
            // Wake lock fail-safe
        }
    }

    private fun startForegroundServiceWithNotification() {
        val notification = buildNotification()
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

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                if (!isPaused) {
                    if (mode == FocusTimerMode.STOPWATCH) {
                        elapsedSeconds++
                        FocusTimerManager.updateTick(elapsedSeconds, remainingSeconds)
                    } else {
                        remainingSeconds--
                        FocusTimerManager.updateTick(elapsedSeconds, remainingSeconds)
                        if (remainingSeconds <= 0) {
                            onTimerComplete()
                            break
                        }
                    }
                    // Update notification every 2 seconds to conserve battery while keeping UI fresh
                    if (elapsedSeconds % 2 == 0 || remainingSeconds % 2 == 0) {
                        updateNotification()
                    }
                }
            }
        }
    }

    private fun onTimerComplete() {
        vibrateCompletion()
        val app = application as? FocusGuardApp
        app?.let {
            serviceScope.launch {
                val bankedMins = (totalDurationSeconds / 60).coerceAtLeast(1)
                it.focusSessionRepository.recordCompletedSession(bankedMins)
                it.preferencesRepository.addMinutesSaved(bankedMins)
            }
        }

        // Show completion heads-up
        showCompletionNotification()
        stopSelf()
    }

    private fun finishSessionAndStop() {
        tickerJob?.cancel()
        val bankedMinutes = if (mode == FocusTimerMode.STOPWATCH) {
            (elapsedSeconds / 60).coerceAtLeast(1)
        } else {
            ((totalDurationSeconds - remainingSeconds) / 60).coerceAtLeast(1)
        }

        val app = application as? FocusGuardApp
        app?.let {
            serviceScope.launch {
                it.focusSessionRepository.recordCompletedSession(bankedMinutes)
                it.preferencesRepository.addMinutesSaved(bankedMinutes)
            }
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action Pause / Resume
        val toggleActionIntent = Intent(this, FocusTimerService::class.java).apply {
            action = if (isPaused) ACTION_RESUME else ACTION_PAUSE
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val toggleTitle = if (isPaused) "▶ Resume" else "⏸ Pause"

        // Action +5 Min
        val addFiveIntent = Intent(this, FocusTimerService::class.java).apply {
            action = ACTION_ADD_FIVE_MIN
        }
        val addFivePendingIntent = PendingIntent.getService(
            this,
            2,
            addFiveIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action Finish
        val finishIntent = Intent(this, FocusTimerService::class.java).apply {
            action = ACTION_FINISH
        }
        val finishPendingIntent = PendingIntent.getService(
            this,
            3,
            finishIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val totalSec = if (mode == FocusTimerMode.STOPWATCH) elapsedSeconds else remainingSeconds
        val formattedTime = "%02d:%02d".format(totalSec / 60, totalSec % 60)
        val subtitle = if (mode == FocusTimerMode.STOPWATCH) {
            "⏱️ Elapsed: $formattedTime • Distractions Blocked"
        } else {
            "⏳ Remaining: $formattedTime • Stay in Flow"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (isPaused) "Focus Session Paused" else "FocusGuard Session Active")
            .setContentText(subtitle)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(0, toggleTitle, togglePendingIntent)
            .addAction(0, "+5 min", addFivePendingIntent)
            .addAction(0, "✓ Finish", finishPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .build()
    }

    private fun showCompletionNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🎉 Focus Session Completed!")
            .setContentText("Great job! Your focus time has been banked to your daily goal.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        manager?.notify(COMPLETION_NOTIFICATION_ID, notification)
    }

    private fun vibrateCompletion() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 300), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val v = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                v?.vibrate(longArrayOf(0, 300, 200, 300), -1)
            }
        } catch (e: Exception) {
            // Vibration fail-safe
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Active Focus Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live ongoing countdown and stopwatch timers"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        tickerJob?.cancel()
        serviceScope.cancel()
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    companion object {
        const val CHANNEL_ID = "focus_timer_channel"
        const val NOTIFICATION_ID = 2001
        const val COMPLETION_NOTIFICATION_ID = 2002

        const val ACTION_START = "com.focusguard.app.action.START_TIMER"
        const val ACTION_PAUSE = "com.focusguard.app.action.PAUSE_TIMER"
        const val ACTION_RESUME = "com.focusguard.app.action.RESUME_TIMER"
        const val ACTION_ADD_FIVE_MIN = "com.focusguard.app.action.ADD_FIVE_MIN"
        const val ACTION_FINISH = "com.focusguard.app.action.FINISH_TIMER"

        const val EXTRA_MODE = "extra_mode"
        const val EXTRA_DURATION_SECONDS = "extra_duration_seconds"
    }
}
