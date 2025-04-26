package com.productiva.android.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Interceptor que verifica la conectividad antes de intentar realizar peticiones de red.
 * Si no hay conexión a Internet, interrumpe la petición con una excepción personalizada.
 */
class NetworkConnectionInterceptor : Interceptor {
    private val TAG = "NetworkInterceptor"
    
    override fun intercept(chain: Interceptor.Chain): Response {
        // Obtener instancia singleton del administrador de conectividad
        val networkStatusManager = NetworkStatusManager.getInstance()
        
        if (!networkStatusManager.isNetworkAvailable()) {
            Log.e(TAG, "Sin conexión a Internet. Cancelando petición de red.")
            throw NoConnectivityException()
        }
        
        // Continuar con la cadena de interceptores si hay conexión
        return try {
            chain.proceed(chain.request())
        } catch (e: IOException) {
            // Marcar la red como problemática si hay error de IO
            networkStatusManager.setNetworkProblem(true)
            Log.e(TAG, "Error de red en la petición: ${e.message}")
            throw e
        }
    }
    
    /**
     * Excepción personalizada para los casos sin conectividad.
     */
    class NoConnectivityException : IOException("No hay conexión a Internet. Verifica tu conexión e intenta nuevamente.")
}