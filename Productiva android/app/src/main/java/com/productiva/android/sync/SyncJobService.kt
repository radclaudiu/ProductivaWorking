package com.productiva.android.sync

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.util.Log
import com.productiva.android.ProductivaApplication
import com.productiva.android.network.RetrofitClient
import com.productiva.android.repository.LabelTemplateRepository
import com.productiva.android.repository.ResourceState
import com.productiva.android.repository.TaskRepository
import com.productiva.android.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Servicio de tareas programadas para sincronización periódica en segundo plano.
 */
class SyncJobService : JobService() {
    
    private val TAG = "SyncJobService"
    private var syncJob: Job? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Clave para almacenar la hora de la última sincronización
    companion object {
        private const val SYNC_PREFERENCES = "sync_preferences"
        private const val LAST_SYNC_TIME = "last_sync_time"
        
        /**
         * Verifica si es necesario sincronizar basado en el tiempo transcurrido.
         */
        fun shouldSync(context: Context, intervalHours: Int = 2): Boolean {
            val prefs = context.getSharedPreferences(SYNC_PREFERENCES, Context.MODE_PRIVATE)
            val lastSync = prefs.getLong(LAST_SYNC_TIME, 0)
            
            if (lastSync == 0L) return true // Nunca sincronizado
            
            val now = System.currentTimeMillis()
            val elapsed = now - lastSync
            val intervalMillis = intervalHours * 60 * 60 * 1000L
            
            return elapsed >= intervalMillis
        }
    }
    
    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Servicio de sincronización iniciado")
        return super.onStartCommand(intent, flags, startId)
    }
    
    /**
     * Método llamado cuando el programador ejecuta la tarea.
     */
    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Iniciando tarea de sincronización programada")
        
        // Iniciar sincronización en una coroutine
        syncJob = serviceScope.launch {
            try {
                val app = application as ProductivaApplication
                val apiService = RetrofitClient.getApiService(applicationContext)
                val database = app.database
                
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
                updateLastSyncTime()
                
                Log.d(TAG, "Sincronización completada con éxito")
                
                // Informar que la tarea ha terminado
                jobFinished(params, false)
            } catch (e: Exception) {
                Log.e(TAG, "Error durante la sincronización", e)
                
                // Solicitar reprogramación de la tarea
                jobFinished(params, true)
            }
        }
        
        // Retornar true indica que la tarea sigue ejecutándose en segundo plano
        return true
    }
    
    /**
     * Método llamado cuando se cancela la tarea.
     */
    override fun onStopJob(params: JobParameters?): Boolean {
        // Cancelar la coroutine de sincronización
        syncJob?.cancel()
        
        Log.d(TAG, "Tarea de sincronización cancelada")
        
        // Retornar true para indicar que la tarea debe ser reprogramada
        return true
    }
    
    /**
     * Actualiza la marca de tiempo de la última sincronización.
     */
    private fun updateLastSyncTime() {
        val prefs = applicationContext.getSharedPreferences(SYNC_PREFERENCES, Context.MODE_PRIVATE)
        prefs.edit().putLong(LAST_SYNC_TIME, System.currentTimeMillis()).apply()
    }
}