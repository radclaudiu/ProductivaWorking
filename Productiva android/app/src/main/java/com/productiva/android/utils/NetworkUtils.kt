package com.productiva.android.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Utilidades para verificar y monitorear la conectividad a Internet.
 */
object NetworkUtils {
    
    // LiveData que indica el estado actual de conectividad
    private val networkStatus = MutableLiveData<Boolean>()
    
    /**
     * Verifica si hay conexión a Internet.
     *
     * @param context Contexto de la aplicación.
     * @return true si hay conexión a Internet, false en caso contrario.
     */
    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            networkInfo != null && networkInfo.isConnected
        }
    }
    
    /**
     * Inicia el monitoreo de cambios en la conectividad.
     * Actualiza el LiveData cuando cambia el estado de la red.
     *
     * @param context Contexto de la aplicación.
     * @return LiveData que emite el estado de conectividad actual.
     */
    fun startNetworkCallback(context: Context): LiveData<Boolean> {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        // Inicializar con el estado actual
        networkStatus.value = isOnline(context)
        
        // Crear callback de red
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                networkStatus.postValue(true)
            }
            
            override fun onLost(network: Network) {
                networkStatus.postValue(false)
            }
        }
        
        // Registrar callback
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        
        return networkStatus
    }
}