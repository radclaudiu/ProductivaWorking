package com.productiva.android.api

import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.model.User
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz Retrofit para definir los endpoints de la API
 */
interface ApiService {
    
    // Autenticación
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<ApiResponse<User>>
    
    // Usuarios
    @GET("users")
    suspend fun getUsers(): Response<ApiResponse<List<User>>>
    
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: Int): Response<ApiResponse<User>>
    
    @GET("users/me")
    suspend fun getCurrentUser(): Response<ApiResponse<User>>
    
    @GET("users/by-company/{companyId}")
    suspend fun getUsersByCompany(@Path("companyId") companyId: Int): Response<ApiResponse<List<User>>>
    
    // Tareas
    @GET("tasks")
    suspend fun getTasks(): Response<ApiResponse<List<Task>>>
    
    @GET("tasks/{id}")
    suspend fun getTask(@Path("id") taskId: Int): Response<ApiResponse<Task>>
    
    @GET("tasks/user/{userId}")
    suspend fun getTasksByUser(@Path("userId") userId: Int): Response<ApiResponse<List<Task>>>
    
    @GET("tasks/company/{companyId}")
    suspend fun getTasksByCompany(@Path("companyId") companyId: Int): Response<ApiResponse<List<Task>>>
    
    @GET("tasks/location/{locationId}")
    suspend fun getTasksByLocation(@Path("locationId") locationId: Int): Response<ApiResponse<List<Task>>>
    
    @POST("tasks")
    suspend fun createTask(@Body task: Task): Response<ApiResponse<Task>>
    
    @PUT("tasks/{id}")
    suspend fun updateTask(@Path("id") taskId: Int, @Body task: Task): Response<ApiResponse<Task>>
    
    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") taskId: Int): Response<ApiResponse<Boolean>>
    
    // Completaciones de tareas
    @GET("task-completions")
    suspend fun getTaskCompletions(): Response<ApiResponse<List<TaskCompletion>>>
    
    @GET("task-completions/task/{taskId}")
    suspend fun getTaskCompletionsByTask(@Path("taskId") taskId: Int): Response<ApiResponse<List<TaskCompletion>>>
    
    @GET("task-completions/user/{userId}")
    suspend fun getTaskCompletionsByUser(@Path("userId") userId: Int): Response<ApiResponse<List<TaskCompletion>>>
    
    @POST("task-completions")
    suspend fun createTaskCompletion(@Body completion: TaskCompletion): Response<ApiResponse<TaskCompletion>>
    
    @Multipart
    @POST("task-completions/with-signature")
    suspend fun createTaskCompletionWithSignature(
        @Part("completion") completion: TaskCompletion,
        @Part signature: MultipartBody.Part
    ): Response<ApiResponse<TaskCompletion>>
    
    @Multipart
    @POST("task-completions/with-photo")
    suspend fun createTaskCompletionWithPhoto(
        @Part("completion") completion: TaskCompletion,
        @Part photo: MultipartBody.Part
    ): Response<ApiResponse<TaskCompletion>>
    
    // Plantillas de etiquetas
    @GET("label-templates")
    suspend fun getLabelTemplates(): Response<ApiResponse<List<LabelTemplate>>>
    
    @GET("label-templates/{id}")
    suspend fun getLabelTemplate(@Path("id") templateId: Int): Response<ApiResponse<LabelTemplate>>
    
    @GET("label-templates/user/{userId}")
    suspend fun getLabelTemplatesByUser(@Path("userId") userId: Int): Response<ApiResponse<List<LabelTemplate>>>
    
    @GET("label-templates/company/{companyId}")
    suspend fun getLabelTemplatesByCompany(@Path("companyId") companyId: Int): Response<ApiResponse<List<LabelTemplate>>>
    
    @POST("label-templates")
    suspend fun createLabelTemplate(@Body template: LabelTemplate): Response<ApiResponse<LabelTemplate>>
    
    @PUT("label-templates/{id}")
    suspend fun updateLabelTemplate(@Path("id") templateId: Int, @Body template: LabelTemplate): Response<ApiResponse<LabelTemplate>>
    
    @DELETE("label-templates/{id}")
    suspend fun deleteLabelTemplate(@Path("id") templateId: Int): Response<ApiResponse<Boolean>>
    
    // Sincronización
    @GET("sync/tasks")
    suspend fun syncTasks(@Query("timestamp") timestamp: Long): Response<ApiResponse<List<Task>>>
    
    @GET("sync/users")
    suspend fun syncUsers(@Query("timestamp") timestamp: Long): Response<ApiResponse<List<User>>>
    
    @POST("sync/task-completions")
    suspend fun syncTaskCompletions(@Body completions: List<TaskCompletion>): Response<ApiResponse<List<TaskCompletion>>>
    
    @GET("sync/label-templates")
    suspend fun syncLabelTemplates(@Query("timestamp") timestamp: Long): Response<ApiResponse<List<LabelTemplate>>>
}