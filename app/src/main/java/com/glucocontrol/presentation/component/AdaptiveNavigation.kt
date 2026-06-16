package com.glucocontrol.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.glucocontrol.presentation.navigation.Screen
import com.glucocontrol.presentation.navigation.navigateTopLevel

private data class NavDestination(val screen: Screen, val label: String, val icon: ImageVector)

private val destinations =
    listOf(
        NavDestination(Screen.Home, "Hoy", Icons.Default.Home),
        NavDestination(Screen.History, "Historial", Icons.Default.DateRange),
    )

@Composable
fun AppBottomBar(navController: NavHostController) {
    val entry by navController.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route

    NavigationBar {
        destinations.forEach { dest ->
            NavigationBarItem(
                selected = currentRoute == dest.screen.route,
                onClick = { navController.navigateTopLevel(dest.screen) },
                icon = { Icon(dest.icon, contentDescription = dest.label) },
                label = { Text(dest.label) },
            )
        }
    }
}

@Composable
fun AppNavigationRail(navController: NavHostController) {
    val entry by navController.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route

    NavigationRail {
        destinations.forEach { dest ->
            NavigationRailItem(
                selected = currentRoute == dest.screen.route,
                onClick = { navController.navigateTopLevel(dest.screen) },
                icon = { Icon(dest.icon, contentDescription = dest.label) },
                label = { Text(dest.label) },
            )
        }
    }
}
