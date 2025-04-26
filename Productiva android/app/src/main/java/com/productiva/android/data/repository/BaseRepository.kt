package com.productiva.android.data.repository

import android.content.Context
import android.util.Log
import com.productiva.android.network.ApiService
import com.productiva.android.network.NetworkResult
import com.productiva.android.network.RetrofitClient
import com.productiva.android.network.safeApiCall
import com.productiva.android.repository.ResourceState
import com.productiva.android.sync.SyncResult
import com.productiva.android.utils.ConnectivityMonitor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Clase base para todos los repositorios, que implementa funcionalidad común
 * como manejo de errores, verificación de conexión, etc.
 *
 * @param context Contexto de la aplicación.
 * @param dispatcher Dispatcher para operaciones de corrutina (por defecto IO).
 */
abstract class BaseRepository(
    protected val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    companion object {
        private const val TAG = "BaseRepository"
    }
    
    // Cliente API para comunicación con el servidor
    protected val apiService: ApiService by lazy {
        RetrofitClient.getApiService(context)
    }
    
    // Monitor de conectividad para verificar el estado de la red
    protected val connectivityMonitor: ConnectivityMonitor by lazy {
        ConnectivityMonitor.getInstance(context)
    }
    
    /**
     * Función utilitaria para realizar operaciones que necesitan conexión a Internet.
     * Verifica la disponibilidad de la red antes de ejecutar la operación.
     *
     * @param operation Operación a ejecutar si hay conexión a Internet.
     * @param errorMessage Mensaje de error a devolver si no hay conexión.
     * @return Resultado de la operación o error.
     */
    protected suspend fun <T> withNetworkCheck(
        operation: suspend () -> NetworkResult<T>,
        errorMessage: String = "No hay conexión a Internet"
    ): NetworkResult<T> {
        return if (connectivityMonitor.isNetworkAvailable()) {
            operation()
        } else {
            NetworkResult.Error(errorMessage)
        }
    }
    
    /**
     * Función utilitaria para obtener datos de manera segura, ya sea de la base de datos local
     * o del servidor remoto, según disponibilidad de conexión y política de refresco.
     *
     * @param shouldFetch Función que determina si se debe obtener datos del servidor.
     * @param remoteFetch Función para obtener datos del servidor.
     * @param localFetch Función para obtener datos de la base de datos local.
     * @param saveFetchResult Función para guardar los datos obtenidos del servidor en la base de datos local.
     * @param onFetchSuccess Función opcional a ejecutar cuando se obtienen datos del servidor.
     * @param onFetchError Función opcional a ejecutar cuando falla la obtención de datos del servidor.
     * @return Flow que emite los estados del recurso durante el proceso.
     */
    protected fun <T> networkBoundResource(
        shouldFetch: (T?) -> Boolean,
        remoteFetch: suspend () -> NetworkResult<T>,
        localFetch: suspend () -> T?,
        saveFetchResult: suspend (T) -> Unit,
        onFetchSuccess: (suspend () -> Unit)? = null,
        onFetchError: (suspend (String) -> Unit)? = null
    ): Flow<ResourceState<T>> = flow {
        // Emitir estado de carga inicial
        emit(ResourceState.Loading())
        
        // Obtener datos de la base de datos local
        val localData = localFetch()
        
        // Emitir datos locales mientras se obtienen datos del servidor
        if (localData != null) {
            emit(ResourceState.Loading(localData))
        }
        
        // Determinar si se debe obtener datos del servidor
        val shouldFetchFromNetwork = shouldFetch(localData)
        
        if (shouldFetchFromNetwork) {
            if (!connectivityMonitor.isNetworkAvailable()) {
                // No hay conexión, emitir datos locales (si hay) o error
                if (localData != null) {
                    emit(ResourceState.Success(localData, isFromCache = true))
                } else {
                    emit(ResourceState.Error("No hay conexión a Internet", data = localData))
                }
                return@flow
            }
            
            try {
                // Obtener datos del servidor
                when (val networkResult = remoteFetch()) {
                    is NetworkResult.Success -> {
                        // Guardar datos en la base de datos local
                        val remoteData = networkResult.data
                        saveFetchResult(remoteData)
                        
                        // Ejecutar callback de éxito si existe
                        onFetchSuccess?.invoke()
                        
                        // Obtener datos actualizados de la base de datos local
                        val updatedData = localFetch() ?: remoteData
                        
                        // Emitir datos actualizados
                        emit(ResourceState.Success(updatedData, isFromCache = false))
                    }
                    is NetworkResult.Error -> {
                        // Ejecutar callback de error si existe
                        onFetchError?.invoke(networkResult.message)
                        
                        // Emitir error con datos locales (si hay)
                        emit(ResourceState.Error(
                            message = networkResult.message,
                            errorCode = networkResult.errorCode,
                            data = localData
                        ))
                    }
                    NetworkResult.Loading -> {
                        // Este caso no debería ocurrir con safeApiCall
                        Log.w(TAG, "Estado de carga inesperado desde remoteFetch")
                    }
                }
                
            } catch (e: Exception) {
                // Error durante la obtención de datos del servidor
                Log.e(TAG, "Error en networkBoundResource", e)
                
                // Ejecutar callback de error si existe
                onFetchError?.invoke(e.message ?: "Error desconocido")
                
                // Emitir error con datos locales (si hay)
                emit(ResourceState.Error(
                    message = e.message ?: "Error desconocido",
                    data = localData
                ))
            }
            
        } else {
            // No es necesario obtener datos del servidor, emitir datos locales
            if (localData != null) {
                emit(ResourceState.Success(localData, isFromCache = true))
            } else {
                // Si no hay datos locales y no se debe obtener datos del servidor,
                // emitir éxito con null (lista vacía, por ejemplo)
                emit(ResourceState.Success(null as T, isFromCache = true))
            }
        }
        
    }.catch { e ->
        // Capturar cualquier excepción durante el flujo
        Log.e(TAG, "Error en flujo networkBoundResource", e)
        emit(ResourceState.Error(e.message ?: "Error desconocido"))
    }.flowOn(dispatcher)
    
    /**
     * Ejecuta una operación de manera segura y devuelve un SyncResult.
     *
     * @param operation Operación a ejecutar.
     * @return Resultado de la sincronización.
     */
    protected suspend fun <T> executeSyncOperation(
        operation: suspend () -> T
    ): SyncResult = withContext(dispatcher) {
        try {
            val result = operation()
            SyncResult.Success(timestamp = System.currentTimeMillis())
        } catch (e: Exception) {
            Log.e(TAG, "Error en executeSyncOperation", e)
            SyncResult.Error(e.message ?: "Error desconocido", e)
        }
    }
}