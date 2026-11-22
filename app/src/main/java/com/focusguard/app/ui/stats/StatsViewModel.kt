package com.focusguard.app.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusguard.app.FocusGuardApp
import com.focusguard.app.data.model.AppUsageInfo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StatsUiState(
    val selectedTimeRange: Int = 0, // 0: Daily, 1: Weekly, 2: Monthly
    val totalTimeTodayMinutes: Long = 0L,
    val dailyMinutesList: List<Pair<String, Float>> = emptyList(),
    val weeklyPoints: List<Float> = emptyList(),
    val appUsageList: List<AppUsageInfo> = emptyList(),
    val totalBlockedAttempts: Int = 0,
    val totalSavedMinutes: Long = 0L,
    val isExporting: Boolean = false,
    val isPremium: Boolean = false
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusGuardApp
    private val usageRepo = app.usageStatsRepository
    private val prefsRepo = app.preferencesRepository

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    private val _exportMessage = MutableSharedFlow<String>()
    val exportMessage: SharedFlow<String> = _exportMessage.asSharedFlow()

    init {
        loadStatsData()
    }

    fun setTimeRange(rangeIndex: Int) {
        _uiState.update { it.copy(selectedTimeRange = rangeIndex) }
    }

    fun loadStatsData() {
        viewModelScope.launch {
            prefsRepo.isPremium.collect { premium ->
                _uiState.update { it.copy(isPremium = premium) }
            }
        }

        viewModelScope.launch {
            val totalMillis = usageRepo.getTodayTotalScreenTimeMillis()
            val totalMinutes = totalMillis / (1000 * 60)
            val weeklyData = usageRepo.getWeeklyDailyMinutes()
            val apps = usageRepo.getTodayAppUsageList()

            _uiState.update {
                it.copy(
                    totalTimeTodayMinutes = totalMinutes,
                    dailyMinutesList = weeklyData,
                    weeklyPoints = weeklyData.map { pair -> pair.second },
                    appUsageList = apps
                )
            }
        }

        viewModelScope.launch {
            usageRepo.getTotalBlockedAttempts().collect { attempts ->
                _uiState.update { it.copy(totalBlockedAttempts = attempts ?: 0) }
            }
        }

        viewModelScope.launch {
            usageRepo.getTotalEstimatedSavedMinutes().collect { savedMins ->
                _uiState.update { it.copy(totalSavedMinutes = savedMins ?: 0L) }
            }
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val result = usageRepo.exportUsageDataToCsv()
            _uiState.update { it.copy(isExporting = false) }

            result.onSuccess { path ->
                _exportMessage.emit("Successfully exported data: $path")
            }.onFailure { err ->
                _exportMessage.emit("Export failed: ${err.message}")
            }
        }
    }
}
