package com.productiva.android.repository

import android.util.Log
import com.productiva.android.dao.TaskDao
import com.productiva.android.dao.TaskCompletionDao
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.network.ApiService
import com.productiva.android.network.NetworkResult
import com.productiva.android.network.safeApiCall
import com.productiva.android.utils.ConnectivityMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date

/**
 * Repositorio para gestionar las tareas.
 * Proporciona métodos para acceder y manipular las tareas, incluyendo sincronización con el servidor.
 */
class TaskRepository(
    private val taskDao: TaskDao,
    private val taskCompletionDao: TaskCompletionDao,
    private val apiService: ApiService,
    private val connectivityMonitor: ConnectivityMonitor
) {
    private val TAG = "TaskRepository"
    
    /**
     * Obtiene todas las tareas.
     *
     * @return Flow con el estado del recurso que contiene la lista de tareas.
     */
    fun getAllTasks(): Flow<ResourceState<List<Task>>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Emitir datos locales primero (caché)
            val localTasks = taskDao.getAllTasks()
            localTasks.collect { tasks ->
                emit(ResourceState.CachedData(tasks))
                
                // Si hay conexión a Internet, intentar sincronizar
                if (connectivityMonitor.isNetworkAvailable()) {
                    fetchAndSyncTasks()
                } else {
                    emit(ResourceState.Offline<List<Task>>())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener tareas", e)
            emit(ResourceState.Error("Error al obtener tareas: ${e.message}", e))
        }
    }
    
    /**
     * Obtiene todas las tareas asignadas a un usuario específico.
     *
     * @param userId ID del usuario.
     * @return Flow con el estado del recurso que contiene la lista de tareas del usuario.
     */
    fun getTasksByUserId(userId: Int): Flow<ResourceState<List<Task>>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Emitir datos locales primero (caché)
            val localTasks = taskDao.getTasksByUserId(userId)
            localTasks.collect { tasks ->
                emit(ResourceState.CachedData(tasks))
                
                // Si hay conexión a Internet, intentar sincronizar
                if (connectivityMonitor.isNetworkAvailable()) {
                    fetchAndSyncTasks()
                } else {
                    emit(ResourceState.Offline<List<Task>>())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener tareas del usuario", e)
            emit(ResourceState.Error("Error al obtener tareas del usuario: ${e.message}", e))
        }
    }
    
    /**
     * Obtiene todas las tareas pendientes asignadas a un usuario específico.
     *
     * @param userId ID del usuario.
     * @return Flow con el estado del recurso que contiene la lista de tareas pendientes del usuario.
     */
    fun getPendingTasksByUserId(userId: Int): Flow<ResourceState<List<Task>>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Emitir datos locales primero (caché)
            val localTasks = taskDao.getPendingTasksByUserId(userId)
            localTasks.collect { tasks ->
                emit(ResourceState.CachedData(tasks))
                
                // Si hay conexión a Internet, intentar sincronizar
                if (connectivityMonitor.isNetworkAvailable()) {
                    fetchAndSyncTasks()
                } else {
                    emit(ResourceState.Offline<List<Task>>())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener tareas pendientes del usuario", e)
            emit(ResourceState.Error("Error al obtener tareas pendientes del usuario: ${e.message}", e))
        }
    }
    
    /**
     * Obtiene una tarea por su ID.
     *
     * @param taskId ID de la tarea.
     * @return Flow con el estado del recurso que contiene la tarea.
     */
    fun getTaskById(taskId: Int): Flow<ResourceState<Task>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Emitir datos locales primero (caché)
            val localTask = taskDao.getTaskById(taskId)
            localTask.collect { task ->
                if (task != null) {
                    emit(ResourceState.CachedData(task))
                    
                    // Si hay conexión a Internet, intentar obtener la versión actualizada
                    if (connectivityMonitor.isNetworkAvailable()) {
                        fetchTaskFromServer(taskId)
                    } else {
                        emit(ResourceState.Offline<Task>())
                    }
                } else {
                    // Si no existe localmente, intentar obtenerla del servidor
                    if (connectivityMonitor.isNetworkAvailable()) {
                        fetchTaskFromServer(taskId)
                    } else {
                        emit(ResourceState.Error("Tarea no encontrada y sin conexión a Internet"))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener tarea por ID", e)
            emit(ResourceState.Error("Error al obtener tarea: ${e.message}", e))
        }
    }
    
    /**
     * Completa una tarea.
     *
     * @param taskId ID de la tarea.
     * @param completion Datos de la completación.
     * @return Flow con el estado del recurso que indica el resultado de la operación.
     */
    fun completeTask(taskId: Int, completion: TaskCompletion): Flow<ResourceState<TaskCompletion>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Marcar como completado localmente
            val task = taskDao.getTaskByIdSync(taskId)
            if (task == null) {
                emit(ResourceState.Error("Tarea no encontrada"))
                return@flow
            }
            
            // Actualizar el estado de la tarea
            taskDao.updateTaskStatus(taskId, "completed")
            
            // Guardar la completación en local
            val localCompletion = completion.copy(
                isLocalOnly = true,
                isSynced = false
            )
            taskCompletionDao.insertTaskCompletion(localCompletion)
            
            // Si hay conexión, sincronizar con el servidor
            if (connectivityMonitor.isNetworkAvailable()) {
                val result = safeApiCall {
                    apiService.completeTask(taskId, completion)
                }
                
                when (result) {
                    is NetworkResult.Success -> {
                        // Actualizar en local con los datos del servidor
                        val serverCompletion = result.data
                        val updatedCompletion = serverCompletion.copy(
                            isLocalOnly = false,
                            isSynced = true
                        )
                        taskCompletionDao.insertTaskCompletion(updatedCompletion)
                        emit(ResourceState.Success(updatedCompletion))
                    }
                    is NetworkResult.Error -> {
                        // Mantener la versión local para sincronizar más tarde
                        emit(ResourceState.Error("Error al sincronizar completación: ${result.message}"))
                    }
                    is NetworkResult.Loading -> {
                        // No debería ocurrir, pero por si acaso
                        Log.d(TAG, "Loading state in completeTask network call")
                    }
                }
            } else {
                // Sin conexión, guardar para sincronizar más tarde
                emit(ResourceState.Success(localCompletion))
                emit(ResourceState.Offline<TaskCompletion>())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al completar tarea", e)
            emit(ResourceState.Error("Error al completar tarea: ${e.message}", e))
        }
    }
    
    /**
     * Sincroniza todas las completaciones de tareas pendientes con el servidor.
     *
     * @return Flow con el estado del recurso que indica el resultado de la sincronización.
     */
    fun syncPendingTaskCompletions(): Flow<ResourceState<Int>> = flow {
        emit(ResourceState.Loading())
        
        if (!connectivityMonitor.isNetworkAvailable()) {
            emit(ResourceState.Offline<Int>())
            return@flow
        }
        
        try {
            // Obtener completaciones pendientes de sincronización
            val pendingCompletions = taskCompletionDao.getTaskCompletionsToSync()
            if (pendingCompletions.isEmpty()) {
                emit(ResourceState.Success(0))
                return@flow
            }
            
            // Sincronizar con el servidor
            val result = safeApiCall {
                apiService.syncTaskCompletions(pendingCompletions)
            }
            
            when (result) {
                is NetworkResult.Success -> {
                    val syncResponse = result.data
                    // Actualizar completaciones en la base de datos local
                    taskCompletionDao.syncTaskCompletionsFromServer(syncResponse.added + syncResponse.updated)
                    
                    // Marcar las completaciones como sincronizadas
                    val syncedIds = syncResponse.added.map { it.id } + syncResponse.updated.map { it.id }
                    if (syncedIds.isNotEmpty()) {
                        taskCompletionDao.markTaskCompletionsAsSynced(syncedIds)
                    }
                    
                    emit(ResourceState.Success(syncedIds.size))
                }
                is NetworkResult.Error -> {
                    emit(ResourceState.Error("Error al sincronizar completaciones: ${result.message}"))
                }
                is NetworkResult.Loading -> {
                    // No debería ocurrir, pero por si acaso
                    Log.d(TAG, "Loading state in syncPendingTaskCompletions network call")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar completaciones", e)
            emit(ResourceState.Error("Error al sincronizar completaciones: ${e.message}", e))
        }
    }
    
    /**
     * Obtiene las completaciones de una tarea.
     *
     * @param taskId ID de la tarea.
     * @return Flow con el estado del recurso que contiene la lista de completaciones.
     */
    fun getTaskCompletions(taskId: Int): Flow<ResourceState<List<TaskCompletion>>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Emitir datos locales
            val localCompletions = taskCompletionDao.getTaskCompletionsByTaskId(taskId)
            localCompletions.collect { completions ->
                emit(ResourceState.Success(completions))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener completaciones de tarea", e)
            emit(ResourceState.Error("Error al obtener completaciones: ${e.message}", e))
        }
    }
    
    /**
     * Obtiene las tareas desde el servidor y las sincroniza con la base de datos local.
     */
    private suspend fun fetchAndSyncTasks() {
        if (!connectivityMonitor.isNetworkAvailable()) {
            return
        }
        
        try {
            // Obtener el timestamp de la última sincronización
            val lastSync = findLastTaskSyncTime()
            
            // Obtener tareas actualizadas desde el servidor
            val result = safeApiCall {
                apiService.getTasks(lastSync)
            }
            
            when (result) {
                is NetworkResult.Success -> {
                    val tasks = result.data
                    val currentTime = System.currentTimeMillis()
                    
                    // Sincronizar con la base de datos local
                    taskDao.syncTasksFromServer(tasks, emptyList(), currentTime)
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Error al obtener tareas del servidor: ${result.message}")
                }
                is NetworkResult.Loading -> {
                    // No debería ocurrir, pero por si acaso
                    Log.d(TAG, "Loading state in fetchAndSyncTasks network call")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar tareas", e)
        }
    }
    
    /**
     * Obtiene una tarea desde el servidor.
     *
     * @param taskId ID de la tarea.
     */
    private suspend fun fetchTaskFromServer(taskId: Int) {
        if (!connectivityMonitor.isNetworkAvailable()) {
            return
        }
        
        try {
            // Implementar cuando haya un endpoint específico para obtener una tarea por ID
            // De momento, sincronizamos todas para obtener la actualizada
            fetchAndSyncTasks()
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener tarea del servidor", e)
        }
    }
    
    /**
     * Encuentra el timestamp de la última sincronización de tareas.
     *
     * @return Timestamp de la última sincronización.
     */
    private suspend fun findLastTaskSyncTime(): Long {
        try {
            // Aquí se podría implementar una lógica más sofisticada para guardar y recuperar
            // el timestamp de la última sincronización exitosa
            return 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener timestamp de última sincronización", e)
            return 0L
        }
    }
}