package com.productiva.android.repository

import android.content.Context
import android.net.Uri
import com.productiva.android.api.ApiService
import com.productiva.android.data.dao.TaskDao
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repositorio para operaciones relacionadas con tareas
 * Centraliza el acceso a los datos de tareas, ya sea de la API o de la base de datos local
 */
class TaskRepository(
    private val apiService: ApiService,
    private val taskDao: TaskDao,
    private val context: Context
) {
    /**
     * Carga tareas del servidor y las guarda en la base de datos local
     */
    suspend fun refreshTasks(token: String, locationId: Int? = null): Result<List<Task>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTasks(
                    token = "Bearer $token",
                    locationId = locationId
                )
                
                if (response.isSuccessful) {
                    val tasks = response.body() ?: emptyList()
                    if (tasks.isNotEmpty()) {
                        taskDao.insertAllTasks(tasks)
                    }
                    Result.success(tasks)
                } else {
                    Result.failure(Exception("Error obteniendo tareas: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Error de red: ${e.message}"))
            }
        }
    }
    
    /**
     * Registra la completitud de una tarea tanto localmente como en el servidor
     */
    suspend fun completeTask(
        token: String,
        taskId: Int,
        userId: Int,
        locationId: Int,
        notes: String? = null,
        photoFilePath: String? = null,
        signatureFilePath: String? = null
    ): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                // Formato para la fecha
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val currentDate = dateFormat.format(Date())
                
                // Crear objeto de completado local
                val taskCompletion = TaskCompletion(
                    taskId = taskId,
                    userId = userId,
                    locationId = locationId,
                    completionDate = currentDate,
                    notes = notes,
                    photoPath = photoFilePath,
                    signaturePath = signatureFilePath,
                    syncStatus = TaskCompletion.SYNC_PENDING
                )
                
                // Guardar en base de datos local
                val localId = taskDao.saveTaskCompletion(taskCompletion)
                
                // Enviar al servidor
                val taskIdRequestBody = taskId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val userIdRequestBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val locationIdRequestBody = locationId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val notesRequestBody = notes?.toRequestBody("text/plain".toMediaTypeOrNull())
                
                // Preparar archivos adjuntos
                var photoPart: MultipartBody.Part? = null
                var signaturePart: MultipartBody.Part? = null
                
                if (!photoFilePath.isNullOrEmpty()) {
                    val photoFile = File(photoFilePath)
                    val photoRequestBody = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    photoPart = MultipartBody.Part.createFormData("photo", photoFile.name, photoRequestBody)
                }
                
                if (!signatureFilePath.isNullOrEmpty()) {
                    val signatureFile = File(signatureFilePath)
                    val signatureRequestBody = signatureFile.asRequestBody("image/png".toMediaTypeOrNull())
                    signaturePart = MultipartBody.Part.createFormData("signature", signatureFile.name, signatureRequestBody)
                }
                
                // Hacer la petición
                val response = apiService.completeTask(
                    token = "Bearer $token",
                    taskId = taskIdRequestBody,
                    userId = userIdRequestBody,
                    locationId = locationIdRequestBody,
                    notes = notesRequestBody,
                    photo = photoPart,
                    signature = signaturePart
                )
                
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        // Actualizar estado de sincronización
                        taskDao.updateTaskCompletionSyncStatus(
                            id = localId.toInt(),
                            syncStatus = TaskCompletion.SYNC_COMPLETE,
                            serverId = responseBody.id
                        )
                    }
                    Result.success(localId)
                } else {
                    Result.failure(Exception("Error al completar tarea: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Error de red: ${e.message}"))
            }
        }
    }
    
    /**
     * Sincroniza completados de tareas pendientes con el servidor
     */
    suspend fun syncPendingTaskCompletions(token: String): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                // Obtener completados pendientes
                val pendingCompletions = taskDao.getPendingSyncTaskCompletions()
                
                if (pendingCompletions.isEmpty()) {
                    return@withContext Result.success(0)
                }
                
                // TODO: Implementar lógica de sincronización real con el servidor
                // En una implementación real, se enviarían los pendingCompletions al servidor
                
                // Simulamos éxito para todos (en una implementación real esto vendría del servidor)
                pendingCompletions.forEach { completion ->
                    taskDao.updateTaskCompletionSyncStatus(
                        id = completion.id,
                        syncStatus = TaskCompletion.SYNC_COMPLETE,
                        serverId = completion.id + 1000 // Simulado
                    )
                }
                
                Result.success(pendingCompletions.size)
            } catch (e: Exception) {
                Result.failure(Exception("Error sincronizando completados: ${e.message}"))
            }
        }
    }
}