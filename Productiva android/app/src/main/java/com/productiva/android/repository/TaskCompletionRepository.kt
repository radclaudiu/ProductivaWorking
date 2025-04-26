package com.productiva.android.repository

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import com.productiva.android.api.ApiClient
import com.productiva.android.api.ApiResponse
import com.productiva.android.database.AppDatabase
import com.productiva.android.database.TaskCompletionDao
import com.productiva.android.model.TaskCompletion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Response
import java.io.File
import java.util.Date

/**
 * Repositorio para gestionar completaciones de tareas
 */
class TaskCompletionRepository(private val context: Context) {
    
    private val apiClient = ApiClient.getInstance(context)
    private val completionDao: TaskCompletionDao
    
    init {
        val database = AppDatabase.getInstance(context)
        completionDao = database.taskCompletionDao()
    }
    
    /**
     * Obtiene todas las completaciones
     */
    fun getAllCompletions(): LiveData<List<TaskCompletion>> {
        return completionDao.getAllCompletions()
    }
    
    /**
     * Obtiene una completación por su ID
     */
    suspend fun getCompletionById(completionId: Int): TaskCompletion? {
        return withContext(Dispatchers.IO) {
            completionDao.getCompletionById(completionId)
        }
    }
    
    /**
     * Obtiene completaciones por tarea
     */
    fun getCompletionsByTaskId(taskId: Int): LiveData<List<TaskCompletion>> {
        return completionDao.getCompletionsByTaskId(taskId)
    }
    
    /**
     * Obtiene completaciones por usuario
     */
    fun getCompletionsByUserId(userId: Int): LiveData<List<TaskCompletion>> {
        return completionDao.getCompletionsByUserId(userId)
    }
    
    /**
     * Inserta una completación
     */
    suspend fun insertCompletion(completion: TaskCompletion): Long {
        return withContext(Dispatchers.IO) {
            completionDao.insert(completion)
        }
    }
    
    /**
     * Actualiza una completación
     */
    suspend fun updateCompletion(completion: TaskCompletion): Int {
        return withContext(Dispatchers.IO) {
            completionDao.update(completion)
        }
    }
    
    /**
     * Crea una completación de tarea
     */
    suspend fun createTaskCompletion(taskId: Int, completion: TaskCompletion): Result<TaskCompletion> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiClient.apiService.createTaskCompletion(taskId, completion)
                handleCompletionResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Actualiza la ruta de firma de una completación
     */
    suspend fun updateSignaturePath(completionId: Int, path: String): Int {
        return withContext(Dispatchers.IO) {
            completionDao.updateSignaturePath(completionId, path)
        }
    }
    
    /**
     * Actualiza la ruta de foto de una completación
     */
    suspend fun updatePhotoPath(completionId: Int, path: String): Int {
        return withContext(Dispatchers.IO) {
            completionDao.updatePhotoPath(completionId, path)
        }
    }
    
    /**
     * Crea una completación de tarea con firma
     */
    suspend fun createTaskCompletionWithSignature(
        taskId: Int,
        completion: TaskCompletion,
        signatureFile: File
    ): Result<TaskCompletion> {
        return withContext(Dispatchers.IO) {
            try {
                // Convertir objeto de completación a JSON
                val completionJson = JSONObject().apply {
                    put("userId", completion.userId)
                    put("notes", completion.notes ?: "")
                    put("locationId", completion.locationId ?: JSONObject.NULL)
                    put("locationName", completion.locationName ?: "")
                    put("latitude", completion.latitude ?: JSONObject.NULL)
                    put("longitude", completion.longitude ?: JSONObject.NULL)
                }.toString()
                
                // Crear parte de datos
                val completionData = completionJson.toRequestBody("application/json".toMediaTypeOrNull())
                
                // Crear parte de firma
                val signatureRequestBody = signatureFile.asRequestBody("image/png".toMediaTypeOrNull())
                val signaturePart = MultipartBody.Part.createFormData(
                    "signature",
                    signatureFile.name,
                    signatureRequestBody
                )
                
                // Enviar a la API
                val response = apiClient.apiService.createTaskCompletionWithSignature(
                    taskId,
                    completionData,
                    signaturePart
                )
                
                // Manejar respuesta
                val result = handleCompletionResponse(response)
                
                // Si fue exitoso, guardar la ruta de la firma localmente
                if (result.isSuccess) {
                    result.getOrNull()?.let { savedCompletion ->
                        // Guardar ruta localmente
                        completionDao.updateSignaturePath(savedCompletion.id, signatureFile.absolutePath)
                    }
                }
                
                result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Crea una completación de tarea con foto
     */
    suspend fun createTaskCompletionWithPhoto(
        taskId: Int,
        completion: TaskCompletion,
        photoFile: File
    ): Result<TaskCompletion> {
        return withContext(Dispatchers.IO) {
            try {
                // Convertir objeto de completación a JSON
                val completionJson = JSONObject().apply {
                    put("userId", completion.userId)
                    put("notes", completion.notes ?: "")
                    put("locationId", completion.locationId ?: JSONObject.NULL)
                    put("locationName", completion.locationName ?: "")
                    put("latitude", completion.latitude ?: JSONObject.NULL)
                    put("longitude", completion.longitude ?: JSONObject.NULL)
                }.toString()
                
                // Crear parte de datos
                val completionData = completionJson.toRequestBody("application/json".toMediaTypeOrNull())
                
                // Crear parte de foto
                val photoRequestBody = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val photoPart = MultipartBody.Part.createFormData(
                    "photo",
                    photoFile.name,
                    photoRequestBody
                )
                
                // Enviar a la API
                val response = apiClient.apiService.createTaskCompletionWithPhoto(
                    taskId,
                    completionData,
                    photoPart
                )
                
                // Manejar respuesta
                val result = handleCompletionResponse(response)
                
                // Si fue exitoso, guardar la ruta de la foto localmente
                if (result.isSuccess) {
                    result.getOrNull()?.let { savedCompletion ->
                        // Guardar ruta localmente
                        completionDao.updatePhotoPath(savedCompletion.id, photoFile.absolutePath)
                    }
                }
                
                result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Obtiene completaciones pendientes de sincronización
     */
    suspend fun getPendingUploadCompletions(): List<TaskCompletion> {
        return withContext(Dispatchers.IO) {
            completionDao.getPendingUpload()
        }
    }
    
    /**
     * Marca una completación como sincronizada
     */
    suspend fun markAsSynced(completionId: Int): Int {
        return withContext(Dispatchers.IO) {
            completionDao.markAsSynced(completionId, Date())
        }
    }
    
    /**
     * Gestiona la respuesta de la API para operaciones con completaciones
     */
    private fun handleCompletionResponse(response: Response<ApiResponse<TaskCompletion>>): Result<TaskCompletion> {
        if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                // Guardar la completación en la base de datos local
                completionDao.insert(apiResponse.data)
                
                return Result.success(apiResponse.data)
            }
            return Result.failure(Exception(apiResponse?.message ?: "Respuesta sin datos"))
        }
        return Result.failure(Exception(response.message() ?: "Error desconocido"))
    }
}