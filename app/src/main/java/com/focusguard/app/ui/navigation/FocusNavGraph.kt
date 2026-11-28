package com.focusguard.app.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.focusguard.app.data.repository.PreferencesRepository
import com.focusguard.app.ui.block.BlockScreen
import com.focusguard.app.ui.focus.ActiveFocusSessionScreen
import com.focusguard.app.ui.focus.FocusScreen
import com.focusguard.app.ui.groups.GroupsScreen
import com.focusguard.app.ui.onboarding.OnboardingScreen
import com.focusguard.app.ui.premium.PremiumPaywallScreen
import com.focusguard.app.ui.profile.ProfileScreen
import com.focusguard.app.ui.schedule.ScheduleScreen
import com.focusguard.app.ui.settings.SettingsScreen
import com.focusguard.app.ui.stats.StatsScreen
import com.focusguard.app.ui.tips.TipsScreen
import com.focusguard.app.ui.theme.AmoledBlack
import com.focusguard.app.ui.theme.CardBorderDark
import com.focusguard.app.ui.theme.CardDark
import com.focusguard.app.ui.theme.GuardGreen
import com.focusguard.app.ui.theme.TextMuted

@Composable
fun FocusNavGraph(
    preferencesRepository: PreferencesRepository,
    navController: NavHostController = rememberNavController()
) {
    val hasCompletedOnboarding by preferencesRepository.hasCompletedOnboarding.collectAsState(initial = true)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevelDestination = currentRoute in listOf(
        Screen.Focus.route,
        Screen.Schedule.route,
        Screen.Groups.route,
        Screen.Block.route
    )

    fun navigateTopLevel(route: String) {
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        containerColor = AmoledBlack,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (isTopLevelDestination) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left 3-Tab Floating Pill (Focus | Schedule | Groups)
                    Surface(
                        shape = RoundedCornerShape(32.dp),
                        color = CardDark,
                        border = BorderStroke(1.dp, CardBorderDark),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(66.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            mainPillNavItems.forEach { item ->
                                val selected = currentRoute == item.route
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { navigateTopLevel(item.route) }
                                        .padding(vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = if (selected) GuardGreen else TextMuted,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (selected) GuardGreen else TextMuted
                                    )
                                }
                            }
                        }
                    }

                    // Right Detached Floating Pill (Blocks)
                    val blocksSelected = currentRoute == blockNavItem.route
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = CardDark,
                        border = BorderStroke(
                            1.dp,
                            if (blocksSelected) GuardGreen.copy(alpha = 0.6f) else CardBorderDark
                        ),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .width(86.dp)
                            .height(66.dp)
                            .clickable { navigateTopLevel(blockNavItem.route) }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = blockNavItem.icon,
                                contentDescription = blockNavItem.title,
                                tint = if (blocksSelected) GuardGreen else TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = blockNavItem.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (blocksSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (blocksSelected) GuardGreen else TextMuted
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = if (hasCompletedOnboarding) Screen.Focus.route else Screen.Onboarding.route
            ) {
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onFinished = {
                            navController.navigate(Screen.Focus.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Focus.route) {
                    FocusScreen(
                        onOpenProfile = { navController.navigate(Screen.Profile.route) },
                        onOpenSettings = { navController.navigate(Screen.Settings.route) },
                        onOpenStats = { navController.navigate(Screen.Stats.route) },
                        onOpenSchedule = { navigateTopLevel(Screen.Schedule.route) },
                        onOpenBlocks = { navigateTopLevel(Screen.Block.route) },
                        onLaunchActiveSession = { navController.navigate(Screen.ActiveSession.route) }
                    )
                }

                composable(Screen.Schedule.route) {
                    ScheduleScreen(
                        onOpenProgress = { navController.navigate(Screen.Stats.route) }
                    )
                }

                composable(Screen.Groups.route) {
                    GroupsScreen(
                        onOpenHelp = { navController.navigate(Screen.Tips.route) },
                        onStartGroupFocus = { navigateTopLevel(Screen.Focus.route) }
                    )
                }

                composable(Screen.Block.route) {
                    BlockScreen(
                        onOpenHelp = { navController.navigate(Screen.Settings.route) }
                    )
                }

                composable(Screen.ActiveSession.route) {
                    ActiveFocusSessionScreen(
                        onOpenGroups = {
                            navController.popBackStack()
                            navigateTopLevel(Screen.Groups.route)
                        },
                        onExitSession = { navController.popBackStack() }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onOpenSettings = { navController.navigate(Screen.Settings.route) },
                        onOpenStats = { navController.navigate(Screen.Stats.route) },
                        onStartFocus = {
                            navController.popBackStack()
                            navigateTopLevel(Screen.Focus.route)
                        }
                    )
                }

                composable(Screen.Stats.route) {
                    StatsScreen(
                        onNavigateToPremium = { navController.navigate(Screen.Premium.route) },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Tips.route) {
                    TipsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToPremium = { navController.navigate(Screen.Premium.route) }
                    )
                }

                composable(Screen.Premium.route) {
                    PremiumPaywallScreen(
                        onDismiss = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
