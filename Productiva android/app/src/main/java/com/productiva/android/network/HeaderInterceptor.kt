package com.productiva.android.network

import android.os.Build
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Interceptor para añadir headers comunes a todas las peticiones.
 */
class HeaderInterceptor : Interceptor {
    
    /**
     * Intercepta la petición y añade headers comunes.
     * 
     * @param chain Cadena de interceptores.
     * @return Respuesta de la petición.
     */
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        // Obtener la petición original
        val originalRequest = chain.request()
        
        // Crear una petición con headers adicionales
        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("User-Agent", "Productiva-Android/${com.productiva.android.BuildConfig.VERSION_NAME}")
            .header("Device-Model", Build.MODEL)
            .header("Device-Manufacturer", Build.MANUFACTURER)
            .header("Device-OS", "Android ${Build.VERSION.RELEASE}")
            .header("Device-SDK", Build.VERSION.SDK_INT.toString())
            .method(originalRequest.method, originalRequest.body)
        
        // Proceder con la petición modificada
        return chain.proceed(requestBuilder.build())
    }
}