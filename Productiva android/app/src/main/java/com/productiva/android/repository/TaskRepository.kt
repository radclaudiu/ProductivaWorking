package com.productiva.android.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.productiva.android.api.ApiService
import com.productiva.android.models.Task
import com.productiva.android.models.TaskCompletion
import com.productiva.android.utils.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * Repositorio para operaciones relacionadas con tareas
 */
class TaskRepository(
    private val apiService: ApiService,
    private val database: AppDatabase
) {
    /**
     * Obtiene todas las tareas de la API y las almacena localmente
     */
    suspend fun refreshTasks(token: String, locationId: Int? = null, status: String? = null) {
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTasks("Bearer $token", locationId, status)
                if (response.isSuccessful && response.body() != null) {
                    database.taskDao().insertAllTasks(response.body()!!)
                }
            } catch (e: Exception) {
                Log.e("TaskRepository", "Error al refrescar tareas", e)
            }
        }
    }

    /**
     * Obtiene todas las tareas localmente
     */
    fun getAllTasks(): LiveData<List<Task>> {
        return database.taskDao().getAllTasks()
    }

    /**
     * Obtiene tareas por ubicación
     */
    fun getTasksByLocation(locationId: Int): LiveData<List<Task>> {
        return database.taskDao().getTasksByLocation(locationId)
    }

    /**
     * Obtiene tareas por estado
     */
    fun getTasksByStatus(status: String): LiveData<List<Task>> {
        return database.taskDao().getTasksByStatus(status)
    }

    /**
     * Completa una tarea y la sincroniza con el servidor
     */
    suspend fun completeTask(
        token: String,
        taskId: Int,
        userId: Int,
        locationId: Int,
        notes: String? = null,
        signatureFilePath: String? = null,
        photoFilePath: String? = null
    ): Result<TaskCompletion> {
        return withContext(Dispatchers.IO) {
            try {
                // Crear un objeto de completado de tarea
                val completionDate = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    .format(java.util.Date())
                
                val taskCompletion = TaskCompletion(
                    taskId = taskId,
                    userId = userId,
                    locationId = locationId,
                    completionDate = completionDate,
                    notes = notes,
                    localSignaturePath = signatureFilePath,
                    localPhotoPath = photoFilePath,
                    localSyncStatus = Task.SYNC_STATUS_PENDING
                )
                
                // Guardar localmente primero
                val localId = database.taskCompletionDao().insertTaskCompletion(taskCompletion)
                
                // Intentar sincronizar con el servidor
                try {
                    val response = apiService.completeTask("Bearer $token", taskCompletion)
                    
                    if (response.isSuccessful && response.body() != null) {
                        val remoteId = response.body()!!.id
                        
                        // Subir archivos si existen
                        if (remoteId != null) {
                            // Subir firma si existe
                            if (!signatureFilePath.isNullOrEmpty()) {
                                uploadFile(token, remoteId, "signature", signatureFilePath)
                            }
                            
                            // Subir foto si existe
                            if (!photoFilePath.isNullOrEmpty()) {
                                uploadFile(token, remoteId, "photo", photoFilePath)
                            }
                        }
                        
                        // Actualizar el registro local con el ID remoto
                        if (remoteId != null) {
                            database.taskCompletionDao().updateCompletionAfterSync(localId.toInt(), remoteId)
                        }
                        
                        // Actualizar el estado de la tarea a completado
                        database.taskDao().updateTaskStatus(taskId, "completed")
                        
                        Result.success(response.body()!!)
                    } else {
                        // Error al sincronizar con el servidor
                        Result.failure(Exception("Error al sincronizar con el servidor: ${response.code()}"))
                    }
                } catch (e: Exception) {
                    // Error de red u otro, pero guardamos localmente para sincronizar después
                    Log.e("TaskRepository", "Error al sincronizar completado de tarea", e)
                    Result.success(taskCompletion) // Devolvemos éxito parcial porque se guardó localmente
                }
            } catch (e: Exception) {
                Log.e("TaskRepository", "Error al completar tarea", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Sube un archivo al servidor
     */
    private suspend fun uploadFile(token: String, taskCompletionId: Int, type: String, filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                
                val taskCompletionIdPart = taskCompletionId.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val typePart = type.toRequestBody("text/plain".toMediaTypeOrNull())
                
                val response = apiService.uploadTaskCompletionFile(
                    "Bearer $token",
                    taskCompletionIdPart,
                    typePart,
                    body
                )
                
                response.isSuccessful
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error al subir archivo: $filePath", e)
            false
        }
    }
    
    /**
     * Sincroniza las tareas completadas pendientes
     */
    suspend fun syncPendingCompletions(token: String) {
        withContext(Dispatchers.IO) {
            try {
                val pendingCompletions = database.taskCompletionDao().getUnsyncedTaskCompletions().value
                pendingCompletions?.forEach { completion ->
                    // Intentar sincronizar cada tarea pendiente
                    try {
                        val response = apiService.completeTask("Bearer $token", completion)
                        if (response.isSuccessful && response.body() != null) {
                            val remoteId = response.body()!!.id
                            if (remoteId != null) {
                                // Subir archivos si existen
                                if (!completion.localSignaturePath.isNullOrEmpty()) {
                                    uploadFile(token, remoteId, "signature", completion.localSignaturePath!!)
                                }
                                
                                if (!completion.localPhotoPath.isNullOrEmpty()) {
                                    uploadFile(token, remoteId, "photo", completion.localPhotoPath!!)
                                }
                                
                                // Actualizar estado de sincronización
                                database.taskCompletionDao().updateCompletionAfterSync(completion.localId, remoteId)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("TaskRepository", "Error al sincronizar tarea pendiente: ${completion.localId}", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("TaskRepository", "Error al sincronizar tareas pendientes", e)
            }
        }
    }
}