package com.productiva.android.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.productiva.android.data.AppDatabase
import com.productiva.android.data.model.CheckpointData
import com.productiva.android.data.model.LabelTemplate
import com.productiva.android.data.model.Product
import com.productiva.android.data.model.Task
import com.productiva.android.data.model.TaskCompletion
import com.productiva.android.network.ApiService
import com.productiva.android.network.RetrofitClient
import com.productiva.android.network.model.SyncRequest
import com.productiva.android.network.model.SyncResponse
import com.productiva.android.session.SessionManager
import com.productiva.android.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.concurrent.TimeUnit

/**
 * Gestor principal de sincronización.
 * Coordina el proceso de sincronización entre la aplicación y el servidor.
 */
class SyncManager(private val context: Context) {
    
    companion object {
        private const val TAG = "SyncManager"
        private const val SYNC_WORK_NAME = "periodic_sync_work"
        private const val SYNC_INTERVAL_MINUTES = 30L
        
        // Instancia única (Singleton)
        @Volatile
        private var instance: SyncManager? = null
        
        /**
         * Obtiene la instancia única de SyncManager.
         *
         * @param context Contexto de la aplicación.
         * @return Instancia de SyncManager.
         */
        fun getInstance(context: Context): SyncManager {
            return instance ?: synchronized(this) {
                instance ?: SyncManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val db = AppDatabase.getDatabase(context)
    private val taskDao = db.taskDao()
    private val taskCompletionDao = db.taskCompletionDao()
    private val productDao = db.productDao()
    private val labelTemplateDao = db.labelTemplateDao()
    private val checkpointDao = db.checkpointDao()
    private val apiService = RetrofitClient.getApiService(context)
    private val sessionManager = SessionManager(context)
    private val notificationHelper = SyncNotificationHelper(context)
    
    // Estado actual de la sincronización
    private val syncState = SyncState()
    
    /**
     * Inicia la sincronización programada.
     * Configura un trabajo periódico con WorkManager.
     */
    fun startPeriodicSync() {
        Log.d(TAG, "Iniciando sincronización periódica")
        
        // Restricciones: requiere conectividad a Internet
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        // Crear solicitud de trabajo periódico
        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()
        
        // Programar el trabajo
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
        
        Log.d(TAG, "Sincronización periódica programada cada $SYNC_INTERVAL_MINUTES minutos")
    }
    
    /**
     * Detiene la sincronización programada.
     */
    fun stopPeriodicSync() {
        Log.d(TAG, "Deteniendo sincronización periódica")
        WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
    }
    
    /**
     * Realiza una sincronización manual.
     * Este método inicia el proceso de sincronización inmediatamente,
     * independientemente de la programación.
     *
     * @return Resultado de la sincronización.
     */
    suspend fun performManualSync(): SyncResult {
        Log.d(TAG, "Iniciando sincronización manual")
        
        // Actualizar estado
        syncState.isRunning = true
        syncState.lastSyncAttemptTime = System.currentTimeMillis()
        
        // Mostrar notificación de inicio
        notificationHelper.showSyncStartedNotification()
        
        try {
            if (!NetworkUtils.isOnline(context)) {
                Log.e(TAG, "No hay conexión a Internet para sincronizar")
                syncState.lastError = "No hay conexión a Internet"
                syncState.isRunning = false
                notificationHelper.showSyncFailedNotification("No hay conexión a Internet")
                return SyncResult.Error("No hay conexión a Internet")
            }
            
            // Token válido
            if (sessionManager.getToken().isNullOrEmpty()) {
                Log.e(TAG, "No hay token de autenticación para sincronizar")
                syncState.lastError = "No hay sesión iniciada"
                syncState.isRunning = false
                notificationHelper.showSyncFailedNotification("No hay sesión iniciada")
                return SyncResult.Error("No hay sesión iniciada")
            }
            
            // Sincronizar todas las entidades
            val taskResult = syncTasks()
            val productResult = syncProducts()
            val labelTemplateResult = syncLabelTemplates()
            val checkpointResult = syncCheckpoints()
            
            // Actualizar estado
            syncState.isRunning = false
            syncState.lastSuccessfulSyncTime = System.currentTimeMillis()
            syncState.lastError = null
            syncState.pendingChangesCount = countPendingChanges()
            
            // Mostrar notificación de éxito
            notificationHelper.showSyncCompletedNotification()
            
            // Devolver resultado combinado
            return SyncResult.Success(
                taskChanges = taskResult,
                productChanges = productResult,
                labelTemplateChanges = labelTemplateResult,
                checkpointChanges = checkpointResult
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la sincronización", e)
            
            // Actualizar estado
            syncState.isRunning = false
            syncState.lastError = e.message ?: "Error desconocido"
            
            // Mostrar notificación de error
            notificationHelper.showSyncFailedNotification(e.message ?: "Error desconocido")
            
            return SyncResult.Error(e.message ?: "Error desconocido")
        }
    }
    
    /**
     * Sincroniza tareas con el servidor.
     *
     * @return Estadísticas de cambios.
     */
    private suspend fun syncTasks(): SyncChanges {
        Log.d(TAG, "Sincronizando tareas")
        
        return withContext(Dispatchers.IO) {
            // Obtener tareas con cambios pendientes
            val pendingTasks = taskDao.getPendingSyncTasks()
            Log.d(TAG, "Tareas pendientes de sincronización: ${pendingTasks.size}")
            
            // Obtener última marca de tiempo de sincronización
            val lastSyncTimestamp = sessionManager.getLastSyncTimestamp("tasks")
            
            // Preparar solicitud de sincronización
            val syncRequest = SyncRequest(
                lastSyncTimestamp = lastSyncTimestamp,
                clientChanges = pendingTasks
            )
            
            try {
                // Enviar solicitud al servidor
                val response: Response<SyncResponse<Task>> = apiService.syncTasks(syncRequest)
                
                if (response.isSuccessful) {
                    val syncResponse = response.body()
                    if (syncResponse != null) {
                        processSyncResponse(syncResponse, pendingTasks)
                        
                        // Guardar nueva marca de tiempo
                        sessionManager.saveLastSyncTimestamp("tasks", syncResponse.timestamp)
                        
                        // Devolver estadísticas
                        return@withContext SyncChanges(
                            sent = pendingTasks.size,
                            received = syncResponse.serverChanges.size,
                            applied = syncResponse.appliedChanges.size,
                            failed = syncResponse.failedChanges.size,
                            conflicts = syncResponse.conflictResolutions.size
                        )
                    }
                }
                
                // Error en la respuesta
                Log.e(TAG, "Error en la sincronización de tareas: ${response.code()} - ${response.message()}")
                throw Exception("Error en la sincronización de tareas: ${response.code()} - ${response.message()}")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error al sincronizar tareas", e)
                throw e
            }
        }
    }
    
    /**
     * Procesa la respuesta de sincronización de tareas.
     *
     * @param syncResponse Respuesta de sincronización.
     * @param pendingTasks Tareas enviadas para sincronizar.
     */
    private suspend fun processSyncResponse(syncResponse: SyncResponse<Task>, pendingTasks: List<Task>) {
        withContext(Dispatchers.IO) {
            // Actualizar estado de las tareas enviadas con éxito
            taskDao.markAsSynced(syncResponse.appliedChanges)
            
            // Actualizar tareas recibidas del servidor
            taskDao.insertAll(syncResponse.serverChanges)
            
            // Procesar resoluciones de conflictos
            for (conflictResolution in syncResponse.conflictResolutions) {
                taskDao.insert(conflictResolution.resolution)
            }
            
            // Procesar fallos
            for (failedChange in syncResponse.failedChanges) {
                val task = pendingTasks.find { it.id == failedChange.id }
                if (task != null) {
                    // Si es un error temporal, mantener el estado pendiente
                    if (failedChange.code == "TEMPORARY_ERROR") {
                        continue
                    }
                    
                    // Si es un error permanente, marcar como fallido
                    taskDao.updateSyncStatus(task.id, Task.SyncStatus.SYNC_FAILED)
                }
            }
        }
    }
    
    /**
     * Sincroniza productos con el servidor.
     *
     * @return Estadísticas de cambios.
     */
    private suspend fun syncProducts(): SyncChanges {
        Log.d(TAG, "Sincronizando productos")
        
        return withContext(Dispatchers.IO) {
            // Obtener productos con cambios pendientes
            val pendingProducts = productDao.getPendingSyncProducts()
            Log.d(TAG, "Productos pendientes de sincronización: ${pendingProducts.size}")
            
            // Obtener última marca de tiempo de sincronización
            val lastSyncTimestamp = sessionManager.getLastSyncTimestamp("products")
            
            // Preparar solicitud de sincronización
            val syncRequest = SyncRequest(
                lastSyncTimestamp = lastSyncTimestamp,
                clientChanges = pendingProducts
            )
            
            try {
                // Enviar solicitud al servidor
                val response: Response<SyncResponse<Product>> = apiService.syncProducts(syncRequest)
                
                if (response.isSuccessful) {
                    val syncResponse = response.body()
                    if (syncResponse != null) {
                        processSyncResponse(syncResponse, pendingProducts)
                        
                        // Guardar nueva marca de tiempo
                        sessionManager.saveLastSyncTimestamp("products", syncResponse.timestamp)
                        
                        // Devolver estadísticas
                        return@withContext SyncChanges(
                            sent = pendingProducts.size,
                            received = syncResponse.serverChanges.size,
                            applied = syncResponse.appliedChanges.size,
                            failed = syncResponse.failedChanges.size,
                            conflicts = syncResponse.conflictResolutions.size
                        )
                    }
                }
                
                // Error en la respuesta
                Log.e(TAG, "Error en la sincronización de productos: ${response.code()} - ${response.message()}")
                throw Exception("Error en la sincronización de productos: ${response.code()} - ${response.message()}")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error al sincronizar productos", e)
                throw e
            }
        }
    }
    
    /**
     * Procesa la respuesta de sincronización de productos.
     *
     * @param syncResponse Respuesta de sincronización.
     * @param pendingProducts Productos enviados para sincronizar.
     */
    private suspend fun processSyncResponse(syncResponse: SyncResponse<Product>, pendingProducts: List<Product>) {
        withContext(Dispatchers.IO) {
            // Actualizar estado de los productos enviados con éxito
            productDao.markAsSynced(syncResponse.appliedChanges)
            
            // Actualizar productos recibidos del servidor
            productDao.insertAll(syncResponse.serverChanges)
            
            // Procesar resoluciones de conflictos
            for (conflictResolution in syncResponse.conflictResolutions) {
                productDao.insert(conflictResolution.resolution)
            }
            
            // Procesar fallos
            for (failedChange in syncResponse.failedChanges) {
                val product = pendingProducts.find { it.id == failedChange.id }
                if (product != null) {
                    // Si es un error temporal, mantener el estado pendiente
                    if (failedChange.code == "TEMPORARY_ERROR") {
                        continue
                    }
                    
                    // Si es un error permanente, marcar como fallido
                    productDao.updateSyncStatus(product.id, Product.SyncStatus.SYNC_FAILED)
                }
            }
        }
    }
    
    /**
     * Sincroniza plantillas de etiquetas con el servidor.
     *
     * @return Estadísticas de cambios.
     */
    private suspend fun syncLabelTemplates(): SyncChanges {
        Log.d(TAG, "Sincronizando plantillas de etiquetas")
        
        return withContext(Dispatchers.IO) {
            // Obtener plantillas de etiquetas con cambios pendientes
            val pendingTemplates = labelTemplateDao.getPendingSyncTemplates()
            Log.d(TAG, "Plantillas de etiquetas pendientes de sincronización: ${pendingTemplates.size}")
            
            // Obtener última marca de tiempo de sincronización
            val lastSyncTimestamp = sessionManager.getLastSyncTimestamp("label_templates")
            
            // Preparar solicitud de sincronización
            val syncRequest = SyncRequest(
                lastSyncTimestamp = lastSyncTimestamp,
                clientChanges = pendingTemplates
            )
            
            try {
                // Enviar solicitud al servidor
                val response: Response<SyncResponse<LabelTemplate>> = apiService.syncLabelTemplates(syncRequest)
                
                if (response.isSuccessful) {
                    val syncResponse = response.body()
                    if (syncResponse != null) {
                        processSyncResponse(syncResponse, pendingTemplates)
                        
                        // Guardar nueva marca de tiempo
                        sessionManager.saveLastSyncTimestamp("label_templates", syncResponse.timestamp)
                        
                        // Devolver estadísticas
                        return@withContext SyncChanges(
                            sent = pendingTemplates.size,
                            received = syncResponse.serverChanges.size,
                            applied = syncResponse.appliedChanges.size,
                            failed = syncResponse.failedChanges.size,
                            conflicts = syncResponse.conflictResolutions.size
                        )
                    }
                }
                
                // Error en la respuesta
                Log.e(TAG, "Error en la sincronización de plantillas de etiquetas: ${response.code()} - ${response.message()}")
                throw Exception("Error en la sincronización de plantillas de etiquetas: ${response.code()} - ${response.message()}")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error al sincronizar plantillas de etiquetas", e)
                throw e
            }
        }
    }
    
    /**
     * Procesa la respuesta de sincronización de plantillas de etiquetas.
     *
     * @param syncResponse Respuesta de sincronización.
     * @param pendingTemplates Plantillas de etiquetas enviadas para sincronizar.
     */
    private suspend fun processSyncResponse(syncResponse: SyncResponse<LabelTemplate>, pendingTemplates: List<LabelTemplate>) {
        withContext(Dispatchers.IO) {
            // Actualizar estado de las plantillas enviadas con éxito
            labelTemplateDao.markAsSynced(syncResponse.appliedChanges)
            
            // Actualizar plantillas recibidas del servidor
            labelTemplateDao.insertAll(syncResponse.serverChanges)
            
            // Procesar resoluciones de conflictos
            for (conflictResolution in syncResponse.conflictResolutions) {
                labelTemplateDao.insert(conflictResolution.resolution)
            }
            
            // Procesar fallos
            for (failedChange in syncResponse.failedChanges) {
                val template = pendingTemplates.find { it.id == failedChange.id }
                if (template != null) {
                    // Si es un error temporal, mantener el estado pendiente
                    if (failedChange.code == "TEMPORARY_ERROR") {
                        continue
                    }
                    
                    // Si es un error permanente, marcar como fallido
                    labelTemplateDao.updateSyncStatus(template.id, LabelTemplate.SyncStatus.SYNC_FAILED)
                }
            }
        }
    }
    
    /**
     * Sincroniza fichajes con el servidor.
     *
     * @return Estadísticas de cambios.
     */
    private suspend fun syncCheckpoints(): SyncChanges {
        Log.d(TAG, "Sincronizando fichajes")
        
        return withContext(Dispatchers.IO) {
            // Obtener fichajes con cambios pendientes
            val pendingCheckpoints = checkpointDao.getPendingSyncCheckpoints()
            Log.d(TAG, "Fichajes pendientes de sincronización: ${pendingCheckpoints.size}")
            
            // Obtener última marca de tiempo de sincronización
            val lastSyncTimestamp = sessionManager.getLastSyncTimestamp("checkpoints")
            
            // Preparar solicitud de sincronización
            val syncRequest = SyncRequest(
                lastSyncTimestamp = lastSyncTimestamp,
                clientChanges = pendingCheckpoints
            )
            
            try {
                // Enviar solicitud al servidor
                val response: Response<SyncResponse<CheckpointData>> = apiService.syncCheckpoints(syncRequest)
                
                if (response.isSuccessful) {
                    val syncResponse = response.body()
                    if (syncResponse != null) {
                        processSyncResponse(syncResponse, pendingCheckpoints)
                        
                        // Guardar nueva marca de tiempo
                        sessionManager.saveLastSyncTimestamp("checkpoints", syncResponse.timestamp)
                        
                        // Devolver estadísticas
                        return@withContext SyncChanges(
                            sent = pendingCheckpoints.size,
                            received = syncResponse.serverChanges.size,
                            applied = syncResponse.appliedChanges.size,
                            failed = syncResponse.failedChanges.size,
                            conflicts = syncResponse.conflictResolutions.size
                        )
                    }
                }
                
                // Error en la respuesta
                Log.e(TAG, "Error en la sincronización de fichajes: ${response.code()} - ${response.message()}")
                throw Exception("Error en la sincronización de fichajes: ${response.code()} - ${response.message()}")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error al sincronizar fichajes", e)
                throw e
            }
        }
    }
    
    /**
     * Procesa la respuesta de sincronización de fichajes.
     *
     * @param syncResponse Respuesta de sincronización.
     * @param pendingCheckpoints Fichajes enviados para sincronizar.
     */
    private suspend fun processSyncResponse(syncResponse: SyncResponse<CheckpointData>, pendingCheckpoints: List<CheckpointData>) {
        withContext(Dispatchers.IO) {
            // Actualizar estado de los fichajes enviados con éxito
            checkpointDao.markAsSynced(syncResponse.appliedChanges)
            
            // Actualizar fichajes recibidos del servidor
            checkpointDao.insertAll(syncResponse.serverChanges)
            
            // Procesar resoluciones de conflictos
            for (conflictResolution in syncResponse.conflictResolutions) {
                checkpointDao.insert(conflictResolution.resolution)
            }
            
            // Procesar fallos
            for (failedChange in syncResponse.failedChanges) {
                val checkpoint = pendingCheckpoints.find { it.id == failedChange.id }
                if (checkpoint != null) {
                    // Si es un error temporal, mantener el estado pendiente
                    if (failedChange.code == "TEMPORARY_ERROR") {
                        continue
                    }
                    
                    // Si es un error permanente, marcar como fallido
                    checkpointDao.updateSyncStatus(checkpoint.id, CheckpointData.SyncStatus.SYNC_FAILED)
                }
            }
        }
    }
    
    /**
     * Cuenta el número total de cambios pendientes de sincronización.
     *
     * @return Número total de cambios pendientes.
     */
    suspend fun countPendingChanges(): Int {
        return withContext(Dispatchers.IO) {
            val taskCount = taskDao.getPendingSyncTasksCount()
            val taskCompletionCount = taskCompletionDao.getPendingSyncCompletionsCount()
            val productCount = productDao.getPendingSyncProductsCount()
            val labelTemplateCount = labelTemplateDao.getPendingSyncTemplatesCount()
            val checkpointCount = checkpointDao.getPendingSyncCheckpointsCount()
            
            taskCount + taskCompletionCount + productCount + labelTemplateCount + checkpointCount
        }
    }
    
    /**
     * Obtiene el estado actual de la sincronización.
     *
     * @return Estado actual de la sincronización.
     */
    fun getSyncState(): SyncState {
        return syncState.copy()
    }
}