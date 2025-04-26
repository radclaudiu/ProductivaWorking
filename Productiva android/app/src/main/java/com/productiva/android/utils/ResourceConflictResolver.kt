package com.productiva.android.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.SparseArray
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Clase utilitaria para resolver conflictos de recursos en tiempo de ejecución.
 * Esta clase se utiliza para garantizar que se carguen los recursos correctos
 * cuando hay potenciales conflictos de nombres o formatos.
 */
class ResourceConflictResolver {
    companion object {
        private const val TAG = "ResourceResolver"
        
        // Caché de recursos de textos e imágenes
        private val stringCache = SparseArray<String>()
        private val drawableCache = SparseArray<Drawable>()
        
        // Lista de recursos que han tenido conflictos
        private val conflictingResources = mutableSetOf<Int>()

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
                    context.resources.getIdentifier("ic_launcher_round", "mipmap", context.packageName),
                    context.resources.getIdentifier("ic_launcher_foreground", "drawable", context.packageName),
                    context.resources.getIdentifier("ic_launcher_background", "drawable", context.packageName)
                )
                
                // Registrar los recursos encontrados
                resourceIds.forEach { id ->
                    if (id != 0) {
                        val name = context.resources.getResourceName(id)
                        Log.d(TAG, "Recurso encontrado: $name (ID: $id)")
                    } else {
                        Log.w(TAG, "Recurso no encontrado para esta consulta")
                    }
                }
                
                // Inicializar el resolvedor de íconos
                IconVersionProvider.logIconCompatibilityInfo(context)
                
                Log.d(TAG, "Resolución de conflictos inicializada correctamente")
            } catch (e: Exception) {
                Log.e(TAG, "Error al inicializar el resolvedor de conflictos", e)
            }
        }
        
        /**
         * Obtiene un recurso drawable de forma segura.
         * 
         * @param context El contexto de la aplicación
         * @param resId ID del recurso drawable
         * @return El drawable solicitado o null si no se encuentra
         */
        fun getDrawableSafely(context: Context, @DrawableRes resId: Int): Drawable? {
            if (resId == 0) return null
            
            // Verificar si está en caché
            val cachedDrawable = drawableCache.get(resId)
            if (cachedDrawable != null) return cachedDrawable
            
            return try {
                val drawable = context.getDrawable(resId)
                if (drawable != null) {
                    drawableCache.put(resId, drawable)
                }
                drawable
            } catch (e: Resources.NotFoundException) {
                Log.e(TAG, "Recurso drawable no encontrado: $resId", e)
                conflictingResources.add(resId)
                null
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener drawable: $resId", e)
                null
            }
        }
        
        /**
         * Obtiene un recurso string de forma segura.
         * 
         * @param context El contexto de la aplicación
         * @param resId ID del recurso string
         * @return El string solicitado o un string vacío si no se encuentra
         */
        fun getStringSafely(context: Context, @StringRes resId: Int): String {
            if (resId == 0) return ""
            
            // Verificar si está en caché
            val cachedString = stringCache.get(resId)
            if (cachedString != null) return cachedString
            
            return try {
                val string = context.getString(resId)
                stringCache.put(resId, string)
                string
            } catch (e: Resources.NotFoundException) {
                Log.e(TAG, "Recurso string no encontrado: $resId", e)
                conflictingResources.add(resId)
                ""
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener string: $resId", e)
                ""
            }
        }
        
        /**
         * Establece un drawable en una vista de forma segura.
         * 
         * @param view La vista donde establecer el drawable
         * @param resId ID del recurso drawable
         * @return true si se estableció correctamente, false en caso contrario
         */
        fun setBackgroundDrawableSafely(view: View, @DrawableRes resId: Int): Boolean {
            if (view.context == null || resId == 0) return false
            
            return try {
                val drawable = getDrawableSafely(view.context, resId)
                drawable?.let {
                    view.background = it
                    true
                } ?: false
            } catch (e: Exception) {
                Log.e(TAG, "Error al establecer background drawable: $resId", e)
                false
            }
        }
        
        /**
         * Registra un conflicto de recursos.
         * 
         * @param resId ID del recurso conflictivo
         * @param message Mensaje descriptivo del conflicto
         */
        fun registerConflict(resId: Int, message: String) {
            if (resId != 0) {
                conflictingResources.add(resId)
                Log.w(TAG, "Conflicto de recursos registrado: $resId - $message")
            }
        }
        
        /**
         * Obtiene la lista de recursos conflictivos.
         * 
         * @return Conjunto de IDs de recursos conflictivos
         */
        fun getConflictingResources(): Set<Int> {
            return conflictingResources.toSet()
        }
        
        /**
         * Limpia la caché de recursos.
         */
        fun clearCache() {
            stringCache.clear()
            drawableCache.clear()
            Log.d(TAG, "Caché de recursos limpiada")
        }
    }
}