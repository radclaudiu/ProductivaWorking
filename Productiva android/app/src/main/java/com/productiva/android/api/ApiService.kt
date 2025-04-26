package com.productiva.android.api

import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz para las llamadas API con Retrofit
 */
interface ApiService {
    
    // ===== Autenticación =====
    
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<ApiResponse<User>>
    
    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Any>>
    
    @GET("auth/user")
    suspend fun getCurrentUser(): Response<ApiResponse<User>>
    
    // ===== Usuarios =====
    
    @GET("users")
    suspend fun getUsers(
        @Query("company_id") companyId: Int? = null
    ): Response<ApiResponse<List<User>>>
    
    @GET("users/{id}")
    suspend fun getUserById(
        @Path("id") userId: Int
    ): Response<ApiResponse<User>>
    
    // ===== Tareas =====
    
    @GET("tasks")
    suspend fun getTasks(
        @Query("user_id") userId: Int? = null,
        @Query("status") status: String? = null,
        @Query("company_id") companyId: Int? = null,
        @Query("location_id") locationId: Int? = null,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<ApiResponse<List<Task>>>
    
    @GET("tasks/{id}")
    suspend fun getTaskById(
        @Path("id") taskId: Int
    ): Response<ApiResponse<Task>>
    
    @POST("tasks")
    suspend fun createTask(
        @Body task: Task
    ): Response<ApiResponse<Task>>
    
    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Path("id") taskId: Int,
        @Body task: Task
    ): Response<ApiResponse<Task>>
    
    @PUT("tasks/{id}/status")
    suspend fun updateTaskStatus(
        @Path("id") taskId: Int,
        @Body statusData: Map<String, String>
    ): Response<ApiResponse<Task>>
    
    // ===== Completaciones de tareas =====
    
    @GET("tasks/{taskId}/completions")
    suspend fun getTaskCompletions(
        @Path("taskId") taskId: Int
    ): Response<ApiResponse<List<TaskCompletion>>>
    
    @POST("tasks/{taskId}/completions")
    suspend fun createTaskCompletion(
        @Path("taskId") taskId: Int,
        @Body completion: TaskCompletion
    ): Response<ApiResponse<TaskCompletion>>
    
    @Multipart
    @POST("tasks/{taskId}/completions/with-signature")
    suspend fun createTaskCompletionWithSignature(
        @Path("taskId") taskId: Int,
        @Part("completion") completionData: RequestBody,
        @Part signature: MultipartBody.Part
    ): Response<ApiResponse<TaskCompletion>>
    
    @Multipart
    @POST("tasks/{taskId}/completions/with-photo")
    suspend fun createTaskCompletionWithPhoto(
        @Path("taskId") taskId: Int,
        @Part("completion") completionData: RequestBody,
        @Part photo: MultipartBody.Part
    ): Response<ApiResponse<TaskCompletion>>
    
    // ===== Plantillas de etiquetas =====
    
    @GET("label-templates")
    suspend fun getLabelTemplates(
        @Query("user_id") userId: Int? = null,
        @Query("company_id") companyId: Int? = null
    ): Response<ApiResponse<List<LabelTemplate>>>
    
    @GET("label-templates/{id}")
    suspend fun getLabelTemplateById(
        @Path("id") templateId: Int
    ): Response<ApiResponse<LabelTemplate>>
    
    @POST("label-templates")
    suspend fun createLabelTemplate(
        @Body template: LabelTemplate
    ): Response<ApiResponse<LabelTemplate>>
    
    @PUT("label-templates/{id}")
    suspend fun updateLabelTemplate(
        @Path("id") templateId: Int,
        @Body template: LabelTemplate
    ): Response<ApiResponse<LabelTemplate>>
    
    @DELETE("label-templates/{id}")
    suspend fun deleteLabelTemplate(
        @Path("id") templateId: Int
    ): Response<ApiResponse<Any>>
    
    // ===== Sincronización =====
    
    @GET("sync/tasks")
    suspend fun syncTasks(
        @Query("since") since: String? = null,
        @Query("user_id") userId: Int? = null
    ): Response<ApiResponse<List<Task>>>
    
    @GET("sync/users")
    suspend fun syncUsers(
        @Query("since") since: String? = null,
        @Query("company_id") companyId: Int? = null
    ): Response<ApiResponse<List<User>>>
    
    @GET("sync/templates")
    suspend fun syncTemplates(
        @Query("since") since: String? = null
    ): Response<ApiResponse<List<LabelTemplate>>>
}