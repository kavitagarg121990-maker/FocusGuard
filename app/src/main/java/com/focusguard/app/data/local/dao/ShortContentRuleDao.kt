package com.focusguard.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusguard.app.data.local.entity.ShortContentRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortContentRuleDao {
    @Query("SELECT * FROM short_content_rules ORDER BY displayName ASC")
    fun getAllRules(): Flow<List<ShortContentRuleEntity>>

    @Query("SELECT * FROM short_content_rules WHERE isEnabled = 1")
    fun getActiveRules(): Flow<List<ShortContentRuleEntity>>

    @Query("SELECT * FROM short_content_rules WHERE isEnabled = 1")
    suspend fun getActiveRulesSync(): List<ShortContentRuleEntity>

    @Query("SELECT * FROM short_content_rules WHERE packageName = :packageName AND isEnabled = 1")
    suspend fun getActiveRulesForPackage(packageName: String): List<ShortContentRuleEntity>

    @Query("SELECT * FROM short_content_rules WHERE id = :id LIMIT 1")
    suspend fun getRuleById(id: String): ShortContentRuleEntity?

    @Query("UPDATE short_content_rules SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setEnabled(id: String, isEnabled: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRule(rule: ShortContentRuleEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaults(rules: List<ShortContentRuleEntity>)
}
