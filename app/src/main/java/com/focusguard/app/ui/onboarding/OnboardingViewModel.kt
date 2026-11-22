package com.focusguard.app.ui.onboarding

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Application
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusguard.app.FocusGuardApp
import com.focusguard.app.service.FocusAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val currentPage: Int = 0,
    val hasUsageAccess: Boolean = false,
    val hasAccessibilityAccess: Boolean = false,
    val userName: String = "",
    val usageLevel: String = "4-6 hours",
    val primaryGoal: String = "Quit Doomscrolling (Reels & Shorts)",
    val dailyGoalHours: Int = 3
)

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusGuardApp
    private val usageRepo = app.usageStatsRepository
    private val prefsRepo = app.preferencesRepository

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        checkPermissions()
    }

    fun checkPermissions() {
        val hasUsage = usageRepo.hasUsageStatsPermission()
        val hasAccessibility = isAccessibilityServiceEnabled()
        _uiState.update {
            it.copy(
                hasUsageAccess = hasUsage,
                hasAccessibilityAccess = hasAccessibility
            )
        }
    }

    fun setPage(page: Int) {
        _uiState.update { it.copy(currentPage = page) }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(userName = name) }
    }

    fun updateUsageLevel(level: String) {
        _uiState.update { it.copy(usageLevel = level) }
    }

    fun updatePrimaryGoal(goal: String) {
        _uiState.update { it.copy(primaryGoal = goal) }
    }

    fun updateDailyGoalHours(hours: Int) {
        _uiState.update { it.copy(dailyGoalHours = hours) }
    }

    fun completeOnboarding(onFinished: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            prefsRepo.setUserProfile(
                name = state.userName.ifBlank { "Champion" },
                usageLevel = state.usageLevel,
                goal = state.primaryGoal,
                dailyGoalMinutes = state.dailyGoalHours * 60
            )
            prefsRepo.setOnboardingCompleted(true)
            onFinished()
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = app.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val expectedServiceName = FocusAccessibilityService::class.java.name
        return enabledServices.any { it.resolveInfo.serviceInfo.name.contains(expectedServiceName) }
    }
}
