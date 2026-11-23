package com.focusguard.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "focusguard_preferences")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val DAILY_GOAL_MINUTES = intPreferencesKey("daily_goal_minutes")
        val STRICT_MODE_ENABLED = booleanPreferencesKey("strict_mode_enabled")
        val ACTIVE_THEME_ID = stringPreferencesKey("active_theme_id")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val BREAK_REMINDER_MINUTES = intPreferencesKey("break_reminder_minutes")
        val BLOCKED_DOMAINS_CSV = stringPreferencesKey("blocked_domains_csv")
        val WHITELIST_PACKAGES_CSV = stringPreferencesKey("whitelist_packages_csv")
        val CURRENT_STREAK_DAYS = intPreferencesKey("current_streak_days")
        val USER_NAME = stringPreferencesKey("user_name")
        val CURRENT_USAGE_LEVEL = stringPreferencesKey("current_usage_level")
        val PRIMARY_GOAL = stringPreferencesKey("primary_goal")
        val LAST_STREAK_DATE = stringPreferencesKey("last_streak_date")
        val TOTAL_TREES_PLANTED = intPreferencesKey("total_trees_planted")
        val TOTAL_MINUTES_SAVED = intPreferencesKey("total_minutes_saved")
        val STRICT_UNLOCK_TYPE = stringPreferencesKey("strict_unlock_type")
        val STRICT_PIN = stringPreferencesKey("strict_pin")
        val EMERGENCY_DELAY_SECONDS = intPreferencesKey("emergency_delay_seconds")
    }

    val userName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.USER_NAME] ?: "Friend"
    }

    val currentUsageLevel: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.CURRENT_USAGE_LEVEL] ?: "4-6 hours"
    }

    val primaryGoal: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.PRIMARY_GOAL] ?: "Quit Doomscrolling (Reels & Shorts)"
    }

    val totalTreesPlanted: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.TOTAL_TREES_PLANTED] ?: 3
    }

    val totalMinutesSaved: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.TOTAL_MINUTES_SAVED] ?: 180
    }

    val strictUnlockType: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.STRICT_UNLOCK_TYPE] ?: "MATH" // "MATH", "PIN", "TIMER"
    }

    val strictPin: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.STRICT_PIN] ?: "1234"
    }

    val emergencyDelaySeconds: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.EMERGENCY_DELAY_SECONDS] ?: 60
    }

    val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.HAS_COMPLETED_ONBOARDING] ?: false
    }

    val isPremium: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_PREMIUM] ?: true  // Changed to true
    }

    val dailyGoalMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.DAILY_GOAL_MINUTES] ?: 300 // default 5 hours
    }

    val isStrictModeEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.STRICT_MODE_ENABLED] ?: false
    }

    val activeThemeId: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.ACTIVE_THEME_ID] ?: "emerald"
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
    }

    val breakReminderMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.BREAK_REMINDER_MINUTES] ?: 45
    }

    val blockedDomains: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val csv = prefs[Keys.BLOCKED_DOMAINS_CSV]
        if (csv == null) {
            listOf("instagram.com", "youtube.com", "tiktok.com", "x.com", "twitter.com", "facebook.com", "reddit.com")
        } else if (csv.isBlank()) {
            emptyList()
        } else {
            csv.split(",").map { it.trim() }
        }
    }

    val whitelistPackages: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        val csv = prefs[Keys.WHITELIST_PACKAGES_CSV] ?: ""
        val defaults = setOf("com.android.phone", "com.google.android.dialer", "com.android.settings")
        if (csv.isBlank()) defaults else defaults + csv.split(",").map { it.trim() }.toSet()
    }

    val currentStreakDays: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.CURRENT_STREAK_DAYS] ?: 1
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.HAS_COMPLETED_ONBOARDING] = completed }
    }

    suspend fun setPremium(premium: Boolean) {
        context.dataStore.edit { it[Keys.IS_PREMIUM] = premium }
    }

    suspend fun setDailyGoalMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.DAILY_GOAL_MINUTES] = minutes }
    }

    suspend fun setStrictMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.STRICT_MODE_ENABLED] = enabled }
    }

    suspend fun setActiveThemeId(themeId: String) {
        context.dataStore.edit { it[Keys.ACTIVE_THEME_ID] = themeId }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setBreakReminderMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.BREAK_REMINDER_MINUTES] = minutes }
    }

    suspend fun addBlockedDomain(domain: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.BLOCKED_DOMAINS_CSV]?.split(",")?.map { it.trim() } ?: emptyList()
            val updated = (current + domain.lowercase().trim()).distinct().joinToString(",")
            prefs[Keys.BLOCKED_DOMAINS_CSV] = updated
        }
    }

    suspend fun removeBlockedDomain(domain: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.BLOCKED_DOMAINS_CSV]?.split(",")?.map { it.trim() } ?: emptyList()
            val updated = current.filterNot { it.equals(domain.trim(), ignoreCase = true) }.joinToString(",")
            prefs[Keys.BLOCKED_DOMAINS_CSV] = updated
        }
    }

    suspend fun updateStreakDays(days: Int) {
        context.dataStore.edit { it[Keys.CURRENT_STREAK_DAYS] = days }
    }

    suspend fun setUserProfile(name: String, usageLevel: String, goal: String, dailyGoalMinutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_NAME] = name.ifBlank { "Friend" }
            prefs[Keys.CURRENT_USAGE_LEVEL] = usageLevel
            prefs[Keys.PRIMARY_GOAL] = goal
            prefs[Keys.DAILY_GOAL_MINUTES] = dailyGoalMinutes
        }
    }

    suspend fun incrementTreesPlanted() {
        context.dataStore.edit { prefs ->
            val cur = prefs[Keys.TOTAL_TREES_PLANTED] ?: 0
            prefs[Keys.TOTAL_TREES_PLANTED] = cur + 1
        }
    }

    suspend fun addMinutesSaved(minutes: Int) {
        context.dataStore.edit { prefs ->
            val cur = prefs[Keys.TOTAL_MINUTES_SAVED] ?: 0
            prefs[Keys.TOTAL_MINUTES_SAVED] = cur + minutes
        }
    }

    suspend fun setStrictUnlockConfig(unlockType: String, pin: String, delaySeconds: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.STRICT_UNLOCK_TYPE] = unlockType
            prefs[Keys.STRICT_PIN] = pin
            prefs[Keys.EMERGENCY_DELAY_SECONDS] = delaySeconds
        }
    }
}
