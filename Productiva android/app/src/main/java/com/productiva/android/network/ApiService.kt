package com.productiva.android.network

import com.productiva.android.data.model.CheckpointData
import com.productiva.android.data.model.LabelTemplate
import com.productiva.android.data.model.Product
import com.productiva.android.data.model.Task
import com.productiva.android.data.model.TaskCompletion
import com.productiva.android.network.model.ApiResponse
import com.productiva.android.network.model.LoginRequest
import com.productiva.android.network.model.LoginResponse
import com.productiva.android.network.model.SyncResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface que define los endpoints de la API REST para comunicación con el servidor.
 */
interface ApiService {
    // ---------- Autenticación ----------
    
    /**
     * Inicia sesión en el sistema.
     *
     * @param request Credenciales de inicio de sesión.
     * @return Respuesta de inicio de sesión.
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    
    /**
     * Cierra la sesión actual.
     *
     * @return Respuesta genérica.
     */
    @POST("auth/logout")
    suspend fun logout(): ApiResponse<Any>
    
    // ---------- Tareas ----------
    
    /**
     * Obtiene la lista de tareas de una empresa.
     *
     * @param companyId ID de la empresa.
     * @return Respuesta con lista de tareas.
     */
    @GET("tasks")
    suspend fun getTasks(
        @Query("company_id") companyId: Int
    ): SyncResponse<Task>
    
    /**
     * Obtiene una tarea por su ID.
     *
     * @param taskId ID de la tarea.
     * @return Tarea solicitada.
     */
    @GET("tasks/{id}")
    suspend fun getTaskById(@Path("id") taskId: Int): Task
    
    /**
     * Crea una nueva tarea.
     *
     * @param task Datos de la tarea a crear.
     * @return Tarea creada con ID asignado.
     */
    @POST("tasks")
    suspend fun createTask(@Body task: Task): Task
    
    /**
     * Actualiza una tarea existente.
     *
     * @param taskId ID de la tarea a actualizar.
     * @param task Datos actualizados de la tarea.
     * @return Tarea actualizada.
     */
    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Path("id") taskId: Int,
        @Body task: Task
    ): Task
    
    /**
     * Elimina una tarea.
     *
     * @param taskId ID de la tarea a eliminar.
     * @return Respuesta genérica.
     */
    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") taskId: Int): ApiResponse<Any>
    
    /**
     * Marca una tarea como completada.
     *
     * @param taskId ID de la tarea a completar.
     * @param completion Datos de completado.
     * @return Tarea actualizada.
     */
    @POST("tasks/{id}/complete")
    suspend fun completeTask(
        @Path("id") taskId: Int,
        @Body completion: TaskCompletion
    ): Task
    
    /**
     * Sincroniza tareas con el servidor.
     *
     * @param data Datos para sincronización.
     * @return Respuesta de sincronización.
     */
    @POST("tasks/sync")
    suspend fun syncTasks(@Body data: Map<String, Any>): ApiResponse<Map<String, SyncResponse<Task>>>
    
    // ---------- Productos ----------
    
    /**
     * Obtiene la lista de productos de una empresa.
     *
     * @param companyId ID de la empresa.
     * @return Respuesta con lista de productos.
     */
    @GET("products")
    suspend fun getProducts(
        @Query("company_id") companyId: Int
    ): SyncResponse<Product>
    
    /**
     * Obtiene un producto por su ID.
     *
     * @param productId ID del producto.
     * @return Producto solicitado.
     */
    @GET("products/{id}")
    suspend fun getProductById(@Path("id") productId: Int): Product
    
    /**
     * Crea un nuevo producto.
     *
     * @param product Datos del producto a crear.
     * @return Producto creado con ID asignado.
     */
    @POST("products")
    suspend fun createProduct(@Body product: Product): Product
    
    /**
     * Actualiza un producto existente.
     *
     * @param productId ID del producto a actualizar.
     * @param product Datos actualizados del producto.
     * @return Producto actualizado.
     */
    @PUT("products/{id}")
    suspend fun updateProduct(
        @Path("id") productId: Int,
        @Body product: Product
    ): Product
    
    /**
     * Elimina un producto.
     *
     * @param productId ID del producto a eliminar.
     * @return Respuesta genérica.
     */
    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") productId: Int): ApiResponse<Any>
    
    /**
     * Sincroniza productos con el servidor.
     *
     * @param data Datos para sincronización.
     * @return Respuesta de sincronización.
     */
    @POST("products/sync")
    suspend fun syncProducts(@Body data: Map<String, Any>): ApiResponse<SyncResponse<Product>>
    
    // ---------- Plantillas de etiquetas ----------
    
    /**
     * Obtiene la lista de plantillas de etiquetas de una empresa.
     *
     * @param companyId ID de la empresa.
     * @return Respuesta con lista de plantillas.
     */
    @GET("label-templates")
    suspend fun getLabelTemplates(
        @Query("company_id") companyId: Int
    ): SyncResponse<LabelTemplate>
    
    /**
     * Obtiene una plantilla de etiqueta por su ID.
     *
     * @param templateId ID de la plantilla.
     * @return Plantilla solicitada.
     */
    @GET("label-templates/{id}")
    suspend fun getLabelTemplateById(@Path("id") templateId: Int): LabelTemplate
    
    /**
     * Crea una nueva plantilla de etiqueta.
     *
     * @param template Datos de la plantilla a crear.
     * @return Plantilla creada con ID asignado.
     */
    @POST("label-templates")
    suspend fun createLabelTemplate(@Body template: LabelTemplate): LabelTemplate
    
    /**
     * Actualiza una plantilla de etiqueta existente.
     *
     * @param templateId ID de la plantilla a actualizar.
     * @param template Datos actualizados de la plantilla.
     * @return Plantilla actualizada.
     */
    @PUT("label-templates/{id}")
    suspend fun updateLabelTemplate(
        @Path("id") templateId: Int,
        @Body template: LabelTemplate
    ): LabelTemplate
    
    /**
     * Elimina una plantilla de etiqueta.
     *
     * @param templateId ID de la plantilla a eliminar.
     * @return Respuesta genérica.
     */
    @DELETE("label-templates/{id}")
    suspend fun deleteLabelTemplate(@Path("id") templateId: Int): ApiResponse<Any>
    
    /**
     * Sincroniza plantillas de etiquetas con el servidor.
     *
     * @param data Datos para sincronización.
     * @return Respuesta de sincronización.
     */
    @POST("label-templates/sync")
    suspend fun syncLabelTemplates(@Body data: Map<String, Any>): ApiResponse<SyncResponse<LabelTemplate>>
    
    // ---------- Fichajes ----------
    
    /**
     * Obtiene la lista de fichajes de una empresa para una fecha específica.
     *
     * @param companyId ID de la empresa.
     * @param date Fecha en formato "yyyy-MM-dd".
     * @return Lista de fichajes.
     */
    @GET("checkpoints")
    suspend fun getCheckpoints(
        @Query("company_id") companyId: Int,
        @Query("date") date: String
    ): List<CheckpointData>
    
    /**
     * Registra un fichaje (entrada o salida).
     *
     * @param checkpoint Datos del fichaje.
     * @return Fichaje registrado con ID asignado.
     */
    @POST("checkpoints")
    suspend fun registerCheckpoint(@Body checkpoint: CheckpointData): CheckpointData
    
    /**
     * Sincroniza fichajes con el servidor.
     *
     * @param data Datos para sincronización.
     * @return Respuesta de sincronización.
     */
    @POST("checkpoints/sync")
    suspend fun syncCheckpoints(@Body data: Map<String, Any>): ApiResponse<SyncResponse<CheckpointData>>
}