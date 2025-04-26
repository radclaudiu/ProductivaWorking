package com.productiva.android.repository

import android.util.Log
import com.productiva.android.database.dao.TaskDao
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.network.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

/**
 * Repositorio para gestionar tareas, sincronizadas con el servidor.
 */
class TaskRepository(
    private val taskDao: TaskDao,
    private val apiService: ApiService
) {
    private val TAG = "TaskRepository"
    
    /**
     * Obtiene todas las tareas almacenadas localmente.
     * 
     * @return Flow con la lista de tareas.
     */
    fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
    }
    
    /**
     * Obtiene una tarea por su ID.
     * 
     * @param taskId ID de la tarea.
     * @return Flow con la tarea o null si no existe.
     */
    fun getTaskById(taskId: Int): Flow<Task?> {
        return taskDao.getTaskById(taskId)
    }
    
    /**
     * Obtiene tareas por estado.
     * 
     * @param status Estado de las tareas a obtener.
     * @return Flow con la lista de tareas.
     */
    fun getTasksByStatus(status: String): Flow<List<Task>> {
        return taskDao.getTasksByStatus(status)
    }
    
    /**
     * Obtiene tareas asignadas a un usuario.
     * 
     * @param userId ID del usuario.
     * @return Flow con la lista de tareas.
     */
    fun getTasksAssignedToUser(userId: Int): Flow<List<Task>> {
        return taskDao.getTasksAssignedToUser(userId)
    }
    
    /**
     * Obtiene tareas pendientes asignadas a un usuario.
     * 
     * @param userId ID del usuario.
     * @return Flow con la lista de tareas.
     */
    fun getPendingTasksForUser(userId: Int): Flow<List<Task>> {
        return taskDao.getPendingTasksForUser(userId)
    }
    
    /**
     * Sincroniza las tareas con el servidor.
     * 
     * @param userId ID del usuario actual para filtrar tareas asignadas.
     * @return Flow con el estado de la operación.
     */
    fun syncTasks(userId: Int? = null): Flow<ResourceState<List<Task>>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // Obtener tareas del servidor
            val response = if (userId != null) {
                apiService.getTasks(userId = userId)
            } else {
                apiService.getTasks()
            }
            
            if (response.isSuccessful) {
                val tasks = response.body()
                
                if (tasks != null) {
                    // Guardar tareas en la base de datos local
                    taskDao.deleteAllTasks() // Limpiar tareas antiguas
                    taskDao.insertTasks(tasks)
                    
                    Log.d(TAG, "Tareas sincronizadas correctamente: ${tasks.size}")
                    emit(ResourceState.Success(tasks))
                } else {
                    emit(ResourceState.Error("Respuesta vacía del servidor"))
                }
            } else {
                emit(ResourceState.Error("Error al obtener tareas: ${response.code()}"))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error de red al sincronizar tareas", e)
            emit(ResourceState.Error("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar tareas", e)
            emit(ResourceState.Error("Error: ${e.message}"))
        }
    }
    
    /**
     * Completa una tarea en local y la marca para sincronización.
     * 
     * @param taskCompletion Información de completado de la tarea.
     * @return Flow con el estado de la operación.
     */
    fun completeTaskLocally(taskCompletion: TaskCompletion): Flow<ResourceState<TaskCompletion>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // Verificar que la tarea existe
            val task = taskDao.getTaskByIdSync(taskCompletion.taskId)
            
            if (task == null) {
                emit(ResourceState.Error("Tarea no encontrada"))
                return@flow
            }
            
            // Actualizar el estado de la tarea localmente
            if (taskCompletion.status == "COMPLETED") {
                taskDao.markTaskAsCompleted(taskCompletion.taskId, taskCompletion.completedAt)
            } else if (taskCompletion.status == "CANCELLED") {
                taskDao.markTaskAsCancelled(taskCompletion.taskId, taskCompletion.completedAt)
            }
            
            // Guardar el completado de la tarea
            taskDao.insertTaskCompletion(taskCompletion)
            
            // Actualizar notas de completado si existen
            if (taskCompletion.notes != null) {
                taskDao.updateCompletionNotes(taskCompletion.taskId, taskCompletion.notes)
            }
            
            Log.d(TAG, "Tarea ${taskCompletion.taskId} completada localmente con estado: ${taskCompletion.status}")
            emit(ResourceState.Success(taskCompletion))
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al completar tarea localmente", e)
            emit(ResourceState.Error("Error: ${e.message}"))
        }
    }
    
    /**
     * Sincroniza los completados de tareas pendientes con el servidor.
     * 
     * @return Flow con el estado de la operación.
     */
    fun syncPendingTaskCompletions(): Flow<ResourceState<Int>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // Obtener completados pendientes de sincronización
            val pendingCompletions = taskDao.getTaskCompletionsForSync()
            
            if (pendingCompletions.isEmpty()) {
                emit(ResourceState.Success(0))
                return@flow
            }
            
            var syncedCount = 0
            val failedTaskIds = mutableListOf<Int>()
            
            for (completion in pendingCompletions) {
                try {
                    // Enviar completado al servidor
                    val response = apiService.completeTask(completion.taskId, completion)
                    
                    if (response.isSuccessful) {
                        // Marcar como sincronizado
                        taskDao.updateTaskCompletionSyncStatus(completion.taskId, false, null)
                        taskDao.markForSync(completion.taskId, false)
                        syncedCount++
                        
                        // Sincronizar firmas y fotos si existen
                        if (completion.localSignaturePath != null) {
                            syncSignature(completion.taskId, completion.localSignaturePath)
                        }
                        
                        if (completion.localPhotoPath != null) {
                            syncPhoto(completion.taskId, completion.localPhotoPath)
                        }
                    } else {
                        val errorMsg = "Error al sincronizar completado: ${response.code()}"
                        taskDao.updateTaskCompletionSyncStatus(completion.taskId, true, errorMsg)
                        failedTaskIds.add(completion.taskId)
                        Log.e(TAG, errorMsg)
                    }
                } catch (e: Exception) {
                    val errorMsg = "Error: ${e.message}"
                    taskDao.updateTaskCompletionSyncStatus(completion.taskId, true, errorMsg)
                    failedTaskIds.add(completion.taskId)
                    Log.e(TAG, "Error al sincronizar completado de tarea ${completion.taskId}", e)
                }
            }
            
            if (syncedCount == pendingCompletions.size) {
                emit(ResourceState.Success(syncedCount))
            } else {
                val errorMsg = "Algunos completados no se pudieron sincronizar: $failedTaskIds"
                emit(ResourceState.Error(errorMsg, syncedCount))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error de red al sincronizar completados", e)
            emit(ResourceState.Error("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar completados", e)
            emit(ResourceState.Error("Error: ${e.message}"))
        }
    }
    
    /**
     * Sincroniza una firma con el servidor.
     * 
     * @param taskId ID de la tarea.
     * @param localSignaturePath Ruta local al archivo de firma.
     * @return true si se sincronizó correctamente, false en caso contrario.
     */
    private suspend fun syncSignature(taskId: Int, localSignaturePath: String): Boolean {
        try {
            val signatureFile = File(localSignaturePath)
            if (!signatureFile.exists()) {
                Log.e(TAG, "Archivo de firma no encontrado: $localSignaturePath")
                return false
            }
            
            val requestFile = signatureFile.asRequestBody("image/png".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("signature", signatureFile.name, requestFile)
            
            val response = apiService.uploadSignature(taskId, body)
            
            if (response.isSuccessful) {
                Log.d(TAG, "Firma sincronizada correctamente para tarea $taskId")
                return true
            } else {
                Log.e(TAG, "Error al sincronizar firma: ${response.code()}")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar firma", e)
            return false
        }
    }
    
    /**
     * Sincroniza una foto con el servidor.
     * 
     * @param taskId ID de la tarea.
     * @param localPhotoPath Ruta local al archivo de foto.
     * @return true si se sincronizó correctamente, false en caso contrario.
     */
    private suspend fun syncPhoto(taskId: Int, localPhotoPath: String): Boolean {
        try {
            val photoFile = File(localPhotoPath)
            if (!photoFile.exists()) {
                Log.e(TAG, "Archivo de foto no encontrado: $localPhotoPath")
                return false
            }
            
            val requestFile = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("photo", photoFile.name, requestFile)
            
            val response = apiService.uploadTaskPhoto(taskId, body)
            
            if (response.isSuccessful) {
                Log.d(TAG, "Foto sincronizada correctamente para tarea $taskId")
                return true
            } else {
                Log.e(TAG, "Error al sincronizar foto: ${response.code()}")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar foto", e)
            return false
        }
    }
    
    /**
     * Guarda una firma localmente y la marca para sincronización.
     * 
     * @param taskId ID de la tarea.
     * @param signaturePath Ruta local al archivo de firma.
     * @return Flow con el estado de la operación.
     */
    fun saveSignatureLocally(taskId: Int, signaturePath: String): Flow<ResourceState<String>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // Verificar que la tarea existe
            val task = taskDao.getTaskByIdSync(taskId)
            
            if (task == null) {
                emit(ResourceState.Error("Tarea no encontrada"))
                return@flow
            }
            
            // Actualizar la ruta de la firma
            taskDao.updateSignaturePath(taskId, signaturePath)
            
            // Actualizar el completado de la tarea si existe
            val completion = taskDao.getTaskCompletionByTaskIdSync(taskId)
            if (completion != null) {
                val updatedCompletion = completion.copy(localSignaturePath = signaturePath, needsSync = true)
                taskDao.insertTaskCompletion(updatedCompletion)
            }
            
            Log.d(TAG, "Firma guardada localmente para tarea $taskId: $signaturePath")
            emit(ResourceState.Success(signaturePath))
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar firma localmente", e)
            emit(ResourceState.Error("Error: ${e.message}"))
        }
    }
    
    /**
     * Guarda una foto localmente y la marca para sincronización.
     * 
     * @param taskId ID de la tarea.
     * @param photoPath Ruta local al archivo de foto.
     * @return Flow con el estado de la operación.
     */
    fun savePhotoLocally(taskId: Int, photoPath: String): Flow<ResourceState<String>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // Verificar que la tarea existe
            val task = taskDao.getTaskByIdSync(taskId)
            
            if (task == null) {
                emit(ResourceState.Error("Tarea no encontrada"))
                return@flow
            }
            
            // Actualizar la ruta de la foto
            taskDao.updatePhotoPath(taskId, photoPath)
            
            // Actualizar el completado de la tarea si existe
            val completion = taskDao.getTaskCompletionByTaskIdSync(taskId)
            if (completion != null) {
                val updatedCompletion = completion.copy(localPhotoPath = photoPath, needsSync = true)
                taskDao.insertTaskCompletion(updatedCompletion)
            }
            
            Log.d(TAG, "Foto guardada localmente para tarea $taskId: $photoPath")
            emit(ResourceState.Success(photoPath))
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar foto localmente", e)
            emit(ResourceState.Error("Error: ${e.message}"))
        }
    }
}