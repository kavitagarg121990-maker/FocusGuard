package com.focusguard.app.ui.focus

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusguard.app.FocusGuardApp
import com.focusguard.app.data.model.FocusSoundType
import com.focusguard.app.data.model.FocusTheme
import com.focusguard.app.service.AudioFocusPlayer
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
    POMODORO,
    STOPWATCH
}

enum class PomodoroPhase {
    WORK,
    SHORT_BREAK,
    LONG_BREAK
}

data class FocusUiState(
    val timerMode: FocusTimerMode = FocusTimerMode.POMODORO,
    val pomodoroPhase: PomodoroPhase = PomodoroPhase.WORK,
    val isRunning: Boolean = false,
    val secondsRemaining: Int = 25 * 60, // Pomodoro default 25m
    val totalPhaseDurationSeconds: Int = 25 * 60,
    val stopwatchSecondsElapsed: Int = 0,
    val workDurationMinutes: Int = 25,
    val breakDurationMinutes: Int = 5,
    val currentStreakDays: Int = 1,
    val selectedSound: FocusSoundType = FocusSoundType.NONE,
    val selectedTheme: FocusTheme = FocusTheme.EMERALD_CALM,
    val isPremium: Boolean = false,
    val completedSessionsCount: Int = 0
)

class FocusViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusGuardApp
    private val sessionRepo = app.focusSessionRepository
    private val prefsRepo = app.preferencesRepository
    private val audioPlayer = AudioFocusPlayer()

    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var sessionStartTimeMillis = 0L

    init {
        loadData()
    }

    private fun loadData() {
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
            sessionRepo.completedCount.collect { count ->
                _uiState.update { it.copy(completedSessionsCount = count) }
            }
        }

        viewModelScope.launch {
            prefsRepo.activeThemeId.collect { themeId ->
                _uiState.update { it.copy(selectedTheme = FocusTheme.fromId(themeId)) }
            }
        }

        viewModelScope.launch {
            sessionRepo.updateStreak()
        }
    }

    fun setTimerMode(mode: FocusTimerMode, onNeedsPremium: () -> Unit) {
        if (mode == FocusTimerMode.POMODORO && !_uiState.value.isPremium) {
            // Free mode allows Stopwatch; Pomodoro is premium
            onNeedsPremium()
            return
        }
        stopTimer()
        _uiState.update { it.copy(timerMode = mode) }
    }

    fun startTimer() {
        if (_uiState.value.isRunning) return
        sessionStartTimeMillis = System.currentTimeMillis()
        _uiState.update { it.copy(isRunning = true) }

        // Play ambient sound if selected
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
                    // Stopwatch mode
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
        val wasRunning = _uiState.value.isRunning
        pauseTimer()

        val elapsedMinutes = if (_uiState.value.timerMode == FocusTimerMode.POMODORO) {
            (_uiState.value.totalPhaseDurationSeconds - _uiState.value.secondsRemaining) / 60
        } else {
            _uiState.value.stopwatchSecondsElapsed / 60
        }

        if (wasRunning && elapsedMinutes >= 1) {
            recordSession(elapsedMinutes, completed = false)
        }

        resetTimerValues()
    }

    private fun onPhaseCompleted() {
        audioPlayer.stop()
        val phase = _uiState.value.pomodoroPhase
        val durationMins = if (phase == PomodoroPhase.WORK) _uiState.value.workDurationMinutes else _uiState.value.breakDurationMinutes

        if (phase == PomodoroPhase.WORK) {
            recordSession(durationMins, completed = true)
            // Switch to break
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
            // Break finished -> back to work
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
                startTimeMillis = sessionStartTimeMillis,
                durationMinutes = minutes,
                sessionType = _uiState.value.timerMode.name,
                isStrict = strict,
                themeId = _uiState.value.selectedTheme.id,
                soundId = _uiState.value.selectedSound.id
            )
        }
    }

    fun selectSound(sound: FocusSoundType, onNeedsPremium: () -> Unit) {
        if (sound.isPremium && !_uiState.value.isPremium) {
            onNeedsPremium()
            return
        }
        _uiState.update { it.copy(selectedSound = sound) }
        if (_uiState.value.isRunning) {
            audioPlayer.play(sound)
        }
    }

    fun selectTheme(theme: FocusTheme, onNeedsPremium: () -> Unit) {
        if (theme.isPremium && !_uiState.value.isPremium) {
            onNeedsPremium()
            return
        }
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
