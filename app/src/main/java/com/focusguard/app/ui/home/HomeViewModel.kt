package com.focusguard.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusguard.app.FocusGuardApp
import com.focusguard.app.data.model.AppUsageInfo
import com.focusguard.app.service.AppBlockerForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class HomeUiState(
    val greeting: String = "Welcome",
    val formattedDate: String = "",
    val totalScreenTimeMinutes: Long = 0L,
    val dailyGoalMinutes: Long = 300L,
    val mostUsedApps: List<AppUsageInfo> = emptyList(),
    val maxUsageMillis: Long = 1L,
    val insightText: String? = null,
    val isInsightDismissed: Boolean = false,
    val isPermissionGranted: Boolean = true,
    val isQuickBlockActive: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusGuardApp
    private val usageRepo = app.usageStatsRepository
    private val prefsRepo = app.preferencesRepository

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
        AppBlockerForegroundService.start(application)
    }

    fun loadDashboardData() {
        val hasPermission = usageRepo.hasUsageStatsPermission()
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 5..11 -> "Good morning ☀️"
            in 12..16 -> "Good afternoon 👋"
            in 17..21 -> "Good evening 🌙"
            else -> "Time to rest 🌌"
        }

        val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        val formattedDate = dateFormat.format(Date())

        _uiState.update {
            it.copy(
                greeting = greeting,
                formattedDate = formattedDate,
                isPermissionGranted = hasPermission
            )
        }

        if (!hasPermission) return

        viewModelScope.launch {
            val totalMillis = usageRepo.getTodayTotalScreenTimeMillis()
            val totalMinutes = totalMillis / (1000 * 60)
            val apps = usageRepo.getTodayAppUsageList()
            val maxMillis = apps.maxOfOrNull { it.totalTimeInForegroundMillis } ?: 1L
            val goal = prefsRepo.dailyGoalMinutes.first().toLong()
            val insight = usageRepo.getSmartInsight()

            _uiState.update {
                it.copy(
                    totalScreenTimeMinutes = totalMinutes,
                    dailyGoalMinutes = goal,
                    mostUsedApps = apps.take(8),
                    maxUsageMillis = maxMillis,
                    insightText = insight
                )
            }
        }
    }

    fun dismissInsight() {
        _uiState.update { it.copy(isInsightDismissed = true) }
    }

    fun toggleQuickBlock() {
        _uiState.update { it.copy(isQuickBlockActive = !it.isQuickBlockActive) }
    }
}
