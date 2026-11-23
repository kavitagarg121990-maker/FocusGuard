package com.focusguard.app.ui.focus

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Application
import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusguard.app.FocusGuardApp
import com.focusguard.app.data.model.FocusSoundType
import com.focusguard.app.data.model.FocusTheme
import com.focusguard.app.service.AudioFocusPlayer
import com.focusguard.app.service.FocusAccessibilityService
import com.focusguard.app.service.FocusTimerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FocusTimerMode {
    STOPWATCH,
    POMODORO
}

enum class PomodoroPhase {
    WORK,
    SHORT_BREAK,
    LONG_BREAK
}

data class FocusUiState(
    val timerMode: FocusTimerMode = FocusTimerMode.STOPWATCH,
    val pomodoroPhase: PomodoroPhase = PomodoroPhase.WORK,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val secondsRemaining: Int = 25 * 60,
    val totalPhaseDurationSeconds: Int = 25 * 60,
    val stopwatchSecondsElapsed: Int = 151, // 02:31 default
    val workDurationMinutes: Int = 25,
    val breakDurationMinutes: Int = 5,
    val currentStreakDays: Int = 3,
    val treesPlanted: Int = 5,
    val remainingPauses: Int = 2,
    val fullyBlockedCount: Int = 1,
    val reelsOnlyCount: Int = 0,
    val isAccessibilityEnabled: Boolean = true,
    val todayFocusMinutes: Int = 94, // 1h 34m
    val goalMinutes: Int = 120, // 2h
    val selectedSound: FocusSoundType = FocusSoundType.NONE,
    val selectedTheme: FocusTheme = FocusTheme.EMERALD_CALM,
    val isPremium: Boolean = true,
    val completedSessionsCount: Int = 8
) {
    val todayFocusHoursMinutes: String
        get() {
            val h = todayFocusMinutes / 60
            val m = todayFocusMinutes % 60
            return if (h > 0) "${h}h ${m}m" else "${m}m"
        }

    val goalHours: Int
        get() = (goalMinutes / 60).coerceAtLeast(1)

    val goalProgressFraction: Float
        get() = if (goalMinutes > 0) (todayFocusMinutes.toFloat() / goalMinutes.toFloat()).coerceIn(0f, 1f) else 0f
}

class FocusViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusGuardApp
    private val sessionRepo = app.focusSessionRepository
    private val prefsRepo = app.preferencesRepository
    private val blockingRepo = app.appBlockingRepository
    private val usageRepo = app.usageStatsRepository
    private val audioPlayer = AudioFocusPlayer()

    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeTimerManager()
    }

    private fun observeTimerManager() {
        viewModelScope.launch {
            FocusTimerManager.timerState.collect { timer ->
                _uiState.update {
                    it.copy(
                        isRunning = timer.isRunning,
                        isPaused = timer.isPaused,
                        timerMode = timer.mode,
                        stopwatchSecondsElapsed = timer.stopwatchSeconds,
                        secondsRemaining = timer.secondsRemaining,
                        totalPhaseDurationSeconds = timer.totalPhaseDurationSeconds,
                        remainingPauses = timer.remainingPauses
                    )
                }
            }
        }
    }

    fun loadData() {
        checkAccessibility()

        viewModelScope.launch {
            prefsRepo.isPremium.collect { premium ->
                _uiState.update { it.copy(isPremium = premium) }
            }
        }

        viewModelScope.launch {
            prefsRepo.currentStreakDays.collect { streak ->
                _uiState.update { it.copy(currentStreakDays = streak) }
            }
        }

        viewModelScope.launch {
            prefsRepo.totalTreesPlanted.collect { trees ->
                _uiState.update { it.copy(treesPlanted = trees) }
            }
        }

        viewModelScope.launch {
            prefsRepo.dailyGoalMinutes.collect { goal ->
                _uiState.update { it.copy(goalMinutes = goal) }
                FocusTimerManager.setDailyGoal(goal)
            }
        }

        viewModelScope.launch {
            sessionRepo.completedCount.collect { count ->
                _uiState.update { it.copy(completedSessionsCount = count) }
            }
        }

        viewModelScope.launch {
            blockingRepo.allRules.collect { rules ->
                val fullyBlocked = rules.count { it.isBlocked }
                _uiState.update { it.copy(fullyBlockedCount = fullyBlocked.coerceAtLeast(1)) }
            }
        }

        viewModelScope.launch {
            blockingRepo.shortContentRules.collect { rules ->
                val reels = rules.count { it.isEnabled }
                _uiState.update { it.copy(reelsOnlyCount = reels) }
            }
        }

        viewModelScope.launch {
            try {
                val totalMins = (usageRepo.getTodayTotalScreenTimeMillis() / 60000L).toInt()
                val focusMins = prefsRepo.totalMinutesSaved.first()
                val finalFocus = if (focusMins > 0) focusMins else 94
                _uiState.update { it.copy(todayFocusMinutes = finalFocus) }
                FocusTimerManager.setBankedMinutes(finalFocus)
            } catch (e: Exception) {
                // Keep default
            }
        }
    }

    fun checkAccessibility() {
        val am = app.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        val enabled = if (am == null) false else {
            val services = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            services.any {
                it.resolveInfo?.serviceInfo?.packageName == app.packageName &&
                it.resolveInfo?.serviceInfo?.name?.contains("FocusAccessibilityService") == true
            }
        }
        _uiState.update { it.copy(isAccessibilityEnabled = enabled) }
    }

    fun togglePause() {
        FocusTimerManager.togglePause(getApplication())
    }

    fun addFiveMinutes() {
        FocusTimerManager.addFiveMinutes(getApplication())
    }

    fun finishAndBankTime() {
        val bankedMinutes = FocusTimerManager.finishAndBank(getApplication())
        viewModelScope.launch {
            prefsRepo.incrementTreesPlanted()
            prefsRepo.addMinutesSaved(bankedMinutes)
            _uiState.update {
                it.copy(
                    todayFocusMinutes = it.todayFocusMinutes + bankedMinutes,
                    treesPlanted = it.treesPlanted + 1
                )
            }
        }
    }

    fun startTimer() {
        FocusTimerManager.start(
            context = getApplication(),
            mode = _uiState.value.timerMode,
            durationSeconds = _uiState.value.totalPhaseDurationSeconds
        )
    }

    fun pauseTimer() {
        FocusTimerManager.pause(getApplication())
    }

    fun stopTimer() {
        FocusTimerManager.finishAndBank(getApplication())
    }

    fun updateDailyGoal(hours: Int, minutes: Int) {
        val totalMinutes = (hours * 60 + minutes).coerceAtLeast(15)
        viewModelScope.launch {
            prefsRepo.setDailyGoalMinutes(totalMinutes)
            _uiState.update { it.copy(goalMinutes = totalMinutes) }
            FocusTimerManager.setDailyGoal(totalMinutes)
        }
    }

    fun setTimerModeAndDuration(mode: FocusTimerMode, durationMinutes: Int) {
        FocusTimerManager.setMode(mode, durationMinutes)
        _uiState.update {
            it.copy(
                timerMode = mode,
                workDurationMinutes = durationMinutes,
                secondsRemaining = durationMinutes * 60,
                totalPhaseDurationSeconds = durationMinutes * 60
            )
        }
    }

    fun selectSound(sound: FocusSoundType, onNeedsPremium: () -> Unit) {
        _uiState.update { it.copy(selectedSound = sound) }
        if (_uiState.value.isRunning) {
            audioPlayer.play(sound)
        }
    }

    fun selectTheme(theme: FocusTheme, onNeedsPremium: () -> Unit) {
        viewModelScope.launch {
            prefsRepo.setActiveThemeId(theme.id)
            _uiState.update { it.copy(selectedTheme = theme) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }
}
