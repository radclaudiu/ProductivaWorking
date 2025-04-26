package com.productiva.android.sync

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.productiva.android.repository.ProductRepository
import com.productiva.android.repository.TaskRepository
import com.productiva.android.repository.ResourceState
import com.productiva.android.utils.PREF_LAST_SYNC_TIME
import com.productiva.android.utils.PREFS_NAME
import com.productiva.android.utils.SYNC_INTERVAL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Gestor de sincronización para la aplicación.
 * Coordina la sincronización de datos entre la base de datos local y el servidor.
 */
class SyncManager private constructor(
    private val context: Context,
    private val taskRepository: TaskRepository,
    private val productRepository: ProductRepository
) {
    companion object {
        private const val TAG = "SyncManager"
        private const val SYNC_WORK_NAME = "periodic_sync_work"
        
        @Volatile
        private var instance: SyncManager? = null
        
        fun getInstance(
            context: Context,
            taskRepository: TaskRepository,
            productRepository: ProductRepository
        ): SyncManager {
            return instance ?: synchronized(this) {
                instance ?: SyncManager(
                    context.applicationContext,
                    taskRepository,
                    productRepository
                ).also { instance = it }
            }
        }
    }
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    // Estado de la sincronización
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    /**
     * Inicia la sincronización periódica en segundo plano.
     */
    fun scheduleSyncWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            SYNC_INTERVAL, TimeUnit.MILLISECONDS
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
        
        Log.d(TAG, "Sincronización periódica programada")
    }
    
    /**
     * Detiene la sincronización periódica en segundo plano.
     */
    fun cancelSyncWork() {
        WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
        Log.d(TAG, "Sincronización periódica cancelada")
    }
    
    /**
     * Realiza una sincronización completa ahora.
     *
     * @return Flow con el resultado de la sincronización.
     */
    fun syncNow(): Flow<ResourceState<SyncResult>> = flow {
        emit(ResourceState.Loading())
        
        // Actualizar estado de sincronización
        _syncState.value = SyncState.Syncing(0f)
        
        try {
            // Sincronizar tareas pendientes primero (completaciones offline)
            var progressPercent = 0f
            _syncState.value = SyncState.Syncing(progressPercent)
            
            var totalSynced = 0
            var totalErrors = 0
            
            // 1. Sincronizar completaciones de tareas
            taskRepository.syncPendingTaskCompletions().collect { result ->
                progressPercent = 25f
                _syncState.value = SyncState.Syncing(progressPercent)
                
                when (result) {
                    is ResourceState.Success -> {
                        totalSynced += result.data
                    }
                    is ResourceState.Error -> {
                        Log.e(TAG, "Error al sincronizar completaciones: ${result.message}")
                        totalErrors++
                    }
                    else -> {
                        // Ignorar otros estados
                    }
                }
            }
            
            // 2. Sincronizar productos modificados localmente
            progressPercent = 50f
            _syncState.value = SyncState.Syncing(progressPercent)
            
            productRepository.syncPendingProducts().collect { result ->
                progressPercent = 75f
                _syncState.value = SyncState.Syncing(progressPercent)
                
                when (result) {
                    is ResourceState.Success -> {
                        totalSynced += result.data
                    }
                    is ResourceState.Error -> {
                        Log.e(TAG, "Error al sincronizar productos: ${result.message}")
                        totalErrors++
                    }
                    else -> {
                        // Ignorar otros estados
                    }
                }
            }
            
            // Actualizar timestamp de última sincronización
            val currentTime = System.currentTimeMillis()
            updateLastSyncTime(currentTime)
            
            // Sincronización completada
            progressPercent = 100f
            _syncState.value = SyncState.Syncing(progressPercent)
            
            val syncResult = SyncResult(
                totalItemsSynced = totalSynced,
                totalErrors = totalErrors,
                timestamp = currentTime
            )
            
            _syncState.value = if (totalErrors == 0) {
                SyncState.Success(syncResult)
            } else {
                SyncState.CompletedWithErrors(syncResult)
            }
            
            emit(ResourceState.Success(syncResult))
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la sincronización", e)
            _syncState.value = SyncState.Error("Error durante la sincronización: ${e.message}")
            emit(ResourceState.Error("Error durante la sincronización: ${e.message}", e))
        }
    }
    
    /**
     * Inicia una sincronización de datos completa en segundo plano.
     */
    fun startBackgroundSync() {
        coroutineScope.launch {
            syncNow().collect()
        }
    }
    
    /**
     * Obtiene el timestamp de la última sincronización exitosa.
     */
    fun getLastSyncTime(): Long {
        return sharedPreferences.getLong(PREF_LAST_SYNC_TIME, 0L)
    }
    
    /**
     * Actualiza el timestamp de la última sincronización exitosa.
     */
    private fun updateLastSyncTime(timestamp: Long) {
        sharedPreferences.edit().putLong(PREF_LAST_SYNC_TIME, timestamp).apply()
    }
}

/**
 * Estados posibles de la sincronización.
 */
sealed class SyncState {
    /**
     * No hay sincronización en progreso.
     */
    object Idle : SyncState()
    
    /**
     * Sincronización en progreso.
     */
    data class Syncing(val progress: Float) : SyncState()
    
    /**
     * Sincronización completada con éxito.
     */
    data class Success(val result: SyncResult) : SyncState()
    
    /**
     * Sincronización completada con errores.
     */
    data class CompletedWithErrors(val result: SyncResult) : SyncState()
    
    /**
     * Error durante la sincronización.
     */
    data class Error(val message: String) : SyncState()
}

/**
 * Resultado de una sincronización.
 */
data class SyncResult(
    val totalItemsSynced: Int,
    val totalErrors: Int,
    val timestamp: Long
)