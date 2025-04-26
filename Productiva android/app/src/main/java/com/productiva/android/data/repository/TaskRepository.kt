package com.productiva.android.data.repository

import android.content.Context
import android.util.Log
import com.productiva.android.data.database.AppDatabase
import com.productiva.android.data.model.Task
import com.productiva.android.data.model.TaskCompletion
import com.productiva.android.network.NetworkResult
import com.productiva.android.network.model.SyncResponse
import com.productiva.android.network.safeApiCall
import com.productiva.android.repository.ResourceState
import com.productiva.android.session.SessionManager
import com.productiva.android.sync.SyncResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.collections.HashMap

/**
 * Repositorio que gestiona el acceso a datos de tareas y completados,
 * tanto desde la base de datos local como desde el servidor remoto.
 */
class TaskRepository private constructor(context: Context) : BaseRepository(context) {
    
    private val taskDao = AppDatabase.getDatabase(context, kotlinx.coroutines.MainScope()).taskDao()
    private val taskCompletionDao = AppDatabase.getDatabase(context, kotlinx.coroutines.MainScope()).taskCompletionDao()
    private val sessionManager = SessionManager.getInstance()
    
    companion object {
        private const val TAG = "TaskRepository"
        
        @Volatile
        private var instance: TaskRepository? = null
        
        /**
         * Obtiene la instancia única del repositorio.
         *
         * @param context Contexto de la aplicación.
         * @return Instancia del repositorio.
         */
        fun getInstance(context: Context): TaskRepository {
            return instance ?: synchronized(this) {
                instance ?: TaskRepository(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Obtiene todas las tareas como flujo de ResourceState.
     *
     * @param forceRefresh Si es true, fuerza una actualización desde el servidor.
     * @return Flujo de ResourceState con las tareas.
     */
    fun getAllTasks(forceRefresh: Boolean = false): Flow<ResourceState<List<Task>>> {
        return networkBoundResource(
            shouldFetch = { tasks -> forceRefresh || tasks.isNullOrEmpty() },
            remoteFetch = {
                val companyId = sessionManager.getCurrentCompanyId()
                safeApiCall {
                    apiService.getTasks(companyId = companyId)
                }
            },
            localFetch = {
                taskDao.getAllTasks()
            },
            saveFetchResult = { response ->
                withContext(Dispatchers.IO) {
                    // Guardar tareas añadidas y actualizadas
                    val tasksToSave = mutableListOf<Task>()
                    
                    response.added.forEach { task ->
                        tasksToSave.add(task.withSyncStatus(Task.SyncStatus.SYNCED))
                    }
                    
                    response.updated.forEach { task ->
                        tasksToSave.add(task.withSyncStatus(Task.SyncStatus.SYNCED))
                    }
                    
                    if (tasksToSave.isNotEmpty()) {
                        taskDao.insertAll(tasksToSave)
                    }
                    
                    // Procesar eliminaciones
                    if (response.deleted.isNotEmpty()) {
                        taskDao.markAsSynced(response.deleted)
                        taskDao.deleteMarkedTasks()
                    }
                }
            }
        )
    }
    
    /**
     * Obtiene una tarea por su ID como flujo de ResourceState.
     *
     * @param taskId ID de la tarea.
     * @param forceRefresh Si es true, fuerza una actualización desde el servidor.
     * @return Flujo de ResourceState con la tarea.
     */
    fun getTaskById(taskId: Int, forceRefresh: Boolean = false): Flow<ResourceState<Task>> {
        return networkBoundResource(
            shouldFetch = { task -> forceRefresh || task == null },
            remoteFetch = {
                safeApiCall {
                    apiService.getTaskById(taskId)
                }
            },
            localFetch = {
                taskDao.getTaskById(taskId)
            },
            saveFetchResult = { task ->
                withContext(Dispatchers.IO) {
                    taskDao.insert(task.withSyncStatus(Task.SyncStatus.SYNCED))
                }
            }
        )
    }
    
    /**
     * Completa una tarea con los datos de completado proporcionados.
     *
     * @param taskId ID de la tarea a completar.
     * @param completion Datos de completado.
     * @return Flujo de ResourceState con la tarea completada.
     */
    suspend fun completeTask(
        taskId: Int,
        completion: TaskCompletion
    ): ResourceState<Task> = withContext(Dispatchers.IO) {
        try {
            // Obtener la tarea de la base de datos
            val task = taskDao.getTaskById(taskId)
                ?: return@withContext ResourceState.Error("Tarea no encontrada")
            
            // Verificar si la tarea ya está completada
            if (task.isCompleted()) {
                return@withContext ResourceState.Error("La tarea ya está completada")
            }
            
            // Actualizar la tarea a estado completado
            val completedTask = task.complete(completion)
            taskDao.update(completedTask)
            
            // Guardar los datos de completado
            val completionWithSyncStatus = completion.copy(
                taskId = taskId,
                syncStatus = TaskCompletion.SyncStatus.PENDING_UPLOAD
            )
            taskCompletionDao.insert(completionWithSyncStatus)
            
            // Intentar sincronizar si hay conexión
            if (connectivityMonitor.isNetworkAvailable()) {
                when (val result = syncTaskCompletion(taskId, completionWithSyncStatus)) {
                    is NetworkResult.Success -> {
                        // Actualizar el estado de sincronización en la base de datos
                        taskDao.updateSyncStatus(taskId, Task.SyncStatus.SYNCED)
                        taskCompletionDao.updateSyncStatus(completionWithSyncStatus.id, TaskCompletion.SyncStatus.SYNCED)
                        
                        return@withContext ResourceState.Success(completedTask)
                    }
                    is NetworkResult.Error -> {
                        // Devolver éxito pero con datos locales
                        Log.e(TAG, "Error al sincronizar completado: ${result.message}")
                        return@withContext ResourceState.Success(completedTask, isFromCache = true)
                    }
                    is NetworkResult.Loading -> {
                        // Este caso no debería ocurrir
                        return@withContext ResourceState.Success(completedTask, isFromCache = true)
                    }
                }
            } else {
                // Sin conexión, devolver éxito pero con datos locales
                return@withContext ResourceState.Success(completedTask, isFromCache = true)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al completar tarea", e)
            ResourceState.Error(e.message ?: "Error desconocido")
        }
    }
    
    /**
     * Sincroniza un completado de tarea con el servidor.
     *
     * @param taskId ID de la tarea.
     * @param completion Datos de completado.
     * @return Resultado de la operación.
     */
    private suspend fun syncTaskCompletion(
        taskId: Int,
        completion: TaskCompletion
    ): NetworkResult<Task> = withNetworkCheck({
        
        // Si hay archivos adjuntos, usar el endpoint multipart
        if (completion.hasPhoto() || completion.hasSignature()) {
            // TODO: Implementar subida de archivos multipart
            // Por ahora, solo sincronizamos los datos básicos
            safeApiCall {
                apiService.completeTask(taskId, completion)
            }
        } else {
            // Sin archivos adjuntos, usar el endpoint normal
            safeApiCall {
                apiService.completeTask(taskId, completion)
            }
        }
    })
    
    /**
     * Crea una nueva tarea.
     *
     * @param task Tarea a crear.
     * @return Flujo de ResourceState con la tarea creada.
     */
    suspend fun createTask(task: Task): ResourceState<Task> = withContext(Dispatchers.IO) {
        try {
            // Preparar la tarea para creación local
            val taskWithStatus = task.copy(
                syncStatus = Task.SyncStatus.PENDING_UPLOAD
            )
            
            // Guardar la tarea en la base de datos local
            val taskId = taskDao.insert(taskWithStatus).toInt()
            val savedTask = taskDao.getTaskById(taskId)
                ?: return@withContext ResourceState.Error("Error al guardar la tarea")
            
            // Intentar sincronizar si hay conexión
            if (connectivityMonitor.isNetworkAvailable()) {
                when (val result = safeApiCall { apiService.createTask(savedTask) }) {
                    is NetworkResult.Success -> {
                        // Actualizar la tarea con los datos del servidor
                        val serverTask = result.data
                        taskDao.insert(serverTask.withSyncStatus(Task.SyncStatus.SYNCED))
                        
                        return@withContext ResourceState.Success(serverTask)
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Error al sincronizar tarea creada: ${result.message}")
                        return@withContext ResourceState.Success(savedTask, isFromCache = true)
                    }
                    is NetworkResult.Loading -> {
                        return@withContext ResourceState.Success(savedTask, isFromCache = true)
                    }
                }
            } else {
                // Sin conexión, devolver éxito pero con datos locales
                return@withContext ResourceState.Success(savedTask, isFromCache = true)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear tarea", e)
            ResourceState.Error(e.message ?: "Error desconocido")
        }
    }
    
    /**
     * Actualiza una tarea existente.
     *
     * @param task Tarea actualizada.
     * @return Flujo de ResourceState con la tarea actualizada.
     */
    suspend fun updateTask(task: Task): ResourceState<Task> = withContext(Dispatchers.IO) {
        try {
            // Preparar la tarea para actualización local
            val taskWithStatus = task.copy(
                syncStatus = Task.SyncStatus.PENDING_UPDATE,
                pendingChanges = true,
                updatedAt = Date()
            )
            
            // Actualizar la tarea en la base de datos local
            taskDao.update(taskWithStatus)
            val savedTask = taskDao.getTaskById(task.id)
                ?: return@withContext ResourceState.Error("Tarea no encontrada")
            
            // Intentar sincronizar si hay conexión
            if (connectivityMonitor.isNetworkAvailable()) {
                when (val result = safeApiCall { apiService.updateTask(task.id, savedTask) }) {
                    is NetworkResult.Success -> {
                        // Actualizar la tarea con los datos del servidor
                        val serverTask = result.data
                        taskDao.insert(serverTask.withSyncStatus(Task.SyncStatus.SYNCED))
                        
                        return@withContext ResourceState.Success(serverTask)
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Error al sincronizar tarea actualizada: ${result.message}")
                        return@withContext ResourceState.Success(savedTask, isFromCache = true)
                    }
                    is NetworkResult.Loading -> {
                        return@withContext ResourceState.Success(savedTask, isFromCache = true)
                    }
                }
            } else {
                // Sin conexión, devolver éxito pero con datos locales
                return@withContext ResourceState.Success(savedTask, isFromCache = true)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar tarea", e)
            ResourceState.Error(e.message ?: "Error desconocido")
        }
    }
    
    /**
     * Elimina una tarea.
     *
     * @param taskId ID de la tarea a eliminar.
     * @return Flujo de ResourceState con el resultado de la operación.
     */
    suspend fun deleteTask(taskId: Int): ResourceState<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Marcar la tarea como eliminada localmente
            taskDao.markAsDeleted(taskId)
            
            // Intentar sincronizar si hay conexión
            if (connectivityMonitor.isNetworkAvailable()) {
                when (val result = safeApiCall { apiService.deleteTask(taskId) }) {
                    is NetworkResult.Success -> {
                        // La tarea se eliminó correctamente en el servidor, eliminarla físicamente
                        val task = taskDao.getTaskById(taskId)
                        if (task != null) {
                            taskDao.updateSyncStatus(taskId, Task.SyncStatus.SYNCED)
                            taskDao.deleteMarkedTasks()
                        }
                        
                        return@withContext ResourceState.Success(true)
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Error al sincronizar eliminación de tarea: ${result.message}")
                        return@withContext ResourceState.Success(true, isFromCache = true)
                    }
                    is NetworkResult.Loading -> {
                        return@withContext ResourceState.Success(true, isFromCache = true)
                    }
                }
            } else {
                // Sin conexión, devolver éxito pero con datos locales
                return@withContext ResourceState.Success(true, isFromCache = true)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar tarea", e)
            ResourceState.Error(e.message ?: "Error desconocido")
        }
    }
    
    /**
     * Sincroniza todas las tareas y completados pendientes con el servidor.
     *
     * @param lastSyncTime Marca de tiempo de la última sincronización.
     * @return Resultado de la sincronización.
     */
    suspend fun syncWithServer(lastSyncTime: Long): SyncResult = executeSyncOperation {
        val companyId = sessionManager.getCurrentCompanyId()
        
        // 1. Obtener todas las tareas pendientes de sincronización
        val pendingTasks = taskDao.getPendingSyncTasks()
        
        // 2. Obtener todos los completados pendientes de sincronización
        val pendingCompletions = taskCompletionDao.getPendingSyncCompletions()
        
        // 3. Preparar los datos para la sincronización
        val syncData = HashMap<String, Any>()
        
        // 3.1. Añadir tareas a crear/actualizar
        val tasksToUpload = pendingTasks.filter { 
            it.syncStatus == Task.SyncStatus.PENDING_UPLOAD || 
            it.syncStatus == Task.SyncStatus.PENDING_UPDATE 
        }
        syncData["tasks"] = tasksToUpload
        
        // 3.2. Añadir tareas a eliminar
        val tasksToDelete = pendingTasks.filter { 
            it.syncStatus == Task.SyncStatus.PENDING_DELETE 
        }.map { it.id }
        syncData["deleted_tasks"] = tasksToDelete
        
        // 3.3. Añadir completados a sincronizar
        syncData["completions"] = pendingCompletions
        
        // 3.4. Añadir última vez sincronizado para recibir actualizaciones del servidor
        syncData["last_sync"] = lastSyncTime
        
        // 3.5. Añadir ID de empresa
        syncData["company_id"] = companyId
        
        // 4. Realizar la sincronización con el servidor
        val result = safeApiCall {
            apiService.syncTasks(syncData)
        }
        
        when (result) {
            is NetworkResult.Success -> {
                // 5. Procesar la respuesta del servidor
                val responseData = result.data.data
                
                // 5.1. Procesar tareas sincronizadas
                val taskResponse = responseData?.get("tasks") as? SyncResponse<Task>
                if (taskResponse != null) {
                    // Guardar tareas añadidas y actualizadas
                    val tasksToSave = mutableListOf<Task>()
                    
                    taskResponse.added.forEach { task ->
                        tasksToSave.add(task.withSyncStatus(Task.SyncStatus.SYNCED))
                    }
                    
                    taskResponse.updated.forEach { task ->
                        tasksToSave.add(task.withSyncStatus(Task.SyncStatus.SYNCED))
                    }
                    
                    if (tasksToSave.isNotEmpty()) {
                        taskDao.insertAll(tasksToSave)
                    }
                    
                    // Marcar como sincronizadas las tareas que enviamos
                    val syncedIds = tasksToUpload.map { it.id }
                    if (syncedIds.isNotEmpty()) {
                        taskDao.markAsSynced(syncedIds)
                    }
                    
                    // Procesar eliminaciones
                    taskResponse.deleted.forEach { taskId ->
                        taskDao.markAsDeleted(taskId)
                    }
                    
                    // Eliminar físicamente las tareas marcadas como eliminadas y ya sincronizadas
                    taskDao.deleteMarkedTasks()
                }
                
                // 5.2. Procesar completados sincronizados
                val completionResponse = responseData?.get("completions") as? SyncResponse<TaskCompletion>
                if (completionResponse != null) {
                    // Guardar completados añadidos y actualizados
                    val completionsToSave = mutableListOf<TaskCompletion>()
                    
                    completionResponse.added.forEach { completion ->
                        completionsToSave.add(completion.withSyncStatus(TaskCompletion.SyncStatus.SYNCED))
                    }
                    
                    completionResponse.updated.forEach { completion ->
                        completionsToSave.add(completion.withSyncStatus(TaskCompletion.SyncStatus.SYNCED))
                    }
                    
                    if (completionsToSave.isNotEmpty()) {
                        taskCompletionDao.insertAll(completionsToSave)
                    }
                    
                    // Marcar como sincronizados los completados que enviamos
                    val syncedIds = pendingCompletions.map { it.id }
                    if (syncedIds.isNotEmpty()) {
                        taskCompletionDao.markAsSynced(syncedIds)
                    }
                }
                
                SyncResult.Success(
                    addedCount = (taskResponse?.added?.size ?: 0) + (completionResponse?.added?.size ?: 0),
                    updatedCount = (taskResponse?.updated?.size ?: 0) + (completionResponse?.updated?.size ?: 0),
                    deletedCount = taskResponse?.deleted?.size ?: 0
                )
            }
            is NetworkResult.Error -> {
                throw Exception(result.message)
            }
            is NetworkResult.Loading -> {
                throw Exception("Estado de carga inesperado")
            }
        }
    }
    
    /**
     * Obtiene el número de elementos pendientes de sincronización.
     *
     * @return Número de elementos pendientes de sincronización.
     */
    suspend fun getPendingSyncCount(): Int = withContext(Dispatchers.IO) {
        val pendingTasks = taskDao.getPendingSyncTasksCount()
        val pendingCompletions = taskCompletionDao.getPendingSyncCompletionsCount()
        
        pendingTasks + pendingCompletions
    }
}