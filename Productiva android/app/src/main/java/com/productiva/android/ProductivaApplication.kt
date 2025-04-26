package com.productiva.android

import android.app.Application
import com.productiva.android.api.ApiClient
import com.productiva.android.api.ApiService
import com.productiva.android.repository.TaskRepository
import com.productiva.android.utils.AppDatabase
import com.productiva.android.utils.SessionManager

/**
 * Clase de aplicación principal
 * Inicializa los componentes globales y proporciona acceso a ellos
 */
class ProductivaApplication : Application() {
    
    // Componentes principales
    lateinit var database: AppDatabase
        private set
    
    lateinit var sessionManager: SessionManager
        private set
    
    lateinit var apiService: ApiService
        private set
    
    // Repositorios
    lateinit var taskRepository: TaskRepository
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar componentes
        sessionManager = SessionManager(applicationContext)
        database = AppDatabase.getDatabase(applicationContext)
        
        // Inicializar ApiService con la URL del servidor
        val serverUrl = sessionManager.getServerUrl()
        apiService = ApiClient.createApiService(serverUrl)
        
        // Inicializar repositorios
        taskRepository = TaskRepository(
            apiService = apiService,
            taskDao = database.taskDao(),
            context = applicationContext
        )
    }
    
    /**
     * Actualiza la URL del servidor y reinicializa el ApiService
     */
    fun updateServerUrl(serverUrl: String) {
        sessionManager.saveServerUrl(serverUrl)
        apiService = ApiClient.createApiService(serverUrl)
        
        // Reinicializar repositorios con el nuevo apiService
        taskRepository = TaskRepository(
            apiService = apiService,
            taskDao = database.taskDao(),
            context = applicationContext
        )
    }
}