package com.focusguard.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Block : Screen("block")
    data object Focus : Screen("focus")
    data object Stats : Screen("stats")
    data object Settings : Screen("settings")
    data object Premium : Screen("premium")
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, "Home", Icons.Default.Home),
    BottomNavItem(Screen.Block.route, "Block", Icons.Default.Block),
    BottomNavItem(Screen.Focus.route, "Focus", Icons.Default.CenterFocusStrong),
    BottomNavItem(Screen.Stats.route, "Stats", Icons.Default.BarChart)
)
