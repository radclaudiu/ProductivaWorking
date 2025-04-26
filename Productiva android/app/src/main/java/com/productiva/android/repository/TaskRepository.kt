package com.productiva.android.repository

import android.content.Context
import com.productiva.android.api.ApiService
import com.productiva.android.data.dao.TaskCompletionDao
import com.productiva.android.data.dao.TaskDao
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date

/**
 * Repositorio para operaciones relacionadas con tareas
 * Centraliza el acceso a los datos de tareas, ya sea de la API o de la base de datos local
 */
class TaskRepository(
    private val apiService: ApiService,
    private val taskDao: TaskDao,
    private val taskCompletionDao: TaskCompletionDao,
    private val context: Context
) {
    private val sessionManager = SessionManager(context)
    
    /**
     * Obtiene las tareas del servidor y las actualiza en la base de datos local
     */
    suspend fun refreshTasks(token: String, locationId: Int): Result<List<Task>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTasks(
                    token = "Bearer $token",
                    locationId = locationId
                )
                
                if (response.isSuccessful) {
                    val tasks = response.body() ?: emptyList()
                    
                    // Guardar tareas en la base de datos local
                    if (tasks.isNotEmpty()) {
                        taskDao.deleteAllTasks()
                        taskDao.insertAllTasks(tasks)
                    }
                    
                    Result.success(tasks)
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Token expirado o inválido"
                        403 -> "Acceso denegado"
                        404 -> "No se encontraron tareas"
                        500 -> "Error en el servidor"
                        else -> "Error: ${response.code()} - ${response.message()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                // Intentar obtener tareas de la base de datos local
                val localTasks = taskDao.getAllTasksSync()
                if (localTasks.isNotEmpty()) {
                    return@withContext Result.success(localTasks)
                }
                
                Result.failure(Exception("Error de conexión: ${e.message}"))
            }
        }
    }
    
    /**
     * Completa una tarea y la guarda localmente
     */
    suspend fun completeTask(
        taskId: Int,
        notes: String?,
        photoPath: String?,
        signaturePath: String?
    ): Result<TaskCompletion> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = sessionManager.getSelectedUserId()
                if (userId == -1) {
                    return@withContext Result.failure(Exception("No hay usuario seleccionado"))
                }
                
                val task = taskDao.getTaskByIdSync(taskId)
                    ?: return@withContext Result.failure(Exception("Tarea no encontrada"))
                
                val completion = TaskCompletion(
                    id = 0, // Se asignará automáticamente
                    taskId = taskId,
                    userId = userId,
                    completedAt = Date(),
                    notes = notes ?: "",
                    photoPath = photoPath,
                    signaturePath = signaturePath,
                    synced = false
                )
                
                // Guardar en la base de datos local
                val id = taskCompletionDao.insertTaskCompletion(completion)
                
                // Obtener el objeto con el ID asignado
                val savedCompletion = taskCompletionDao.getTaskCompletionByIdSync(id.toInt())
                    ?: return@withContext Result.failure(Exception("Error al guardar el completado"))
                
                Result.success(savedCompletion)
            } catch (e: Exception) {
                Result.failure(Exception("Error al completar la tarea: ${e.message}"))
            }
        }
    }
    
    /**
     * Sincroniza los completados de tareas pendientes con el servidor
     */
    suspend fun syncPendingTaskCompletions(token: String): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val pendingCompletions = taskCompletionDao.getPendingTaskCompletionsSync()
                if (pendingCompletions.isEmpty()) {
                    return@withContext Result.success(0)
                }
                
                var syncedCount = 0
                
                for (completion in pendingCompletions) {
                    // Preparar archivos para subir
                    val photoFile = completion.photoPath?.let { File(it) }
                    val signatureFile = completion.signaturePath?.let { File(it) }
                    
                    val response = apiService.completeTask(
                        token = "Bearer $token",
                        taskId = completion.taskId,
                        userId = completion.userId,
                        notes = completion.notes,
                        photo = photoFile,
                        signature = signatureFile
                    )
                    
                    if (response.isSuccessful) {
                        // Marcar como sincronizado
                        completion.synced = true
                        taskCompletionDao.updateTaskCompletion(completion)
                        syncedCount++
                    }
                }
                
                Result.success(syncedCount)
            } catch (e: Exception) {
                Result.failure(Exception("Error de sincronización: ${e.message}"))
            }
        }
    }
    
    /**
     * Obtiene el historial de completados de una tarea
     */
    suspend fun getTaskCompletionHistory(token: String, taskId: Int): Result<List<TaskCompletion>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTaskCompletions(
                    token = "Bearer $token",
                    taskId = taskId
                )
                
                if (response.isSuccessful) {
                    val completions = response.body() ?: emptyList()
                    
                    // Guardar en la base de datos local
                    if (completions.isNotEmpty()) {
                        taskCompletionDao.insertAllTaskCompletions(completions)
                    }
                    
                    Result.success(completions)
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Token expirado o inválido"
                        403 -> "Acceso denegado"
                        404 -> "No se encontró el historial"
                        500 -> "Error en el servidor"
                        else -> "Error: ${response.code()} - ${response.message()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                // Intentar obtener de la base de datos local
                val localCompletions = taskCompletionDao.getTaskCompletionsByTaskIdSync(taskId)
                if (localCompletions.isNotEmpty()) {
                    return@withContext Result.success(localCompletions)
                }
                
                Result.failure(Exception("Error de conexión: ${e.message}"))
            }
        }
    }
}