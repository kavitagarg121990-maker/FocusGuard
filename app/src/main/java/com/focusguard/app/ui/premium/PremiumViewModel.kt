package com.focusguard.app.ui.premium

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusguard.app.FocusGuardApp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PremiumUiState(
    val isPremium: Boolean = false,
    val selectedPlanIndex: Int = 1 // 0: Monthly ($4.99/mo), 1: Yearly ($29.99/yr - 50% OFF)
)

class PremiumViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusGuardApp
    private val prefsRepo = app.preferencesRepository

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    private val _upgradeSuccessEvent = MutableSharedFlow<Unit>()
    val upgradeSuccessEvent: SharedFlow<Unit> = _upgradeSuccessEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            prefsRepo.isPremium.collect { premium ->
                _uiState.update { it.copy(isPremium = premium) }
            }
        }
    }

    fun selectPlan(index: Int) {
        _uiState.update { it.copy(selectedPlanIndex = index) }
    }

    fun mockUnlockPremium(onSuccess: () -> Unit) {
        viewModelScope.launch {
            prefsRepo.setPremium(true)
            _upgradeSuccessEvent.emit(Unit)
            onSuccess()
        }
    }

    fun resetToFree() {
        viewModelScope.launch {
            prefsRepo.setPremium(false)
        }
    }
}
