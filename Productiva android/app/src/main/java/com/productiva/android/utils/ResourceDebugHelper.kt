package com.productiva.android.utils

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File

/**
 * Clase utilitaria para ayudar a depurar problemas de recursos en la aplicación.
 * Proporciona métodos para analizar y reportar información detallada sobre los recursos
 * disponibles en la aplicación, lo que puede ser útil para identificar problemas de compilación.
 */
class ResourceDebugHelper {
    companion object {
        private const val TAG = "ResourceDebugHelper"
        
        /**
         * Analiza y registra información detallada sobre los recursos de la aplicación.
         * Esto puede ser útil para depurar problemas de compilación relacionados con recursos.
         * 
         * @param context El contexto de la aplicación
         */
        fun analyzeResources(context: Context) {
            Log.d(TAG, "==== INICIO DE ANÁLISIS DE RECURSOS ====")
            Log.d(TAG, "Versión de Android: ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})")
            
            // Analizar recursos de iconos
            analyzeIconResources(context)
            
            // Analizar valores y estilos
            analyzeValueResources(context)
            
            // Analizar recursos de drawables
            analyzeDrawableResources(context)
            
            Log.d(TAG, "==== FIN DE ANÁLISIS DE RECURSOS ====")
        }
        
        /**
         * Analiza los recursos de íconos disponibles.
         */
        private fun analyzeIconResources(context: Context) {
            Log.d(TAG, "-- RECURSOS DE ICONOS --")
            
            val iconResources = listOf(
                "ic_launcher" to "mipmap",
                "ic_launcher_round" to "mipmap",
                "ic_launcher_foreground" to "drawable",
                "ic_launcher_background" to "color"
            )
            
            iconResources.forEach { (name, type) ->
                val id = context.resources.getIdentifier(name, type, context.packageName)
                Log.d(TAG, "Recurso '$name' ($type): ${if (id != 0) "ENCONTRADO (ID: $id)" else "NO ENCONTRADO"}")
            }
        }
        
        /**
         * Analiza los recursos de valores (strings, colors, styles, etc.)
         */
        private fun analyzeValueResources(context: Context) {
            Log.d(TAG, "-- RECURSOS DE VALORES --")
            
            // Verificar temas y estilos importantes
            val styleResources = listOf(
                "Theme.Productiva" to "style",
                "AppIconAdaptive" to "style"
            )
            
            styleResources.forEach { (name, type) ->
                val id = context.resources.getIdentifier(name, type, context.packageName)
                Log.d(TAG, "Estilo '$name': ${if (id != 0) "ENCONTRADO (ID: $id)" else "NO ENCONTRADO"}")
            }
            
            // Verificar colores importantes
            val colorResources = listOf(
                "colorPrimary", "colorPrimaryDark", "colorAccent", "colorPrimaryLight", "colorAccentLight"
            )
            
            colorResources.forEach { name ->
                val id = context.resources.getIdentifier(name, "color", context.packageName)
                Log.d(TAG, "Color '$name': ${if (id != 0) "ENCONTRADO (ID: $id)" else "NO ENCONTRADO"}")
            }
        }
        
        /**
         * Analiza los recursos de drawables disponibles.
         */
        private fun analyzeDrawableResources(context: Context) {
            Log.d(TAG, "-- RECURSOS DE DRAWABLES --")
            
            val drawableResources = listOf(
                "ic_user", "ic_change_user", "ic_logout", "logo"
            )
            
            drawableResources.forEach { name ->
                val id = context.resources.getIdentifier(name, "drawable", context.packageName)
                Log.d(TAG, "Drawable '$name': ${if (id != 0) "ENCONTRADO (ID: $id)" else "NO ENCONTRADO"}")
            }
        }
        
        /**
         * Reporta una solución detallada para problemas comunes de compilación.
         * Este método puede invocarse desde diferentes partes de la aplicación
         * para proporcionar información útil para resolver problemas.
         */
        fun reportSolutionForBuildIssues() {
            Log.e(TAG, "==== SOLUCIONES PARA PROBLEMAS DE COMPILACIÓN ====")
            Log.e(TAG, "Si continúas teniendo problemas de compilación, intenta las siguientes soluciones:")
            Log.e(TAG, "1. Invalidar caché y reiniciar (File > Invalidate Caches / Restart)")
            Log.e(TAG, "2. Limpiar el proyecto (Build > Clean Project)")
            Log.e(TAG, "3. Eliminar manualmente las carpetas .gradle y build")
            Log.e(TAG, "4. Verificar que el SDK está correctamente instalado")
            Log.e(TAG, "5. Asegurar que todos los archivos de recursos tienen nombres únicos")
            Log.e(TAG, "6. Si hay conflictos con íconos, eliminar manualmente los .png duplicados")
            Log.e(TAG, "==== FIN DE REPORTE DE SOLUCIONES ====")
        }
    }
}