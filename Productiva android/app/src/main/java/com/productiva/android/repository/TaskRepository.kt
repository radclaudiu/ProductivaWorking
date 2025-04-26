package com.productiva.android.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.productiva.android.api.ApiClient
import com.productiva.android.api.ApiResponse
import com.productiva.android.database.AppDatabase
import com.productiva.android.database.TaskDao
import com.productiva.android.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.Date

/**
 * Repositorio para gestionar tareas
 */
class TaskRepository(context: Context) {
    
    private val apiClient = ApiClient.getInstance(context)
    private val taskDao: TaskDao
    
    init {
        val database = AppDatabase.getInstance(context)
        taskDao = database.taskDao()
    }
    
    /**
     * Obtiene todas las tareas de la base de datos local
     */
    fun getAllTasks(): LiveData<List<Task>> {
        return taskDao.getAllTasks()
    }
    
    /**
     * Obtiene una tarea por su ID de la base de datos local
     */
    suspend fun getTaskById(taskId: Int): Task? {
        return withContext(Dispatchers.IO) {
            taskDao.getTaskById(taskId)
        }
    }
    
    /**
     * Obtiene tareas por usuario
     */
    fun getTasksByUser(userId: Int): LiveData<List<Task>> {
        return taskDao.getTasksByUser(userId)
    }
    
    /**
     * Obtiene tareas por estado
     */
    fun getTasksByStatus(status: String): LiveData<List<Task>> {
        return taskDao.getTasksByStatus(status)
    }
    
    /**
     * Obtiene tareas por usuario y estado
     */
    fun getTasksByUserAndStatus(userId: Int, status: String): LiveData<List<Task>> {
        return taskDao.getTasksByUserAndStatus(userId, status)
    }
    
    /**
     * Busca tareas por título o descripción
     */
    fun searchTasks(query: String): LiveData<List<Task>> {
        return taskDao.searchTasks(query)
    }
    
    /**
     * Inserta una tarea en la base de datos local
     */
    suspend fun insertTask(task: Task): Long {
        return withContext(Dispatchers.IO) {
            taskDao.insert(task)
        }
    }
    
    /**
     * Actualiza una tarea en la base de datos local
     */
    suspend fun updateTask(task: Task): Int {
        return withContext(Dispatchers.IO) {
            taskDao.update(task)
        }
    }
    
    /**
     * Cambia el estado de una tarea
     */
    suspend fun updateTaskStatus(taskId: Int, status: String): Result<Task> {
        return withContext(Dispatchers.IO) {
            try {
                // Actualizar en la API
                val statusData = mapOf("status" to status)
                val response = apiClient.apiService.updateTaskStatus(taskId, statusData)
                
                if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                    // Actualizar localmente
                    taskDao.updateTaskStatus(taskId, status, Date())
                    
                    // Obtener la tarea actualizada
                    val updatedTask = response.body()?.data
                    if (updatedTask != null) {
                        taskDao.insert(updatedTask)
                        return@withContext Result.success(updatedTask)
                    }
                }
                
                Result.failure(Exception(response.message() ?: "Error al actualizar estado"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Elimina una tarea por su ID
     */
    suspend fun deleteTask(taskId: Int): Int {
        return withContext(Dispatchers.IO) {
            taskDao.deleteTaskById(taskId)
        }
    }
    
    /**
     * Obtiene una tarea por su ID desde la API y la guarda localmente
     */
    suspend fun fetchTaskById(taskId: Int): Result<Task> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiClient.apiService.getTaskById(taskId)
                handleTaskResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Obtiene tareas desde la API y las guarda en la base de datos local
     */
    suspend fun fetchTasks(
        userId: Int? = null,
        status: String? = null,
        companyId: Int? = null,
        locationId: Int? = null
    ): Result<List<Task>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiClient.apiService.getTasks(
                    userId = userId,
                    status = status,
                    companyId = companyId,
                    locationId = locationId
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { tasks ->
                        taskDao.insertAll(tasks)
                        return@withContext Result.success(tasks)
                    }
                }
                
                Result.failure(Exception(response.message() ?: "Error al obtener tareas"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Crea una nueva tarea
     */
    suspend fun createTask(task: Task): Result<Task> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiClient.apiService.createTask(task)
                handleTaskResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Actualiza una tarea existente
     */
    suspend fun updateTaskOnServer(task: Task): Result<Task> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiClient.apiService.updateTask(task.id, task)
                handleTaskResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Sincroniza tareas actualizadas desde una fecha específica
     */
    suspend fun syncTasksUpdatedSince(since: Date, userId: Int? = null): Result<List<Task>> {
        return withContext(Dispatchers.IO) {
            try {
                val sinceStr = since.time.toString()
                val response = apiClient.apiService.syncTasks(sinceStr, userId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { tasks ->
                        taskDao.insertAll(tasks)
                        return@withContext Result.success(tasks)
                    }
                }
                
                Result.failure(Exception(response.message() ?: "Error al sincronizar tareas"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Obtiene tareas pendientes de sincronización
     */
    suspend fun getUnsyncedTasks(): List<Task> {
        return withContext(Dispatchers.IO) {
            taskDao.getUnsynced()
        }
    }
    
    /**
     * Gestiona la respuesta de la API para operaciones con tareas
     */
    private fun handleTaskResponse(response: Response<ApiResponse<Task>>): Result<Task> {
        if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                // Guardar la tarea en la base de datos local
                taskDao.insert(apiResponse.data)
                
                return Result.success(apiResponse.data)
            }
            return Result.failure(Exception(apiResponse?.message ?: "Respuesta sin datos"))
        }
        return Result.failure(Exception(response.message() ?: "Error desconocido"))
    }
    
    /**
     * Carga tareas desde API con indicador de carga
     */
    fun loadTasksWithStatusFlow(
        userId: Int? = null,
        status: String? = null,
        companyId: Int? = null
    ): Flow<Resource<List<Task>>> = flow {
        emit(Resource.Loading())
        
        try {
            // Emitir datos locales primero
            val localTasks = if (userId != null && status != null) {
                taskDao.getTasksByUserAndStatus(userId, status)
            } else if (userId != null) {
                taskDao.getTasksByUser(userId)
            } else if (status != null) {
                taskDao.getTasksByStatus(status)
            } else {
                taskDao.getAllTasks()
            }
            
            emit(Resource.Loading(data = localTasks.value))
            
            // Cargar desde API
            val response = apiClient.apiService.getTasks(
                userId = userId,
                status = status,
                companyId = companyId
            )
            
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { tasks ->
                    taskDao.insertAll(tasks)
                    emit(Resource.Success(tasks))
                } ?: emit(Resource.Error("No se encontraron tareas"))
            } else {
                emit(Resource.Error(response.message() ?: "Error al cargar tareas"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error desconocido"))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Clase para representar el estado de un recurso
     */
    sealed class Resource<T>(
        val data: T? = null,
        val message: String? = null
    ) {
        class Success<T>(data: T) : Resource<T>(data)
        class Loading<T>(data: T? = null) : Resource<T>(data)
        class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    }
}