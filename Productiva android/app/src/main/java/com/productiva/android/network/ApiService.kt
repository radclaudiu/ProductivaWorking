package com.productiva.android.network

import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.Product
import com.productiva.android.model.Task
import com.productiva.android.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz de Retrofit para comunicarse con la API del servidor.
 */
interface ApiService {
    // Endpoints de autenticación
    
    /**
     * Inicia sesión con las credenciales proporcionadas.
     */
    @POST("api/auth/login")
    suspend fun login(@Body credentials: Map<String, String>): Response<LoginResponse>
    
    /**
     * Cierra la sesión actual.
     */
    @POST("api/auth/logout")
    suspend fun logout(): Response<Map<String, Any>>
    
    /**
     * Obtiene información del usuario actual.
     */
    @GET("api/auth/user")
    suspend fun getCurrentUser(): Response<User>
    
    // Endpoints de tareas
    
    /**
     * Obtiene todas las tareas.
     */
    @GET("api/tasks")
    suspend fun getAllTasks(): Response<List<Task>>
    
    /**
     * Obtiene tareas asignadas a un usuario específico.
     */
    @GET("api/tasks/user/{user_id}")
    suspend fun getTasksAssignedToUser(@Path("user_id") userId: Int): Response<List<Task>>
    
    /**
     * Obtiene una tarea por su ID.
     */
    @GET("api/tasks/{task_id}")
    suspend fun getTaskById(@Path("task_id") taskId: Int): Response<Task>
    
    /**
     * Completa una tarea con los datos proporcionados.
     */
    @POST("api/tasks/{task_id}/complete")
    suspend fun completeTask(
        @Path("task_id") taskId: Int,
        @Body completionData: Map<String, Any>
    ): Response<Task>
    
    // Endpoints de productos
    
    /**
     * Obtiene todos los productos.
     */
    @GET("api/products")
    suspend fun getAllProducts(): Response<List<Product>>
    
    /**
     * Obtiene productos por ubicación.
     */
    @GET("api/products/location/{location_id}")
    suspend fun getProductsByLocation(@Path("location_id") locationId: Int): Response<List<Product>>
    
    /**
     * Obtiene productos por empresa.
     */
    @GET("api/products/company/{company_id}")
    suspend fun getProductsByCompany(@Path("company_id") companyId: Int): Response<List<Product>>
    
    /**
     * Obtiene un producto por su ID.
     */
    @GET("api/products/{product_id}")
    suspend fun getProductById(@Path("product_id") productId: Int): Response<Product>
    
    /**
     * Actualiza un producto con los datos proporcionados.
     */
    @POST("api/products/{product_id}")
    suspend fun updateProduct(
        @Path("product_id") productId: Int,
        @Body productData: Map<String, Any>
    ): Response<Product>
    
    // Endpoints de plantillas de etiquetas
    
    /**
     * Obtiene todas las plantillas de etiquetas.
     */
    @GET("api/label-templates")
    suspend fun getLabelTemplates(): Response<List<LabelTemplate>>
    
    /**
     * Obtiene una plantilla de etiqueta por su ID.
     */
    @GET("api/label-templates/{template_id}")
    suspend fun getLabelTemplateById(@Path("template_id") templateId: Int): Response<LabelTemplate>
    
    /**
     * Actualiza el contador de uso de una plantilla.
     */
    @POST("api/label-templates/{template_id}/usage")
    suspend fun updateLabelTemplateUsage(
        @Path("template_id") templateId: Int,
        @Body usageData: Map<String, Any>
    ): Response<LabelTemplate>
    
    // Endpoints de ubicaciones
    
    /**
     * Obtiene todas las ubicaciones.
     */
    @GET("api/locations")
    suspend fun getLocations(): Response<List<LocationResponse>>
    
    /**
     * Obtiene una ubicación por su ID.
     */
    @GET("api/locations/{location_id}")
    suspend fun getLocationById(@Path("location_id") locationId: Int): Response<LocationResponse>
    
    // Endpoints de usuarios
    
    /**
     * Obtiene todos los usuarios.
     */
    @GET("api/users")
    suspend fun getUsers(): Response<List<User>>
    
    /**
     * Obtiene usuarios por empresa.
     */
    @GET("api/users/company/{company_id}")
    suspend fun getUsersByCompany(@Path("company_id") companyId: Int): Response<List<User>>
    
    // Endpoints de búsqueda
    
    /**
     * Busca productos por nombre, código o código de barras.
     */
    @GET("api/search/products")
    suspend fun searchProducts(@Query("query") query: String): Response<List<Product>>
}

/**
 * Clase de respuesta para el login.
 */
data class LoginResponse(
    val token: String,
    val expiresAt: String,
    val user: User
)

/**
 * Clase de respuesta para ubicaciones.
 */
data class LocationResponse(
    val id: Int,
    val name: String,
    val address: String?,
    val company_id: Int,
    val company_name: String?,
    val is_active: Boolean
)