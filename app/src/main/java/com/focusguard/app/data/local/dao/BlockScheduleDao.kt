package com.focusguard.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focusguard.app.data.local.entity.BlockScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockScheduleDao {
    @Query("SELECT * FROM block_schedules ORDER BY startHour ASC, startMinute ASC")
    fun getAllSchedules(): Flow<List<BlockScheduleEntity>>

    @Query("SELECT * FROM block_schedules WHERE isEnabled = 1")
    fun getActiveSchedules(): Flow<List<BlockScheduleEntity>>

    @Query("SELECT * FROM block_schedules WHERE isEnabled = 1")
    suspend fun getActiveSchedulesSync(): List<BlockScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: BlockScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: BlockScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: BlockScheduleEntity)

    @Query("UPDATE block_schedules SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setScheduleEnabled(id: Long, isEnabled: Boolean)

    @Query("SELECT COUNT(*) FROM block_schedules")
    suspend fun countSchedules(): Int
}
