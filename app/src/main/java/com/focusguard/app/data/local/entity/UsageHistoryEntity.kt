package com.focusguard.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Historical daily snapshots of per-app screen time and blocked distraction attempts.
 * Preserves stats indefinitely on-device even if the OS UsageStats roll over.
 */
@Entity(
    tableName = "usage_history",
    indices = [Index(value = ["epochDay", "packageName"], unique = true)]
)
data class UsageHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long, // Epoch day (days since Jan 1, 1970)
    val packageName: String,
    val appName: String,
    val totalTimeInForegroundMillis: Long,
    val blockedAttemptsCount: Int = 0,
    val estimatedTimeSavedMinutes: Long = 0L
)
