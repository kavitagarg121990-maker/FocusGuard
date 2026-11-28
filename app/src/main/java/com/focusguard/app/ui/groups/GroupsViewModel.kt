package com.focusguard.app.ui.groups

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusguard.app.FocusGuardApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FocusGroupItem(
    val id: String,
    val name: String,
    val membersLabel: String,
    val emoji: String,
    val accentHex: Long,
    val isCustom: Boolean = false,
    val isJoined: Boolean = false
)

data class GroupsUiState(
    val myGroups: List<FocusGroupItem> = emptyList(),
    val suggestedGroups: List<FocusGroupItem> = emptyList(),
    val showAllSuggested: Boolean = false
)

class GroupsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusGuardApp
    private val prefsRepo = app.preferencesRepository

    private val builtInGroups = listOf(
        FocusGroupItem("design_exam", "design exam 💫 (uceed, nid, ...)", "5.9k+ members", "🎨", 0xFF7E57C2),
        FocusGroupItem("wbchse_12", "Wbchse class 12", "3.4k+ members", "📐", 0xFF26A69A),
        FocusGroupItem("board_12_pcb", "12th Board PCB", "11k+ members", "📋", 0xFFEF6C00),
        FocusGroupItem("upsc_cse", "UPSC CSE Warriors", "18k+ members", "🏛️", 0xFF42A5F5),
        FocusGroupItem("neet_ug", "NEET UG Aspirants", "24k+ members", "🩺", 0xFFEC407A),
        FocusGroupItem("jee_prep", "IIT JEE Focus Room", "19k+ members", "⚛️", 0xFF66BB6A)
    )

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(prefsRepo.joinedGroupIds, prefsRepo.customGroupsRaw) { joinedIds, customRaw ->
                val customItems = if (customRaw.isBlank()) {
                    emptyList()
                } else {
                    customRaw.split(";").mapNotNull { entry ->
                        val parts = entry.split("|")
                        if (parts.size >= 3) {
                            FocusGroupItem(
                                id = parts[0],
                                name = parts[1],
                                membersLabel = "${parts[2]} • Local Room",
                                emoji = "📚",
                                accentHex = 0xFF6AE03B,
                                isCustom = true,
                                isJoined = joinedIds.contains(parts[0])
                            )
                        } else null
                    }
                }

                val builtInMapped = builtInGroups.map { item ->
                    item.copy(isJoined = joinedIds.contains(item.id))
                }

                val myList = customItems + builtInMapped.filter { it.isJoined }
                val suggestedList = builtInMapped.filter { !it.isJoined }
                myList to suggestedList
            }.collect { (myList, suggestedList) ->
                _uiState.update {
                    it.copy(
                        myGroups = myList,
                        suggestedGroups = suggestedList
                    )
                }
            }
        }
    }

    fun toggleJoinGroup(groupId: String) {
        viewModelScope.launch {
            prefsRepo.toggleJoinedGroup(groupId)
        }
    }

    fun createCustomGroup(name: String, topic: String) {
        if (name.isBlank()) return
        val cleanId = "custom_${System.currentTimeMillis()}"
        viewModelScope.launch {
            prefsRepo.addCustomGroup(cleanId, name.trim(), topic.ifBlank { "Study Squad" })
        }
    }

    fun toggleViewAll() {
        _uiState.update { it.copy(showAllSuggested = !it.showAllSuggested) }
    }
}
