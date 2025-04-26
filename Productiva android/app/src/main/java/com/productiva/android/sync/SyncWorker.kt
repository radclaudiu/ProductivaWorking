package com.productiva.android.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.productiva.android.ProductivaApplication
import com.productiva.android.network.RetrofitClient
import com.productiva.android.repository.LabelTemplateRepository
import com.productiva.android.repository.ResourceState
import com.productiva.android.repository.TaskRepository
import com.productiva.android.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

/**
 * Worker de sincronización que ejecuta las operaciones en segundo plano.
 * Utiliza WorkManager para programar y ejecutar las sincronizaciones.
 */
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    
    private val TAG = "SyncWorker"
    private val prefs = appContext.getSharedPreferences("sync_preferences", Context.MODE_PRIVATE)
    
    // Repositorios
    private val apiService = RetrofitClient.getApiService(appContext)
    private val database = (appContext.applicationContext as ProductivaApplication).database
    private val userRepository = UserRepository(database.userDao(), apiService)
    private val taskRepository = TaskRepository(appContext, database.taskDao(), database.taskCompletionDao(), apiService)
    private val labelTemplateRepository = LabelTemplateRepository(database.labelTemplateDao(), apiService)
    
    /**
     * Ejecuta las operaciones de sincronización.
     */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando sincronización programada")
            
            // Sincronizar usuarios
            userRepository.syncUsers().collect { state ->
                if (state is ResourceState.Error) {
                    Log.e(TAG, "Error sincronizando usuarios: ${state.message}")
                }
            }
            
            // Sincronizar tareas
            taskRepository.syncTasks().collect { state ->
                if (state is ResourceState.Error) {
                    Log.e(TAG, "Error sincronizando tareas: ${state.message}")
                }
            }
            
            // Sincronizar plantillas de etiquetas
            labelTemplateRepository.syncLabelTemplates().collect { state ->
                if (state is ResourceState.Error) {
                    Log.e(TAG, "Error sincronizando plantillas: ${state.message}")
                }
            }
            
            // Sincronizar finalizaciones pendientes
            taskRepository.syncPendingCompletions().collect { state ->
                if (state is ResourceState.Error) {
                    Log.e(TAG, "Error sincronizando finalizaciones: ${state.message}")
                }
            }
            
            // Actualizar tiempo de última sincronización
            prefs.edit().putLong("last_sync_time", System.currentTimeMillis()).apply()
            
            Log.d(TAG, "Sincronización completada con éxito")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la sincronización", e)
            Result.retry()
        }
    }
}