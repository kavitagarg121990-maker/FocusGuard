package com.focusguard.app.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.focusguard.app.data.repository.PreferencesRepository
import com.focusguard.app.ui.block.BlockScreen
import com.focusguard.app.ui.focus.FocusScreen
import com.focusguard.app.ui.home.HomeScreen
import com.focusguard.app.ui.onboarding.OnboardingScreen
import com.focusguard.app.ui.premium.PremiumPaywallScreen
import com.focusguard.app.ui.settings.SettingsScreen
import com.focusguard.app.ui.stats.StatsScreen
import com.focusguard.app.ui.tips.TipsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusNavGraph(
    preferencesRepository: PreferencesRepository,
    navController: NavHostController = rememberNavController()
) {
    val hasCompletedOnboarding by preferencesRepository.hasCompletedOnboarding.collectAsState(initial = true)
    val isPremium by preferencesRepository.isPremium.collectAsState(initial = true)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevelDestination = currentRoute in listOf(
        Screen.Home.route,
        Screen.Focus.route,
        Screen.Block.route,
        Screen.Stats.route,
        Screen.Tips.route,
        Screen.Settings.route
    )

    Scaffold(
        containerColor = Color(0xFF090E0C),
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (isTopLevelDestination) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        shape = RoundedCornerShape(32.dp),
                        color = Color(0xFF131F1B).copy(alpha = 0.98f),
                        border = BorderStroke(1.dp, Color(0xFF1E362E)),
                        shadowElevation = 10.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            bottomNavItems.forEach { item ->
                                val isSelected = currentRoute == item.route
                                if (isSelected) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFF1B4D3E),
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.title,
                                                tint = Color(0xFF00E676),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF00E676)
                                                )
                                            )
                                        }
                                    }
                                } else {
                                    IconButton(
                                        onClick = {
                                            if (currentRoute != item.route) {
                                                navController.navigate(item.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.title,
                                            tint = Color(0xFF7A8D87),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
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

                composable(Screen.Home.route) {
                    HomeScreen(
                        onNavigateToFocus = { navController.navigate(Screen.Focus.route) },
                        onNavigateToBlock = { navController.navigate(Screen.Block.route) },
                        onNavigateToStats = { navController.navigate(Screen.Stats.route) }
                    )
                }

                composable(Screen.Focus.route) {
                    FocusScreen(
                        onNavigateToPremium = { navController.navigate(Screen.Premium.route) }
                    )
                }

                composable(Screen.Block.route) {
                    BlockScreen(
                        onNavigateToPremium = { navController.navigate(Screen.Premium.route) }
                    )
                }

                composable(Screen.Stats.route) {
                    StatsScreen(
                        onNavigateToPremium = { navController.navigate(Screen.Premium.route) }
                    )
                }

                composable(Screen.Tips.route) {
                    TipsScreen()
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
