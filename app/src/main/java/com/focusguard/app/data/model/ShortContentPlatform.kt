package com.focusguard.app.data.model

/**
 * Supported short-form video platforms and sections for in-app micro-blocking.
 */
enum class ShortContentPlatform(
    val id: String,
    val displayName: String,
    val packageName: String,
    val description: String,
    val disclaimer: String? = null
) {
    YOUTUBE_SHORTS(
        id = "youtube_shorts",
        displayName = "YouTube Shorts",
        packageName = "com.google.android.youtube",
        description = "Blocks the Shorts tab and Shorts playback feed while keeping main video search and watch functional."
    ),
    INSTAGRAM_REELS(
        id = "instagram_reels",
        displayName = "Instagram Reels",
        packageName = "com.instagram.android",
        description = "Blocks the Reels tab and in-feed short video viewer without preventing DMs or profile viewing."
    ),
    INSTAGRAM_EXPLORE(
        id = "instagram_explore",
        displayName = "Instagram Explore",
        packageName = "com.instagram.android",
        description = "Blocks the algorithm-driven Explore discovery grid to reduce doom-scrolling."
    ),
    FACEBOOK_REELS(
        id = "facebook_reels",
        displayName = "Facebook Reels",
        packageName = "com.facebook.katana",
        description = "Intercepts the Reels tab and continuous story/reels video cards."
    ),
    SNAPCHAT_SPOTLIGHT(
        id = "snapchat_spotlight",
        displayName = "Snapchat Spotlight",
        packageName = "com.snapchat.android",
        description = "Blocks the Spotlight algorithmic tab while leaving private snaps and chats open."
    ),
    TIKTOK_FYP(
        id = "tiktok_fyp",
        displayName = "TikTok 'For You' Feed",
        packageName = "com.zhiliaoapp.musically",
        description = "Blocks the algorithmic loop feed.",
        disclaimer = "TikTok's entire core app is short-form; we recommend setting a full daily app limit instead."
    );

    companion object {
        fun fromId(id: String): ShortContentPlatform? = entries.find { it.id == id }
    }
}

/**
 * Focus audio themes and ambient generators.
 */
enum class FocusSoundType(val id: String, val title: String, val isPremium: Boolean) {
    NONE("none", "Silent", false),
    WHITE_NOISE("white_noise", "White Noise", true),
    RAIN("rain", "Gentle Rain", true),
    BINAURAL("binaural", "Binaural Focus (40Hz)", true)
}

/**
 * Visual themes for Focus session screens.
 */
enum class FocusTheme(
    val id: String,
    val displayName: String,
    val isPremium: Boolean,
    val primaryColorHex: Long,
    val backgroundColorHex: Long
) {
    EMERALD_CALM("emerald", "Emerald Calm", false, 0xFF1D6A5D, 0xFFF4F7F5),
    SLATE_MINIMAL("slate", "Slate Minimal", false, 0xFF3E5055, 0xFF121617),
    LOFI_SUNSET("sunset", "Lofi Sunset", true, 0xFFC86D51, 0xFF1E1418),
    DEEP_FOREST("forest", "Deep Forest", true, 0xFF2D5A46, 0xFF0D1B14),
    MIDNIGHT_NEON("midnight", "Midnight Cyber", true, 0xFF6C5CE7, 0xFF0B0A12);

    companion object {
        fun fromId(id: String): FocusTheme = entries.find { it.id == id } ?: EMERALD_CALM
    }
}
