package com.productiva.android.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Administrador de estado de la red.
 * Monitorea la conectividad del dispositivo y notifica los cambios.
 */
class NetworkStatusManager(private val context: Context) {
    private val TAG = "NetworkStatusManager"
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    // Estado observable para la interfaz de usuario
    private val _networkAvailable = MutableLiveData<Boolean>()
    val networkAvailable: LiveData<Boolean> = _networkAvailable
    
    // Flow para la capa de datos
    private val _networkState = MutableStateFlow(checkNetworkAvailability())
    val networkState: StateFlow<Boolean> = _networkState
    
    // Estado para problemas de conexión (conexión inestable)
    private var hasNetworkProblem = false
    
    init {
        // Inicializar estado
        val initialState = checkNetworkAvailability()
        _networkAvailable.value = initialState
        _networkState.value = initialState
        
        // Registrar callback para monitorear cambios de conectividad
        setupNetworkCallback()
        
        Log.d(TAG, "NetworkStatusManager inicializado, estado inicial: $initialState")
    }
    
    private fun setupNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
            
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.d(TAG, "Red disponible")
                updateNetworkStatus(true)
            }
            
            override fun onLost(network: Network) {
                super.onLost(network)
                Log.d(TAG, "Red perdida")
                updateNetworkStatus(false)
            }
            
            override fun onUnavailable() {
                super.onUnavailable()
                Log.d(TAG, "Red no disponible")
                updateNetworkStatus(false)
            }
        }
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
    
    /**
     * Actualiza el estado de la red y notifica los cambios
     */
    private fun updateNetworkStatus(isAvailable: Boolean) {
        if (isAvailable) {
            // Al recuperar la conexión, resetear el flag de problema
            hasNetworkProblem = false
        }
        
        _networkAvailable.postValue(isAvailable)
        _networkState.value = isAvailable
    }
    
    /**
     * Verifica si hay conexión a Internet disponible
     */
    fun isNetworkAvailable(): Boolean {
        return if (hasNetworkProblem) {
            // Si se ha detectado un problema de red, forzar a false
            false
        } else {
            // Verificar conectividad real
            checkNetworkAvailability()
        }
    }
    
    /**
     * Marca si hay problemas con la red (para casos donde hay conectividad
     * pero no respuesta del servidor)
     */
    fun setNetworkProblem(hasProblem: Boolean) {
        hasNetworkProblem = hasProblem
        if (hasProblem) {
            _networkAvailable.postValue(false)
            _networkState.value = false
        }
    }
    
    /**
     * Verifica la disponibilidad real de la red
     */
    private fun checkNetworkAvailability(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(
                connectivityManager.activeNetwork
            )
            return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            @Suppress("DEPRECATION")
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return activeNetworkInfo?.isConnected == true
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: NetworkStatusManager? = null
        
        fun getInstance(): NetworkStatusManager {
            return INSTANCE ?: throw IllegalStateException(
                "NetworkStatusManager debe ser inicializado en la clase Application"
            )
        }
        
        fun initialize(context: Context): NetworkStatusManager {
            return INSTANCE ?: synchronized(this) {
                val instance = NetworkStatusManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}