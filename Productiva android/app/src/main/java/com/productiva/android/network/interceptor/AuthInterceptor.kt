package com.productiva.android.network.interceptor

import android.content.Context
import android.util.Log
import com.productiva.android.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Interceptor para añadir token de autenticación a las peticiones.
 * Añade el token JWT en el header Authorization.
 */
class AuthInterceptor(private val context: Context) : Interceptor {
    
    companion object {
        private const val TAG = "AuthInterceptor"
    }
    
    /**
     * Intercepta la petición y añade el header de autorización.
     *
     * @param chain Cadena de interceptores.
     * @return Respuesta de la petición.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // No añadir token en peticiones de login
        if (isAuthenticationRequest(originalRequest)) {
            return chain.proceed(originalRequest)
        }
        
        val sessionManager = SessionManager(context)
        val token = sessionManager.getToken()
        
        return if (token.isNullOrEmpty()) {
            Log.w(TAG, "No hay token de autenticación disponible")
            chain.proceed(originalRequest)
        } else {
            val authorizedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            
            Log.d(TAG, "Añadido token de autenticación a la petición: ${originalRequest.url}")
            chain.proceed(authorizedRequest)
        }
    }
    
    /**
     * Determina si la petición es de autenticación (login).
     *
     * @param request Petición a evaluar.
     * @return true si es una petición de autenticación, false en caso contrario.
     */
    private fun isAuthenticationRequest(request: Request): Boolean {
        val url = request.url.toString().toLowerCase()
        return url.contains("/login") || url.contains("/auth/login")
    }
}