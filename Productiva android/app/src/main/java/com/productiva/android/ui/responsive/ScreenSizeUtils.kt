package com.productiva.android.ui.responsive

import android.content.Context
import android.content.res.Configuration
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

/**
 * Utilidades para adaptar la interfaz de usuario a diferentes tamaños de pantalla.
 * Proporciona métodos para determinar si el dispositivo es una tablet y ajustar layouts.
 */
object ScreenSizeUtils {

    /**
     * Determina si el dispositivo actual es una tablet basado en su tamaño de pantalla.
     *
     * @param context Contexto de la aplicación.
     * @return true si el dispositivo es una tablet, false si es un teléfono.
     */
    fun isTablet(context: Context): Boolean {
        return (context.resources.configuration.screenLayout and
                Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    /**
     * Determina si el dispositivo está en orientación horizontal (landscape).
     *
     * @param context Contexto de la aplicación.
     * @return true si está en orientación horizontal, false en caso contrario.
     */
    fun isLandscape(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    /**
     * Obtiene la clase de tamaño de ancho de ventana actual.
     * Útil para ajustar layouts con Jetpack Compose.
     *
     * @param configuration Configuración actual.
     * @return Clase de tamaño de ancho (COMPACT, MEDIUM o EXPANDED).
     */
    fun getWindowWidthSizeClass(configuration: Configuration): WindowWidthSizeClass {
        return when {
            configuration.screenWidthDp < 600 -> WindowWidthSizeClass.Compact
            configuration.screenWidthDp < 840 -> WindowWidthSizeClass.Medium
            else -> WindowWidthSizeClass.Expanded
        }
    }

    /**
     * Composable que devuelve la clase de tamaño de ancho actual.
     *
     * @return Clase de tamaño de ancho.
     */
    @Composable
    fun currentWindowWidthSizeClass(): WindowWidthSizeClass {
        val configuration = LocalConfiguration.current
        return getWindowWidthSizeClass(configuration)
    }

    /**
     * Determina si el layout debe estar en modo de dos paneles.
     * Útil para interfaces maestro-detalle.
     *
     * @param widthSizeClass Clase de tamaño de ancho.
     * @return true si se debe usar layout de dos paneles, false en caso contrario.
     */
    fun shouldUseTwoPaneLayout(widthSizeClass: WindowWidthSizeClass): Boolean {
        return widthSizeClass == WindowWidthSizeClass.Expanded || 
               widthSizeClass == WindowWidthSizeClass.Medium
    }

    /**
     * Calcula el ancho recomendado para un elemento de lista en función del tamaño de pantalla.
     * Útil para grids adaptables.
     *
     * @param widthSizeClass Clase de tamaño de ancho.
     * @return Ancho recomendado para un elemento en dp.
     */
    fun getRecommendedItemWidth(widthSizeClass: WindowWidthSizeClass): Int {
        return when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> 156
            WindowWidthSizeClass.Medium -> 180
            WindowWidthSizeClass.Expanded -> 200
        }
    }

    /**
     * Determina el número de columnas óptimo para una cuadrícula en la pantalla actual.
     *
     * @param configuration Configuración actual.
     * @param itemWidth Ancho deseado para cada elemento en dp.
     * @return Número de columnas recomendado.
     */
    fun calculateOptimalColumnCount(configuration: Configuration, itemWidth: Int): Int {
        val screenWidth = configuration.screenWidthDp
        return maxOf(2, screenWidth / itemWidth)
    }
}