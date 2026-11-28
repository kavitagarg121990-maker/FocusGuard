package com.focusguard.app.ui.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusguard.app.FocusGuardApp
import com.focusguard.app.data.local.entity.BlockScheduleEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SuggestedScheduleTemplate(
    val title: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysMask: Int,
    val subtitle: String
)

data class ScheduleUiState(
    val schedules: List<BlockScheduleEntity> = emptyList()
)

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusGuardApp
    private val blockingRepo = app.appBlockingRepository

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    val suggestedTemplates = listOf(
        SuggestedScheduleTemplate(
            title = "Morning Study",
            startHour = 6,
            startMinute = 0,
            endHour = 8,
            endMinute = 0,
            daysMask = 62, // Mon-Fri
            subtitle = "06:00 AM - 08:00 AM | Mon - Fri"
        ),
        SuggestedScheduleTemplate(
            title = "Afternoon Study",
            startHour = 14,
            startMinute = 0,
            endHour = 16,
            endMinute = 0,
            daysMask = 127,
            subtitle = "02:00 PM - 04:00 PM | Daily"
        ),
        SuggestedScheduleTemplate(
            title = "Evening Deep Work",
            startHour = 18,
            startMinute = 0,
            endHour = 21,
            endMinute = 0,
            daysMask = 127,
            subtitle = "06:00 PM - 09:00 PM | Daily"
        ),
        SuggestedScheduleTemplate(
            title = "Night Wind-Down",
            startHour = 22,
            startMinute = 0,
            endHour = 7,
            endMinute = 0,
            daysMask = 127,
            subtitle = "10:00 PM - 07:00 AM | Daily"
        )
    )

    init {
        viewModelScope.launch {
            blockingRepo.blockSchedules.collect { list ->
                _uiState.update { it.copy(schedules = list) }
            }
        }
    }

    fun toggleSchedule(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            blockingRepo.toggleScheduleEnabled(id, enabled)
        }
    }

    fun deleteSchedule(schedule: BlockScheduleEntity) {
        viewModelScope.launch {
            blockingRepo.deleteBlockSchedule(schedule)
        }
    }

    fun addSuggested(template: SuggestedScheduleTemplate) {
        addSchedule(
            title = template.title,
            startHour = template.startHour,
            startMinute = template.startMinute,
            endHour = template.endHour,
            endMinute = template.endMinute,
            daysMask = template.daysMask
        )
    }

    fun addSchedule(
        title: String,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        daysMask: Int
    ) {
        viewModelScope.launch {
            blockingRepo.addBlockSchedule(
                BlockScheduleEntity(
                    title = title.ifBlank { "Focus Block" },
                    startHour = startHour,
                    startMinute = startMinute,
                    endHour = endHour,
                    endMinute = endMinute,
                    daysOfWeekMask = daysMask,
                    isEnabled = true
                )
            )
        }
    }
}
