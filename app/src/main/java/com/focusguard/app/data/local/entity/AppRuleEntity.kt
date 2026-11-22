package com.focusguard.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persists blocking rules and daily limits for installed packages.
 */
@Entity(tableName = "app_rules")
data class AppRuleEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isBlocked: Boolean = false,
    val dailyLimitMinutes: Int? = null,
    val isSystemApp: Boolean = false,
    val isWhitelisted: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)
