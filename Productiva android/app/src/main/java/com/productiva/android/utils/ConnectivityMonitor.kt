package com.productiva.android.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Monitor de conectividad que supervisa el estado de la conexión de red.
 * Proporciona información sobre si el dispositivo está conectado a Internet
 * y notifica a los observadores cuando cambia el estado de la conexión.
 */
class ConnectivityMonitor(private val context: Context) {
    
    private val TAG = "ConnectivityMonitor"
    
    // Gestor de conectividad del sistema
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    // Estado de conectividad actual como StateFlow
    private val _connectionState = MutableStateFlow(getInitialConnectionState())
    val connectionState: StateFlow<ConnectionState> = _connectionState
    
    // Observadores del estado de conexión
    private val observers = mutableListOf<(Boolean) -> Unit>()
    
    // Callback para cambios de red
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Red disponible")
            updateConnectionState(true)
        }
        
        override fun onLost(network: Network) {
            Log.d(TAG, "Red perdida")
            updateConnectionState(false)
        }
        
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            val hasValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            Log.d(TAG, "Capacidades de red cambiadas - Internet: $hasInternet, Validada: $hasValidated")
            
            if (hasInternet && hasValidated) {
                updateConnectionState(true)
            }
        }
    }
    
    init {
        registerNetworkCallback()
    }
    
    /**
     * Registra el callback de red para monitorizar cambios en la conectividad.
     */
    private fun registerNetworkCallback() {
        try {
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
            Log.d(TAG, "Monitor de conectividad registrado")
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar monitor de conectividad", e)
        }
    }
    
    /**
     * Obtiene el estado inicial de la conexión.
     */
    private fun getInitialConnectionState(): ConnectionState {
        val isConnected = isConnected()
        val connectionType = getConnectionType()
        
        return ConnectionState(
            isConnected = isConnected,
            connectionType = connectionType,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Actualiza el estado de conexión y notifica a los observadores.
     */
    private fun updateConnectionState(isConnected: Boolean) {
        val connectionType = getConnectionType()
        
        val newState = ConnectionState(
            isConnected = isConnected,
            connectionType = connectionType,
            timestamp = System.currentTimeMillis()
        )
        
        _connectionState.value = newState
        
        // Notificar a los observadores
        observers.forEach { it(isConnected) }
    }
    
    /**
     * Verifica si el dispositivo está conectado a Internet.
     */
    fun isConnected(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return false
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
                
                return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            } else {
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                @Suppress("DEPRECATION")
                networkInfo?.isConnected == true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar conectividad", e)
            false
        }
    }
    
    /**
     * Obtiene el tipo de conexión actual.
     */
    private fun getConnectionType(): ConnectionType {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return ConnectionType.NONE
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return ConnectionType.NONE
                
                return when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> ConnectionType.VPN
                    else -> ConnectionType.UNKNOWN
                }
            } else {
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                
                @Suppress("DEPRECATION")
                return when (networkInfo?.type) {
                    ConnectivityManager.TYPE_WIFI -> ConnectionType.WIFI
                    ConnectivityManager.TYPE_MOBILE -> ConnectionType.CELLULAR
                    ConnectivityManager.TYPE_ETHERNET -> ConnectionType.ETHERNET
                    ConnectivityManager.TYPE_VPN -> ConnectionType.VPN
                    else -> ConnectionType.UNKNOWN
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener tipo de conexión", e)
            return ConnectionType.UNKNOWN
        }
    }
    
    /**
     * Registra un observador para los cambios de conectividad.
     */
    fun observe(observer: (Boolean) -> Unit) {
        observers.add(observer)
        // Entregar el estado actual inmediatamente
        observer(isConnected())
    }
    
    /**
     * Elimina un observador.
     */
    fun removeObserver(observer: (Boolean) -> Unit) {
        observers.remove(observer)
    }
    
    /**
     * Obtiene el estado actual de la conexión.
     */
    fun getCurrentConnectionState(): ConnectionState {
        return _connectionState.value
    }
    
    /**
     * Debe llamarse cuando el componente que utiliza este monitor se destruye.
     */
    fun release() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            observers.clear()
            Log.d(TAG, "Monitor de conectividad liberado")
        } catch (e: Exception) {
            Log.e(TAG, "Error al liberar monitor de conectividad", e)
        }
    }
}

/**
 * Estado de la conexión.
 */
data class ConnectionState(
    val isConnected: Boolean,
    val connectionType: ConnectionType,
    val timestamp: Long
)

/**
 * Tipos de conexión.
 */
enum class ConnectionType {
    NONE,
    WIFI,
    CELLULAR,
    ETHERNET,
    VPN,
    UNKNOWN
}