package com.productiva.android

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.productiva.android.api.ApiClient
import com.productiva.android.api.ApiService
import com.productiva.android.bluetooth.BluetoothPrinterManager
import com.productiva.android.data.AppDatabase
import com.productiva.android.repository.TaskRepository
import com.productiva.android.utils.SessionManager

/**
 * Clase de aplicación personalizada para la inicialización global
 */
class ProductivaApplication : Application() {
    
    // Base de datos
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }
    
    // API Service
    val apiService: ApiService by lazy {
        ApiClient.create(sessionManager.getServerUrl())
    }
    
    // Session Manager
    val sessionManager: SessionManager by lazy {
        SessionManager(this)
    }
    
    // Bluetooth Adapter
    val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    
    // BluetoothPrinterManager
    val bluetoothPrinterManager: BluetoothPrinterManager by lazy {
        BluetoothPrinterManager(this)
    }
    
    // Repositorio de tareas
    val taskRepository: TaskRepository by lazy {
        TaskRepository(
            apiService = apiService,
            taskDao = database.taskDao(),
            taskCompletionDao = database.taskCompletionDao(),
            context = this
        )
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializaciones adicionales si son necesarias
    }
    
    /**
     * Actualiza la URL del servidor y recrea el ApiService
     */
    fun updateServerUrl(newUrl: String) {
        sessionManager.saveServerUrl(newUrl)
        // Re-crear el servicio API con la nueva URL
        ApiClient.resetInstance(newUrl)
    }
}