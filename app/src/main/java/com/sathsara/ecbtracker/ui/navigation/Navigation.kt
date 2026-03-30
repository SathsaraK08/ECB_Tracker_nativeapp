package com.sathsara.ecbtracker.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sathsara.ecbtracker.R
import com.sathsara.ecbtracker.logic.AppDestination
import com.sathsara.ecbtracker.ui.components.BrandHeader
import com.sathsara.ecbtracker.ui.screens.HomeScreen
import com.sathsara.ecbtracker.ui.screens.LogScreen
import com.sathsara.ecbtracker.ui.screens.LoginScreen
import com.sathsara.ecbtracker.ui.screens.OnboardingScreen
import com.sathsara.ecbtracker.ui.screens.RecordsScreen
import com.sathsara.ecbtracker.ui.screens.SettingsScreen
import com.sathsara.ecbtracker.ui.theme.CyanPrimary
import com.sathsara.ecbtracker.ui.theme.Muted
import com.sathsara.ecbtracker.ui.viewmodel.AppStateViewModel

private data class BottomNavItem(
    val screen: Screen,
    val iconRes: Int,
    val label: String
)

@Composable
fun EcbNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Screen.Gate.route) {
        composable(Screen.Gate.route) {
            AppGateRoute(navController = navController)
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.Gate.route) {
                        popUpTo(Screen.Gate.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Gate.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreenScaffold(
                onLogout = {
                    navController.navigate(Screen.Gate.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun AppGateRoute(
    navController: NavHostController,
    viewModel: AppStateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isLoading, uiState.destination) {
        if (!uiState.isLoading) {
            val target = when (uiState.destination) {
                AppDestination.LOGIN -> Screen.Login.route
                AppDestination.ONBOARDING -> Screen.Onboarding.route
                AppDestination.MAIN -> Screen.Main.route
            }

            navController.navigate(target) {
                popUpTo(Screen.Gate.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        BrandHeader(
            title = "ECB Tracker",
            subtitle = "Preparing your workspace"
        )
    }
}

@Composable
private fun MainScreenScaffold(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navItems = listOf(
        BottomNavItem(Screen.Home, R.drawable.ic_home, "Home"),
        BottomNavItem(Screen.Log, R.drawable.ic_add, "Log"),
        BottomNavItem(Screen.History, R.drawable.ic_list, "History"),
        BottomNavItem(Screen.Settings, R.drawable.ic_settings, "Settings")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = item.iconRes),
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanPrimary,
                            selectedTextColor = CyanPrimary,
                            unselectedIconColor = Muted,
                            unselectedTextColor = Muted,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(onLogReading = { navController.navigate(Screen.Log.route) })
            }
            composable(Screen.Log.route) {
                LogScreen(onSaved = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                })
            }
            composable(Screen.History.route) {
                RecordsScreen(onLogReading = { navController.navigate(Screen.Log.route) })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onLogout = onLogout)
            }
        }
    }
}
