package com.focusguard.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores platform-specific detection targets for in-app short-form content.
 *
 * NOTE: App UI updates by Google/Meta/ByteDance can change resource IDs or node
 * hierarchies over time. This table decouples the detection strings from the AccessibilityService,
 * allowing instant local rules updates and customizations.
 *
 * All accessibility tree inspection happens strictly on-device in memory.
 * No keystrokes, personal messages, or screen contents are ever read or logged.
 */
@Entity(tableName = "short_content_rules")
data class ShortContentRuleEntity(
    @PrimaryKey val id: String, // e.g. "youtube_shorts"
    val displayName: String,
    val packageName: String,
    val isEnabled: Boolean = false,
    val targetResourceIdsCsv: String, // comma-separated resource-id fragments
    val targetContentDescriptionsCsv: String, // comma-separated content-description strings
    val frictionDelaySeconds: Int = 3,
    val lastUpdatedMillis: Long = System.currentTimeMillis()
)
