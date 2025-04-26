package com.productiva.android.utils

import android.content.Context
import android.os.Build
import android.util.Log

/**
 * Clase utilitaria para proporcionar los íconos correctos según la versión de Android.
 * 
 * Esta clase gestiona la selección de íconos adaptativos para Android 8.0+ (API 26+)
 * y los íconos tradicionales para versiones anteriores.
 */
class IconVersionProvider {
    companion object {
        private const val TAG = "IconVersionProvider"
        
        /**
         * Determina si el dispositivo soporta íconos adaptativos.
         * 
         * @return true si el dispositivo tiene Android 8.0+ (API 26+)
         */
        fun supportsAdaptiveIcons(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        }
        
        /**
         * Registra información sobre la compatibilidad de íconos.
         * 
         * @param context El contexto de la aplicación
         */
        fun logIconCompatibilityInfo(context: Context) {
            Log.d(TAG, "Configuración de íconos:")
            Log.d(TAG, "Versión Android: ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})")
            Log.d(TAG, "¿Soporta íconos adaptativos? ${supportsAdaptiveIcons()}")
            
            // Verificar disponibilidad de recursos de íconos
            try {
                val launcher = context.resources.getIdentifier("ic_launcher", "mipmap", context.packageName)
                val launcherRound = context.resources.getIdentifier("ic_launcher_round", "mipmap", context.packageName)
                
                Log.d(TAG, "Recurso ic_launcher disponible: ${launcher != 0}")
                Log.d(TAG, "Recurso ic_launcher_round disponible: ${launcherRound != 0}")
                
                // Verificar íconos específicos para API 26+
                if (supportsAdaptiveIcons()) {
                    val adaptiveBackground = context.resources.getIdentifier(
                        "ic_launcher_background", "color", context.packageName)
                    val adaptiveForeground = context.resources.getIdentifier(
                        "ic_launcher_foreground", "drawable", context.packageName)
                    
                    Log.d(TAG, "Recurso ic_launcher_background disponible: ${adaptiveBackground != 0}")
                    Log.d(TAG, "Recurso ic_launcher_foreground disponible: ${adaptiveForeground != 0}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al verificar recursos de íconos", e)
            }
        }
    }
}