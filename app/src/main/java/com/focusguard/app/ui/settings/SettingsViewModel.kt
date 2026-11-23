package com.focusguard.app.ui.settings

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusguard.app.FocusGuardApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val dailyGoalMinutes: Int = 120,
    val notificationsEnabled: Boolean = true,
    val breakReminderMinutes: Int = 45,
    val isStrictModeEnabled: Boolean = false,
    val isPremium: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val soundChimeEnabled: Boolean = true,
    val pomodoroWorkMinutes: Int = 25,
    val pomodoroBreakMinutes: Int = 5,
    val isAccessibilityGranted: Boolean = false,
    val isUsageStatsGranted: Boolean = false,
    val isOverlayGranted: Boolean = false,
    val isNotificationGranted: Boolean = false,
    val whitelistPackages: Set<String> = emptySet()
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusGuardApp
    private val prefsRepo = app.preferencesRepository

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        checkPermissions()
    }

    fun checkPermissions() {
        val context = getApplication<Application>()

        // 1. Accessibility Service
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        val accessibilityOk = am?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            ?.any { it.resolveInfo?.serviceInfo?.packageName == context.packageName } ?: false

        // 2. Usage Stats
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps?.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps?.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        val usageOk = mode == AppOpsManager.MODE_ALLOWED

        // 3. Overlay
        val overlayOk = Settings.canDrawOverlays(context)

        // 4. Notifications
        val notifOk = NotificationManagerCompat.from(context).areNotificationsEnabled()

        _uiState.update {
            it.copy(
                isAccessibilityGranted = accessibilityOk,
                isUsageStatsGranted = usageOk,
                isOverlayGranted = overlayOk,
                isNotificationGranted = notifOk
            )
        }
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
            prefsRepo.hapticsEnabled.collect { haptic ->
                _uiState.update { it.copy(hapticsEnabled = haptic) }
            }
        }

        viewModelScope.launch {
            prefsRepo.soundChimeEnabled.collect { chime ->
                _uiState.update { it.copy(soundChimeEnabled = chime) }
            }
        }

        viewModelScope.launch {
            prefsRepo.pomodoroWorkMinutes.collect { workMins ->
                _uiState.update { it.copy(pomodoroWorkMinutes = workMins) }
            }
        }

        viewModelScope.launch {
            prefsRepo.pomodoroBreakMinutes.collect { breakMins ->
                _uiState.update { it.copy(pomodoroBreakMinutes = breakMins) }
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

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setHapticsEnabled(enabled)
        }
    }

    fun setSoundChimeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setSoundChimeEnabled(enabled)
        }
    }

    fun setPomodoroWorkMinutes(minutes: Int) {
        viewModelScope.launch {
            prefsRepo.setPomodoroWorkMinutes(minutes)
        }
    }

    fun setPomodoroBreakMinutes(minutes: Int) {
        viewModelScope.launch {
            prefsRepo.setPomodoroBreakMinutes(minutes)
        }
    }

    fun setStrictMode(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setStrictMode(enabled)
        }
    }
}
