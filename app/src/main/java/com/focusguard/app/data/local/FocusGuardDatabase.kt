package com.focusguard.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.focusguard.app.data.local.dao.AppRuleDao
import com.focusguard.app.data.local.dao.BlockScheduleDao
import com.focusguard.app.data.local.dao.FocusSessionDao
import com.focusguard.app.data.local.dao.ShortContentRuleDao
import com.focusguard.app.data.local.dao.UsageHistoryDao
import com.focusguard.app.data.local.entity.AppRuleEntity
import com.focusguard.app.data.local.entity.BlockScheduleEntity
import com.focusguard.app.data.local.entity.FocusSessionEntity
import com.focusguard.app.data.local.entity.ShortContentRuleEntity
import com.focusguard.app.data.local.entity.UsageHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AppRuleEntity::class,
        ShortContentRuleEntity::class,
        BlockScheduleEntity::class,
        FocusSessionEntity::class,
        UsageHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FocusGuardDatabase : RoomDatabase() {
    abstract fun appRuleDao(): AppRuleDao
    abstract fun shortContentRuleDao(): ShortContentRuleDao
    abstract fun blockScheduleDao(): BlockScheduleDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun usageHistoryDao(): UsageHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: FocusGuardDatabase? = null

        fun getInstance(context: Context): FocusGuardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FocusGuardDatabase::class.java,
                    "focusguard_database.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed initial short-form content detection rules
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedShortContentRules(database.shortContentRuleDao())
                    }
                }
            }

            private suspend fun seedShortContentRules(dao: ShortContentRuleDao) {
                val defaults = listOf(
                    ShortContentRuleEntity(
                        id = "youtube_shorts",
                        displayName = "YouTube Shorts",
                        packageName = "com.google.android.youtube",
                        isEnabled = true,
                        targetResourceIdsCsv = "shorts_container,shorts_pivot,reel_recycler,reel_watch_fragment,shorts_shelf,shorts_video_player,pivot_bar",
                        targetContentDescriptionsCsv = "Shorts"
                    ),
                    ShortContentRuleEntity(
                        id = "instagram_reels",
                        displayName = "Instagram Reels",
                        packageName = "com.instagram.android",
                        isEnabled = true,
                        targetResourceIdsCsv = "clips_tab,clips_viewer_container,reel_viewer_root,feed_clips_video_container,clips_video_container,clips_swipe_refresh_layout",
                        targetContentDescriptionsCsv = "Reels,Reels tab"
                    ),
                    ShortContentRuleEntity(
                        id = "instagram_explore",
                        displayName = "Instagram Explore",
                        packageName = "com.instagram.android",
                        isEnabled = false,
                        targetResourceIdsCsv = "search_tab,explore_grid,action_bar_search_edit_text,grid_recycler",
                        targetContentDescriptionsCsv = "Search and Explore"
                    ),
                    ShortContentRuleEntity(
                        id = "facebook_reels",
                        displayName = "Facebook Reels",
                        packageName = "com.facebook.katana",
                        isEnabled = true,
                        targetResourceIdsCsv = "fb_shorts_container,fb_reels_tab,reels_video_view,fb_shorts_viewer_fragment",
                        targetContentDescriptionsCsv = "Reels"
                    ),
                    ShortContentRuleEntity(
                        id = "snapchat_spotlight",
                        displayName = "Snapchat Spotlight",
                        packageName = "com.snapchat.android",
                        isEnabled = false,
                        targetResourceIdsCsv = "spotlight_tab,spotlight_root,vertical_page_indicator",
                        targetContentDescriptionsCsv = "Spotlight"
                    ),
                    ShortContentRuleEntity(
                        id = "tiktok_fyp",
                        displayName = "TikTok 'For You' Feed",
                        packageName = "com.zhiliaoapp.musically",
                        isEnabled = false,
                        targetResourceIdsCsv = "viewpager,main_feed,feed_tab,viewpager_item",
                        targetContentDescriptionsCsv = "For You"
                    )
                )
                dao.insertDefaults(defaults)
            }
        }
    }
}
