package com.productiva.android.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Monitor de conectividad que permite observar cambios en el estado de la conexión a Internet.
 */
class ConnectivityMonitor(private val context: Context) {
    
    private val TAG = "ConnectivityMonitor"
    
    // Estado actual de la conexión
    private val _connectionState = MutableLiveData<ConnectionState>()
    val connectionState: LiveData<ConnectionState> = _connectionState
    
    // Callback para cambios en la red
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Red disponible")
            checkConnectionType()
        }
        
        override fun onLost(network: Network) {
            Log.d(TAG, "Red perdida")
            _connectionState.postValue(ConnectionState.Disconnected)
        }
        
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            Log.d(TAG, "Capacidades de red cambiadas")
            checkConnectionType(networkCapabilities)
        }
    }
    
    init {
        // Iniciar con verificación inmediata del estado
        val initialState = getCurrentConnectionState()
        _connectionState.value = initialState
        
        // Registrar para escuchar cambios en la red
        registerNetworkCallback()
    }
    
    /**
     * Registra el callback para recibir actualizaciones de red.
     */
    private fun registerNetworkCallback() {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar networkCallback", e)
        }
    }
    
    /**
     * Desregistra el callback de red.
     */
    fun unregisterNetworkCallback() {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error al desregistrar networkCallback", e)
        }
    }
    
    /**
     * Verifica el tipo de conexión actual basado en las capacidades de red.
     */
    private fun checkConnectionType(networkCapabilities: NetworkCapabilities? = null) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = networkCapabilities ?: run {
            val activeNetwork = connectivityManager.activeNetwork ?: return
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return
        }
        
        val state = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                ConnectionState.Connected(ConnectionType.WIFI)
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                ConnectionState.Connected(ConnectionType.CELLULAR)
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                ConnectionState.Connected(ConnectionType.ETHERNET)
            }
            else -> ConnectionState.Connected(ConnectionType.OTHER)
        }
        
        _connectionState.postValue(state)
    }
    
    /**
     * Obtiene el estado actual de conexión.
     */
    fun getCurrentConnectionState(): ConnectionState {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        if (capabilities == null) {
            return ConnectionState.Disconnected
        }
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                ConnectionState.Connected(ConnectionType.WIFI)
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                ConnectionState.Connected(ConnectionType.CELLULAR)
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                ConnectionState.Connected(ConnectionType.ETHERNET)
            }
            else -> ConnectionState.Connected(ConnectionType.OTHER)
        }
    }
    
    /**
     * Verifica si hay conexión a Internet.
     */
    fun isConnected(): Boolean {
        return getCurrentConnectionState() is ConnectionState.Connected
    }
    
    /**
     * Fuerza una actualización del estado de conexión.
     */
    fun refresh() {
        val state = getCurrentConnectionState()
        _connectionState.postValue(state)
    }
    
    /**
     * Tipos de conexión posibles.
     */
    enum class ConnectionType {
        WIFI,
        CELLULAR,
        ETHERNET,
        OTHER
    }
    
    /**
     * Estados posibles de conexión.
     */
    sealed class ConnectionState {
        data class Connected(val type: ConnectionType) : ConnectionState()
        object Disconnected : ConnectionState()
    }
}