package com.focusguard.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusguard.app.data.local.entity.UsageHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageHistoryDao {
    @Query("SELECT * FROM usage_history WHERE epochDay = :epochDay ORDER BY totalTimeInForegroundMillis DESC")
    fun getUsageForDay(epochDay: Long): Flow<List<UsageHistoryEntity>>

    @Query("SELECT * FROM usage_history WHERE epochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY epochDay ASC")
    fun getUsageInRange(startEpochDay: Long, endEpochDay: Long): Flow<List<UsageHistoryEntity>>

    @Query("SELECT * FROM usage_history WHERE epochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY epochDay ASC")
    suspend fun getUsageInRangeSync(startEpochDay: Long, endEpochDay: Long): List<UsageHistoryEntity>

    @Query("SELECT * FROM usage_history ORDER BY epochDay ASC")
    suspend fun getAllUsageSync(): List<UsageHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDayUsage(entries: List<UsageHistoryEntity>)

    @Query("SELECT SUM(blockedAttemptsCount) FROM usage_history WHERE epochDay >= :sinceEpochDay")
    fun getTotalBlockedAttemptsSince(sinceEpochDay: Long): Flow<Int?>

    @Query("SELECT SUM(estimatedTimeSavedMinutes) FROM usage_history WHERE epochDay >= :sinceEpochDay")
    fun getTotalEstimatedSavedMinutesSince(sinceEpochDay: Long): Flow<Long?>

    @Query("UPDATE usage_history SET blockedAttemptsCount = blockedAttemptsCount + 1, estimatedTimeSavedMinutes = estimatedTimeSavedMinutes + :addedMinutes WHERE epochDay = :epochDay AND packageName = :packageName")
    suspend fun incrementBlockedAttempt(epochDay: Long, packageName: String, addedMinutes: Long)
}
