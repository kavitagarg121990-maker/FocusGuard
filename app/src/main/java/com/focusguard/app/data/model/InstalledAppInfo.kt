package com.focusguard.app.data.model

import android.graphics.drawable.Drawable

/**
 * Information representing an installed application on the device.
 */
data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val isSystemApp: Boolean = false,
    val isBlocked: Boolean = false,
    val dailyLimitMinutes: Int? = null,
    val usageTimeTodayMillis: Long = 0L,
    val hasActiveSchedule: Boolean = false
)

/**
 * Summary of usage metrics for an app for stats and charts.
 */
data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val totalTimeInForegroundMillis: Long,
    val lastTimeUsed: Long = 0L,
    val launchCount: Int = 0,
    val dailyLimitMinutes: Int? = null,
    val isBlocked: Boolean = false
) {
    val totalMinutes: Long
        get() = totalTimeInForegroundMillis / (1000 * 60)

    val formattedDuration: String
        get() {
            val totalMins = totalMinutes
            val hours = totalMins / 60
            val mins = totalMins % 60
            return when {
                hours > 0 -> "${hours}h ${mins}m"
                mins > 0 -> "${mins}m"
                else -> "< 1m"
            }
        }
}
