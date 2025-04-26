package com.productiva.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.productiva.android.database.AppDatabase
import com.productiva.android.network.ApiService
import com.productiva.android.network.RetrofitClient
import com.productiva.android.repository.LabelTemplateRepository
import com.productiva.android.repository.ProductRepository
import com.productiva.android.repository.TaskRepository
import com.productiva.android.services.BrotherPrintService
import com.productiva.android.session.SessionManager
import com.productiva.android.sync.SyncManager
import com.productiva.android.utils.ConnectivityMonitor
import com.productiva.android.utils.NOTIFICATION_CHANNEL_ID
import com.productiva.android.utils.SYNC_NOTIFICATION_CHANNEL_ID
import com.productiva.android.utils.TASKS_NOTIFICATION_CHANNEL_ID

/**
 * Clase de aplicación principal.
 * Inicializa los componentes principales y proporciona acceso a los mismos.
 */
class ProductivaApplication : Application() {
    
    companion object {
        private const val TAG = "ProductivaApplication"
    }
    
    // Servicios y repositorios de la aplicación
    private lateinit var connectivityMonitor: ConnectivityMonitor
    private lateinit var database: AppDatabase
    private lateinit var apiService: ApiService
    private lateinit var taskRepository: TaskRepository
    private lateinit var productRepository: ProductRepository
    private lateinit var labelTemplateRepository: LabelTemplateRepository
    private lateinit var syncManager: SyncManager
    private lateinit var brotherPrintService: BrotherPrintService
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar componentes principales
        initComponents()
        
        // Crear canales de notificación
        createNotificationChannels()
        
        // Iniciar sincronización periódica
        syncManager.scheduleSyncWork()
        
        // Inicializar el SessionManager
        SessionManager.getInstance().init(this)
        
        Log.d(TAG, "Aplicación inicializada correctamente")
    }
    
    /**
     * Inicializa los componentes principales de la aplicación.
     */
    private fun initComponents() {
        // Inicializar monitor de conectividad
        connectivityMonitor = ConnectivityMonitor.getInstance(this)
        
        // Inicializar base de datos
        database = AppDatabase.getInstance(this)
        
        // Inicializar servicio API
        apiService = RetrofitClient.getApiService(this)
        
        // Inicializar repositorios
        taskRepository = TaskRepository(
            database.taskDao(),
            database.taskCompletionDao(),
            apiService,
            connectivityMonitor
        )
        
        productRepository = ProductRepository(
            database.productDao(),
            apiService,
            connectivityMonitor
        )
        
        labelTemplateRepository = LabelTemplateRepository(
            database.labelTemplateDao(),
            apiService,
            connectivityMonitor
        )
        
        // Inicializar servicio de impresión
        brotherPrintService = BrotherPrintService(this)
        
        // Inicializar gestor de sincronización
        syncManager = SyncManager.getInstance(
            this,
            taskRepository,
            productRepository
        )
    }
    
    /**
     * Crea los canales de notificación para Android 8.0+.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Canal principal de notificaciones
            val mainChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Notificaciones generales",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones generales de la aplicación"
            }
            
            // Canal de sincronización
            val syncChannel = NotificationChannel(
                SYNC_NOTIFICATION_CHANNEL_ID,
                "Sincronización",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones sobre el estado de sincronización"
            }
            
            // Canal de tareas
            val tasksChannel = NotificationChannel(
                TASKS_NOTIFICATION_CHANNEL_ID,
                "Tareas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones sobre tareas pendientes y recordatorios"
            }
            
            // Registrar los canales
            notificationManager.createNotificationChannels(
                listOf(mainChannel, syncChannel, tasksChannel)
            )
        }
    }
    
    /**
     * Obtiene el monitor de conectividad.
     */
    fun getConnectivityMonitor(): ConnectivityMonitor = connectivityMonitor
    
    /**
     * Obtiene la instancia de la base de datos.
     */
    fun getDatabase(): AppDatabase = database
    
    /**
     * Obtiene el servicio API.
     */
    fun getApiService(): ApiService = apiService
    
    /**
     * Obtiene el repositorio de tareas.
     */
    fun getTaskRepository(): TaskRepository = taskRepository
    
    /**
     * Obtiene el repositorio de productos.
     */
    fun getProductRepository(): ProductRepository = productRepository
    
    /**
     * Obtiene el repositorio de plantillas de etiquetas.
     */
    fun getLabelTemplateRepository(): LabelTemplateRepository = labelTemplateRepository
    
    /**
     * Obtiene el gestor de sincronización.
     */
    fun getSyncManager(): SyncManager = syncManager
    
    /**
     * Obtiene el servicio de impresión Brother.
     */
    fun getBrotherPrintService(): BrotherPrintService = brotherPrintService
}