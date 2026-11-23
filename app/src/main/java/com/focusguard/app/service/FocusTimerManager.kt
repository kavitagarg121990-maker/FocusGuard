package com.focusguard.app.service

import android.content.Context
import android.content.Intent
import com.focusguard.app.ui.focus.FocusTimerMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FocusTimerState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val mode: FocusTimerMode = FocusTimerMode.STOPWATCH,
    val stopwatchSeconds: Int = 151, // Initial 02:31
    val secondsRemaining: Int = 25 * 60,
    val totalPhaseDurationSeconds: Int = 25 * 60,
    val remainingPauses: Int = 2,
    val focusMinutesBankedToday: Int = 94,
    val dailyGoalMinutes: Int = 120
) {
    val formattedTime: String
        get() {
            val totalSec = if (mode == FocusTimerMode.STOPWATCH) stopwatchSeconds else secondsRemaining
            val mins = totalSec / 60
            val secs = totalSec % 60
            return "%02d:%02d".format(mins, secs)
        }

    val progressFraction: Float
        get() {
            return if (mode == FocusTimerMode.STOPWATCH) {
                // Circular ring sweeps up smoothly every 60s
                (stopwatchSeconds % 60) / 60f
            } else {
                if (totalPhaseDurationSeconds > 0) {
                    ((totalPhaseDurationSeconds - secondsRemaining).toFloat() / totalPhaseDurationSeconds.toFloat())
                        .coerceIn(0f, 1f)
                } else 0f
            }
        }
}

object FocusTimerManager {

    private val _timerState = MutableStateFlow(FocusTimerState())
    val timerState: StateFlow<FocusTimerState> = _timerState.asStateFlow()

    fun start(context: Context, mode: FocusTimerMode = _timerState.value.mode, durationSeconds: Int = _timerState.value.totalPhaseDurationSeconds) {
        _timerState.update {
            it.copy(
                isRunning = true,
                isPaused = false,
                mode = mode,
                totalPhaseDurationSeconds = durationSeconds,
                secondsRemaining = durationSeconds,
                stopwatchSeconds = if (mode == FocusTimerMode.STOPWATCH) it.stopwatchSeconds else 0
            )
        }
        val intent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_START
            putExtra(FocusTimerService.EXTRA_MODE, mode.name)
            putExtra(FocusTimerService.EXTRA_DURATION_SECONDS, durationSeconds)
        }
        try {
            context.startForegroundService(intent)
        } catch (e: Exception) {
            context.startService(intent)
        }
    }

    fun pause(context: Context) {
        if (!_timerState.value.isRunning || _timerState.value.remainingPauses <= 0) return
        _timerState.update {
            it.copy(
                isPaused = true,
                remainingPauses = (it.remainingPauses - 1).coerceAtLeast(0)
            )
        }
        val intent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_PAUSE
        }
        context.startService(intent)
    }

    fun resume(context: Context) {
        if (!_timerState.value.isRunning) return
        _timerState.update { it.copy(isPaused = false) }
        val intent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_RESUME
        }
        context.startService(intent)
    }

    fun togglePause(context: Context) {
        if (_timerState.value.isPaused) {
            resume(context)
        } else if (_timerState.value.isRunning) {
            pause(context)
        } else {
            start(context)
        }
    }

    fun addFiveMinutes(context: Context) {
        _timerState.update {
            if (it.mode == FocusTimerMode.STOPWATCH) {
                it.copy(stopwatchSeconds = it.stopwatchSeconds + 300)
            } else {
                it.copy(
                    secondsRemaining = it.secondsRemaining + 300,
                    totalPhaseDurationSeconds = it.totalPhaseDurationSeconds + 300
                )
            }
        }
        val intent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_ADD_FIVE_MIN
        }
        context.startService(intent)
    }

    fun finishAndBank(context: Context): Int {
        val current = _timerState.value
        val bankedMinutes = if (current.mode == FocusTimerMode.STOPWATCH) {
            (current.stopwatchSeconds / 60).coerceAtLeast(1)
        } else {
            ((current.totalPhaseDurationSeconds - current.secondsRemaining) / 60).coerceAtLeast(1)
        }

        _timerState.update {
            it.copy(
                isRunning = false,
                isPaused = false,
                stopwatchSeconds = 0,
                secondsRemaining = it.totalPhaseDurationSeconds,
                remainingPauses = 2,
                focusMinutesBankedToday = it.focusMinutesBankedToday + bankedMinutes
            )
        }

        val intent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_FINISH
        }
        context.startService(intent)
        return bankedMinutes
    }

    fun setMode(mode: FocusTimerMode, durationMinutes: Int) {
        val durationSec = durationMinutes * 60
        _timerState.update {
            it.copy(
                mode = mode,
                totalPhaseDurationSeconds = durationSec,
                secondsRemaining = durationSec,
                stopwatchSeconds = 0
            )
        }
    }

    fun setDailyGoal(minutes: Int) {
        _timerState.update { it.copy(dailyGoalMinutes = minutes) }
    }

    fun setBankedMinutes(minutes: Int) {
        _timerState.update { it.copy(focusMinutesBankedToday = minutes) }
    }

    fun updateTick(elapsed: Int, remaining: Int) {
        _timerState.update {
            it.copy(
                stopwatchSeconds = elapsed,
                secondsRemaining = remaining
            )
        }
    }
}
