package com.productiva.android.network.interceptor

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Interceptor para verificar la conectividad a Internet.
 * Lanza una excepción si no hay conexión a Internet.
 */
class ConnectionInterceptor(private val context: Context) : Interceptor {
    
    companion object {
        private const val TAG = "ConnectionInterceptor"
    }
    
    /**
     * Intercepta la petición y verifica la conectividad a Internet.
     *
     * @param chain Cadena de interceptores.
     * @return Respuesta de la petición.
     * @throws IOException Si no hay conexión a Internet.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isOnline()) {
            Log.e(TAG, "No hay conexión a Internet")
            throw NoConnectivityException()
        }
        
        return chain.proceed(chain.request())
    }
    
    /**
     * Verifica si hay conexión a Internet.
     *
     * @return true si hay conexión a Internet, false en caso contrario.
     */
    private fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                   capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                   capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }
    
    /**
     * Excepción lanzada cuando no hay conexión a Internet.
     */
    class NoConnectivityException : IOException("No hay conexión a Internet")
}