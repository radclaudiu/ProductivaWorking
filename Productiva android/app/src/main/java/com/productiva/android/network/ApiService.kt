package com.productiva.android.network

import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.Product
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.model.User
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz de servicios de API para comunicarse con el servidor web.
 * Define todos los endpoints disponibles para la aplicación.
 */
interface ApiService {
    /**
     * Autenticación de usuario.
     */
    @FormUrlEncoded
    @POST("api/login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): ApiResponse<User>
    
    /**
     * Cierre de sesión.
     */
    @POST("api/logout")
    suspend fun logout(): ApiResponse<Unit>
    
    /**
     * Obtiene la información del usuario actual.
     */
    @GET("api/user")
    suspend fun getCurrentUser(): ApiResponse<User>
    
    // ===== TAREAS =====
    
    /**
     * Obtiene todas las tareas.
     */
    @GET("api/tasks")
    suspend fun getAllTasks(): ApiResponse<List<Task>>
    
    /**
     * Obtiene las tareas asignadas a un usuario.
     */
    @GET("api/tasks/user/{userId}")
    suspend fun getTasksByUser(
        @Path("userId") userId: Int
    ): ApiResponse<List<Task>>
    
    /**
     * Obtiene tareas por estado.
     */
    @GET("api/tasks/status/{status}")
    suspend fun getTasksByStatus(
        @Path("status") status: String
    ): ApiResponse<List<Task>>
    
    /**
     * Obtiene una tarea específica.
     */
    @GET("api/tasks/{taskId}")
    suspend fun getTaskById(
        @Path("taskId") taskId: Int
    ): ApiResponse<Task>
    
    /**
     * Completa una tarea.
     */
    @POST("api/tasks/{taskId}/complete")
    suspend fun completeTask(
        @Path("taskId") taskId: Int,
        @Body taskCompletion: TaskCompletion
    ): ApiResponse<Task>
    
    /**
     * Marca una tarea como cancelada.
     */
    @POST("api/tasks/{taskId}/cancel")
    suspend fun cancelTask(
        @Path("taskId") taskId: Int,
        @Body reason: Map<String, String>
    ): ApiResponse<Task>
    
    // ===== PRODUCTOS =====
    
    /**
     * Obtiene todos los productos.
     */
    @GET("api/products")
    suspend fun getAllProducts(): ApiResponse<List<Product>>
    
    /**
     * Obtiene los productos de una compañía.
     */
    @GET("api/products/company/{companyId}")
    suspend fun getProductsByCompany(
        @Path("companyId") companyId: Int
    ): ApiResponse<List<Product>>
    
    /**
     * Obtiene los productos de una ubicación.
     */
    @GET("api/products/location/{locationId}")
    suspend fun getProductsByLocation(
        @Path("locationId") locationId: Int
    ): ApiResponse<List<Product>>
    
    /**
     * Obtiene un producto específico.
     */
    @GET("api/products/{productId}")
    suspend fun getProductById(
        @Path("productId") productId: Int
    ): ApiResponse<Product>
    
    /**
     * Busca productos por nombre, código o código de barras.
     */
    @GET("api/products/search")
    suspend fun searchProducts(
        @Query("query") query: String
    ): ApiResponse<List<Product>>
    
    /**
     * Actualiza un producto.
     */
    @PUT("api/products/{productId}")
    suspend fun updateProduct(
        @Path("productId") productId: Int,
        @Body product: Product
    ): ApiResponse<Product>
    
    // ===== PLANTILLAS DE ETIQUETAS =====
    
    /**
     * Obtiene todas las plantillas de etiquetas.
     */
    @GET("api/label-templates")
    suspend fun getAllLabelTemplates(): ApiResponse<List<LabelTemplate>>
    
    /**
     * Obtiene una plantilla específica.
     */
    @GET("api/label-templates/{templateId}")
    suspend fun getLabelTemplateById(
        @Path("templateId") templateId: Int
    ): ApiResponse<LabelTemplate>
    
    /**
     * Obtiene plantillas por tipo.
     */
    @GET("api/label-templates/type/{type}")
    suspend fun getLabelTemplatesByType(
        @Path("type") type: String
    ): ApiResponse<List<LabelTemplate>>
    
    /**
     * Actualiza una plantilla.
     */
    @PUT("api/label-templates/{templateId}")
    suspend fun updateLabelTemplate(
        @Path("templateId") templateId: Int,
        @Body template: LabelTemplate
    ): ApiResponse<LabelTemplate>
    
    /**
     * Actualiza el contador de uso de una plantilla.
     */
    @POST("api/label-templates/{templateId}/increment-use")
    suspend fun incrementLabelTemplateUseCount(
        @Path("templateId") templateId: Int
    ): ApiResponse<LabelTemplate>
}