package com.productiva.android.network

import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de Retrofit que define los endpoints de la API de Productiva.
 */
interface ApiService {
    
    // Endpoints de Autenticación
    
    /**
     * Inicia sesión con credenciales.
     */
    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("username") username: String, 
        @Field("password") password: String
    ): Response<User>
    
    /**
     * Cierra la sesión actual.
     */
    @POST("logout")
    suspend fun logout(): Response<Unit>
    
    // Endpoints de Usuarios
    
    /**
     * Obtiene todos los usuarios.
     */
    @GET("users")
    suspend fun getUsers(): Response<List<User>>
    
    /**
     * Obtiene un usuario por su ID.
     */
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: Int): Response<User>
    
    /**
     * Obtiene usuarios por compañía.
     */
    @GET("users/company/{companyId}")
    suspend fun getUsersByCompany(@Path("companyId") companyId: Int): Response<List<User>>
    
    // Endpoints de Tareas
    
    /**
     * Obtiene todas las tareas.
     */
    @GET("tasks")
    suspend fun getTasks(): Response<List<Task>>
    
    /**
     * Obtiene tareas asignadas a un usuario.
     */
    @GET("tasks/assigned/{userId}")
    suspend fun getTasksAssignedToUser(@Path("userId") userId: Int): Response<List<Task>>
    
    /**
     * Obtiene tareas por estado.
     */
    @GET("tasks/status/{status}")
    suspend fun getTasksByStatus(@Path("status") status: String): Response<List<Task>>
    
    /**
     * Obtiene una tarea por su ID.
     */
    @GET("tasks/{id}")
    suspend fun getTaskById(@Path("id") taskId: Int): Response<Task>
    
    /**
     * Actualiza el estado de una tarea.
     */
    @FormUrlEncoded
    @PATCH("tasks/{id}/status")
    suspend fun updateTaskStatus(
        @Path("id") taskId: Int,
        @Field("status") status: String
    ): Response<Task>
    
    // Endpoints de Finalización de Tareas
    
    /**
     * Registra una finalización de tarea.
     */
    @Multipart
    @POST("tasks/{taskId}/complete")
    suspend fun completeTask(
        @Path("taskId") taskId: Int,
        @Part("completion") completion: RequestBody,
        @Part signature: MultipartBody.Part?,
        @Part photo: MultipartBody.Part?
    ): Response<TaskCompletion>
    
    /**
     * Obtiene las finalizaciones de una tarea.
     */
    @GET("tasks/{taskId}/completions")
    suspend fun getTaskCompletions(@Path("taskId") taskId: Int): Response<List<TaskCompletion>>
    
    // Endpoints de Plantillas de Etiquetas
    
    /**
     * Obtiene todas las plantillas de etiquetas.
     */
    @GET("label-templates")
    suspend fun getLabelTemplates(): Response<List<LabelTemplate>>
    
    /**
     * Obtiene plantillas de etiquetas por compañía.
     */
    @GET("label-templates/company/{companyId}")
    suspend fun getLabelTemplatesByCompany(@Path("companyId") companyId: Int): Response<List<LabelTemplate>>
    
    /**
     * Obtiene una plantilla de etiqueta por su ID.
     */
    @GET("label-templates/{id}")
    suspend fun getLabelTemplateById(@Path("id") templateId: Int): Response<LabelTemplate>
}