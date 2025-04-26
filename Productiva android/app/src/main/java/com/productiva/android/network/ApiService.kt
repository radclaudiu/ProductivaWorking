package com.productiva.android.network

import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.model.User
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz que define los endpoints de la API para comunicarse con el backend.
 */
interface ApiService {
    
    /**
     * Autenticación
     */
    @POST("api/auth/login")
    suspend fun login(
        @Body credentials: Map<String, String>
    ): Response<LoginResponse>
    
    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<User>
    
    /**
     * Usuarios
     */
    @GET("api/users")
    suspend fun getUsers(): Response<List<User>>
    
    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") userId: Int): Response<User>
    
    /**
     * Tareas
     */
    @GET("api/tasks")
    suspend fun getTasks(
        @Query("status") status: String? = null,
        @Query("userId") userId: Int? = null
    ): Response<List<Task>>
    
    @GET("api/tasks/{id}")
    suspend fun getTaskById(@Path("id") taskId: Int): Response<Task>
    
    @POST("api/tasks/{id}/complete")
    suspend fun completeTask(
        @Path("id") taskId: Int,
        @Body completionData: TaskCompletion
    ): Response<TaskCompletion>
    
    @Multipart
    @POST("api/tasks/{id}/signature")
    suspend fun uploadSignature(
        @Path("id") taskId: Int,
        @Part signature: MultipartBody.Part
    ): Response<ResponseBody>
    
    @Multipart
    @POST("api/tasks/{id}/photo")
    suspend fun uploadTaskPhoto(
        @Path("id") taskId: Int,
        @Part photo: MultipartBody.Part
    ): Response<ResponseBody>
    
    /**
     * Plantillas de etiquetas
     */
    @GET("api/label-templates")
    suspend fun getLabelTemplates(): Response<List<LabelTemplate>>
    
    @GET("api/label-templates/{id}")
    suspend fun getLabelTemplateById(@Path("id") templateId: Int): Response<LabelTemplate>
    
    @POST("api/label-templates/{id}/usage")
    suspend fun updateLabelTemplateUsage(@Path("id") templateId: Int): Response<ResponseBody>
    
    /**
     * Sincronizaciones
     */
    @GET("api/sync/status")
    suspend fun getSyncStatus(): Response<SyncStatusResponse>
    
    @POST("api/sync/tasks")
    suspend fun syncTaskCompletions(
        @Body completions: List<TaskCompletion>
    ): Response<SyncTaskResponse>
    
    /**
     * Datos adicionales
     */
    @GET("api/company-info")
    suspend fun getCompanyInfo(): Response<CompanyInfoResponse>
    
    /**
     * Clase para respuesta de login.
     */
    data class LoginResponse(
        val token: String,
        val user: User,
        val expiresAt: Long
    )
    
    /**
     * Clase para información de sincronización.
     */
    data class SyncStatusResponse(
        val lastSyncTimestamp: Long,
        val pendingChanges: Int,
        val serverVersion: String
    )
    
    /**
     * Clase para respuesta de sincronización de tareas.
     */
    data class SyncTaskResponse(
        val success: Boolean,
        val syncedCount: Int,
        val failedCount: Int,
        val errors: List<String>?
    )
    
    /**
     * Clase para información de empresa.
     */
    data class CompanyInfoResponse(
        val id: Int,
        val name: String,
        val logo: String?,
        val settings: Map<String, Any>
    )
}