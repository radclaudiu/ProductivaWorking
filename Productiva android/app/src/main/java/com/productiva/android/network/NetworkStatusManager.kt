package com.productiva.android.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Administrador del estado de conectividad de red.
 * Esta clase proporciona información sobre el estado actual de la conexión
 * a Internet y notifica a los observadores sobre cambios en la conectividad.
 *
 * @property context Contexto de la aplicación
 */
class NetworkStatusManager(private val context: Context) {

    private val _connectionStatus = MutableStateFlow(ConnectionStatus())
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val connectivityManager = 
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    init {
        // Inicializamos el estado actual de la conexión
        updateConnectionStatus()
        
        // Registramos un callback para monitorear cambios en la conectividad
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
            
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // La conexión a Internet está disponible
            _connectionStatus.value = ConnectionStatus(isConnected = true)
        }
        
        override fun onLost(network: Network) {
            // La conexión a Internet se ha perdido
            _connectionStatus.value = ConnectionStatus(isConnected = false)
        }
        
        override fun onCapabilitiesChanged(
            network: Network, 
            networkCapabilities: NetworkCapabilities
        ) {
            // Las capacidades de la red han cambiado
            val isConnected = networkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )
            
            val isMetered = !networkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_NOT_METERED
            )
            
            _connectionStatus.value = ConnectionStatus(
                isConnected = isConnected,
                isMetered = isMetered
            )
        }
    }
    
    /**
     * Actualiza el estado de la conexión basado en las capacidades actuales.
     * Este método se llama al inicializar el manager y puede ser llamado
     * manualmente si se necesita forzar una actualización del estado.
     */
    fun updateConnectionStatus() {
        val isConnected = isNetworkAvailable()
        _connectionStatus.value = ConnectionStatus(isConnected = isConnected)
    }
    
    /**
     * Verifica si hay una red disponible con acceso a Internet.
     */
    private fun isNetworkAvailable(): Boolean {
        return when {
            // Para Android 6.0+ (API 23+)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                val network = connectivityManager.activeNetwork
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            }
            
            // Para versiones anteriores
            else -> {
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                @Suppress("DEPRECATION")
                networkInfo?.isConnected == true
            }
        }
    }
    
    /**
     * Libera recursos al destruir la instancia.
     * Debe llamarse al finalizar el ciclo de vida de la aplicación.
     */
    fun cleanup() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // Ignoramos errores al desregistrar el callback (podría no estar registrado)
        }
    }
    
    /**
     * Clase que representa el estado actual de la conexión.
     */
    data class ConnectionStatus(
        val isConnected: Boolean = false,
        val isMetered: Boolean = true  // Por defecto asumimos red con medición
    )
}