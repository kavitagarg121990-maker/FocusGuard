package com.focusguard.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusguard.app.data.local.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startTimeMillis DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE completed = 1 ORDER BY startTimeMillis DESC LIMIT :limit")
    fun getRecentCompletedSessions(limit: Int = 20): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Query("SELECT DISTINCT epochDay FROM focus_sessions WHERE completed = 1 ORDER BY epochDay DESC")
    suspend fun getDistinctCompletedEpochDays(): List<Long>

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions WHERE completed = 1")
    fun getTotalFocusMinutes(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE completed = 1")
    fun getCompletedSessionCount(): Flow<Int>
}
