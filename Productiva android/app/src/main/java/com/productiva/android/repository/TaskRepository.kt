package com.productiva.android.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.productiva.android.api.ApiClient
import com.productiva.android.api.ApiResponse
import com.productiva.android.dao.TaskDao
import com.productiva.android.database.AppDatabase
import com.productiva.android.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.Date

/**
 * Repositorio para manejar operaciones relacionadas con tareas
 */
class TaskRepository(private val context: Context) {
    
    private val taskDao: TaskDao = AppDatabase.getDatabase(context).taskDao()
    private val apiClient = ApiClient.getInstance(context)
    
    /**
     * Obtiene todas las tareas como LiveData desde la base de datos local
     */
    fun getAllTasks(): LiveData<List<Task>> {
        return taskDao.getAllTasks()
    }
    
    /**
     * Obtiene las tareas por usuario como LiveData desde la base de datos local
     */
    fun getTasksByUser(userId: Int): LiveData<List<Task>> {
        return taskDao.getTasksByUser(userId)
    }
    
    /**
     * Obtiene las tareas por estado como LiveData desde la base de datos local
     */
    fun getTasksByStatus(status: String): LiveData<List<Task>> {
        return taskDao.getTasksByStatus(status)
    }
    
    /**
     * Obtiene las tareas por usuario y estado como LiveData desde la base de datos local
     */
    fun getTasksByUserAndStatus(userId: Int, status: String): LiveData<List<Task>> {
        return taskDao.getTasksByUserAndStatus(userId, status)
    }
    
    /**
     * Obtiene las tareas por ubicación como LiveData desde la base de datos local
     */
    fun getTasksByLocation(locationId: Int): LiveData<List<Task>> {
        return taskDao.getTasksByLocation(locationId)
    }
    
    /**
     * Obtiene las tareas por rango de fechas como LiveData desde la base de datos local
     */
    fun getTasksByDateRange(startDate: Date, endDate: Date): LiveData<List<Task>> {
        return taskDao.getTasksByDateRange(startDate.time, endDate.time)
    }
    
    /**
     * Obtiene una tarea por su ID desde la base de datos local
     */
    suspend fun getTaskById(taskId: Int): Task? = withContext(Dispatchers.IO) {
        return@withContext taskDao.getTaskById(taskId)
    }
    
    /**
     * Obtiene la cantidad de tareas pendientes para un usuario
     */
    suspend fun getPendingTaskCount(userId: Int): Int = withContext(Dispatchers.IO) {
        return@withContext taskDao.getPendingTaskCount(userId)
    }
    
    /**
     * Obtiene las tareas asignadas a un usuario desde el servidor
     */
    suspend fun fetchTasksByUser(userId: Int): Result<List<Task>> = withContext(Dispatchers.IO) {
        try {
            val response: Response<ApiResponse<List<Task>>> = apiClient.apiService.getTasksByUser(userId)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    // Guarda las tareas en la base de datos local
                    taskDao.insertAll(apiResponse.data)
                    return@withContext Result.success(apiResponse.data)
                } else {
                    return@withContext Result.failure(Exception(apiResponse?.message ?: "Error en la respuesta"))
                }
            } else {
                return@withContext Result.failure(Exception("Error al obtener tareas: ${response.code()}"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Obtiene las tareas por compañía desde el servidor
     */
    suspend fun fetchTasksByCompany(companyId: Int): Result<List<Task>> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.apiService.getTasksByCompany(companyId)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    // Guarda las tareas en la base de datos local
                    taskDao.insertAll(apiResponse.data)
                    return@withContext Result.success(apiResponse.data)
                } else {
                    return@withContext Result.failure(Exception(apiResponse?.message ?: "Error en la respuesta"))
                }
            } else {
                return@withContext Result.failure(Exception("Error al obtener tareas: ${response.code()}"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Obtiene las tareas por ubicación desde el servidor
     */
    suspend fun fetchTasksByLocation(locationId: Int): Result<List<Task>> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.apiService.getTasksByLocation(locationId)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    // Guarda las tareas en la base de datos local
                    taskDao.insertAll(apiResponse.data)
                    return@withContext Result.success(apiResponse.data)
                } else {
                    return@withContext Result.failure(Exception(apiResponse?.message ?: "Error en la respuesta"))
                }
            } else {
                return@withContext Result.failure(Exception("Error al obtener tareas: ${response.code()}"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Actualiza una tarea en el servidor y localmente
     */
    suspend fun updateTask(task: Task): Result<Task> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.apiService.updateTask(task.id, task)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    // Actualiza la tarea en la base de datos local
                    taskDao.update(apiResponse.data)
                    return@withContext Result.success(apiResponse.data)
                } else {
                    return@withContext Result.failure(Exception(apiResponse?.message ?: "Error en la respuesta"))
                }
            } else {
                return@withContext Result.failure(Exception("Error al actualizar tarea: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Si hay un error de conexión, actualizamos localmente y marcamos para sincronizar después
            val taskWithSyncFlag = task.copy(isSynced = false, lastSync = System.currentTimeMillis())
            taskDao.update(taskWithSyncFlag)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Crea una nueva tarea en el servidor y localmente
     */
    suspend fun createTask(task: Task): Result<Task> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.apiService.createTask(task)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    // Guarda la tarea en la base de datos local
                    taskDao.insert(apiResponse.data)
                    return@withContext Result.success(apiResponse.data)
                } else {
                    return@withContext Result.failure(Exception(apiResponse?.message ?: "Error en la respuesta"))
                }
            } else {
                return@withContext Result.failure(Exception("Error al crear tarea: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Si hay un error de conexión, guardamos localmente y marcamos para sincronizar después
            val taskWithSyncFlag = task.copy(isSynced = false, lastSync = System.currentTimeMillis())
            val id = taskDao.insert(taskWithSyncFlag)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Elimina una tarea del servidor y localmente
     */
    suspend fun deleteTask(taskId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.apiService.deleteTask(taskId)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    // Elimina la tarea de la base de datos local
                    taskDao.deleteTaskById(taskId)
                    return@withContext Result.success(true)
                } else {
                    return@withContext Result.failure(Exception(apiResponse?.message ?: "Error en la respuesta"))
                }
            } else {
                return@withContext Result.failure(Exception("Error al eliminar tarea: ${response.code()}"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Sincroniza las tareas con el servidor
     */
    suspend fun syncTasks(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // Obtener la última sincronización
            val lastSync = System.currentTimeMillis() - (24 * 60 * 60 * 1000) // 24 horas por defecto
            
            val response = apiClient.apiService.syncTasks(lastSync)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    // Guarda las tareas en la base de datos local
                    taskDao.insertAll(apiResponse.data)
                    
                    // Sincroniza las tareas modificadas localmente
                    val tasksToSync = taskDao.getTasksToSync(lastSync)
                    // Aquí deberías implementar la lógica para enviar las tareas al servidor
                    
                    return@withContext Result.success(apiResponse.data.size)
                } else {
                    return@withContext Result.failure(Exception(apiResponse?.message ?: "Error en la respuesta"))
                }
            } else {
                return@withContext Result.failure(Exception("Error al sincronizar tareas: ${response.code()}"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
}