package com.productiva.android.network

import com.productiva.android.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor para añadir token de autenticación a las peticiones.
 * Agrega el header de autorización con el token JWT a todas las peticiones.
 */
class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Si no hay token, enviar la petición sin modificar
        val token = sessionManager.getAuthToken() ?: return chain.proceed(originalRequest)
        
        // Construir nueva petición con el token de autenticación
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        
        return chain.proceed(newRequest)
    }
}