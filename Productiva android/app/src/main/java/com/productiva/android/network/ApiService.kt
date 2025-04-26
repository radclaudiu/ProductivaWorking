package com.productiva.android.network

import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.Product
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz para la comunicación con la API del portal web.
 * Define todos los endpoints y operaciones que pueden realizarse.
 */
interface ApiService {
    /**
     * Autenticación de usuario.
     *
     * @param email Email del usuario.
     * @param password Contraseña del usuario.
     * @return Respuesta con los datos del usuario autenticado y el token.
     */
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<ApiResponse<LoginResponse>>
    
    /**
     * Cierre de sesión.
     *
     * @return Respuesta de confirmación.
     */
    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Any>>
    
    /**
     * Obtiene el usuario actual.
     *
     * @return Datos del usuario actual.
     */
    @GET("auth/user")
    suspend fun getCurrentUser(): Response<ApiResponse<User>>
    
    /**
     * Obtiene todos los productos.
     *
     * @param lastSyncTime Timestamp de la última sincronización para obtener solo productos nuevos o actualizados.
     * @return Lista de productos.
     */
    @GET("products")
    suspend fun getProducts(
        @Query("last_sync") lastSyncTime: Long? = null
    ): Response<ApiResponse<List<Product>>>
    
    /**
     * Obtiene las tareas para el usuario actual.
     *
     * @param lastSyncTime Timestamp de la última sincronización para obtener solo tareas nuevas o actualizadas.
     * @return Lista de tareas.
     */
    @GET("tasks")
    suspend fun getTasks(
        @Query("last_sync") lastSyncTime: Long? = null
    ): Response<ApiResponse<List<Task>>>
    
    /**
     * Completa una tarea.
     *
     * @param taskId ID de la tarea.
     * @param completion Datos de la completación.
     * @return Respuesta con la confirmación.
     */
    @POST("tasks/{taskId}/complete")
    suspend fun completeTask(
        @Path("taskId") taskId: Int,
        @Body completion: TaskCompletion
    ): Response<ApiResponse<TaskCompletion>>
    
    /**
     * Sincroniza completaciones de tareas realizadas offline.
     *
     * @param completions Lista de completaciones de tareas.
     * @return Respuesta con las completaciones procesadas.
     */
    @POST("tasks/sync-completions")
    suspend fun syncTaskCompletions(
        @Body completions: List<TaskCompletion>
    ): Response<ApiResponse<SyncResponse<TaskCompletion>>>
    
    /**
     * Obtiene las plantillas de etiquetas.
     *
     * @param lastSyncTime Timestamp de la última sincronización para obtener solo plantillas nuevas o actualizadas.
     * @return Lista de plantillas de etiquetas.
     */
    @GET("label-templates")
    suspend fun getLabelTemplates(
        @Query("last_sync") lastSyncTime: Long? = null
    ): Response<ApiResponse<List<LabelTemplate>>>
    
    /**
     * Obtiene un producto por su ID.
     *
     * @param productId ID del producto.
     * @return Datos del producto.
     */
    @GET("products/{productId}")
    suspend fun getProductById(
        @Path("productId") productId: Int
    ): Response<ApiResponse<Product>>
    
    /**
     * Crea un nuevo producto.
     *
     * @param product Datos del producto.
     * @return Producto creado.
     */
    @POST("products")
    suspend fun createProduct(
        @Body product: Product
    ): Response<ApiResponse<Product>>
    
    /**
     * Actualiza un producto existente.
     *
     * @param productId ID del producto.
     * @param product Datos del producto.
     * @return Producto actualizado.
     */
    @PUT("products/{productId}")
    suspend fun updateProduct(
        @Path("productId") productId: Int,
        @Body product: Product
    ): Response<ApiResponse<Product>>
    
    /**
     * Elimina un producto.
     *
     * @param productId ID del producto.
     * @return Confirmación de eliminación.
     */
    @DELETE("products/{productId}")
    suspend fun deleteProduct(
        @Path("productId") productId: Int
    ): Response<ApiResponse<Any>>
    
    /**
     * Sincroniza productos creados o actualizados offline.
     *
     * @param products Lista de productos.
     * @return Respuesta con los productos procesados.
     */
    @POST("products/sync")
    suspend fun syncProducts(
        @Body products: List<Product>
    ): Response<ApiResponse<SyncResponse<Product>>>
}

/**
 * Modelo para el resultado de sincronización.
 */
data class SyncResponse<T>(
    val added: List<T> = emptyList(),
    val updated: List<T> = emptyList(),
    val deleted: List<Int> = emptyList(),
    val conflicts: List<SyncConflict<T>> = emptyList()
)

/**
 * Modelo para conflictos de sincronización.
 */
data class SyncConflict<T>(
    val id: Int,
    val localData: T,
    val serverData: T,
    val resolution: String = "server" // "server" o "local"
)

/**
 * Modelo para la respuesta de login.
 */
data class LoginResponse(
    val user: User,
    val token: String
)