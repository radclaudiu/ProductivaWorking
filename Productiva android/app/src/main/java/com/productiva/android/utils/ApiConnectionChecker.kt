package com.productiva.android.utils

import android.content.Context
import android.util.Log
import com.productiva.android.network.ApiService
import com.productiva.android.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/**
 * Utilidad para verificar la conexión con el servidor de la API.
 */
class ApiConnectionChecker(private val context: Context) {
    
    private val TAG = "ApiConnectionChecker"
    
    // Servicio de API
    private val apiService: ApiService by lazy {
        RetrofitClient.getApiService(context)
    }
    
    // Timestamp de la última verificación
    private var lastCheckTimestamp = 0L
    
    // Resultado de la última verificación
    private var lastCheckResult: ConnectionResult? = null
    
    // Tiempo mínimo entre verificaciones (para evitar demasiadas llamadas)
    private val MIN_CHECK_INTERVAL = TimeUnit.MINUTES.toMillis(2)
    
    /**
     * Verifica la conexión con el servidor de API.
     * 
     * @param forceCheck Si es true, fuerza una nueva verificación incluso si hay una reciente.
     * @return Flow que emite un ConnectionResult con el resultado de la verificación.
     */
    fun checkApiConnection(forceCheck: Boolean = false): Flow<ConnectionResult> = flow {
        // Comprobar si podemos usar el resultado de cache
        val now = System.currentTimeMillis()
        if (!forceCheck && lastCheckResult != null && (now - lastCheckTimestamp) < MIN_CHECK_INTERVAL) {
            emit(lastCheckResult!!)
            return@flow
        }
        
        // Primero verificar si hay conexión a Internet
        val connectivityMonitor = ConnectivityMonitor(context)
        if (!connectivityMonitor.isConnected()) {
            val result = ConnectionResult.NoInternet
            lastCheckResult = result
            lastCheckTimestamp = now
            emit(result)
            return@flow
        }
        
        // Verificar conexión al servidor
        emit(ConnectionResult.Checking)
        
        try {
            val result = withContext(Dispatchers.IO) {
                try {
                    // Intentar obtener información de sincronización del servidor
                    val response = apiService.getSyncStatus()
                    processApiResponse(response)
                } catch (e: Exception) {
                    handleNetworkException(e)
                }
            }
            
            lastCheckResult = result
            lastCheckTimestamp = now
            emit(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la verificación de API", e)
            val result = ConnectionResult.Error("Error: ${e.message}")
            lastCheckResult = result
            lastCheckTimestamp = now
            emit(result)
        }
    }
    
    /**
     * Procesa la respuesta de la API y determina el resultado de conexión.
     */
    private fun <T> processApiResponse(response: Response<T>): ConnectionResult {
        return if (response.isSuccessful) {
            ConnectionResult.Connected(response.code())
        } else {
            when (response.code()) {
                401, 403 -> ConnectionResult.AuthError(response.code(), response.message())
                404 -> ConnectionResult.ServerError(response.code(), "Endpoint no encontrado")
                500, 502, 503, 504 -> ConnectionResult.ServerError(
                    response.code(),
                    "Error en el servidor: ${response.message()}"
                )
                else -> ConnectionResult.Error(
                    "Error de API: ${response.code()} - ${response.message()}"
                )
            }
        }
    }
    
    /**
     * Maneja excepciones de red y determina el tipo de error.
     */
    private fun handleNetworkException(exception: Exception): ConnectionResult {
        return when (exception) {
            is SocketTimeoutException -> ConnectionResult.Timeout
            is ConnectException -> ConnectionResult.ServerUnavailable
            is UnknownHostException -> ConnectionResult.ServerUnavailable
            else -> ConnectionResult.Error("Error de red: ${exception.message}")
        }
    }
    
    /**
     * Realiza una verificación completa del estado de la API incluyendo autenticación.
     */
    fun checkApiWithAuth(forceCheck: Boolean = false): Flow<ConnectionResult> = flow {
        // Verificar primero la conexión básica
        checkApiConnection(forceCheck).collect { basicResult ->
            if (basicResult !is ConnectionResult.Connected) {
                emit(basicResult)
                return@collect
            }
            
            // Si está conectado, verificar la autenticación
            val sessionManager = SessionManager(context)
            if (!sessionManager.isLoggedIn()) {
                emit(ConnectionResult.NotAuthenticated)
                return@collect
            }
            
            // Verificar si el token ha expirado
            if (sessionManager.isTokenExpired()) {
                emit(ConnectionResult.TokenExpired)
                return@collect
            }
            
            try {
                // Intentar obtener información del usuario actual
                val result = withContext(Dispatchers.IO) {
                    try {
                        val response = apiService.getCurrentUser()
                        processApiResponse(response)
                    } catch (e: Exception) {
                        handleNetworkException(e)
                    }
                }
                
                emit(result)
            } catch (e: Exception) {
                Log.e(TAG, "Error durante la verificación de autenticación", e)
                emit(ConnectionResult.Error("Error: ${e.message}"))
            }
        }
    }
    
    /**
     * Resultados posibles de la verificación de conexión.
     */
    sealed class ConnectionResult {
        object Checking : ConnectionResult()
        object NoInternet : ConnectionResult()
        object ServerUnavailable : ConnectionResult()
        object Timeout : ConnectionResult()
        object NotAuthenticated : ConnectionResult()
        object TokenExpired : ConnectionResult()
        data class Connected(val statusCode: Int) : ConnectionResult()
        data class AuthError(val statusCode: Int, val message: String) : ConnectionResult()
        data class ServerError(val statusCode: Int, val message: String) : ConnectionResult()
        data class Error(val message: String) : ConnectionResult()
    }
}