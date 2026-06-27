package com.glucocontrol.presentation.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.glucocontrol.presentation.component.AppBottomBar
import com.glucocontrol.presentation.component.AppNavigationRail
import com.glucocontrol.presentation.screen.addreading.AddEditReadingScreen
import com.glucocontrol.presentation.screen.auth.AuthScreen
import com.glucocontrol.presentation.screen.auth.AuthViewModel
import com.glucocontrol.presentation.screen.chart.ChartScreen
import com.glucocontrol.presentation.screen.history.HistoryScreen
import com.glucocontrol.presentation.screen.home.HomeScreen
import com.glucocontrol.presentation.screen.readingdetail.ReadingDetailScreen

/**
 * Punto de entrada de la UI. Selecciona automáticamente entre:
 * - [AppBottomBar] + [Scaffold] para teléfono (ancho < 600 dp)
 * - [AppNavigationRail] lateral para tableta (ancho ≥ 600 dp)
 */
@Composable
fun GlucoApp() {
    val navController = rememberNavController()
    val isTablet = LocalConfiguration.current.screenWidthDp >= 600

    // Ocultar la navegación en AuthScreen para no permitir acceso sin sesión
    val currentRoute by navController.currentBackStackEntryAsState()
    val showNav = currentRoute?.destination?.route != Screen.Auth.route

    if (isTablet) {
        Row(Modifier.fillMaxSize()) {
            if (showNav) AppNavigationRail(navController = navController)
            AppNavHost(navController = navController, modifier = Modifier.weight(1f))
        }
    } else {
        Scaffold(
            bottomBar = {
                if (showNav) AppBottomBar(navController = navController)
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
    // Activity-scoped: el mismo AuthViewModel comparte estado entre AuthScreen y HomeScreen
    val activity = LocalContext.current as ComponentActivity
    val authViewModel: AuthViewModel = hiltViewModel(activity)

    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route,
        modifier = modifier,
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    // Reemplaza Auth en la back stack para que "Atrás" no vuelva al login
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onAddReading = { navController.navigate(Screen.AddEditReading.route()) },
                onHistory = { navController.navigate(Screen.History.route) },
                onChart = { navController.navigate(Screen.Chart.route()) },
                onSignOut = {
                    authViewModel.signOut()
                    // Limpia toda la back stack y vuelve a la pantalla de login
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onEditReading = { id -> navController.navigate(Screen.AddEditReading.route(id)) },
                onViewDetail = { id -> navController.navigate(Screen.ReadingDetail.route(id)) },
            )
        }

        composable(
            route = Screen.ReadingDetail.route,
            arguments = listOf(navArgument("readingId") { type = NavType.LongType }),
        ) {
            ReadingDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Screen.AddEditReading.route(id)) },
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
    if (screen == Screen.Home) {
        // popBackStack garantiza llegar al dashboard desde cualquier nivel de profundidad
        popBackStack(route = Screen.Home.route, inclusive = false)
    } else {
        navigate(screen.route) {
            popUpTo(graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}
