package com.productiva.android.sync

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.*
import com.productiva.android.repository.LabelTemplateRepository
import com.productiva.android.repository.ResourceState
import com.productiva.android.repository.TaskRepository
import com.productiva.android.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Administrador de sincronización que coordina las operaciones de sincronización
 * entre la aplicación móvil y el servidor web de Productiva.
 */
class SyncManager(
    private val context: Context,
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository,
    private val labelTemplateRepository: LabelTemplateRepository
) {
    private val TAG = "SyncManager"
    private val prefs: SharedPreferences = context.getSharedPreferences(SYNC_PREFERENCES, Context.MODE_PRIVATE)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    companion object {
        private const val SYNC_PREFERENCES = "sync_preferences"
        private const val LAST_SYNC_TIME = "last_sync_time"
        private const val WORK_TAG_PERIODIC = "sync_periodic"
        private const val WORK_TAG_IMMEDIATE = "sync_immediate"
        private const val SYNC_INTERVAL_HOURS = 2L // Sincronización cada 2 horas
    }
    
    /**
     * Inicia la sincronización periódica en segundo plano.
     */
    fun setupPeriodicSync() {
        try {
            // Restricciones para la sincronización (necesita conectividad)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            // Configurar trabajo periódico
            val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                SYNC_INTERVAL_HOURS, TimeUnit.HOURS,
                PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS, TimeUnit.MILLISECONDS
            )
                .setConstraints(constraints)
                .addTag(WORK_TAG_PERIODIC)
                .build()
            
            // Registrar trabajo con WorkManager
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_TAG_PERIODIC,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    syncWorkRequest
                )
            
            Log.d(TAG, "Sincronización periódica configurada cada $SYNC_INTERVAL_HOURS horas")
        } catch (e: Exception) {
            Log.e(TAG, "Error al configurar sincronización periódica", e)
        }
    }
    
    /**
     * Realiza una sincronización inmediata.
     */
    fun syncNow() {
        try {
            // Restricciones para la sincronización (necesita conectividad)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            // Configurar trabajo único
            val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .addTag(WORK_TAG_IMMEDIATE)
                .build()
            
            // Registrar trabajo con WorkManager
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    WORK_TAG_IMMEDIATE,
                    ExistingWorkPolicy.REPLACE,
                    syncWorkRequest
                )
            
            Log.d(TAG, "Sincronización inmediata solicitada")
        } catch (e: Exception) {
            Log.e(TAG, "Error al solicitar sincronización inmediata", e)
            // Intentar sincronización manual si falla WorkManager
            syncManually()
        }
    }
    
    /**
     * Realiza una sincronización manual (sin usar WorkManager).
     */
    private fun syncManually() {
        coroutineScope.launch {
            try {
                Log.d(TAG, "Iniciando sincronización manual")
                syncUsers()
                syncTasks()
                syncLabelTemplates()
                syncPendingCompletions()
                updateLastSyncTime()
                Log.d(TAG, "Sincronización manual completada")
            } catch (e: Exception) {
                Log.e(TAG, "Error en sincronización manual", e)
            }
        }
    }
    
    /**
     * Sincroniza los usuarios desde el servidor.
     */
    private suspend fun syncUsers() {
        try {
            userRepository.syncUsers().collect { state ->
                when (state) {
                    is ResourceState.Success -> {
                        Log.d(TAG, "Sincronización de usuarios completada: ${state.data?.size ?: 0} usuarios")
                    }
                    is ResourceState.Error -> {
                        Log.e(TAG, "Error en sincronización de usuarios: ${state.message}")
                    }
                    else -> {} // Estado de carga, no hacer nada
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización de usuarios", e)
        }
    }
    
    /**
     * Sincroniza las tareas desde el servidor.
     */
    private suspend fun syncTasks() {
        try {
            taskRepository.syncTasks().collect { state ->
                when (state) {
                    is ResourceState.Success -> {
                        Log.d(TAG, "Sincronización de tareas completada: ${state.data?.size ?: 0} tareas")
                    }
                    is ResourceState.Error -> {
                        Log.e(TAG, "Error en sincronización de tareas: ${state.message}")
                    }
                    else -> {} // Estado de carga, no hacer nada
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización de tareas", e)
        }
    }
    
    /**
     * Sincroniza las plantillas de etiquetas desde el servidor.
     */
    private suspend fun syncLabelTemplates() {
        try {
            labelTemplateRepository.syncLabelTemplates().collect { state ->
                when (state) {
                    is ResourceState.Success -> {
                        Log.d(TAG, "Sincronización de plantillas completada: ${state.data?.size ?: 0} plantillas")
                    }
                    is ResourceState.Error -> {
                        Log.e(TAG, "Error en sincronización de plantillas: ${state.message}")
                    }
                    else -> {} // Estado de carga, no hacer nada
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización de plantillas", e)
        }
    }
    
    /**
     * Sincroniza las finalizaciones de tareas pendientes al servidor.
     */
    private suspend fun syncPendingCompletions() {
        try {
            taskRepository.syncPendingCompletions().collect { state ->
                when (state) {
                    is ResourceState.Success -> {
                        val count = state.data ?: 0
                        if (count > 0) {
                            Log.d(TAG, "Sincronización de finalizaciones completada: $count finalizaciones")
                        }
                    }
                    is ResourceState.Error -> {
                        Log.e(TAG, "Error en sincronización de finalizaciones: ${state.message}")
                    }
                    else -> {} // Estado de carga, no hacer nada
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización de finalizaciones", e)
        }
    }
    
    /**
     * Actualiza la marca de tiempo de la última sincronización.
     */
    private fun updateLastSyncTime() {
        prefs.edit().putLong(LAST_SYNC_TIME, System.currentTimeMillis()).apply()
    }
    
    /**
     * Obtiene el tiempo transcurrido desde la última sincronización.
     * @return Tiempo en milisegundos desde la última sincronización, o -1 si nunca se ha sincronizado.
     */
    fun getTimeSinceLastSync(): Long {
        val lastSync = prefs.getLong(LAST_SYNC_TIME, -1)
        if (lastSync == -1L) return -1
        
        return System.currentTimeMillis() - lastSync
    }
    
    /**
     * Formatea el tiempo desde la última sincronización para mostrar al usuario.
     */
    fun getFormattedLastSyncTime(): String {
        val timeSinceLastSync = getTimeSinceLastSync()
        
        return when {
            timeSinceLastSync < 0 -> "Nunca"
            timeSinceLastSync < 60 * 1000 -> "Hace menos de un minuto"
            timeSinceLastSync < 60 * 60 * 1000 -> {
                val minutes = timeSinceLastSync / (60 * 1000)
                "Hace $minutes ${if (minutes == 1L) "minuto" else "minutos"}"
            }
            timeSinceLastSync < 24 * 60 * 60 * 1000 -> {
                val hours = timeSinceLastSync / (60 * 60 * 1000)
                "Hace $hours ${if (hours == 1L) "hora" else "horas"}"
            }
            else -> {
                val days = timeSinceLastSync / (24 * 60 * 60 * 1000)
                "Hace $days ${if (days == 1L) "día" else "días"}"
            }
        }
    }
    
    /**
     * Cancela todas las operaciones de sincronización programadas.
     */
    fun cancelAllSync() {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG_PERIODIC)
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG_IMMEDIATE)
        Log.d(TAG, "Todas las sincronizaciones canceladas")
    }
}