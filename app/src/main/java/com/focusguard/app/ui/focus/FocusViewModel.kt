package com.focusguard.app.ui.focus

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Application
import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusguard.app.FocusGuardApp
import com.focusguard.app.data.local.entity.BlockScheduleEntity
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
    TIMER,
    STOPWATCH,
    POMODORO
}

enum class PomodoroPhase {
    WORK,
    SHORT_BREAK,
    LONG_BREAK
}

data class ScreenTimeSegment(
    val label: String,
    val fraction: Float,
    val colorHex: Long
)

data class FocusUiState(
    val userName: String = "Aryan",
    val timerMode: FocusTimerMode = FocusTimerMode.TIMER,
    val pomodoroPhase: PomodoroPhase = PomodoroPhase.WORK,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val secondsRemaining: Int = 25 * 60,
    val totalPhaseDurationSeconds: Int = 25 * 60,
    val stopwatchSecondsElapsed: Int = 0,
    val workDurationMinutes: Int = 25,
    val breakDurationMinutes: Int = 5,
    val breakCount: Int = 1,
    val currentStreakDays: Int = 1,
    val treesPlanted: Int = 0,
    val remainingPauses: Int = 5,
    val blockedAppsCount: Int = 0,
    val isAccessibilityEnabled: Boolean = true,
    val hasUsagePermission: Boolean = true,
    val todayFocusMinutes: Int = 0,
    val goalMinutes: Int = 240,
    val todayScreenTimeMinutes: Int = 0,
    val screenTimeSegments: List<ScreenTimeSegment> = emptyList(),
    val upcomingSchedule: BlockScheduleEntity? = null,
    val activeGroupName: String? = null,
    val activeTag: String = "Untagged",
    val isUltraFocus: Boolean = false,
    val selectedSound: FocusSoundType = FocusSoundType.NONE,
    val selectedTheme: FocusTheme = FocusTheme.EMERALD_CALM,
    val isPremium: Boolean = true,
    val completedSessionsCount: Int = 0
) {
    val todayFocusFormatted: String
        get() {
            val h = todayFocusMinutes / 60
            val m = todayFocusMinutes % 60
            return if (h > 0) "${h}h ${m}m" else "${m}m"
        }

    val goalFormatted: String
        get() {
            val h = goalMinutes / 60
            val m = goalMinutes % 60
            return if (m == 0) "${h}h" else "${h}h ${m}m"
        }

    val todayScreenTimeFormatted: String
        get() {
            val h = todayScreenTimeMinutes / 60
            val m = todayScreenTimeMinutes % 60
            return if (h > 0) "${h}h ${m}m" else "${m}m"
        }

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
                        remainingPauses = timer.remainingPauses,
                        activeTag = timer.activeTag,
                        isUltraFocus = timer.isUltraFocus
                    )
                }
            }
        }
    }

    fun loadData() {
        checkPermissions()
        refreshScreenTime()

        viewModelScope.launch {
            prefsRepo.userName.collect { name ->
                _uiState.update { it.copy(userName = name) }
            }
        }

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
            prefsRepo.totalMinutesSaved.collect { saved ->
                _uiState.update { it.copy(todayFocusMinutes = saved) }
                FocusTimerManager.setBankedMinutes(saved)
            }
        }

        viewModelScope.launch {
            prefsRepo.activeSessionTag.collect { tag ->
                _uiState.update { it.copy(activeTag = tag) }
                FocusTimerManager.setActiveTag(tag)
            }
        }

        viewModelScope.launch {
            prefsRepo.ultraFocusEnabled.collect { ultra ->
                _uiState.update { it.copy(isUltraFocus = ultra) }
                FocusTimerManager.setUltraFocus(ultra)
            }
        }

        viewModelScope.launch {
            prefsRepo.joinedGroupIds.collect { ids ->
                val label = when {
                    ids.isEmpty() -> null
                    else -> ids.first().replace("_", " ").replaceFirstChar { it.uppercase() }
                }
                _uiState.update { it.copy(activeGroupName = label) }
            }
        }

        viewModelScope.launch {
            sessionRepo.completedCount.collect { count ->
                _uiState.update { it.copy(completedSessionsCount = count) }
            }
        }

        viewModelScope.launch {
            blockingRepo.allRules.collect { rules ->
                val count = rules.count { it.isBlocked || it.dailyLimitMinutes != null }
                _uiState.update { it.copy(blockedAppsCount = count) }
            }
        }

        viewModelScope.launch {
            blockingRepo.blockSchedules.collect { schedules ->
                val next = schedules.firstOrNull { it.isEnabled } ?: schedules.firstOrNull()
                _uiState.update { it.copy(upcomingSchedule = next) }
            }
        }
    }

    fun checkPermissions() {
        val am = app.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        val enabled = if (am == null) false else {
            val services = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            services.any {
                it.resolveInfo?.serviceInfo?.packageName == app.packageName &&
                    it.resolveInfo?.serviceInfo?.name?.contains("FocusAccessibilityService") == true
            }
        }
        val hasUsage = usageRepo.hasUsageStatsPermission()
        _uiState.update {
            it.copy(
                isAccessibilityEnabled = enabled,
                hasUsagePermission = hasUsage
            )
        }
    }

    fun refreshScreenTime() {
        viewModelScope.launch {
            try {
                if (!usageRepo.hasUsageStatsPermission()) {
                    _uiState.update {
                        it.copy(
                            hasUsagePermission = false,
                            todayScreenTimeMinutes = 0,
                            screenTimeSegments = emptyList()
                        )
                    }
                    return@launch
                }
                val totalMins = (usageRepo.getTodayTotalScreenTimeMillis() / 60000L).toInt()
                val apps = usageRepo.getTodayAppUsageList().take(4)
                val colors = listOf(0xFF6AE03B, 0xFFFF9800, 0xFF42A5F5, 0xFFE5484D)
                val segments = if (totalMins > 0 && apps.isNotEmpty()) {
                    apps.mapIndexed { idx, appInfo ->
                        val frac = (appInfo.totalMinutes.toFloat() / totalMins.toFloat()).coerceIn(0.05f, 0.7f)
                        ScreenTimeSegment(
                            label = appInfo.appName,
                            fraction = frac,
                            colorHex = colors[idx % colors.size]
                        )
                    }
                } else {
                    emptyList()
                }
                _uiState.update {
                    it.copy(
                        hasUsagePermission = true,
                        todayScreenTimeMinutes = totalMins,
                        screenTimeSegments = segments
                    )
                }
            } catch (e: Exception) {
                // Keep safe fallback
            }
        }
    }

    fun startSession() {
        val state = _uiState.value
        val durationSec = if (state.timerMode == FocusTimerMode.STOPWATCH) 0 else state.workDurationMinutes * 60
        viewModelScope.launch {
            if (state.isUltraFocus) {
                prefsRepo.setStrictMode(true)
            }
        }
        FocusTimerManager.start(
            context = getApplication(),
            mode = state.timerMode,
            durationSeconds = durationSec,
            tag = state.activeTag,
            ultraFocus = state.isUltraFocus
        )
        if (state.selectedSound != FocusSoundType.NONE) {
            audioPlayer.play(state.selectedSound)
        }
    }

    fun togglePause() {
        FocusTimerManager.togglePause(getApplication())
    }

    fun addFiveMinutes() {
        FocusTimerManager.addFiveMinutes(getApplication())
    }

    fun finishAndBankTime() {
        audioPlayer.stop()
        val bankedMinutes = FocusTimerManager.finishAndBank(getApplication())
        viewModelScope.launch {
            prefsRepo.incrementTreesPlanted()
            prefsRepo.addMinutesSaved(bankedMinutes)
            if (_uiState.value.isUltraFocus) {
                prefsRepo.setStrictMode(false)
            }
        }
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

    fun setBreakConfig(breaks: Int, minutes: Int) {
        _uiState.update { it.copy(breakCount = breaks, breakDurationMinutes = minutes) }
        viewModelScope.launch {
            prefsRepo.setPomodoroBreakMinutes(minutes)
        }
    }

    fun setSessionTag(tag: String) {
        FocusTimerManager.setActiveTag(tag)
        _uiState.update { it.copy(activeTag = tag) }
        viewModelScope.launch {
            prefsRepo.setActiveSessionTag(tag)
        }
    }

    fun toggleUltraFocus() {
        val next = !_uiState.value.isUltraFocus
        FocusTimerManager.setUltraFocus(next)
        _uiState.update { it.copy(isUltraFocus = next) }
        viewModelScope.launch {
            prefsRepo.setUltraFocusEnabled(next)
        }
    }

    fun selectSound(sound: FocusSoundType) {
        _uiState.update { it.copy(selectedSound = sound) }
        if (_uiState.value.isRunning) {
            audioPlayer.play(sound)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }
}
