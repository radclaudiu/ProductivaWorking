package com.productiva.android.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.productiva.android.data.repository.CheckpointRepository
import com.productiva.android.data.repository.LabelTemplateRepository
import com.productiva.android.data.repository.ProductRepository
import com.productiva.android.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Administrador central para coordinar todas las operaciones de sincronización.
 * Maneja la sincronización de tareas, productos, plantillas y fichajes con el servidor.
 */
class SyncManager private constructor(private val context: Context) {
    
    // Repositorios
    private val taskRepository by lazy { TaskRepository.getInstance(context) }
    private val productRepository by lazy { ProductRepository.getInstance(context) }
    private val labelTemplateRepository by lazy { LabelTemplateRepository.getInstance(context) }
    private val checkpointRepository by lazy { CheckpointRepository.getInstance(context) }
    
    // Estado de sincronización observable
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState
    
    // Gestor de notificaciones de sincronización
    private val syncNotificationHelper by lazy { SyncNotificationHelper(context) }
    
    // Alcance de corrutinas para operaciones asíncronas
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    companion object {
        private const val TAG = "SyncManager"
        
        // Nombre para el trabajo periódico de sincronización
        private const val PERIODIC_SYNC_WORK_NAME = "productiva_periodic_sync"
        
        // Intervalo de sincronización periódica (en minutos)
        private const val SYNC_INTERVAL_MINUTES = 15L
        
        @Volatile
        private var instance: SyncManager? = null
        
        /**
         * Obtiene la instancia única del administrador de sincronización.
         *
         * @param context Contexto de la aplicación.
         * @return Instancia del administrador de sincronización.
         */
        fun getInstance(context: Context): SyncManager {
            return instance ?: synchronized(this) {
                instance ?: SyncManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Programa una sincronización periódica en segundo plano.
     */
    fun schedulePeriodicalSync() {
        try {
            // Definir restricciones para la sincronización
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Requiere conexión a Internet
                .build()
            
            // Crear solicitud de trabajo periódico
            val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES // Flexibilidad de 5 minutos
            )
                .setConstraints(constraints)
                .build()
            
            // Programar trabajo periódico, reemplazando cualquier trabajo existente
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicSyncRequest
            )
            
            Log.d(TAG, "Sincronización periódica programada cada $SYNC_INTERVAL_MINUTES minutos")
        } catch (e: Exception) {
            Log.e(TAG, "Error al programar sincronización periódica", e)
        }
    }
    
    /**
     * Inicia una sincronización completa con el servidor.
     *
     * @param forceRefresh Si es true, fuerza una sincronización completa ignorando la última fecha de sincronización.
     * @return Resultado de la sincronización.
     */
    suspend fun syncAll(forceRefresh: Boolean = false): SyncResult = withContext(Dispatchers.IO) {
        try {
            // Actualizar estado a sincronizando
            updateSyncState(SyncState.Syncing())
            
            // Mostrar notificación de sincronización iniciada
            syncNotificationHelper.showSyncInProgressNotification()
            
            Log.d(TAG, "Iniciando sincronización completa...")
            
            // Obtener última fecha de sincronización o 0 si es forzada
            val lastSyncTime = if (forceRefresh) 0L else getLastSyncTime()
            
            // Sincronizar cada tipo de datos
            val taskResult = syncTasks(lastSyncTime)
            updateSyncProgress(25)
            
            val productResult = syncProducts(lastSyncTime)
            updateSyncProgress(50)
            
            val templateResult = syncTemplates(lastSyncTime)
            updateSyncProgress(75)
            
            val checkpointResult = syncCheckpoints(lastSyncTime)
            updateSyncProgress(100)
            
            // Determinar resultado global
            val combinedResult = combineResults(
                taskResult, productResult, templateResult, checkpointResult
            )
            
            // Actualizar última fecha de sincronización
            setLastSyncTime(System.currentTimeMillis())
            
            // Mostrar notificación de sincronización completada
            if (combinedResult is SyncResult.Success) {
                syncNotificationHelper.showSyncCompletedNotification(
                    combinedResult.addedCount,
                    combinedResult.updatedCount,
                    combinedResult.deletedCount
                )
            } else if (combinedResult is SyncResult.Error) {
                syncNotificationHelper.showSyncErrorNotification(combinedResult.message)
            }
            
            // Actualizar estado a completado o error
            updateSyncState(
                if (combinedResult is SyncResult.Success) {
                    SyncState.Completed(combinedResult)
                } else {
                    SyncState.Error(
                        (combinedResult as SyncResult.Error).message,
                        (combinedResult as SyncResult.Error).exception
                    )
                }
            )
            
            Log.d(TAG, "Sincronización completa terminada: $combinedResult")
            
            combinedResult
            
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la sincronización completa", e)
            
            // Mostrar notificación de error
            syncNotificationHelper.showSyncErrorNotification(e.message ?: "Error desconocido")
            
            // Actualizar estado a error
            val errorResult = SyncResult.Error(e.message ?: "Error desconocido", e)
            updateSyncState(SyncState.Error(errorResult.message, errorResult.exception))
            
            errorResult
        }
    }
    
    /**
     * Sincroniza tareas con el servidor.
     *
     * @param lastSyncTime Marca de tiempo de la última sincronización.
     * @return Resultado de la sincronización.
     */
    private suspend fun syncTasks(lastSyncTime: Long): SyncResult {
        return try {
            Log.d(TAG, "Sincronizando tareas...")
            taskRepository.syncWithServer(lastSyncTime)
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar tareas", e)
            SyncResult.Error("Error al sincronizar tareas: ${e.message}", e)
        }
    }
    
    /**
     * Sincroniza productos con el servidor.
     *
     * @param lastSyncTime Marca de tiempo de la última sincronización.
     * @return Resultado de la sincronización.
     */
    private suspend fun syncProducts(lastSyncTime: Long): SyncResult {
        return try {
            Log.d(TAG, "Sincronizando productos...")
            productRepository.syncWithServer(lastSyncTime)
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar productos", e)
            SyncResult.Error("Error al sincronizar productos: ${e.message}", e)
        }
    }
    
    /**
     * Sincroniza plantillas de etiquetas con el servidor.
     *
     * @param lastSyncTime Marca de tiempo de la última sincronización.
     * @return Resultado de la sincronización.
     */
    private suspend fun syncTemplates(lastSyncTime: Long): SyncResult {
        return try {
            Log.d(TAG, "Sincronizando plantillas de etiquetas...")
            labelTemplateRepository.syncWithServer(lastSyncTime)
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar plantillas", e)
            SyncResult.Error("Error al sincronizar plantillas: ${e.message}", e)
        }
    }
    
    /**
     * Sincroniza fichajes con el servidor.
     *
     * @param lastSyncTime Marca de tiempo de la última sincronización.
     * @return Resultado de la sincronización.
     */
    private suspend fun syncCheckpoints(lastSyncTime: Long): SyncResult {
        return try {
            Log.d(TAG, "Sincronizando fichajes...")
            checkpointRepository.syncWithServer(lastSyncTime)
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar fichajes", e)
            SyncResult.Error("Error al sincronizar fichajes: ${e.message}", e)
        }
    }
    
    /**
     * Combina varios resultados de sincronización en uno solo.
     *
     * @param results Resultados de sincronización a combinar.
     * @return Resultado combinado.
     */
    private fun combineResults(vararg results: SyncResult): SyncResult {
        // Si hay algún error, devolver el primer error encontrado
        val firstError = results.firstOrNull { it is SyncResult.Error } as? SyncResult.Error
        if (firstError != null) {
            return firstError
        }
        
        // Combinar resultados exitosos
        var totalAdded = 0
        var totalUpdated = 0
        var totalDeleted = 0
        
        results.forEach { result ->
            if (result is SyncResult.Success) {
                totalAdded += result.addedCount
                totalUpdated += result.updatedCount
                totalDeleted += result.deletedCount
            }
        }
        
        return SyncResult.Success(
            addedCount = totalAdded,
            updatedCount = totalUpdated,
            deletedCount = totalDeleted,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Verifica si hay datos pendientes de sincronización.
     *
     * @return True si hay datos pendientes, False en caso contrario.
     */
    suspend fun hasPendingSync(): Boolean = withContext(Dispatchers.IO) {
        try {
            val tasksPending = taskRepository.getPendingSyncCount() > 0
            val productsPending = productRepository.getPendingSyncCount() > 0
            val templatesPending = labelTemplateRepository.getPendingSyncCount() > 0
            val checkpointsPending = checkpointRepository.getPendingSyncCount() > 0
            
            tasksPending || productsPending || templatesPending || checkpointsPending
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar datos pendientes", e)
            false
        }
    }
    
    /**
     * Obtiene la cantidad total de elementos pendientes de sincronización.
     *
     * @return Número total de elementos pendientes.
     */
    suspend fun getPendingSyncCount(): Int = withContext(Dispatchers.IO) {
        try {
            val tasksPending = taskRepository.getPendingSyncCount()
            val productsPending = productRepository.getPendingSyncCount()
            val templatesPending = labelTemplateRepository.getPendingSyncCount()
            val checkpointsPending = checkpointRepository.getPendingSyncCount()
            
            tasksPending + productsPending + templatesPending + checkpointsPending
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener cantidad de elementos pendientes", e)
            0
        }
    }
    
    /**
     * Obtiene la marca de tiempo de la última sincronización.
     *
     * @return Marca de tiempo en milisegundos.
     */
    private fun getLastSyncTime(): Long {
        // Utilizar preferencias compartidas para almacenar la última fecha de sincronización
        val prefs = context.getSharedPreferences(SYNC_PREFERENCES, Context.MODE_PRIVATE)
        return prefs.getLong(LAST_SYNC_KEY, 0)
    }
    
    /**
     * Establece la marca de tiempo de la última sincronización.
     *
     * @param timestamp Marca de tiempo en milisegundos.
     */
    private fun setLastSyncTime(timestamp: Long) {
        val prefs = context.getSharedPreferences(SYNC_PREFERENCES, Context.MODE_PRIVATE)
        prefs.edit().putLong(LAST_SYNC_KEY, timestamp).apply()
    }
    
    /**
     * Actualiza el estado de sincronización.
     *
     * @param state Nuevo estado.
     */
    private fun updateSyncState(state: SyncState) {
        coroutineScope.launch {
            _syncState.emit(state)
        }
    }
    
    /**
     * Actualiza el progreso de la sincronización.
     *
     * @param progress Porcentaje de progreso (0-100).
     */
    private fun updateSyncProgress(progress: Int) {
        coroutineScope.launch {
            val currentState = _syncState.value
            if (currentState is SyncState.Syncing) {
                _syncState.emit(SyncState.Syncing(progress))
                
                // Actualizar notificación de progreso
                syncNotificationHelper.updateSyncProgressNotification(progress)
            }
        }
    }
    
    /**
     * Inicia una sincronización manual.
     */
    fun requestSync() {
        coroutineScope.launch {
            syncAll(forceRefresh = false)
        }
    }
    
    /**
     * Fuerza una sincronización completa, ignorando la última fecha de sincronización.
     */
    fun requestFullSync() {
        coroutineScope.launch {
            syncAll(forceRefresh = true)
        }
    }
    
    companion object {
        // Nombre y clave para preferencias compartidas
        private const val SYNC_PREFERENCES = "productiva_sync_preferences"
        private const val LAST_SYNC_KEY = "last_sync_timestamp"
    }
}