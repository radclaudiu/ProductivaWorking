package com.productiva.android.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

/**
 * Utilidad para manejar iconos y asegurar compatibilidad de recursos.
 * Esta clase se usa para garantizar que los iconos se carguen correctamente,
 * independientemente del formato de archivo (WebP o PNG).
 */
class IconUtil {
    companion object {
        /**
         * Carga un icono de los recursos de la aplicación.
         * Este método garantiza la compatibilidad con recursos WebP.
         *
         * @param context El contexto de la aplicación
         * @param resourceId El ID del recurso a cargar
         * @return El drawable cargado desde los recursos
         */
        fun loadIcon(context: Context, resourceId: Int) = 
            ContextCompat.getDrawable(context, resourceId)
            
        /**
         * Verifica si el dispositivo admite WebP.
         * En Android moderno, WebP está soportado nativamente.
         *
         * @return true si el dispositivo admite WebP
         */
        fun isWebPSupported(): Boolean {
            // WebP es compatible con Android 4.0+ (API 14+)
            return true
        }
    }
}