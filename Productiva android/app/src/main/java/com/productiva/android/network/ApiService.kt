package com.productiva.android.network

import com.productiva.android.data.model.CheckpointData
import com.productiva.android.data.model.LabelTemplate
import com.productiva.android.data.model.Product
import com.productiva.android.data.model.Task
import com.productiva.android.data.model.TaskCompletion
import com.productiva.android.network.model.ApiResponse
import com.productiva.android.network.model.AuthRequest
import com.productiva.android.network.model.AuthResponse
import com.productiva.android.network.model.Printer
import com.productiva.android.network.model.SyncResponse
import com.productiva.android.network.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.Date

/**
 * Interfaz de servicio Retrofit que define todas las operaciones de la API.
 */
interface ApiService {
    
    // ===== Autenticación y Usuario =====
    
    /**
     * Inicia sesión con credenciales de usuario.
     *
     * @param authRequest Credenciales de usuario.
     * @return Respuesta con token de autenticación e información del usuario.
     */
    @POST("auth/login")
    suspend fun login(@Body authRequest: AuthRequest): AuthResponse
    
    /**
     * Renueva el token de acceso usando el token de refresco.
     *
     * @return Respuesta con nuevo token de autenticación.
     */
    @POST("auth/refresh")
    suspend fun refreshToken(): AuthResponse
    
    /**
     * Obtiene información del usuario actual.
     *
     * @return Información del usuario autenticado.
     */
    @GET("user/info")
    suspend fun getUserInfo(): User
    
    /**
     * Cierra la sesión del usuario.
     *
     * @return Respuesta indicando el resultado.
     */
    @POST("auth/logout")
    suspend fun logout(): ApiResponse<Nothing>
    
    // ===== Productos =====
    
    /**
     * Obtiene todos los productos.
     *
     * @param companyId ID de la empresa (opcional).
     * @param lastSyncTime Marca de tiempo de la última sincronización.
     * @return Lista de productos.
     */
    @GET("products")
    suspend fun getProducts(
        @Query("company_id") companyId: Int? = null,
        @Query("last_sync") lastSyncTime: Long? = null
    ): SyncResponse<Product>
    
    /**
     * Obtiene un producto por su ID.
     *
     * @param productId ID del producto.
     * @return Producto.
     */
    @GET("products/{id}")
    suspend fun getProductById(@Path("id") productId: Int): Product
    
    /**
     * Crea un nuevo producto.
     *
     * @param product Datos del producto.
     * @return Producto creado.
     */
    @POST("products")
    suspend fun createProduct(@Body product: Product): Product
    
    /**
     * Actualiza un producto existente.
     *
     * @param productId ID del producto.
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
     * @param productId ID del producto.
     * @return Respuesta indicando el resultado.
     */
    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") productId: Int): ApiResponse<Nothing>
    
    /**
     * Sincroniza productos desde el dispositivo al servidor.
     *
     * @param products Lista de productos a sincronizar.
     * @param deletedIds Lista de IDs de productos eliminados localmente.
     * @return Respuesta de sincronización.
     */
    @POST("products/sync")
    suspend fun syncProducts(
        @Body syncData: Map<String, Any>
    ): ApiResponse<SyncResponse<Product>>
    
    // ===== Tareas =====
    
    /**
     * Obtiene todas las tareas.
     *
     * @param companyId ID de la empresa (opcional).
     * @param lastSyncTime Marca de tiempo de la última sincronización.
     * @param status Estado de las tareas (opcional).
     * @return Lista de tareas.
     */
    @GET("tasks")
    suspend fun getTasks(
        @Query("company_id") companyId: Int? = null,
        @Query("last_sync") lastSyncTime: Long? = null,
        @Query("status") status: String? = null
    ): SyncResponse<Task>
    
    /**
     * Obtiene una tarea por su ID.
     *
     * @param taskId ID de la tarea.
     * @return Tarea.
     */
    @GET("tasks/{id}")
    suspend fun getTaskById(@Path("id") taskId: Int): Task
    
    /**
     * Crea una nueva tarea.
     *
     * @param task Datos de la tarea.
     * @return Tarea creada.
     */
    @POST("tasks")
    suspend fun createTask(@Body task: Task): Task
    
    /**
     * Actualiza una tarea existente.
     *
     * @param taskId ID de la tarea.
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
     * @param taskId ID de la tarea.
     * @return Respuesta indicando el resultado.
     */
    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") taskId: Int): ApiResponse<Nothing>
    
    /**
     * Completa una tarea.
     *
     * @param taskId ID de la tarea.
     * @param completion Datos de completado.
     * @return Tarea completada.
     */
    @POST("tasks/{id}/complete")
    suspend fun completeTask(
        @Path("id") taskId: Int,
        @Body completion: TaskCompletion
    ): Task
    
    /**
     * Registra completado de tarea con archivos adjuntos (foto y firma).
     *
     * @param taskId ID de la tarea.
     * @param completionData Datos de completado como RequestBody.
     * @param photo Foto adjunta (opcional).
     * @param signature Firma digital (opcional).
     * @return Tarea completada.
     */
    @Multipart
    @POST("tasks/{id}/complete-with-files")
    suspend fun completeTaskWithFiles(
        @Path("id") taskId: Int,
        @Part("completion") completionData: RequestBody,
        @Part photo: MultipartBody.Part?,
        @Part signature: MultipartBody.Part?
    ): Task
    
    /**
     * Sincroniza tareas desde el dispositivo al servidor.
     *
     * @param syncData Datos de sincronización con tareas y completados a sincronizar.
     * @return Respuesta de sincronización.
     */
    @POST("tasks/sync")
    suspend fun syncTasks(
        @Body syncData: Map<String, Any>
    ): ApiResponse<Map<String, SyncResponse<*>>>
    
    // ===== Plantillas de Etiquetas =====
    
    /**
     * Obtiene todas las plantillas de etiquetas.
     *
     * @param companyId ID de la empresa (opcional).
     * @param lastSyncTime Marca de tiempo de la última sincronización.
     * @return Lista de plantillas.
     */
    @GET("label-templates")
    suspend fun getLabelTemplates(
        @Query("company_id") companyId: Int? = null,
        @Query("last_sync") lastSyncTime: Long? = null
    ): SyncResponse<LabelTemplate>
    
    /**
     * Obtiene una plantilla de etiqueta por su ID.
     *
     * @param templateId ID de la plantilla.
     * @return Plantilla de etiqueta.
     */
    @GET("label-templates/{id}")
    suspend fun getLabelTemplateById(@Path("id") templateId: Int): LabelTemplate
    
    /**
     * Crea una nueva plantilla de etiqueta.
     *
     * @param template Datos de la plantilla.
     * @return Plantilla creada.
     */
    @POST("label-templates")
    suspend fun createLabelTemplate(@Body template: LabelTemplate): LabelTemplate
    
    /**
     * Actualiza una plantilla de etiqueta existente.
     *
     * @param templateId ID de la plantilla.
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
     * @param templateId ID de la plantilla.
     * @return Respuesta indicando el resultado.
     */
    @DELETE("label-templates/{id}")
    suspend fun deleteLabelTemplate(@Path("id") templateId: Int): ApiResponse<Nothing>
    
    /**
     * Sincroniza plantillas de etiquetas desde el dispositivo al servidor.
     *
     * @param syncData Datos de sincronización.
     * @return Respuesta de sincronización.
     */
    @POST("label-templates/sync")
    suspend fun syncLabelTemplates(
        @Body syncData: Map<String, Any>
    ): ApiResponse<SyncResponse<LabelTemplate>>
    
    // ===== Impresoras =====
    
    /**
     * Obtiene todas las impresoras configuradas.
     *
     * @param companyId ID de la empresa (opcional).
     * @return Lista de impresoras.
     */
    @GET("printers")
    suspend fun getPrinters(
        @Query("company_id") companyId: Int? = null
    ): List<Printer>
    
    /**
     * Obtiene una impresora por su ID.
     *
     * @param printerId ID de la impresora.
     * @return Impresora.
     */
    @GET("printers/{id}")
    suspend fun getPrinterById(@Path("id") printerId: Int): Printer
    
    /**
     * Registra una nueva impresora en el servidor.
     *
     * @param printer Datos de la impresora.
     * @return Impresora registrada.
     */
    @POST("printers")
    suspend fun registerPrinter(@Body printer: Printer): Printer
    
    /**
     * Actualiza una impresora existente.
     *
     * @param printerId ID de la impresora.
     * @param printer Datos actualizados de la impresora.
     * @return Impresora actualizada.
     */
    @PUT("printers/{id}")
    suspend fun updatePrinter(
        @Path("id") printerId: Int,
        @Body printer: Printer
    ): Printer
    
    /**
     * Elimina una impresora.
     *
     * @param printerId ID de la impresora.
     * @return Respuesta indicando el resultado.
     */
    @DELETE("printers/{id}")
    suspend fun deletePrinter(@Path("id") printerId: Int): ApiResponse<Nothing>
    
    // ===== Fichajes =====
    
    /**
     * Obtiene los fichajes del día actual.
     *
     * @param companyId ID de la empresa (opcional).
     * @param employeeId ID del empleado (opcional).
     * @param date Fecha para filtrar (formato yyyy-MM-dd).
     * @return Lista de fichajes.
     */
    @GET("checkpoints")
    suspend fun getCheckpoints(
        @Query("company_id") companyId: Int? = null,
        @Query("employee_id") employeeId: Int? = null,
        @Query("date") date: String? = null
    ): List<CheckpointData>
    
    /**
     * Registra un nuevo fichaje (entrada o salida).
     *
     * @param checkpoint Datos del fichaje.
     * @return Fichaje registrado.
     */
    @POST("checkpoints")
    suspend fun registerCheckpoint(@Body checkpoint: CheckpointData): CheckpointData
    
    /**
     * Sincroniza fichajes desde el dispositivo al servidor.
     *
     * @param syncData Datos de sincronización.
     * @return Respuesta de sincronización.
     */
    @POST("checkpoints/sync")
    suspend fun syncCheckpoints(
        @Body syncData: Map<String, Any>
    ): ApiResponse<SyncResponse<CheckpointData>>
}