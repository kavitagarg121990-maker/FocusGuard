package com.focusguard.app.ui.block

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusguard.app.FocusGuardApp
import com.focusguard.app.data.local.entity.BlockScheduleEntity
import com.focusguard.app.data.local.entity.ShortContentRuleEntity
import com.focusguard.app.data.model.InstalledAppInfo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BlockUiState(
    val selectedTab: Int = 0, // 0: Apps, 1: Shorts & Reels, 2: Schedules & Web
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val filteredApps: List<InstalledAppInfo> = emptyList(),
    val searchQuery: String = "",
    val showSystemApps: Boolean = false,
    val isLoadingApps: Boolean = true,
    val selectedAppForDetail: InstalledAppInfo? = null,
    val shortContentRules: List<ShortContentRuleEntity> = emptyList(),
    val blockSchedules: List<BlockScheduleEntity> = emptyList(),
    val blockedWebsites: List<String> = emptyList(),
    val isStrictModeEnabled: Boolean = false,
    val isPremium: Boolean = false
)

class BlockViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusGuardApp
    private val blockingRepo = app.appBlockingRepository
    private val prefsRepo = app.preferencesRepository

    private val _uiState = MutableStateFlow(BlockUiState())
    val uiState: StateFlow<BlockUiState> = _uiState.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

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
            prefsRepo.isStrictModeEnabled.collect { strict ->
                _uiState.update { it.copy(isStrictModeEnabled = strict) }
            }
        }

        viewModelScope.launch {
            prefsRepo.blockedDomains.collect { domains ->
                _uiState.update { it.copy(blockedWebsites = domains) }
            }
        }

        viewModelScope.launch {
            blockingRepo.shortContentRules.collect { rules ->
                _uiState.update { it.copy(shortContentRules = rules) }
            }
        }

        viewModelScope.launch {
            blockingRepo.blockSchedules.collect { schedules ->
                _uiState.update { it.copy(blockSchedules = schedules) }
            }
        }

        refreshInstalledApps()
    }

    fun setTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update {
            val filtered = filterAppsList(it.installedApps, query)
            it.copy(searchQuery = query, filteredApps = filtered)
        }
    }

    fun toggleShowSystemApps() {
        val newShow = !_uiState.value.showSystemApps
        _uiState.update { it.copy(showSystemApps = newShow) }
        refreshInstalledApps()
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingApps = true) }
            val apps = blockingRepo.getInstalledApps(showSystemApps = _uiState.value.showSystemApps)
            val filtered = filterAppsList(apps, _uiState.value.searchQuery)
            _uiState.update { it.copy(installedApps = apps, filteredApps = filtered, isLoadingApps = false) }
        }
    }

    fun selectAppForDetail(appInfo: InstalledAppInfo?) {
        _uiState.update { it.copy(selectedAppForDetail = appInfo) }
    }

    fun toggleAppBlock(appInfo: InstalledAppInfo, isBlocked: Boolean) {
        viewModelScope.launch {
            blockingRepo.setAppBlocked(appInfo.packageName, appInfo.appName, isBlocked)
            refreshInstalledApps()
        }
    }

    fun setAppDailyLimit(appInfo: InstalledAppInfo, limitMinutes: Int?) {
        viewModelScope.launch {
            val result = blockingRepo.setAppDailyLimit(appInfo.packageName, appInfo.appName, limitMinutes)
            result.onSuccess {
                refreshInstalledApps()
            }.onFailure { e ->
                _errorMessage.emit(e.message ?: "Failed to set daily limit")
            }
        }
    }

    fun toggleShortContentRule(ruleId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            blockingRepo.setShortContentRuleEnabled(ruleId, isEnabled)
        }
    }

    fun toggleStrictMode(enable: Boolean, onNeedsPremium: () -> Unit) {
        viewModelScope.launch {
            val isPrem = prefsRepo.isPremium.first()
            if (!isPrem && enable) {
                onNeedsPremium()
                return@launch
            }
            prefsRepo.setStrictMode(enable)
        }
    }

    fun addBlockSchedule(schedule: BlockScheduleEntity, onNeedsPremium: () -> Unit) {
        viewModelScope.launch {
            val result = blockingRepo.addBlockSchedule(schedule)
            result.onFailure {
                onNeedsPremium()
                _errorMessage.emit(it.message ?: "Premium feature limit reached")
            }
        }
    }

    fun deleteBlockSchedule(schedule: BlockScheduleEntity) {
        viewModelScope.launch {
            blockingRepo.deleteBlockSchedule(schedule)
        }
    }

    fun toggleScheduleActive(id: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            blockingRepo.toggleScheduleEnabled(id, isEnabled)
        }
    }

    fun addBlockedWebsite(domain: String) {
        if (domain.isBlank()) return
        viewModelScope.launch {
            prefsRepo.addBlockedDomain(domain)
        }
    }

    fun removeBlockedWebsite(domain: String) {
        viewModelScope.launch {
            prefsRepo.removeBlockedDomain(domain)
        }
    }

    private fun filterAppsList(apps: List<InstalledAppInfo>, query: String): List<InstalledAppInfo> {
        if (query.isBlank()) return apps
        val lower = query.lowercase().trim()
        return apps.filter { it.appName.lowercase().contains(lower) || it.packageName.lowercase().contains(lower) }
    }
}
