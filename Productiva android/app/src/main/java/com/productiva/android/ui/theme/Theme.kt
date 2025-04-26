package com.productiva.android.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Esquema de color para tema claro
private val LightColorScheme = lightColorScheme(
    primary = ProductivaBlue,
    onPrimary = White,
    primaryContainer = LightBlue,
    onPrimaryContainer = DarkBlue,
    secondary = ProductivaOrange,
    onSecondary = White,
    secondaryContainer = LightOrange,
    onSecondaryContainer = DarkOrange,
    tertiary = ProductivaGreen,
    onTertiary = White,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = DarkGray,
    surfaceVariant = LightGray,
    error = ProductivaRed,
    onError = White
)

// Esquema de color para tema oscuro
private val DarkColorScheme = darkColorScheme(
    primary = ProductivaBlueDark,
    onPrimary = White,
    primaryContainer = DarkBlue,
    onPrimaryContainer = LightBlue,
    secondary = ProductivaOrangeDark,
    onSecondary = White,
    secondaryContainer = DarkOrange,
    onSecondaryContainer = LightOrange,
    tertiary = ProductivaGreenDark,
    onTertiary = White,
    background = DarkBackground,
    onBackground = White,
    surface = DarkSurface,
    onSurface = LightGray,
    surfaceVariant = DarkGray,
    error = ProductivaRedDark,
    onError = White
)

/**
 * Tema principal de la aplicación Productiva.
 */
@Composable
fun ProductivaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Soporte para Colores Dinámicos (Android 12+)
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    // Configurar el color de la barra de estado
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = ProductivaTypography,
        shapes = Shapes,
        content = content
    )
}