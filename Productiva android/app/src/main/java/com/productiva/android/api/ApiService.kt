package com.productiva.android.api

import com.productiva.android.model.Company
import com.productiva.android.model.Location
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
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

/**
 * Interfaz para los servicios de la API de Productiva
 * Define todas las llamadas a endpoints del servidor
 */
interface ApiService {
    /**
     * Autenticación
     */
    @FormUrlEncoded
    @POST("api/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<AuthResponse>
    
    /**
     * Usuarios
     */
    @GET("api/users")
    suspend fun getUsers(
        @Header("Authorization") token: String
    ): Response<List<User>>
    
    @GET("api/users/{id}")
    suspend fun getUserById(
        @Header("Authorization") token: String,
        @Path("id") userId: Int
    ): Response<User>
    
    /**
     * Tareas
     */
    @GET("api/tasks")
    suspend fun getTasks(
        @Header("Authorization") token: String,
        @Query("location_id") locationId: Int? = null
    ): Response<List<Task>>
    
    @GET("api/tasks/{id}")
    suspend fun getTaskById(
        @Header("Authorization") token: String,
        @Path("id") taskId: Int
    ): Response<Task>
    
    /**
     * Completado de tareas
     */
    @Multipart
    @POST("api/task_completions")
    suspend fun completeTask(
        @Header("Authorization") token: String,
        @Part("task_id") taskId: RequestBody,
        @Part("user_id") userId: RequestBody,
        @Part("location_id") locationId: RequestBody,
        @Part("notes") notes: RequestBody?,
        @Part photo: MultipartBody.Part?,
        @Part signature: MultipartBody.Part?
    ): Response<TaskCompletionResponse>
    
    /**
     * Empresas
     */
    @GET("api/companies")
    suspend fun getCompanies(
        @Header("Authorization") token: String
    ): Response<List<Company>>
    
    /**
     * Ubicaciones
     */
    @GET("api/locations")
    suspend fun getLocations(
        @Header("Authorization") token: String,
        @Query("company_id") companyId: Int? = null
    ): Response<List<Location>>
    
    /**
     * Sincronización
     */
    @POST("api/sync")
    suspend fun synchronize(
        @Header("Authorization") token: String,
        @Body syncData: SyncRequest
    ): Response<SyncResponse>
    
    /**
     * Impresión de etiquetas
     */
    @GET("api/tasks/{taskId}/label")
    suspend fun getTaskLabel(
        @Header("Authorization") token: String,
        @Path("taskId") taskId: Int
    ): Response<ResponseBody>
}

/**
 * Clases para respuestas de la API
 */
data class AuthResponse(
    val token: String,
    val user: User
)

data class TaskCompletionResponse(
    val id: Int,
    val message: String
)

data class SyncRequest(
    val taskCompletions: List<TaskCompletion>
)

data class SyncResponse(
    val success: Boolean,
    val message: String,
    val syncedItems: List<SyncedItem>
)

data class SyncedItem(
    val localId: Int,
    val serverId: Int,
    val type: String
)