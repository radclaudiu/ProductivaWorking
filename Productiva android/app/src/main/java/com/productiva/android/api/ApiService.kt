package com.productiva.android.api

import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.model.User
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz que define los endpoints de la API REST
 */
interface ApiService {
    
    /**
     * Autenticación y usuarios
     */
    @FormUrlEncoded
    @POST("api/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<Map<String, Any>>
    
    @GET("api/users")
    suspend fun getUsers(): Response<List<User>>
    
    @GET("api/users/{id}")
    suspend fun getUserById(
        @Path("id") userId: Int
    ): Response<User>
    
    @GET("api/locations/{locationId}/users")
    suspend fun getUsersByLocation(
        @Path("locationId") locationId: Int
    ): Response<List<User>>
    
    @GET("api/companies/{companyId}/users")
    suspend fun getUsersByCompany(
        @Path("companyId") companyId: Int
    ): Response<List<User>>
    
    /**
     * Tareas
     */
    @GET("api/tasks")
    suspend fun getAllTasks(): Response<List<Task>>
    
    @GET("api/tasks/{id}")
    suspend fun getTaskById(
        @Path("id") taskId: Int
    ): Response<Task>
    
    @GET("api/tasks/user/{userId}")
    suspend fun getTasksByUser(
        @Path("userId") userId: Int
    ): Response<List<Task>>
    
    @GET("api/tasks/status/{status}")
    suspend fun getTasksByStatus(
        @Path("status") status: String
    ): Response<List<Task>>
    
    @GET("api/tasks/location/{locationId}")
    suspend fun getTasksByLocation(
        @Path("locationId") locationId: Int
    ): Response<List<Task>>
    
    @FormUrlEncoded
    @POST("api/tasks")
    suspend fun createTask(
        @Field("title") title: String,
        @Field("description") description: String?,
        @Field("assigned_to") assignedTo: Int?,
        @Field("location_id") locationId: Int?,
        @Field("due_date") dueDate: String?,
        @Field("priority") priority: Int?
    ): Response<Task>
    
    @FormUrlEncoded
    @PUT("api/tasks/{id}")
    suspend fun updateTask(
        @Path("id") taskId: Int,
        @Field("title") title: String?,
        @Field("description") description: String?,
        @Field("assigned_to") assignedTo: Int?,
        @Field("location_id") locationId: Int?,
        @Field("status") status: String?,
        @Field("due_date") dueDate: String?,
        @Field("priority") priority: Int?
    ): Response<Task>
    
    /**
     * Completado de tareas
     */
    @GET("api/task_completions/{taskId}")
    suspend fun getTaskCompletions(
        @Path("taskId") taskId: Int
    ): Response<List<TaskCompletion>>
    
    @Multipart
    @POST("api/task_completions")
    suspend fun completeTask(
        @Part("task_id") taskId: Int,
        @Part("comments") comments: String?,
        @Part("completed_by") completedBy: Int,
        @Part signature: MultipartBody.Part?,
        @Part photo: MultipartBody.Part?,
        @Part("latitude") latitude: Double?,
        @Part("longitude") longitude: Double?
    ): Response<TaskCompletion>
    
    /**
     * Plantillas de etiquetas
     */
    @GET("api/label_templates")
    suspend fun getLabelTemplates(): Response<List<Map<String, Any>>>
    
    @GET("api/label_templates/{id}")
    suspend fun getLabelTemplateById(
        @Path("id") templateId: Int
    ): Response<Map<String, Any>>
    
    /**
     * Ubicaciones
     */
    @GET("api/locations")
    suspend fun getLocations(): Response<List<Map<String, Any>>>
    
    @GET("api/locations/{id}")
    suspend fun getLocationById(
        @Path("id") locationId: Int
    ): Response<Map<String, Any>>
    
    /**
     * Descarga de archivos
     */
    @Streaming
    @GET
    suspend fun downloadFile(
        @Url fileUrl: String
    ): Response<ResponseBody>
}