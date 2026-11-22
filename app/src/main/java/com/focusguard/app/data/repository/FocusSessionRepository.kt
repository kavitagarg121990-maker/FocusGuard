package com.focusguard.app.data.repository

import com.focusguard.app.data.local.dao.FocusSessionDao
import com.focusguard.app.data.local.entity.FocusSessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class FocusSessionRepository(
    private val focusSessionDao: FocusSessionDao,
    private val preferencesRepository: PreferencesRepository
) {
    val allSessions: Flow<List<FocusSessionEntity>> = focusSessionDao.getAllSessions()
    val totalFocusMinutes: Flow<Int?> = focusSessionDao.getTotalFocusMinutes()
    val completedCount: Flow<Int> = focusSessionDao.getCompletedSessionCount()

    suspend fun recordCompletedSession(
        startTimeMillis: Long,
        durationMinutes: Int,
        sessionType: String,
        isStrict: Boolean,
        themeId: String,
        soundId: String
    ): Long = withContext(Dispatchers.IO) {
        val currentEpochDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        val entity = FocusSessionEntity(
            startTimeMillis = startTimeMillis,
            durationMinutes = durationMinutes,
            sessionType = sessionType,
            completed = true,
            isStrict = isStrict,
            themeId = themeId,
            soundId = soundId,
            epochDay = currentEpochDay
        )
        val id = focusSessionDao.insertSession(entity)
        updateStreak()
        id
    }

    suspend fun updateStreak(): Int = withContext(Dispatchers.IO) {
        val days = focusSessionDao.getDistinctCompletedEpochDays()
        if (days.isEmpty()) {
            preferencesRepository.updateStreakDays(0)
            return@withContext 0
        }

        val todayEpoch = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        var streak = 0
        var expectedDay = if (days.contains(todayEpoch)) todayEpoch else todayEpoch - 1

        for (day in days) {
            if (day == expectedDay) {
                streak++
                expectedDay--
            } else if (day < expectedDay) {
                break
            }
        }

        preferencesRepository.updateStreakDays(streak)
        streak
    }
}
