package com.focusguard.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusguard.app.FocusGuardApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val dailyGoalMinutes: Int = 300,
    val notificationsEnabled: Boolean = true,
    val breakReminderMinutes: Int = 45,
    val isStrictModeEnabled: Boolean = false,
    val isPremium: Boolean = false,
    val whitelistPackages: Set<String> = emptySet()
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusGuardApp
    private val prefsRepo = app.preferencesRepository

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            prefsRepo.dailyGoalMinutes.collect { goal ->
                _uiState.update { it.copy(dailyGoalMinutes = goal) }
            }
        }

        viewModelScope.launch {
            prefsRepo.isPremium.collect { premium ->
                _uiState.update { it.copy(isPremium = premium) }
            }
        }

        viewModelScope.launch {
            prefsRepo.notificationsEnabled.collect { enabled ->
                _uiState.update { it.copy(notificationsEnabled = enabled) }
            }
        }

        viewModelScope.launch {
            prefsRepo.isStrictModeEnabled.collect { strict ->
                _uiState.update { it.copy(isStrictModeEnabled = strict) }
            }
        }

        viewModelScope.launch {
            prefsRepo.whitelistPackages.collect { list ->
                _uiState.update { it.copy(whitelistPackages = list) }
            }
        }
    }

    fun setDailyGoalMinutes(minutes: Int) {
        viewModelScope.launch {
            prefsRepo.setDailyGoalMinutes(minutes)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setNotificationsEnabled(enabled)
        }
    }
}
