package com.productiva.android.api

import com.productiva.android.models.Location
import com.productiva.android.models.Task
import com.productiva.android.models.TaskCompletion
import com.productiva.android.models.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // Autenticación
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>
    
    // Obtener usuarios (normalmente para el portal de tareas)
    @GET("users")
    suspend fun getUsers(
        @Header("Authorization") token: String,
        @Query("location_id") locationId: Int? = null
    ): Response<List<User>>
    
    // Obtener información del usuario actual
    @GET("users/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<User>
    
    // Obtener ubicaciones
    @GET("locations")
    suspend fun getLocations(
        @Header("Authorization") token: String,
        @Query("company_id") companyId: Int? = null
    ): Response<List<Location>>
    
    // Obtener tareas
    @GET("tasks")
    suspend fun getTasks(
        @Header("Authorization") token: String,
        @Query("location_id") locationId: Int? = null,
        @Query("status") status: String? = null
    ): Response<List<Task>>
    
    // Obtener tareas por ID
    @GET("tasks/{taskId}")
    suspend fun getTaskById(
        @Header("Authorization") token: String,
        @Path("taskId") taskId: Int
    ): Response<Task>
    
    // Obtener completados de tareas
    @GET("task_completions")
    suspend fun getTaskCompletions(
        @Header("Authorization") token: String,
        @Query("task_id") taskId: Int? = null,
        @Query("user_id") userId: Int? = null
    ): Response<List<TaskCompletion>>
    
    // Completar una tarea
    @POST("task_completions")
    suspend fun completeTask(
        @Header("Authorization") token: String,
        @Body taskCompletion: TaskCompletion
    ): Response<TaskCompletion>
    
    // Subir una imagen/firma para completado de tarea
    @Multipart
    @POST("upload/task_completion")
    suspend fun uploadTaskCompletionFile(
        @Header("Authorization") token: String,
        @Part("task_completion_id") taskCompletionId: RequestBody,
        @Part("type") type: RequestBody, // "photo" o "signature"
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>
}