package com.productiva.android

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.brother.sdk.BrotherPrintLibrary
import com.productiva.android.database.AppDatabase
import com.productiva.android.utils.AppLogger
import com.productiva.android.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Clase de aplicación principal para inicializar componentes esenciales
 */
class ProductivaApplication : Application() {
    
    // Ámbito de corrutina para la aplicación
    private val applicationScope = CoroutineScope(SupervisorJob())
    
    // Base de datos única en toda la aplicación
    lateinit var database: AppDatabase
        private set
    
    // Gestor de preferencias
    lateinit var preferenceManager: PreferenceManager
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        instance = this
        
        // Inicializar logger
        AppLogger.init(this)
        
        // Inicializar base de datos
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "productiva_database"
        )
            .fallbackToDestructiveMigration() // En producción usar migrations adecuadas
            .build()
        
        // Inicializar gestor de preferencias
        preferenceManager = PreferenceManager(this)
        
        // Inicializar SDK de Brother
        initializeBrotherSDK()
        
        AppLogger.d(TAG, "Aplicación inicializada correctamente")
    }
    
    /**
     * Inicializa el SDK de Brother para impresión
     */
    private fun initializeBrotherSDK() {
        try {
            // Inicializar biblioteca de Brother
            BrotherPrintLibrary.initialize(this)
            AppLogger.d(TAG, "SDK de Brother inicializado correctamente")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error al inicializar SDK de Brother: ${e.message}")
        }
    }
    
    companion object {
        private const val TAG = "ProductivaApplication"
        
        lateinit var instance: ProductivaApplication
            private set
    }
}