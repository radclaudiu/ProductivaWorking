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
import com.productiva.android.utils.SessionManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull

/**
 * Worker para ejecutar tareas de sincronización en segundo plano.
 */
class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    private val TAG = "SyncWorker"
    
    /**
     * Método principal que ejecuta el trabajo de sincronización.
     */
    override suspend fun doWork(): Result {
        Log.d(TAG, "Iniciando trabajo de sincronización")
        
        // Verificar si hay sesión activa
        val sessionManager = SessionManager(applicationContext)
        if (!sessionManager.isLoggedIn()) {
            Log.d(TAG, "No hay sesión activa, cancelando sincronización")
            return Result.failure()
        }
        
        // Obtener las instancias necesarias
        val app = applicationContext as ProductivaApplication
        val database = app.database
        val apiService = RetrofitClient.getApiService(applicationContext)
        
        // Inicializar repositorios
        val userRepository = UserRepository(database.userDao(), apiService)
        val taskRepository = TaskRepository(
            applicationContext,
            database.taskDao(),
            database.taskCompletionDao(),
            apiService
        )
        val labelTemplateRepository = LabelTemplateRepository(
            database.labelTemplateDao(),
            apiService
        )
        
        try {
            // 1. Sincronizar usuarios
            val userSyncResult = userRepository.syncUsers().firstOrNull()
            if (userSyncResult is ResourceState.Error) {
                Log.e(TAG, "Error sincronizando usuarios: ${userSyncResult.message}")
            } else {
                Log.d(TAG, "Sincronización de usuarios completada")
            }
            
            // 2. Sincronizar tareas
            val taskSyncResult = taskRepository.syncTasks().firstOrNull()
            if (taskSyncResult is ResourceState.Error) {
                Log.e(TAG, "Error sincronizando tareas: ${taskSyncResult.message}")
            } else {
                Log.d(TAG, "Sincronización de tareas completada")
            }
            
            // 3. Sincronizar plantillas de etiquetas
            val templateSyncResult = labelTemplateRepository.syncLabelTemplates().firstOrNull()
            if (templateSyncResult is ResourceState.Error) {
                Log.e(TAG, "Error sincronizando plantillas: ${templateSyncResult.message}")
            } else {
                Log.d(TAG, "Sincronización de plantillas completada")
            }
            
            // 4. Sincronizar finalizaciones pendientes
            val completionSyncResult = taskRepository.syncPendingCompletions().firstOrNull()
            if (completionSyncResult is ResourceState.Error) {
                Log.e(TAG, "Error sincronizando finalizaciones: ${completionSyncResult.message}")
            } else {
                Log.d(TAG, "Sincronización de finalizaciones completada")
            }
            
            // Actualizar tiempo de última sincronización
            val syncManager = SyncManager(applicationContext)
            syncManager.updateLastSyncTime(System.currentTimeMillis())
            
            // Comprobar si hubo errores críticos
            if (userSyncResult is ResourceState.Error && 
                taskSyncResult is ResourceState.Error && 
                templateSyncResult is ResourceState.Error) {
                // Si todos los procesos principales fallaron, considerar fallida la sincronización
                Log.e(TAG, "La sincronización falló en todos los componentes principales")
                return Result.retry()
            }
            
            Log.d(TAG, "Trabajo de sincronización completado con éxito")
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error durante el trabajo de sincronización", e)
            return Result.retry()
        }
    }
}