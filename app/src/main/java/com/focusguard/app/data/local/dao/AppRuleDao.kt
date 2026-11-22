package com.focusguard.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focusguard.app.data.local.entity.AppRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppRuleDao {
    @Query("SELECT * FROM app_rules ORDER BY appName ASC")
    fun getAllRules(): Flow<List<AppRuleEntity>>

    @Query("SELECT * FROM app_rules WHERE isBlocked = 1 OR dailyLimitMinutes IS NOT NULL")
    fun getEnforcedRules(): Flow<List<AppRuleEntity>>

    @Query("SELECT * FROM app_rules WHERE isBlocked = 1 OR dailyLimitMinutes IS NOT NULL")
    suspend fun getEnforcedRulesSync(): List<AppRuleEntity>

    @Query("SELECT * FROM app_rules WHERE packageName = :packageName LIMIT 1")
    suspend fun getRuleForPackage(packageName: String): AppRuleEntity?

    @Query("SELECT * FROM app_rules WHERE packageName = :packageName LIMIT 1")
    fun observeRuleForPackage(packageName: String): Flow<AppRuleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRule(rule: AppRuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRules(rules: List<AppRuleEntity>)

    @Query("UPDATE app_rules SET isBlocked = :isBlocked WHERE packageName = :packageName")
    suspend fun updateBlockedStatus(packageName: String, isBlocked: Boolean)

    @Query("UPDATE app_rules SET dailyLimitMinutes = :limitMinutes WHERE packageName = :packageName")
    suspend fun updateDailyLimit(packageName: String, limitMinutes: Int?)

    @Query("UPDATE app_rules SET isWhitelisted = :isWhitelisted WHERE packageName = :packageName")
    suspend fun updateWhitelistStatus(packageName: String, isWhitelisted: Boolean)

    @Query("SELECT COUNT(*) FROM app_rules WHERE dailyLimitMinutes IS NOT NULL")
    suspend fun countLimitedApps(): Int

    @Query("DELETE FROM app_rules WHERE packageName = :packageName")
    suspend fun deleteRule(packageName: String)
}
