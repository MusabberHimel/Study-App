package com.musabber.pomofocus.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.musabber.pomofocus.ui.screens.AboutScreen
import com.musabber.pomofocus.ui.screens.MainScreen
import com.musabber.pomofocus.ui.screens.SettingsScreen
import com.musabber.pomofocus.ui.screens.StatisticsScreen
import com.musabber.pomofocus.viewmodel.SettingsViewModel
import com.musabber.pomofocus.viewmodel.StatisticsViewModel
import com.musabber.pomofocus.viewmodel.TimerViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val timerViewModel: TimerViewModel = viewModel()
    val statisticsViewModel: StatisticsViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.AboutDeveloper.route) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Screen.Main.route,
                        onClick = {
                            navController.navigate(Screen.Main.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Main") },
                        label = { Text("Main") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Statistics.route,
                        onClick = {
                            navController.navigate(Screen.Statistics.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Filled.BarChart, contentDescription = "Statistics") },
                        label = { Text("Statistics") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Settings.route,
                        onClick = {
                            navController.navigate(Screen.Settings.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Main.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Main.route) {
                MainScreen(viewModel = timerViewModel)
            }
            composable(Screen.Statistics.route) {
                StatisticsScreen(viewModel = statisticsViewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateToAbout = {
                        navController.navigate(Screen.AboutDeveloper.route)
                    }
                )
            }
            composable(Screen.AboutDeveloper.route) {
                AboutScreen()
            }
        }
    }
}