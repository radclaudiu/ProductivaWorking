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
import kotlinx.coroutines.flow.asStateFlow

/**
 * Constantes utilizadas en la aplicación.
 */
// Preferencias
const val PREFS_NAME = "ProductivaPrefs"
const val PREF_LAST_SYNC_TIME = "lastSyncTime"

// Canales de notificación
const val NOTIFICATION_CHANNEL_ID = "productiva_general"
const val SYNC_NOTIFICATION_CHANNEL_ID = "productiva_sync"
const val TASKS_NOTIFICATION_CHANNEL_ID = "productiva_tasks"

// Sync
const val SYNC_NOTIFICATION_ID = 1001
const val SYNC_INTERVAL = 15 * 60 * 1000L // 15 minutos

/**
 * Monitor de conectividad que proporciona información sobre el estado de la conexión a Internet.
 * Implementa el patrón Singleton para garantizar una única instancia en la aplicación.
 */
class ConnectivityMonitor private constructor(private val context: Context) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _isNetworkAvailableFlow = MutableStateFlow(checkNetworkAvailability())
    val isNetworkAvailableFlow: StateFlow<Boolean> = _isNetworkAvailableFlow.asStateFlow()
    
    companion object {
        private const val TAG = "ConnectivityMonitor"
        
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
    
    init {
        registerNetworkCallback()
    }
    
    /**
     * Verifica si hay conexión a Internet disponible.
     *
     * @return true si hay conexión a Internet, false en caso contrario.
     */
    fun isNetworkAvailable(): Boolean {
        return checkNetworkAvailability()
    }
    
    /**
     * Registra un callback para monitorear cambios en la conectividad.
     */
    private fun registerNetworkCallback() {
        try {
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            
            connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    Log.d(TAG, "Network available")
                    _isNetworkAvailableFlow.value = true
                }
                
                override fun onLost(network: Network) {
                    super.onLost(network)
                    Log.d(TAG, "Network lost")
                    _isNetworkAvailableFlow.value = false
                }
                
                override fun onUnavailable() {
                    super.onUnavailable()
                    Log.d(TAG, "Network unavailable")
                    _isNetworkAvailableFlow.value = false
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error registering network callback", e)
        }
    }
    
    /**
     * Verifica la disponibilidad de la red.
     *
     * @return true si hay conexión a Internet, false en caso contrario.
     */
    private fun checkNetworkAvailability(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                
                networkCapabilities != null && (
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
            } else {
                // Método antiguo para versiones anteriores a Android M
                @Suppress("DEPRECATION")
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                @Suppress("DEPRECATION")
                activeNetworkInfo != null && activeNetworkInfo.isConnected
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network availability", e)
            false
        }
    }
}