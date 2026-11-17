package com.dariusepure.gofindauto.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dariusepure.gofindauto.R
import com.dariusepure.gofindauto.ui.cars.AddCarScreen
import com.dariusepure.gofindauto.ui.cars.CarListScreen
import com.dariusepure.gofindauto.ui.settings.SettingsScreen

sealed class MainTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Garage : MainTab(
        route = "garage",
        labelRes = R.string.car_list_title,
        selectedIcon = Icons.Filled.DirectionsCar,
        unselectedIcon = Icons.Outlined.DirectionsCar
    )
    data object Discover : MainTab(
        route = "discover",
        labelRes = R.string.car_add_title,
        selectedIcon = Icons.Filled.PhotoCamera,
        unselectedIcon = Icons.Outlined.PhotoCamera
    )
    data object Settings : MainTab(
        route = "settings_tab",
        labelRes = R.string.common_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}

@Composable
fun MainScreen(
    onCarClick: (String) -> Unit,
    onEditCarClick: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val tabs = listOf(
        MainTab.Garage,
        MainTab.Discover,
        MainTab.Settings
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(tab.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.Garage.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(MainTab.Garage.route) {
                CarListScreen(
                    onCarClick = onCarClick,
                    onAddCarClick = { navController.navigate(MainTab.Discover.route) },
                    onEditCarClick = onEditCarClick,
                    onLogout = onLogout
                )
            }
            composable(MainTab.Discover.route) {
                AddCarScreen(
                    onCarSaved = {
                        navController.navigate(MainTab.Garage.route) {
                            popUpTo(MainTab.Garage.route) { inclusive = false }
                        }
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(MainTab.Settings.route) {
                SettingsScreen(
                    onLogout = onLogout
                )
            }
        }
    }
}
