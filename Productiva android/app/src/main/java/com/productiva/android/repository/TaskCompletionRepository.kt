package com.productiva.android.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.productiva.android.api.ApiClient
import com.productiva.android.api.ApiResponse
import com.productiva.android.dao.TaskCompletionDao
import com.productiva.android.database.AppDatabase
import com.productiva.android.model.TaskCompletion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import java.util.Date

/**
 * Repositorio para manejar operaciones relacionadas con completaciones de tareas
 */
class TaskCompletionRepository(private val context: Context) {
    
    private val taskCompletionDao: TaskCompletionDao = AppDatabase.getDatabase(context).taskCompletionDao()
    private val apiClient = ApiClient.getInstance(context)
    
    /**
     * Obtiene las completaciones por tarea como LiveData desde la base de datos local
     */
    fun getCompletionsByTaskId(taskId: Int): LiveData<List<TaskCompletion>> {
        return taskCompletionDao.getCompletionsByTaskId(taskId)
    }
    
    /**
     * Obtiene las completaciones por usuario como LiveData desde la base de datos local
     */
    fun getCompletionsByUserId(userId: Int): LiveData<List<TaskCompletion>> {
        return taskCompletionDao.getCompletionsByUserId(userId)
    }
    
    /**
     * Obtiene las completaciones por rango de fechas como LiveData desde la base de datos local
     */
    fun getCompletionsByDateRange(startDate: Date, endDate: Date): LiveData<List<TaskCompletion>> {
        return taskCompletionDao.getCompletionsByDateRange(startDate.time, endDate.time)
    }
    
    /**
     * Obtiene una completación por su ID desde la base de datos local
     */
    suspend fun getCompletionById(completionId: Int): TaskCompletion? = withContext(Dispatchers.IO) {
        return@withContext taskCompletionDao.getCompletionById(completionId)
    }
    
    /**
     * Obtiene la cantidad de completaciones en un rango de fechas
     */
    suspend fun getCompletionCountInDateRange(userId: Int, startDate: Date, endDate: Date): Int = withContext(Dispatchers.IO) {
        return@withContext taskCompletionDao.getCompletionCountInDateRange(userId, startDate.time, endDate.time)
    }
    
    /**
     * Obtiene las completaciones con firma
     */
    suspend fun getCompletionsWithSignatures(): List<TaskCompletion> = withContext(Dispatchers.IO) {
        return@withContext taskCompletionDao.getCompletionsWithSignatures()
    }
    
    /**
     * Crea una nueva completación en el servidor y localmente
     */
    suspend fun createTaskCompletion(completion: TaskCompletion): Result<TaskCompletion> = withContext(Dispatchers.IO) {
        try {
            val response: Response<ApiResponse<TaskCompletion>> = apiClient.apiService.createTaskCompletion(completion)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    // Guarda la completación en la base de datos local
                    taskCompletionDao.insert(apiResponse.data)
                    return@withContext Result.success(apiResponse.data)
                } else {
                    return@withContext Result.failure(Exception(apiResponse?.message ?: "Error en la respuesta"))
                }
            } else {
                return@withContext Result.failure(Exception("Error al crear completación: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Si hay un error de conexión, guardamos localmente y marcamos para sincronizar después
            val completionWithSyncFlag = completion.copy(isSynced = false, lastSync = System.currentTimeMillis())
            val id = taskCompletionDao.insert(completionWithSyncFlag)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Crea una nueva completación con firma en el servidor y localmente
     */
    suspend fun createTaskCompletionWithSignature(completion: TaskCompletion, signatureFile: File): Result<TaskCompletion> = withContext(Dispatchers.IO) {
        try {
            // Crear la parte de la firma
            val requestFile = signatureFile.asRequestBody("image/png".toMediaTypeOrNull())
            val signaturePart = MultipartBody.Part.createFormData("signature", signatureFile.name, requestFile)
            
            val response = apiClient.apiService.createTaskCompletionWithSignature(completion, signaturePart)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    // Guarda la completación en la base de datos local
                    taskCompletionDao.insert(apiResponse.data)
                    return@withContext Result.success(apiResponse.data)
                } else {
                    return@withContext Result.failure(Exception(apiResponse?.message ?: "Error en la respuesta"))
                }
            } else {
                return@withContext Result.failure(Exception("Error al crear completación con firma: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Si hay un error de conexión, guardamos localmente y marcamos para sincronizar después
            val completionWithSignaturePath = completion.copy(
                signaturePath = signatureFile.absolutePath,
                isSynced = false,
                lastSync = System.currentTimeMillis()
            )
            val id = taskCompletionDao.insert(completionWithSignaturePath)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Crea una nueva completación con foto en el servidor y localmente
     */
    suspend fun createTaskCompletionWithPhoto(completion: TaskCompletion, photoFile: File): Result<TaskCompletion> = withContext(Dispatchers.IO) {
        try {
            // Crear la parte de la foto
            val requestFile = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData("photo", photoFile.name, requestFile)
            
            val response = apiClient.apiService.createTaskCompletionWithPhoto(completion, photoPart)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    // Guarda la completación en la base de datos local
                    taskCompletionDao.insert(apiResponse.data)
                    return@withContext Result.success(apiResponse.data)
                } else {
                    return@withContext Result.failure(Exception(apiResponse?.message ?: "Error en la respuesta"))
                }
            } else {
                return@withContext Result.failure(Exception("Error al crear completación con foto: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Si hay un error de conexión, guardamos localmente y marcamos para sincronizar después
            val completionWithPhotoPath = completion.copy(
                photoPath = photoFile.absolutePath,
                isSynced = false,
                lastSync = System.currentTimeMillis()
            )
            val id = taskCompletionDao.insert(completionWithPhotoPath)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Sincroniza las completaciones no sincronizadas con el servidor
     */
    suspend fun syncTaskCompletions(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // Obtener las completaciones no sincronizadas
            val unsyncedCompletions = taskCompletionDao.getUnsyncedCompletions()
            
            if (unsyncedCompletions.isEmpty()) {
                return@withContext Result.success(0)
            }
            
            val response = apiClient.apiService.syncTaskCompletions(unsyncedCompletions)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    // Actualizar las completaciones con las del servidor
                    taskCompletionDao.insertAll(apiResponse.data)
                    return@withContext Result.success(apiResponse.data.size)
                } else {
                    return@withContext Result.failure(Exception(apiResponse?.message ?: "Error en la respuesta"))
                }
            } else {
                return@withContext Result.failure(Exception("Error al sincronizar completaciones: ${response.code()}"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
}