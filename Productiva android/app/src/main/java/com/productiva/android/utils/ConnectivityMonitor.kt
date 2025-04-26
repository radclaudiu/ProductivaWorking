package com.productiva.android.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Clase que monitorea el estado de la conexión a Internet.
 * Proporciona un flujo observable del estado de la red para que los componentes
 * puedan reaccionar a cambios en la conectividad.
 */
class ConnectivityMonitor private constructor(context: Context) {
    
    // Flujo observable del estado de la red
    private val _networkAvailable = MutableStateFlow(false)
    val networkAvailable: StateFlow<Boolean> = _networkAvailable
    
    // ConnectivityManager para monitorear la red
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    // Callback para recibir eventos de cambio en la red
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _networkAvailable.value = true
        }
        
        override fun onLost(network: Network) {
            // Verificar si todavía hay otra red disponible antes de actualizar a "sin conexión"
            val activeNetwork = connectivityManager.activeNetwork
            if (activeNetwork == null) {
                _networkAvailable.value = false
            }
        }
        
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            // Actualizar el estado si cambian las capacidades de la red
            val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            
            _networkAvailable.value = hasInternet
        }
    }
    
    init {
        // Registrar callback para recibir actualizaciones de red
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        
        // Verificar el estado inicial de la red
        _networkAvailable.value = isNetworkAvailable()
    }
    
    /**
     * Verifica si hay una conexión a Internet disponible.
     *
     * @return true si hay conexión a Internet, false en caso contrario.
     */
    fun isNetworkAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Método para limpiar recursos cuando ya no se necesita el monitor.
     */
    fun unregister() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // Ignorar errores si ya estaba desregistrado
        }
    }
    
    companion object {
        @Volatile
        private var instance: ConnectivityMonitor? = null
        
        /**
         * Obtiene la instancia única del monitor de conectividad.
         *
         * @param context Contexto de la aplicación.
         * @return Instancia del monitor de conectividad.
         */
        fun getInstance(context: Context): ConnectivityMonitor {
            return instance ?: synchronized(this) {
                instance ?: ConnectivityMonitor(context.applicationContext).also { instance = it }
            }
        }
    }
}