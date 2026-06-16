package com.glucocontrol.presentation.navigation

/** Rutas de navegación de la app. readingId = -1 indica "nueva lectura". */
sealed class Screen(val route: String) {
    data object Home : Screen("home")

    data object History : Screen("history")

    data object AddEditReading : Screen("add_edit/{readingId}") {
        fun route(readingId: Long = -1L) = "add_edit/$readingId"
    }

    // period es el nombre del enum ChartPeriod (WEEKLY / MONTHLY)
    data object Chart : Screen("chart/{period}") {
        fun route(period: String = "WEEKLY") = "chart/$period"
    }
}
