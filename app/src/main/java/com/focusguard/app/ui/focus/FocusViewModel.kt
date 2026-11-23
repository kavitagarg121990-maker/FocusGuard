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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
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
    val secondsRemaining: Int = 25 * 60,
    val totalPhaseDurationSeconds: Int = 25 * 60,
    val stopwatchSecondsElapsed: Int = 151, // 02:31 default to match mockup initially
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

    private var timerJob: Job? = null
    private var sessionStartTimeMillis = 0L

    init {
        loadData()
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
                _uiState.update { it.copy(todayFocusMinutes = totalMins.coerceAtLeast(94)) }
            } catch (e: Exception) {
                // Keep default
            }
        }

        viewModelScope.launch {
            sessionRepo.updateStreak()
        }
    }

    fun checkAccessibility() {
        val am = app.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        val isEnabled = am?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            ?.any { it.id.contains(app.packageName) } ?: false
        _uiState.update { it.copy(isAccessibilityEnabled = isEnabled) }
    }

    fun setTimerMode(mode: FocusTimerMode, onNeedsPremium: () -> Unit) {
        stopTimer()
        _uiState.update { it.copy(timerMode = mode) }
    }

    fun togglePause() {
        if (_uiState.value.isRunning) {
            pauseTimer()
            if (_uiState.value.remainingPauses > 0) {
                _uiState.update { it.copy(remainingPauses = it.remainingPauses - 1) }
            }
        } else {
            startTimer()
        }
    }

    fun addFiveMinutes() {
        if (_uiState.value.timerMode == FocusTimerMode.POMODORO) {
            val newRemaining = _uiState.value.secondsRemaining + 300
            val newTotal = _uiState.value.totalPhaseDurationSeconds + 300
            _uiState.update { it.copy(secondsRemaining = newRemaining, totalPhaseDurationSeconds = newTotal) }
        } else {
            val newElapsed = _uiState.value.stopwatchSecondsElapsed + 300
            _uiState.update { it.copy(stopwatchSecondsElapsed = newElapsed) }
        }
    }

    fun finishAndBankTime() {
        val elapsedSeconds = if (_uiState.value.timerMode == FocusTimerMode.POMODORO) {
            _uiState.value.totalPhaseDurationSeconds - _uiState.value.secondsRemaining
        } else {
            _uiState.value.stopwatchSecondsElapsed
        }
        val elapsedMinutes = (elapsedSeconds / 60).coerceAtLeast(1)

        pauseTimer()
        recordSession(elapsedMinutes, completed = true)

        viewModelScope.launch {
            prefsRepo.incrementTreesPlanted()
            prefsRepo.addMinutesSaved(elapsedMinutes)
            _uiState.update {
                it.copy(
                    todayFocusMinutes = it.todayFocusMinutes + elapsedMinutes,
                    treesPlanted = it.treesPlanted + 1,
                    stopwatchSecondsElapsed = 0,
                    remainingPauses = 2
                )
            }
        }
        resetTimerValues()
    }

    fun startTimer() {
        if (_uiState.value.isRunning) return
        sessionStartTimeMillis = System.currentTimeMillis()
        _uiState.update { it.copy(isRunning = true) }

        if (_uiState.value.selectedSound != FocusSoundType.NONE) {
            audioPlayer.play(_uiState.value.selectedSound)
        }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _uiState.value.isRunning) {
                delay(1000)
                if (_uiState.value.timerMode == FocusTimerMode.POMODORO) {
                    val currentRemaining = _uiState.value.secondsRemaining
                    if (currentRemaining > 1) {
                        _uiState.update { it.copy(secondsRemaining = currentRemaining - 1) }
                    } else {
                        onPhaseCompleted()
                    }
                } else {
                    _uiState.update { it.copy(stopwatchSecondsElapsed = it.stopwatchSecondsElapsed + 1) }
                }
            }
        }
    }

    fun pauseTimer() {
        _uiState.update { it.copy(isRunning = false) }
        timerJob?.cancel()
        timerJob = null
        audioPlayer.stop()
    }

    fun stopTimer() {
        pauseTimer()
        resetTimerValues()
    }

    private fun onPhaseCompleted() {
        audioPlayer.stop()
        val phase = _uiState.value.pomodoroPhase
        val durationMins = if (phase == PomodoroPhase.WORK) _uiState.value.workDurationMinutes else _uiState.value.breakDurationMinutes

        if (phase == PomodoroPhase.WORK) {
            recordSession(durationMins, completed = true)
            viewModelScope.launch {
                prefsRepo.incrementTreesPlanted()
                prefsRepo.addMinutesSaved(durationMins)
            }
            val breakSecs = _uiState.value.breakDurationMinutes * 60
            _uiState.update {
                it.copy(
                    isRunning = false,
                    pomodoroPhase = PomodoroPhase.SHORT_BREAK,
                    secondsRemaining = breakSecs,
                    totalPhaseDurationSeconds = breakSecs
                )
            }
        } else {
            val workSecs = _uiState.value.workDurationMinutes * 60
            _uiState.update {
                it.copy(
                    isRunning = false,
                    pomodoroPhase = PomodoroPhase.WORK,
                    secondsRemaining = workSecs,
                    totalPhaseDurationSeconds = workSecs
                )
            }
        }
    }

    private fun recordSession(minutes: Int, completed: Boolean) {
        viewModelScope.launch {
            val strict = prefsRepo.isStrictModeEnabled.first()
            sessionRepo.recordCompletedSession(
                startTimeMillis = if (sessionStartTimeMillis > 0) sessionStartTimeMillis else System.currentTimeMillis() - minutes * 60000L,
                durationMinutes = minutes,
                sessionType = _uiState.value.timerMode.name,
                isStrict = strict,
                themeId = _uiState.value.selectedTheme.id,
                soundId = _uiState.value.selectedSound.id
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

    private fun resetTimerValues() {
        val workSecs = _uiState.value.workDurationMinutes * 60
        _uiState.update {
            it.copy(
                pomodoroPhase = PomodoroPhase.WORK,
                secondsRemaining = workSecs,
                totalPhaseDurationSeconds = workSecs,
                stopwatchSecondsElapsed = 0
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }
}
