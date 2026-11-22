package com.focusguard.app

import android.app.Application
import com.focusguard.app.data.local.FocusGuardDatabase
import com.focusguard.app.data.repository.AppBlockingRepository
import com.focusguard.app.data.repository.FocusSessionRepository
import com.focusguard.app.data.repository.PreferencesRepository
import com.focusguard.app.data.repository.UsageStatsRepository
import com.focusguard.app.worker.DailySnapshotWorker

class FocusGuardApp : Application() {

    val database: FocusGuardDatabase by lazy {
        FocusGuardDatabase.getInstance(this)
    }

    val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepository(this)
    }

    val appBlockingRepository: AppBlockingRepository by lazy {
        AppBlockingRepository(
            context = this,
            appRuleDao = database.appRuleDao(),
            shortContentRuleDao = database.shortContentRuleDao(),
            blockScheduleDao = database.blockScheduleDao(),
            preferencesRepository = preferencesRepository
        )
    }

    val focusSessionRepository: FocusSessionRepository by lazy {
        FocusSessionRepository(
            focusSessionDao = database.focusSessionDao(),
            preferencesRepository = preferencesRepository
        )
    }

    val usageStatsRepository: UsageStatsRepository by lazy {
        UsageStatsRepository(
            context = this,
            usageHistoryDao = database.usageHistoryDao(),
            appRuleDao = database.appRuleDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        // Schedule daily background snapshots to persist long-term historical stats
        DailySnapshotWorker.schedule(this)
    }
}
