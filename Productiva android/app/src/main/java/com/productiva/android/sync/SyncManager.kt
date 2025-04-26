package com.productiva.android.sync

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.productiva.android.data.database.AppDatabase
import com.productiva.android.data.model.SyncPendingOperation
import com.productiva.android.data.model.SyncState
import com.productiva.android.network.NetworkStatusManager
import com.productiva.android.network.RetrofitClient
import com.productiva.android.repository.ResourceState
import com.productiva.android.utils.SessionManager
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Administrador de sincronización para la aplicación.
 * Coordina el proceso de sincronización bidireccional entre la base de datos local
 * y el servidor remoto, gestionando operaciones pendientes y resolución de conflictos.
 */
class SyncManager(
    private val context: Context,
    private val database: AppDatabase,
    private val sessionManager: SessionManager,
    private val networkStatusManager: NetworkStatusManager
) {
    private val TAG = "SyncManager"
    private val syncJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + syncJob)
    private val isSyncing = AtomicBoolean(false)
    
    // API Service para comunicación con el servidor
    private val apiService = RetrofitClient.getApiService(sessionManager)
    
    // Estado de sincronización observable
    private val _syncState = MutableLiveData<ResourceState<SyncState>>(ResourceState.Idle())
    val syncState: LiveData<ResourceState<SyncState>> = _syncState
    
    /**
     * Inicia un proceso de sincronización completo:
     * 1. Envía cambios locales pendientes al servidor
     * 2. Recupera datos actualizados del servidor
     * 3. Resuelve conflictos si es necesario
     */
    fun syncAll(force: Boolean = false) {
        if (isSyncing.getAndSet(true) && !force) {
            Log.d(TAG, "Sincronización ya en progreso, ignorando solicitud")
            return
        }
        
        // Verificar conectividad antes de intentar sincronizar
        if (!networkStatusManager.isNetworkAvailable()) {
            _syncState.postValue(ResourceState.Error("No hay conexión a Internet"))
            isSyncing.set(false)
            return
        }
        
        _syncState.postValue(ResourceState.Loading())
        
        scope.launch {
            try {
                Log.d(TAG, "Iniciando sincronización completa")
                
                // Paso 1: Enviar cambios locales pendientes
                val pendingResult = processPendingOperations()
                
                // Paso 2: Recuperar datos actualizados
                val downloadResult = downloadServerData()
                
                // Verificar resultados y actualizar estado
                if (pendingResult && downloadResult) {
                    _syncState.postValue(ResourceState.Success(SyncState(true, System.currentTimeMillis())))
                    Log.d(TAG, "Sincronización completada con éxito")
                } else {
                    _syncState.postValue(ResourceState.Error("Sincronización parcial - Algunos datos no se sincronizaron"))
                    Log.w(TAG, "Sincronización parcial - Algunos datos no se sincronizaron")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error durante la sincronización: ${e.message}", e)
                _syncState.postValue(ResourceState.Error("Error de sincronización: ${e.message}"))
            } finally {
                isSyncing.set(false)
            }
        }
    }
    
    /**
     * Procesa todas las operaciones pendientes almacenadas localmente.
     * Envía cambios al servidor y maneja posibles conflictos.
     */
    private suspend fun processPendingOperations(): Boolean {
        try {
            val pendingOperations = database.syncPendingOperationDao().getAllPendingOperations()
            
            if (pendingOperations.isEmpty()) {
                Log.d(TAG, "No hay operaciones pendientes para sincronizar")
                return true
            }
            
            Log.d(TAG, "Procesando ${pendingOperations.size} operaciones pendientes")
            
            // Agrupar operaciones por tipo para optimizar el proceso
            val groupedOperations = pendingOperations.groupBy { it.entityType }
            
            for ((entityType, operations) in groupedOperations) {
                // Procesar operaciones por tipo de entidad
                when (entityType) {
                    SyncPendingOperation.ENTITY_CHECKPOINT_RECORD -> processCheckpointRecords(operations)
                    SyncPendingOperation.ENTITY_TASK_ASSIGNMENT -> processTaskAssignments(operations)
                    SyncPendingOperation.ENTITY_CASH_REGISTER -> processCashRegisters(operations)
                    // Otros tipos de entidades
                }
            }
            
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error procesando operaciones pendientes: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Descarga datos actualizados del servidor y actualiza la base de datos local.
     */
    private suspend fun downloadServerData(): Boolean {
        try {
            // Obtener último timestamp de sincronización
            val lastSyncTimestamp = sessionManager.getLastSyncTimestamp()
            
            // Descargar datos por tipo
            val checkpointsResult = downloadCheckpoints(lastSyncTimestamp)
            val employeesResult = downloadEmployees(lastSyncTimestamp)
            val tasksResult = downloadTasks(lastSyncTimestamp)
            val productsResult = downloadProducts(lastSyncTimestamp)
            val labelTemplatesResult = downloadLabelTemplates(lastSyncTimestamp)
            
            // Actualizar timestamp de sincronización si todo fue exitoso
            if (checkpointsResult && employeesResult && tasksResult && 
                productsResult && labelTemplatesResult) {
                sessionManager.saveLastSyncTimestamp(System.currentTimeMillis())
                return true
            }
            
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error descargando datos del servidor: ${e.message}", e)
            return false
        }
    }
    
    // Métodos para procesar tipos específicos de operaciones pendientes
    private suspend fun processCheckpointRecords(operations: List<SyncPendingOperation>) {
        // Implementación para sincronizar registros de fichajes
    }
    
    private suspend fun processTaskAssignments(operations: List<SyncPendingOperation>) {
        // Implementación para sincronizar asignaciones de tareas
    }
    
    private suspend fun processCashRegisters(operations: List<SyncPendingOperation>) {
        // Implementación para sincronizar arqueos de caja
    }
    
    // Métodos para descargar datos específicos del servidor
    private suspend fun downloadCheckpoints(lastSyncTimestamp: Long): Boolean {
        // Implementación para descargar puntos de fichaje
        return true
    }
    
    private suspend fun downloadEmployees(lastSyncTimestamp: Long): Boolean {
        // Implementación para descargar empleados
        return true
    }
    
    private suspend fun downloadTasks(lastSyncTimestamp: Long): Boolean {
        // Implementación para descargar tareas
        return true
    }
    
    private suspend fun downloadProducts(lastSyncTimestamp: Long): Boolean {
        // Implementación para descargar productos
        return true
    }
    
    private suspend fun downloadLabelTemplates(lastSyncTimestamp: Long): Boolean {
        // Implementación para descargar plantillas de etiquetas
        return true
    }
    
    /**
     * Cancela el trabajo de sincronización en curso
     */
    fun cancelSync() {
        if (isSyncing.get()) {
            syncJob.cancel()
            isSyncing.set(false)
            _syncState.postValue(ResourceState.Idle())
        }
    }
    
    /**
     * Limpia recursos cuando ya no se necesita el SyncManager
     */
    fun onDestroy() {
        syncJob.cancel()
    }
}