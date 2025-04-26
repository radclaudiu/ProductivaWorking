package com.productiva.android.network

import com.productiva.android.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de servicios para la API REST.
 * Define todos los endpoints disponibles para comunicarse con el servidor.
 */
interface ApiService {

    /**
     * Autenticación
     */
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    @POST("auth/logout")
    suspend fun logout(): Response<StatusResponse>
    
    /**
     * Endpoints para empresas
     */
    @GET("companies")
    suspend fun getCompanies(): Response<List<Company>>
    
    @GET("companies/{id}")
    suspend fun getCompanyById(@Path("id") id: Int): Response<Company>
    
    /**
     * Endpoints para empleados
     */
    @GET("employees")
    suspend fun getEmployees(@Query("company_id") companyId: Int? = null): Response<List<Employee>>
    
    @GET("employees/{id}")
    suspend fun getEmployeeById(@Path("id") id: Int): Response<Employee>
    
    /**
     * Endpoints para puntos de fichaje
     */
    @GET("checkpoints")
    suspend fun getCheckpoints(@Query("company_id") companyId: Int? = null): Response<List<Checkpoint>>
    
    @GET("checkpoints/{id}")
    suspend fun getCheckpointById(@Path("id") id: Int): Response<Checkpoint>
    
    /**
     * Endpoints para registros de fichaje
     */
    @GET("checkpoint_records")
    suspend fun getCheckpointRecords(
        @Query("employee_id") employeeId: Int? = null,
        @Query("checkpoint_id") checkpointId: Int? = null,
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null
    ): Response<List<CheckpointRecord>>
    
    @POST("checkpoint_records")
    suspend fun createCheckpointRecord(@Body record: CheckpointRecord): Response<CheckpointRecord>
    
    @PUT("checkpoint_records/{id}")
    suspend fun updateCheckpointRecord(
        @Path("id") id: Int, 
        @Body record: CheckpointRecord
    ): Response<CheckpointRecord>
    
    /**
     * Endpoints para tareas
     */
    @GET("tasks")
    suspend fun getTasks(
        @Query("company_id") companyId: Int? = null,
        @Query("status") status: String? = null
    ): Response<List<Task>>
    
    @GET("tasks/{id}")
    suspend fun getTaskById(@Path("id") id: Int): Response<Task>
    
    @POST("tasks")
    suspend fun createTask(@Body task: Task): Response<Task>
    
    @PUT("tasks/{id}")
    suspend fun updateTask(@Path("id") id: Int, @Body task: Task): Response<Task>
    
    /**
     * Endpoints para asignaciones de tareas
     */
    @GET("task_assignments")
    suspend fun getTaskAssignments(
        @Query("employee_id") employeeId: Int? = null,
        @Query("task_id") taskId: Int? = null,
        @Query("status") status: String? = null
    ): Response<List<TaskAssignment>>
    
    @POST("task_assignments")
    suspend fun createTaskAssignment(@Body assignment: TaskAssignment): Response<TaskAssignment>
    
    @PUT("task_assignments/{id}")
    suspend fun updateTaskAssignment(
        @Path("id") id: Int, 
        @Body assignment: TaskAssignment
    ): Response<TaskAssignment>
    
    /**
     * Endpoints para productos
     */
    @GET("products")
    suspend fun getProducts(
        @Query("company_id") companyId: Int? = null,
        @Query("category") category: String? = null
    ): Response<List<Product>>
    
    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Int): Response<Product>
    
    /**
     * Endpoints para plantillas de etiquetas
     */
    @GET("label_templates")
    suspend fun getLabelTemplates(@Query("company_id") companyId: Int? = null): Response<List<LabelTemplate>>
    
    @GET("label_templates/{id}")
    suspend fun getLabelTemplateById(@Path("id") id: Int): Response<LabelTemplate>
    
    /**
     * Endpoints para arqueos de caja
     */
    @GET("cash_registers")
    suspend fun getCashRegisters(
        @Query("company_id") companyId: Int? = null,
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null
    ): Response<List<CashRegister>>
    
    @POST("cash_registers")
    suspend fun createCashRegister(@Body cashRegister: CashRegister): Response<CashRegister>
    
    /**
     * Endpoint para sincronización
     */
    @POST("sync")
    suspend fun syncData(@Body syncRequest: SyncRequest): Response<SyncResponse>
    
    @GET("sync/last_update")
    suspend fun getLastUpdateTimestamp(): Response<Map<String, Long>>
}