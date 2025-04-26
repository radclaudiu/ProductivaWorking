package com.productiva.android.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Monitor de conectividad a Internet.
 * Proporciona información sobre el estado de la conexión a Internet.
 */
class ConnectivityMonitor private constructor(context: Context) {
    private val TAG = "ConnectivityMonitor"
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _networkStatus = MutableStateFlow(false)
    
    /**
     * Estado actual de la conexión a Internet como un Flow.
     */
    val networkStatus: StateFlow<Boolean> = _networkStatus.asStateFlow()
    
    init {
        // Verificar el estado inicial de la conexión
        _networkStatus.value = isConnected()
        
        // Registrar callback para cambios en la conectividad
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network available")
                _networkStatus.value = true
            }
            
            override fun onLost(network: Network) {
                Log.d(TAG, "Network lost")
                _networkStatus.value = isConnected() // Verificar si hay otras redes disponibles
            }
            
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val hasValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                
                Log.d(TAG, "Network capabilities changed. Has Internet: $hasInternet, Validated: $hasValidated")
                
                if (hasInternet && hasValidated) {
                    _networkStatus.value = true
                }
            }
        }
        
        // Registrar para todos los tipos de redes
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
    
    /**
     * Comprueba si hay conexión a Internet en este momento.
     */
    fun isNetworkAvailable(): Boolean {
        return _networkStatus.value
    }
    
    /**
     * Verifica si hay alguna conexión activa.
     */
    private fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Devuelve el estado actual de la conexión.
     */
    fun getCurrentConnectionState(): ConnectionState {
        return if (isNetworkAvailable()) {
            ConnectionState.Connected
        } else {
            ConnectionState.Disconnected
        }
    }
    
    companion object {
        @Volatile
        private var instance: ConnectivityMonitor? = null
        
        /**
         * Obtiene la instancia única del monitor de conectividad.
         */
        fun getInstance(context: Context): ConnectivityMonitor {
            return instance ?: synchronized(this) {
                instance ?: ConnectivityMonitor(context.applicationContext).also { instance = it }
            }
        }
    }
}

/**
 * Estados posibles de la conexión a Internet.
 */
sealed class ConnectionState {
    object Connected : ConnectionState()
    object Disconnected : ConnectionState()
    object Checking : ConnectionState()
}