package com.productiva.android.network

import com.productiva.android.data.model.CheckpointData
import com.productiva.android.data.model.LabelTemplate
import com.productiva.android.data.model.Product
import com.productiva.android.data.model.Task
import com.productiva.android.data.model.TaskCompletion
import com.productiva.android.network.model.LoginRequest
import com.productiva.android.network.model.LoginResponse
import com.productiva.android.network.model.SyncRequest
import com.productiva.android.network.model.SyncResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Interfaz de servicio para la API REST.
 * Define todos los endpoints disponibles para la aplicación.
 */
interface ApiService {
    
    /**
     * Servicio de autenticación para iniciar sesión.
     *
     * @param loginRequest Credenciales de inicio de sesión.
     * @return Respuesta con token y datos de usuario.
     */
    @POST(ApiConfig.Endpoints.LOGIN)
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    /**
     * Servicio para cerrar sesión.
     *
     * @return Respuesta vacía.
     */
    @POST(ApiConfig.Endpoints.LOGOUT)
    suspend fun logout(): Response<Void>
    
    /**
     * Servicio para obtener todas las tareas.
     *
     * @return Lista de tareas.
     */
    @GET(ApiConfig.Endpoints.TASKS)
    suspend fun getTasks(): Response<List<Task>>
    
    /**
     * Servicio para obtener una tarea por su ID.
     *
     * @param id ID de la tarea.
     * @return Tarea.
     */
    @GET(ApiConfig.Endpoints.TASK_DETAIL)
    suspend fun getTaskById(@Path("id") id: Int): Response<Task>
    
    /**
     * Servicio para completar una tarea.
     *
     * @param id ID de la tarea.
     * @param completion Datos de completado.
     * @return Tarea actualizada.
     */
    @POST(ApiConfig.Endpoints.TASK_COMPLETE)
    suspend fun completeTask(@Path("id") id: Int, @Body completion: TaskCompletion): Response<Task>
    
    /**
     * Servicio para sincronizar tareas.
     *
     * @param syncRequest Datos de sincronización.
     * @return Respuesta de sincronización.
     */
    @POST(ApiConfig.Endpoints.TASKS_SYNC)
    suspend fun syncTasks(@Body syncRequest: SyncRequest<Task>): Response<SyncResponse<Task>>
    
    /**
     * Servicio para obtener todos los productos.
     *
     * @return Lista de productos.
     */
    @GET(ApiConfig.Endpoints.PRODUCTS)
    suspend fun getProducts(): Response<List<Product>>
    
    /**
     * Servicio para obtener un producto por su ID.
     *
     * @param id ID del producto.
     * @return Producto.
     */
    @GET(ApiConfig.Endpoints.PRODUCT_DETAIL)
    suspend fun getProductById(@Path("id") id: Int): Response<Product>
    
    /**
     * Servicio para sincronizar productos.
     *
     * @param syncRequest Datos de sincronización.
     * @return Respuesta de sincronización.
     */
    @POST(ApiConfig.Endpoints.PRODUCTS_SYNC)
    suspend fun syncProducts(@Body syncRequest: SyncRequest<Product>): Response<SyncResponse<Product>>
    
    /**
     * Servicio para obtener todas las plantillas de etiquetas.
     *
     * @return Lista de plantillas de etiquetas.
     */
    @GET(ApiConfig.Endpoints.LABEL_TEMPLATES)
    suspend fun getLabelTemplates(): Response<List<LabelTemplate>>
    
    /**
     * Servicio para obtener una plantilla de etiqueta por su ID.
     *
     * @param id ID de la plantilla.
     * @return Plantilla de etiqueta.
     */
    @GET(ApiConfig.Endpoints.LABEL_TEMPLATE_DETAIL)
    suspend fun getLabelTemplateById(@Path("id") id: Int): Response<LabelTemplate>
    
    /**
     * Servicio para sincronizar plantillas de etiquetas.
     *
     * @param syncRequest Datos de sincronización.
     * @return Respuesta de sincronización.
     */
    @POST(ApiConfig.Endpoints.LABEL_TEMPLATES_SYNC)
    suspend fun syncLabelTemplates(@Body syncRequest: SyncRequest<LabelTemplate>): Response<SyncResponse<LabelTemplate>>
    
    /**
     * Servicio para obtener todos los fichajes.
     *
     * @return Lista de fichajes.
     */
    @GET(ApiConfig.Endpoints.CHECKPOINTS)
    suspend fun getCheckpoints(): Response<List<CheckpointData>>
    
    /**
     * Servicio para obtener un fichaje por su ID.
     *
     * @param id ID del fichaje.
     * @return Fichaje.
     */
    @GET(ApiConfig.Endpoints.CHECKPOINT_DETAIL)
    suspend fun getCheckpointById(@Path("id") id: Int): Response<CheckpointData>
    
    /**
     * Servicio para sincronizar fichajes.
     *
     * @param syncRequest Datos de sincronización.
     * @return Respuesta de sincronización.
     */
    @POST(ApiConfig.Endpoints.CHECKPOINTS_SYNC)
    suspend fun syncCheckpoints(@Body syncRequest: SyncRequest<CheckpointData>): Response<SyncResponse<CheckpointData>>
}