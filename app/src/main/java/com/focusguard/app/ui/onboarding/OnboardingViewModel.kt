package com.focusguard.app.ui.onboarding

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Application
import android.content.Context
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
    val step: Int = 0, // 0: Intro, 1: Exam Y/N, 2: Exam Pick, 3: Study Freq, 4: Scroll Habit, 5: Climb, 6: Stat, 7: Shield Story, 8: Profile & Permissions
    val isPreparingExam: Boolean? = null,
    val selectedExam: String = "",
    val examSearchQuery: String = "",
    val studyFrequency: String = "",
    val scrollHabit: String = "",
    val userName: String = "",
    val dailyGoalHours: Int = 4,
    val hasUsageAccess: Boolean = false,
    val hasAccessibilityAccess: Boolean = false
)

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusGuardApp
    private val usageRepo = app.usageStatsRepository
    private val prefsRepo = app.preferencesRepository

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    val availableExams = listOf(
        "UPSC",
        "NEET",
        "IIT JEE",
        "Class 10",
        "Class 11",
        "Class 12",
        "Banking & SSC",
        "GATE / CAT",
        "College / Coding"
    )

    val studyFrequencies = listOf(
        "Rarely",
        "Sometimes",
        "Only when I'm motivated",
        "I don't study",
        "During exam time"
    )

    val scrollHabits = listOf(
        "In bed after waking up",
        "During study breaks",
        "Late at night",
        "All day"
    )

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

    fun setPreparingExam(preparing: Boolean) {
        _uiState.update { it.copy(isPreparingExam = preparing) }
    }

    fun setSelectedExam(exam: String) {
        _uiState.update { it.copy(selectedExam = exam) }
    }

    fun setExamSearchQuery(query: String) {
        _uiState.update { it.copy(examSearchQuery = query) }
    }

    fun setStudyFrequency(freq: String) {
        _uiState.update { it.copy(studyFrequency = freq) }
    }

    fun setScrollHabit(habit: String) {
        _uiState.update { it.copy(scrollHabit = habit) }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(userName = name) }
    }

    fun nextStep() {
        val current = _uiState.value
        val next = when (current.step) {
            1 -> if (current.isPreparingExam == false) 3 else 2
            else -> (current.step + 1).coerceAtMost(8)
        }
        _uiState.update { it.copy(step = next) }
    }

    fun previousStep() {
        val current = _uiState.value
        val prev = when (current.step) {
            3 -> if (current.isPreparingExam == false) 1 else 2
            else -> (current.step - 1).coerceAtLeast(0)
        }
        _uiState.update { it.copy(step = prev) }
    }

    fun examStatisticHeadline(): Pair<String, String> {
        val state = _uiState.value
        if (state.isPreparingExam == false || state.selectedExam.isBlank()) {
            return "4+ hours" to "lost daily by average students to endless reels & shorts"
        }
        return when {
            state.selectedExam.contains("12", ignoreCase = true) ->
                "17,68,968" to "students took CBSE Class 12 boards this year"
            state.selectedExam.contains("10", ignoreCase = true) ->
                "22,38,827" to "students appeared for Class 10 boards this year"
            state.selectedExam.contains("NEET", ignoreCase = true) ->
                "24,06,079" to "students registered for NEET UG this year"
            state.selectedExam.contains("JEE", ignoreCase = true) ->
                "14,15,110" to "aspirants competed in IIT JEE this year"
            state.selectedExam.contains("UPSC", ignoreCase = true) ->
                "13,40,000+" to "aspirants applied for UPSC Civil Services"
            else ->
                "18,50,000+" to "students are competing for ${state.selectedExam} this year"
        }
    }

    fun completeOnboarding(onFinished: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            prefsRepo.setOnboardingQuizProfile(
                name = state.userName.ifBlank { "Aryan" },
                isPreparingExam = state.isPreparingExam ?: true,
                selectedExam = state.selectedExam.ifBlank { "Class 12" },
                studyFrequency = state.studyFrequency.ifBlank { "Sometimes" },
                scrollHabit = state.scrollHabit.ifBlank { "During study breaks" },
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
