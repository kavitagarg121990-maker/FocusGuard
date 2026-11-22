package com.focusguard.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persists history of completed or recorded focus / pomodoro sessions for streaks and stats.
 */
@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTimeMillis: Long,
    val durationMinutes: Int,
    val sessionType: String, // "POMODORO", "STOPWATCH"
    val completed: Boolean,
    val isStrict: Boolean,
    val themeId: String,
    val soundId: String,
    val epochDay: Long // System.currentTimeMillis() / 86400000 for streak queries
)
