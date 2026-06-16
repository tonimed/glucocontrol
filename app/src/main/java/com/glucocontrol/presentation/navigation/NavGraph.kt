package com.glucocontrol.presentation.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.glucocontrol.presentation.component.AppBottomBar
import com.glucocontrol.presentation.component.AppNavigationRail
import com.glucocontrol.presentation.screen.addreading.AddEditReadingScreen
import com.glucocontrol.presentation.screen.chart.ChartScreen
import com.glucocontrol.presentation.screen.history.HistoryScreen
import com.glucocontrol.presentation.screen.home.HomeScreen

/**
 * Punto de entrada de la UI. Selecciona automáticamente entre:
 * - [AppBottomBar] + [Scaffold] para teléfono (ancho < 600 dp)
 * - [AppNavigationRail] lateral para tableta (ancho ≥ 600 dp)
 */
@Composable
fun GlucoApp() {
    val navController = rememberNavController()
    val isTablet = LocalConfiguration.current.screenWidthDp >= 600

    if (isTablet) {
        Row(Modifier.fillMaxSize()) {
            AppNavigationRail(navController = navController)
            AppNavHost(navController = navController, modifier = Modifier.weight(1f))
        }
    } else {
        Scaffold(
            bottomBar = {
                // Visible en todas las pantallas para permitir volver al dashboard siempre
                AppBottomBar(navController = navController)
            },
        ) { padding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onAddReading = { navController.navigate(Screen.AddEditReading.route()) },
                onHistory = { navController.navigate(Screen.History.route) },
                onChart = { navController.navigate(Screen.Chart.route()) },
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onEditReading = { id -> navController.navigate(Screen.AddEditReading.route(id)) },
            )
        }
        composable(
            route = Screen.AddEditReading.route,
            arguments = listOf(navArgument("readingId") { type = NavType.LongType }),
        ) {
            AddEditReadingScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.Chart.route,
            arguments = listOf(
                navArgument("period") {
                    type = NavType.StringType
                    defaultValue = "WEEKLY"
                },
            ),
        ) {
            ChartScreen(onBack = { navController.popBackStack() })
        }
    }
}

/** Navega a una pantalla principal guardando estado y evitando copias en la back stack. */
internal fun NavHostController.navigateTopLevel(screen: Screen) {
    navigate(screen.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
