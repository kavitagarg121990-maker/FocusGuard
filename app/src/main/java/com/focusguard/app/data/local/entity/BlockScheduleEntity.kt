package com.focusguard.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Recurring block schedules (e.g., "Sleep Time" 22:00 to 07:00, "Deep Work" 09:00 to 12:00).
 */
@Entity(tableName = "block_schedules")
data class BlockScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeekMask: Int, // Bitmask: Sun=1, Mon=2, Tue=4, Wed=8, Thu=16, Fri=32, Sat=64
    val isEnabled: Boolean = true,
    val targetPackagesCsv: String = "" // Empty = all blocked apps; or specific packages
) {
    /**
     * Checks if this schedule is currently active for the provided day-of-week and time.
     * @param dayOfWeek 1 = Sunday ... 7 = Saturday (Calendar.DAY_OF_WEEK)
     */
    fun matchesTime(dayOfWeek: Int, currentHour: Int, currentMinute: Int): Boolean {
        if (!isEnabled) return false
        val dayBit = 1 shl (dayOfWeek - 1)
        if ((daysOfWeekMask and dayBit) == 0) return false

        val currentTotalMinutes = currentHour * 60 + currentMinute
        val startTotalMinutes = startHour * 60 + startMinute
        val endTotalMinutes = endHour * 60 + endMinute

        return if (startTotalMinutes <= endTotalMinutes) {
            // Normal range (e.g., 09:00 to 17:00)
            currentTotalMinutes in startTotalMinutes until endTotalMinutes
        } else {
            // Overnight range (e.g., 22:00 to 07:00)
            currentTotalMinutes >= startTotalMinutes || currentTotalMinutes < endTotalMinutes
        }
    }
}
