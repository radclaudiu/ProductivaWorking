package com.productiva.android.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import com.productiva.android.database.dao.TaskDao
import com.productiva.android.database.dao.TaskCompletionDao
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.network.ApiService
import com.productiva.android.utils.DateConverters
import com.productiva.android.utils.FileUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.Date

/**
 * Repositorio para gestionar tareas tanto en la base de datos local como en el servidor.
 */
class TaskRepository(
    private val context: Context,
    private val taskDao: TaskDao,
    private val taskCompletionDao: TaskCompletionDao,
    private val apiService: ApiService
) {
    private val TAG = "TaskRepository"
    
    /**
     * Obtiene todas las tareas desde la base de datos local.
     */
    fun getAllTasks(): LiveData<List<Task>> {
        return taskDao.getAllTasks()
    }
    
    /**
     * Obtiene tareas por estado.
     */
    fun getTasksByStatus(status: String): LiveData<List<Task>> {
        return taskDao.getTasksByStatus(status)
    }
    
    /**
     * Obtiene tareas por compañía.
     */
    fun getTasksByCompany(companyId: Int): LiveData<List<Task>> {
        return taskDao.getTasksByCompany(companyId)
    }
    
    /**
     * Obtiene tareas asignadas a un usuario.
     */
    fun getTasksAssignedToUser(userId: Int): LiveData<List<Task>> {
        return taskDao.getTasksAssignedToUser(userId)
    }
    
    /**
     * Obtiene tareas asignadas a un usuario con un estado específico.
     */
    fun getTasksAssignedToUserByStatus(userId: Int, status: String): LiveData<List<Task>> {
        return taskDao.getTasksAssignedToUserByStatus(userId, status)
    }
    
    /**
     * Obtiene una tarea por su ID.
     */
    suspend fun getTaskById(taskId: Int): Task? {
        return withContext(Dispatchers.IO) {
            taskDao.getTaskById(taskId)
        }
    }
    
    /**
     * Actualiza el estado de una tarea.
     */
    suspend fun updateTaskStatus(taskId: Int, status: String): Flow<ResourceState<Task>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Intentar actualizar en el servidor
            val response = apiService.updateTaskStatus(taskId, status)
            
            if (response.isSuccessful) {
                val updatedTask = response.body()
                
                if (updatedTask != null) {
                    // Actualizar en base de datos local
                    withContext(Dispatchers.IO) {
                        taskDao.update(updatedTask)
                    }
                    
                    emit(ResourceState.Success(updatedTask))
                } else {
                    emit(ResourceState.Error("No se pudo actualizar la tarea"))
                }
            } else {
                emit(ResourceState.Error("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar estado de tarea", e)
            
            // Actualizar localmente con timestamp actual
            val currentTime = DateConverters.getCurrentDateTimeFormatted()
            
            withContext(Dispatchers.IO) {
                val affectedRows = taskDao.updateTaskStatus(taskId, status, currentTime)
                
                if (affectedRows > 0) {
                    val updatedTask = taskDao.getTaskById(taskId)
                    
                    if (updatedTask != null) {
                        emit(ResourceState.Success(updatedTask, isFromCache = true))
                    } else {
                        emit(ResourceState.Error("No se pudo encontrar la tarea actualizada"))
                    }
                } else {
                    emit(ResourceState.Error("No se pudo actualizar la tarea localmente"))
                }
            }
        }
    }
    
    /**
     * Sincroniza las tareas desde el servidor con la base de datos local.
     */
    suspend fun syncTasks(): Flow<ResourceState<List<Task>>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Obtener tareas del servidor
            val response = apiService.getTasks()
            
            if (response.isSuccessful) {
                val tasks = response.body() ?: emptyList()
                
                // Guardar en base de datos local
                withContext(Dispatchers.IO) {
                    taskDao.insertAll(tasks)
                }
                
                emit(ResourceState.Success(tasks))
            } else {
                emit(ResourceState.Error("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar tareas", e)
            emit(ResourceState.Error("Error de red: ${e.message}"))
        }
    }
    
    /**
     * Obtiene las finalizaciones de una tarea específica.
     */
    fun getCompletionsForTask(taskId: Int): LiveData<List<TaskCompletion>> {
        return taskCompletionDao.getCompletionsForTask(taskId)
    }
    
    /**
     * Registra una finalización de tarea con firma y/o foto.
     */
    suspend fun completeTask(
        taskId: Int,
        userId: Int,
        comments: String?,
        signatureUri: Uri?,
        photoUri: Uri?,
        status: String = "ok",
        latitude: Double? = null,
        longitude: Double? = null
    ): Flow<ResourceState<TaskCompletion>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Preparar datos para la petición
            val completionDate = Date()
            
            // Guardar archivos localmente primero
            val signatureFile = signatureUri?.let { FileUtils.createFileFromUri(context, it, "signature_${taskId}_") }
            val photoFile = photoUri?.let { FileUtils.createFileFromUri(context, it, "photo_${taskId}_") }
            
            // Preparar objeto de finalización
            val completion = TaskCompletion(
                taskId = taskId,
                completedBy = userId,
                completionDate = completionDate,
                comments = comments,
                signatureFile = signatureFile?.name,
                photoFile = photoFile?.name,
                locationLatitude = latitude,
                locationLongitude = longitude,
                completionStatus = status
            )
            
            // Intentar enviar al servidor
            val completionJson = Gson().toJson(completion)
            val completionRequestBody = completionJson.toRequestBody("application/json".toMediaTypeOrNull())
            
            // Preparar archivos para multipart si existen
            val signatureMultipart = signatureFile?.let {
                MultipartBody.Part.createFormData(
                    "signature",
                    it.name,
                    it.asRequestBody("image/png".toMediaTypeOrNull())
                )
            }
            
            val photoMultipart = photoFile?.let {
                MultipartBody.Part.createFormData(
                    "photo",
                    it.name,
                    it.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )
            }
            
            // Enviar al servidor
            val response = apiService.completeTask(
                taskId,
                completionRequestBody,
                signatureMultipart,
                photoMultipart
            )
            
            if (response.isSuccessful) {
                val serverCompletion = response.body()
                
                if (serverCompletion != null) {
                    // Actualizar estado de la tarea en local
                    withContext(Dispatchers.IO) {
                        // Guardar la finalización en base de datos local
                        val localId = taskCompletionDao.insert(completion).toInt()
                        
                        // Marcar como sincronizada
                        serverCompletion.serverId?.let {
                            taskCompletionDao.markAsSynced(localId, it)
                        }
                        
                        // Actualizar estado de la tarea
                        taskDao.updateTaskStatus(taskId, "completed", DateConverters.getCurrentDateTimeFormatted())
                    }
                    
                    emit(ResourceState.Success(serverCompletion))
                } else {
                    emit(ResourceState.Error("Respuesta vacía del servidor"))
                }
            } else {
                emit(ResourceState.Error("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al completar tarea", e)
            
            // Almacenar la finalización localmente en caso de error de red
            try {
                val localCompletion = TaskCompletion(
                    taskId = taskId,
                    completedBy = userId,
                    completionDate = Date(),
                    comments = comments,
                    signatureFile = signatureUri?.toString(),
                    photoFile = photoUri?.toString(),
                    locationLatitude = latitude,
                    locationLongitude = longitude,
                    completionStatus = status,
                    isSynced = false
                )
                
                withContext(Dispatchers.IO) {
                    // Guardar la finalización en base de datos local
                    val localId = taskCompletionDao.insert(localCompletion).toInt()
                    
                    // Actualizar estado de la tarea
                    taskDao.updateTaskStatus(taskId, "completed", DateConverters.getCurrentDateTimeFormatted())
                    
                    // Recuperar la finalización guardada
                    val savedCompletion = taskCompletionDao.getTaskCompletionById(localId)
                    
                    if (savedCompletion != null) {
                        emit(ResourceState.Success(savedCompletion, isFromCache = true))
                    } else {
                        emit(ResourceState.Error("Error al guardar localmente: no se pudo recuperar"))
                    }
                }
            } catch (localError: Exception) {
                Log.e(TAG, "Error al guardar localmente", localError)
                emit(ResourceState.Error("Error completo: ${e.message}. Error local: ${localError.message}"))
            }
        }
    }
    
    /**
     * Sincroniza las finalizaciones de tareas pendientes.
     */
    suspend fun syncPendingCompletions(): Flow<ResourceState<Int>> = flow {
        emit(ResourceState.Loading())
        
        try {
            val unsyncedCompletions = withContext(Dispatchers.IO) {
                taskCompletionDao.getUnsyncedCompletions()
            }
            
            if (unsyncedCompletions.isEmpty()) {
                emit(ResourceState.Success(0))
                return@flow
            }
            
            var syncedCount = 0
            
            // Intentar sincronizar cada finalización pendiente
            for (completion in unsyncedCompletions) {
                try {
                    // Preparar archivos si existen
                    val signatureFile = completion.signatureFile?.let { File(it) }
                    val photoFile = completion.photoFile?.let { File(it) }
                    
                    // Preparar request
                    val completionJson = Gson().toJson(completion)
                    val completionRequestBody = completionJson.toRequestBody("application/json".toMediaTypeOrNull())
                    
                    // Preparar archivos para multipart si existen
                    val signatureMultipart = signatureFile?.let {
                        if (it.exists()) {
                            MultipartBody.Part.createFormData(
                                "signature",
                                it.name,
                                it.asRequestBody("image/png".toMediaTypeOrNull())
                            )
                        } else null
                    }
                    
                    val photoMultipart = photoFile?.let {
                        if (it.exists()) {
                            MultipartBody.Part.createFormData(
                                "photo",
                                it.name,
                                it.asRequestBody("image/jpeg".toMediaTypeOrNull())
                            )
                        } else null
                    }
                    
                    // Enviar al servidor
                    val response = apiService.completeTask(
                        completion.taskId,
                        completionRequestBody,
                        signatureMultipart,
                        photoMultipart
                    )
                    
                    if (response.isSuccessful) {
                        val serverCompletion = response.body()
                        
                        if (serverCompletion != null && serverCompletion.serverId != null) {
                            // Marcar como sincronizada
                            withContext(Dispatchers.IO) {
                                taskCompletionDao.markAsSynced(completion.id, serverCompletion.serverId)
                            }
                            
                            syncedCount++
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al sincronizar finalización ${completion.id}", e)
                    // Continuar con la siguiente finalización
                }
            }
            
            emit(ResourceState.Success(syncedCount))
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar finalizaciones pendientes", e)
            emit(ResourceState.Error("Error al sincronizar: ${e.message}"))
        }
    }
}