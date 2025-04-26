package com.productiva.android.network

import android.util.Log
import com.productiva.android.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Interceptor para añadir el token de autorización a las peticiones.
 */
class AuthInterceptor : Interceptor {
    
    private val TAG = "AuthInterceptor"
    
    /**
     * Intercepta la petición y añade el token de autorización si existe.
     * 
     * @param chain Cadena de interceptores.
     * @return Respuesta de la petición.
     */
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        // Obtener la petición original
        val originalRequest = chain.request()
        
        // Verificar si la ruta requiere autorización
        if (!isAuthRequired(originalRequest.url.toString())) {
            return chain.proceed(originalRequest)
        }
        
        // Intentar obtener el token
        val token = SessionManager.getInstance().getAuthToken()
        
        // Si no hay token, continuar sin él
        if (token.isNullOrEmpty()) {
            Log.d(TAG, "No hay token de autorización disponible")
            return chain.proceed(originalRequest)
        }
        
        // Crear una petición con el token
        val requestBuilder = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .method(originalRequest.method, originalRequest.body)
        
        // Proceder con la petición modificada
        return chain.proceed(requestBuilder.build())
    }
    
    /**
     * Determina si una URL requiere autorización.
     * 
     * @param url URL de la petición.
     * @return true si la URL requiere autorización, false en caso contrario.
     */
    private fun isAuthRequired(url: String): Boolean {
        // Rutas que no requieren autorización
        val noAuthPaths = listOf(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/ping"
        )
        
        // Verificar si la URL contiene alguna de las rutas que no requieren autorización
        for (path in noAuthPaths) {
            if (url.contains(path)) {
                return false
            }
        }
        
        // Por defecto, requerir autorización
        return true
    }
}