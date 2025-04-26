package com.productiva.android.utils

import android.content.Context
import android.util.Log
import com.productiva.android.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Verificador de conexión a la API que comprueba periódicamente si el servidor está disponible.
 * Proporciona información sobre si el servidor está accesible y notifica a los observadores
 * cuando cambia el estado de la conexión.
 */
class ApiConnectionChecker(private val context: Context) {
    
    private val TAG = "ApiConnectionChecker"
    
    // Monitor de conectividad
    private val connectivityMonitor = ConnectivityMonitor(context)
    
    // Estado de conexión con la API
    private val _apiConnectionState = MutableStateFlow(getInitialConnectionState())
    val apiConnectionState: StateFlow<ConnectionState> = _apiConnectionState
    
    // Indicador de comprobación en curso
    private val isChecking = AtomicBoolean(false)
    
    // Trabajo de comprobación periódica
    private var checkJob: Job? = null
    
    // Ámbito de corrutina para comprobaciones
    private val checkerScope = CoroutineScope(Dispatchers.IO)
    
    // URL base de la API
    private val apiBaseUrl = RetrofitClient.getApiBaseUrl(context)
    
    init {
        // Observar cambios en la conectividad de red
        connectivityMonitor.observe { isConnected ->
            if (isConnected) {
                // Cuando hay conexión, verificar la API
                checkApiConnection()
            } else {
                // Sin conexión, actualizar estado
                updateConnectionState(false)
            }
        }
    }
    
    /**
     * Obtiene el estado inicial de la conexión.
     */
    private fun getInitialConnectionState(): ConnectionState {
        val isConnected = connectivityMonitor.isConnected()
        
        return ConnectionState(
            isConnected = isConnected,
            connectionType = ConnectionType.UNKNOWN,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Inicia la comprobación periódica de la conexión a la API.
     * 
     * @param intervalMinutes Intervalo entre comprobaciones en minutos.
     */
    fun startPeriodicChecks(intervalMinutes: Long = 15) {
        // Cancelar trabajo anterior si existe
        stopPeriodicChecks()
        
        // Iniciar nuevo trabajo
        checkJob = checkerScope.launch {
            while (true) {
                checkApiConnection()
                withContext(Dispatchers.IO) {
                    // Esperar para la siguiente comprobación
                    Thread.sleep(TimeUnit.MINUTES.toMillis(intervalMinutes))
                }
            }
        }
        
        Log.d(TAG, "Comprobación periódica iniciada con intervalo de $intervalMinutes minutos")
    }
    
    /**
     * Detiene la comprobación periódica de la conexión a la API.
     */
    fun stopPeriodicChecks() {
        checkJob?.cancel()
        checkJob = null
        Log.d(TAG, "Comprobación periódica detenida")
    }
    
    /**
     * Comprueba la conexión a la API.
     * 
     * @return true si la API está accesible, false en caso contrario.
     */
    fun checkApiConnection() {
        // Evitar comprobaciones simultáneas
        if (isChecking.getAndSet(true)) {
            return
        }
        
        // Verificar primero si hay conexión a Internet
        if (!connectivityMonitor.isConnected()) {
            updateConnectionState(false)
            isChecking.set(false)
            return
        }
        
        // Iniciar comprobación en segundo plano
        checkerScope.launch {
            try {
                val isApiConnected = pingApi()
                updateConnectionState(isApiConnected)
            } catch (e: Exception) {
                Log.e(TAG, "Error al comprobar conexión con API", e)
                updateConnectionState(false)
            } finally {
                isChecking.set(false)
            }
        }
    }
    
    /**
     * Realiza un ping a la API para verificar su disponibilidad.
     * 
     * @return true si la API está accesible, false en caso contrario.
     */
    private suspend fun pingApi(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$apiBaseUrl/api/ping")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.requestMethod = "GET"
                
                val responseCode = connection.responseCode
                Log.d(TAG, "Código de respuesta de la API: $responseCode")
                
                return@withContext responseCode == HttpURLConnection.HTTP_OK
            } catch (e: Exception) {
                Log.e(TAG, "Error al hacer ping a la API", e)
                return@withContext false
            }
        }
    }
    
    /**
     * Actualiza el estado de conexión con la API.
     * 
     * @param isConnected true si la API está accesible, false en caso contrario.
     */
    private fun updateConnectionState(isConnected: Boolean) {
        val currentState = _apiConnectionState.value
        
        // Solo actualizar si cambia el estado
        if (currentState.isConnected != isConnected) {
            val newState = ConnectionState(
                isConnected = isConnected,
                connectionType = connectivityMonitor.getCurrentConnectionState().connectionType,
                timestamp = System.currentTimeMillis()
            )
            
            _apiConnectionState.value = newState
            Log.d(TAG, "Estado de conexión con API actualizado: $isConnected")
        }
    }
    
    /**
     * Obtiene el estado actual de la conexión.
     */
    fun getCurrentConnectionState(): ConnectionState {
        return _apiConnectionState.value
    }
    
    /**
     * Libera recursos cuando el componente que utiliza este verificador se destruye.
     */
    fun release() {
        stopPeriodicChecks()
        connectivityMonitor.release()
        Log.d(TAG, "Verificador de conexión liberado")
    }
}