package com.productiva.android.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import com.productiva.android.ProductivaApplication
import com.productiva.android.api.ApiService
import com.productiva.android.dao.TaskDao
import com.productiva.android.dao.TaskCompletionDao
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import java.util.Date

/**
 * Repositorio para la gestión de tareas
 */
class TaskRepository(private val app: ProductivaApplication) {
    
    private val taskDao: TaskDao = app.database.taskDao()
    private val taskCompletionDao: TaskCompletionDao = app.database.taskCompletionDao()
    private val apiService: ApiService = app.apiService
    private val fileUtils = FileUtils(app)
    
    /**
     * Obtiene todas las tareas de la base de datos local
     * @return LiveData con la lista de tareas
     */
    fun getAllTasks(): LiveData<List<Task>> {
        return taskDao.getAllTasks()
    }
    
    /**
     * Obtiene una tarea por su ID
     * @param taskId ID de la tarea
     * @return Tarea encontrada o null
     */
    suspend fun getTaskById(taskId: Int): Task? {
        return withContext(Dispatchers.IO) {
            taskDao.getTaskById(taskId)
        }
    }
    
    /**
     * Obtiene tareas asignadas a un usuario
     * @param userId ID del usuario asignado
     * @return LiveData con la lista de tareas asignadas
     */
    fun getTasksByAssignee(userId: Int): LiveData<List<Task>> {
        return taskDao.getTasksByAssignee(userId)
    }
    
    /**
     * Obtiene tareas por estado
     * @param status Estado de las tareas a buscar
     * @return LiveData con la lista de tareas con el estado especificado
     */
    fun getTasksByStatus(status: String): LiveData<List<Task>> {
        return taskDao.getTasksByStatus(status)
    }
    
    /**
     * Obtiene tareas por estado y usuario asignado
     * @param status Estado de las tareas a buscar
     * @param userId ID del usuario asignado
     * @return LiveData con la lista de tareas filtradas
     */
    fun getTasksByStatusAndAssignee(status: String, userId: Int): LiveData<List<Task>> {
        return taskDao.getTasksByStatusAndAssignee(status, userId)
    }
    
    /**
     * Obtiene tareas por ubicación
     * @param locationId ID de la ubicación
     * @return LiveData con la lista de tareas de la ubicación
     */
    fun getTasksByLocation(locationId: Int): LiveData<List<Task>> {
        return taskDao.getTasksByLocation(locationId)
    }
    
    /**
     * Sincroniza tareas desde el servidor
     * @return True si la sincronización fue exitosa
     */
    suspend fun syncTasks(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Sincronizar tareas al servidor
                syncLocalTasksToServer()
                
                // Obtener tareas del servidor
                val response = apiService.getAllTasks()
                if (response.isSuccessful) {
                    response.body()?.let { tasks ->
                        taskDao.insertTasks(tasks)
                        return@withContext true
                    }
                }
                false
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Sincroniza tareas por usuario desde el servidor
     * @param userId ID del usuario asignado
     * @return True si la sincronización fue exitosa
     */
    suspend fun syncTasksByUser(userId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTasksByUser(userId)
                if (response.isSuccessful) {
                    response.body()?.let { tasks ->
                        taskDao.insertTasks(tasks)
                        return@withContext true
                    }
                }
                false
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Sincroniza tareas por ubicación desde el servidor
     * @param locationId ID de la ubicación
     * @return True si la sincronización fue exitosa
     */
    suspend fun syncTasksByLocation(locationId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTasksByLocation(locationId)
                if (response.isSuccessful) {
                    response.body()?.let { tasks ->
                        taskDao.insertTasks(tasks)
                        return@withContext true
                    }
                }
                false
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Completa una tarea
     * @param taskId ID de la tarea
     * @param comments Comentarios opcionales
     * @param completedBy ID del usuario que completa la tarea
     * @param signatureUri URI del archivo de firma (opcional)
     * @param photoUri URI del archivo de foto (opcional)
     * @param latitude Latitud donde se completó la tarea (opcional)
     * @param longitude Longitud donde se completó la tarea (opcional)
     * @return True si la tarea se completó correctamente
     */
    suspend fun completeTask(
        taskId: Int,
        comments: String?,
        completedBy: Int,
        signatureUri: Uri?,
        photoUri: Uri?,
        latitude: Double?,
        longitude: Double?
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Almacenar en base de datos local primero
                val taskCompletion = TaskCompletion(
                    taskId = taskId,
                    completedBy = completedBy,
                    completedAt = Date(),
                    comments = comments,
                    signaturePath = signatureUri?.toString(),
                    photoPath = photoUri?.toString(),
                    latitude = latitude,
                    longitude = longitude,
                    synced = false,
                    isLocalOnly = true
                )
                
                val localId = taskCompletionDao.insertTaskCompletion(taskCompletion)
                
                // Actualizar el estado de la tarea
                val task = taskDao.getTaskById(taskId)
                task?.let {
                    val updatedTask = it.copy(status = "completed", updatedAt = Date())
                    taskDao.updateTask(updatedTask)
                }
                
                // Intentar sincronizar inmediatamente si hay conexión
                syncTaskCompletionToServer(taskCompletion.copy(id = localId.toInt()))
                
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Sincroniza los registros de completado de tareas pendientes al servidor
     * @return Número de registros sincronizados correctamente
     */
    suspend fun syncCompletions(): Int {
        return withContext(Dispatchers.IO) {
            var syncedCount = 0
            val pendingCompletions = taskCompletionDao.getUnsyncedCompletions()
            
            for (completion in pendingCompletions) {
                if (syncTaskCompletionToServer(completion)) {
                    syncedCount++
                }
            }
            
            syncedCount
        }
    }
    
    /**
     * Sincroniza un registro de completado específico al servidor
     * @param taskCompletion Registro de completado a sincronizar
     * @return True si la sincronización fue exitosa
     */
    private suspend fun syncTaskCompletionToServer(taskCompletion: TaskCompletion): Boolean {
        return try {
            var signaturePart: MultipartBody.Part? = null
            var photoPart: MultipartBody.Part? = null
            
            // Preparar archivo de firma si existe
            taskCompletion.signaturePath?.let { pathUri ->
                val uri = Uri.parse(pathUri)
                val file = fileUtils.getFileFromUri(uri)
                file?.let {
                    val requestBody = it.asRequestBody("image/png".toMediaTypeOrNull())
                    signaturePart = MultipartBody.Part.createFormData("signature", it.name, requestBody)
                }
            }
            
            // Preparar archivo de foto si existe
            taskCompletion.photoPath?.let { pathUri ->
                val uri = Uri.parse(pathUri)
                val file = fileUtils.getFileFromUri(uri)
                file?.let {
                    val requestBody = it.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    photoPart = MultipartBody.Part.createFormData("photo", it.name, requestBody)
                }
            }
            
            // Enviar al servidor
            val response = apiService.completeTask(
                taskId = taskCompletion.taskId,
                comments = taskCompletion.comments,
                completedBy = taskCompletion.completedBy ?: app.sessionManager.getSelectedUserId(),
                signature = signaturePart,
                photo = photoPart,
                latitude = taskCompletion.latitude,
                longitude = taskCompletion.longitude
            )
            
            if (response.isSuccessful) {
                // Actualizar estado de sincronización
                taskCompletionDao.updateSyncStatus(
                    id = taskCompletion.id,
                    synced = true,
                    lastSynced = Date()
                )
                
                // También actualizar el estado de la tarea
                val task = taskDao.getTaskById(taskCompletion.taskId)
                task?.let {
                    taskDao.updateSyncStatus(
                        taskId = it.id,
                        synced = true,
                        lastSynced = Date()
                    )
                }
                
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Sincroniza las tareas locales al servidor
     */
    private suspend fun syncLocalTasksToServer(): Int {
        var syncedCount = 0
        val unsyncedTasks = taskDao.getUnsyncedTasks()
        
        for (task in unsyncedTasks) {
            if (task.isLocalOnly) {
                // Crear tarea en el servidor
                val response = apiService.createTask(
                    title = task.title,
                    description = task.description,
                    assignedTo = task.assignedTo,
                    locationId = task.locationId,
                    dueDate = task.dueDate?.toString(),
                    priority = task.priority
                )
                
                if (response.isSuccessful) {
                    syncedCount++
                    taskDao.updateSyncStatus(
                        taskId = task.id,
                        synced = true,
                        lastSynced = Date()
                    )
                }
            } else {
                // Actualizar tarea existente
                val response = apiService.updateTask(
                    taskId = task.id,
                    title = task.title,
                    description = task.description,
                    assignedTo = task.assignedTo,
                    locationId = task.locationId,
                    status = task.status,
                    dueDate = task.dueDate?.toString(),
                    priority = task.priority
                )
                
                if (response.isSuccessful) {
                    syncedCount++
                    taskDao.updateSyncStatus(
                        taskId = task.id,
                        synced = true,
                        lastSynced = Date()
                    )
                }
            }
        }
        
        return syncedCount
    }
}