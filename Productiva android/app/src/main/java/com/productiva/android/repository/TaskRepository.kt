package com.productiva.android.repository

import android.util.Log
import com.productiva.android.dao.TaskDao
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.Base64

/**
 * Repositorio para gestionar tareas.
 */
class TaskRepository(
    private val taskDao: TaskDao,
    private val apiService: ApiService
) {
    private val TAG = "TaskRepository"
    
    /**
     * Obtiene todas las tareas.
     */
    fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
    }
    
    /**
     * Obtiene todas las tareas asignadas a un usuario.
     */
    fun getTasksAssignedToUser(userId: Int): Flow<List<Task>> {
        return taskDao.getTasksByAssignedUser(userId)
    }
    
    /**
     * Obtiene una tarea por su ID.
     */
    fun getTaskById(taskId: Int): Flow<Task?> {
        return taskDao.getTaskById(taskId)
    }
    
    /**
     * Obtiene una tarea por su ID (versión sincrónica).
     */
    suspend fun getTaskByIdSync(taskId: Int): Task? {
        return withContext(Dispatchers.IO) {
            taskDao.getTaskByIdSync(taskId)
        }
    }
    
    /**
     * Obtiene tareas por estado.
     */
    fun getTasksByStatus(status: String): Flow<List<Task>> {
        return taskDao.getTasksByStatus(status)
    }
    
    /**
     * Obtiene tareas por estado para un usuario específico.
     */
    fun getTasksByStatusForUser(userId: Int, status: String): Flow<List<Task>> {
        return taskDao.getTasksByStatusAndUser(status, userId)
    }
    
    /**
     * Sincroniza tareas con el servidor.
     */
    suspend fun syncTasks(userId: Int? = null): Flow<ResourceState<List<Task>>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // Intentar sincronizar cualquier tarea completada pendiente primero
            syncPendingTaskCompletions()
            
            // Obtener tareas del servidor
            val response = if (userId != null) {
                apiService.getTasksAssignedToUser(userId)
            } else {
                apiService.getAllTasks()
            }
            
            if (response.isSuccessful) {
                val serverTasks = response.body() ?: emptyList()
                
                // Obtener tareas locales que necesitan sincronización
                val localTasks = taskDao.getTasksNeedingSyncSync()
                
                // Actualizar tareas locales con datos del servidor, preservando cambios locales pendientes
                withContext(Dispatchers.IO) {
                    // Filtrar solo tareas que no están pendientes de sincronización
                    val tasksToUpdate = serverTasks.filter { serverTask ->
                        localTasks.none { it.id == serverTask.id && it.needsSync }
                    }
                    
                    // Insertar o actualizar tareas
                    taskDao.upsertTasks(tasksToUpdate)
                    
                    // También incluir las tareas locales que necesitan sincronización
                    // en la lista de tareas resultante
                    val resultTasks = ArrayList<Task>(tasksToUpdate)
                    resultTasks.addAll(localTasks)
                    
                    emit(ResourceState.Success(resultTasks))
                }
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Sesión expirada"
                    403 -> "Sin permiso para acceder a las tareas"
                    404 -> "No se encontraron tareas"
                    else -> "Error del servidor: ${response.code()}"
                }
                
                emit(ResourceState.Error(errorMessage))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error de conexión al sincronizar tareas", e)
            
            // En caso de error de conexión, devolver las tareas locales
            val localTasks = taskDao.getAllTasksSync()
            emit(ResourceState.Success(localTasks, "Usando datos locales"))
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar tareas", e)
            emit(ResourceState.Error("Error al sincronizar tareas: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Completa una tarea localmente.
     */
    suspend fun completeTaskLocally(completion: TaskCompletion): Flow<ResourceState<Task>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // Buscar la tarea
            val task = taskDao.getTaskByIdSync(completion.taskId)
            
            if (task == null) {
                emit(ResourceState.Error("Tarea no encontrada"))
                return@flow
            }
            
            // Registrar el completado en la base de datos local
            taskDao.insertTaskCompletion(completion)
            
            // Actualizar el estado de la tarea según el tipo de completado
            val updatedTask = when (completion.status) {
                "COMPLETED" -> task.markAsCompleted(
                    userId = completion.userId,
                    userName = "Usuario Local", // Esto se actualizará en la próxima sincronización
                    notes = completion.notes,
                    hasSignature = completion.hasSignature,
                    hasPhoto = completion.hasPhoto,
                    localSignaturePath = completion.localSignaturePath,
                    localPhotoPath = completion.localPhotoPath
                )
                "CANCELLED" -> task.markAsCancelled(
                    userId = completion.userId,
                    userName = "Usuario Local", // Esto se actualizará en la próxima sincronización
                    notes = completion.notes
                )
                else -> task.markForSync()
            }
            
            // Guardar la tarea actualizada
            taskDao.updateTask(updatedTask)
            
            emit(ResourceState.Success(updatedTask))
        } catch (e: Exception) {
            Log.e(TAG, "Error al completar tarea localmente", e)
            emit(ResourceState.Error("Error al completar tarea: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Sincroniza completados de tareas pendientes con el servidor.
     */
    suspend fun syncPendingTaskCompletions(): Flow<ResourceState<Int>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // Obtener completados pendientes
            val pendingCompletions = taskDao.getPendingTaskCompletionsSync()
            
            if (pendingCompletions.isEmpty()) {
                emit(ResourceState.Success(0, "No hay completados pendientes"))
                return@flow
            }
            
            var successCount = 0
            
            for (completion in pendingCompletions) {
                try {
                    // Preparar datos para enviar al servidor
                    val completionData = mutableMapOf(
                        "task_id" to completion.taskId,
                        "status" to completion.status,
                        "notes" to (completion.notes ?: ""),
                        "completion_date" to completion.completionDate
                    )
                    
                    // Procesar firma si existe
                    if (completion.hasSignature) {
                        val signatureData = if (completion.signatureData != null) {
                            completion.signatureData
                        } else if (completion.localSignaturePath != null) {
                            // Convertir archivo a Base64
                            val file = File(completion.localSignaturePath)
                            if (file.exists()) {
                                val bytes = file.readBytes()
                                Base64.getEncoder().encodeToString(bytes)
                            } else {
                                null
                            }
                        } else {
                            null
                        }
                        
                        if (signatureData != null) {
                            completionData["signature_data"] = signatureData
                        }
                    }
                    
                    // Procesar foto si existe
                    if (completion.hasPhoto) {
                        val photoData = if (completion.photoData != null) {
                            completion.photoData
                        } else if (completion.localPhotoPath != null) {
                            // Convertir archivo a Base64
                            val file = File(completion.localPhotoPath)
                            if (file.exists()) {
                                val bytes = file.readBytes()
                                Base64.getEncoder().encodeToString(bytes)
                            } else {
                                null
                            }
                        } else {
                            null
                        }
                        
                        if (photoData != null) {
                            completionData["photo_data"] = photoData
                        }
                    }
                    
                    // Enviar completado al servidor
                    val response = apiService.completeTask(completion.taskId, completionData)
                    
                    if (response.isSuccessful) {
                        // Marcar como sincronizado
                        taskDao.updateTaskCompletion(completion.markAsSynced())
                        
                        // Actualizar tarea local con datos del servidor si está disponible
                        response.body()?.let { serverTask ->
                            val localTask = taskDao.getTaskByIdSync(completion.taskId)
                            if (localTask != null) {
                                val updatedTask = localTask.updateFromServer(serverTask)
                                taskDao.updateTask(updatedTask)
                            }
                        }
                        
                        successCount++
                    } else {
                        Log.e(TAG, "Error al sincronizar completado de tarea: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al procesar completado de tarea", e)
                }
            }
            
            emit(ResourceState.Success(successCount, "Completados sincronizados: $successCount/${pendingCompletions.size}"))
        } catch (e: IOException) {
            Log.e(TAG, "Error de conexión al sincronizar completados", e)
            emit(ResourceState.Error("Error de conexión: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar completados", e)
            emit(ResourceState.Error("Error al sincronizar completados: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
}