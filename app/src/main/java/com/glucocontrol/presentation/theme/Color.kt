package com.glucocontrol.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Colores de estado de glucosa — accesibles desde cualquier componente
val GlucoseLow = Color(0xFFE65100) // Naranja: por debajo del rango objetivo
val GlucoseNormal = Color(0xFF2E7D32) // Verde:   dentro del rango objetivo
val GlucoseHigh = Color(0xFFC62828) // Rojo:    por encima del rango objetivo

private val Blue700 = Color(0xFF1976D2)
private val Blue200 = Color(0xFF90CAF9)
private val Blue50 = Color(0xFFE3F2FD)
private val Teal600 = Color(0xFF00897B)
private val Teal200 = Color(0xFF80CBC4)
private val Grey900 = Color(0xFF212121)
private val Grey50 = Color(0xFFFAFAFA)

internal val LightColorScheme =
    lightColorScheme(
        primary = Blue700,
        onPrimary = Color.White,
        primaryContainer = Blue50,
        onPrimaryContainer = Grey900,
        secondary = Teal600,
        onSecondary = Color.White,
        secondaryContainer = Teal200,
        background = Color.White,
        surface = Color.White,
        onBackground = Grey900,
        onSurface = Grey900,
    )

internal val DarkColorScheme =
    darkColorScheme(
        primary = Blue200,
        onPrimary = Grey900,
        primaryContainer = Blue700,
        secondary = Teal200,
        onSecondary = Grey900,
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
    )
