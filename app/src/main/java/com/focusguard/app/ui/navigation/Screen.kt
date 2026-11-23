package com.focusguard.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Focus : Screen("focus")
    data object Block : Screen("block")
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

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, "Home", Icons.Default.HourglassEmpty),
    BottomNavItem(Screen.Focus.route, "Focus", Icons.Default.Timer),
    BottomNavItem(Screen.Block.route, "Block", Icons.Default.Block),
    BottomNavItem(Screen.Stats.route, "Stats", Icons.Default.BarChart),
    BottomNavItem(Screen.Tips.route, "Tips", Icons.Default.Lightbulb),
    BottomNavItem(Screen.Settings.route, "Settings", Icons.Default.Settings)
)
