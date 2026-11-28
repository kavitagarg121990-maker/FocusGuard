package com.focusguard.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Focus : Screen("focus")
    data object Schedule : Screen("schedule")
    data object Groups : Screen("groups")
    data object Block : Screen("block")
    data object ActiveSession : Screen("active_session")
    data object Profile : Screen("profile")
    data object Stats : Screen("stats")
    data object Tips : Screen("tips")
    data object Settings : Screen("settings")
    data object Premium : Screen("premium")
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

val mainPillNavItems = listOf(
    BottomNavItem(Screen.Focus.route, "Focus", Icons.Default.HourglassEmpty),
    BottomNavItem(Screen.Schedule.route, "Schedule", Icons.Default.CalendarMonth),
    BottomNavItem(Screen.Groups.route, "Groups", Icons.Default.Groups)
)

val blockNavItem = BottomNavItem(Screen.Block.route, "Blocks", Icons.Default.Block)
