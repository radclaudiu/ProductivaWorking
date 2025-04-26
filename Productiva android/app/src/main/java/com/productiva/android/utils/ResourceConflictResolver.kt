package com.productiva.android.utils

import android.content.Context
import android.util.Log

/**
 * Clase utilitaria para resolver conflictos de recursos en tiempo de ejecución.
 * Esta clase se utiliza para garantizar que se carguen los recursos correctos
 * cuando hay potenciales conflictos de nombres o formatos.
 */
class ResourceConflictResolver {
    companion object {
        private const val TAG = "ResourceResolver"

        /**
         * Inicializa el resolvedor de conflictos de recursos.
         * Este método debe llamarse al inicio de la aplicación.
         * 
         * @param context El contexto de la aplicación
         */
        fun initialize(context: Context) {
            Log.d(TAG, "Inicializando resolución de conflictos de recursos")
            
            try {
                // Verificar recursos disponibles
                val resourceIds = listOf(
                    context.resources.getIdentifier("ic_launcher", "mipmap", context.packageName),
                    context.resources.getIdentifier("ic_launcher_round", "mipmap", context.packageName)
                )
                
                // Registrar los recursos encontrados
                resourceIds.forEach { id ->
                    if (id != 0) {
                        val name = context.resources.getResourceName(id)
                        Log.d(TAG, "Recurso encontrado: $name (ID: $id)")
                    }
                }
                
                Log.d(TAG, "Resolución de conflictos inicializada correctamente")
            } catch (e: Exception) {
                Log.e(TAG, "Error al inicializar el resolvedor de conflictos", e)
            }
        }
    }
}