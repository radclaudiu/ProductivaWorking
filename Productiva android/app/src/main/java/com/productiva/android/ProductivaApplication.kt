package com.productiva.android

import android.app.Application
import android.util.Log
import com.productiva.android.database.AppDatabase
import com.productiva.android.session.SessionManager
import com.productiva.android.sync.SyncJobService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Clase de aplicación principal.
 * Se encarga de inicializar componentes y servicios comunes.
 */
class ProductivaApplication : Application() {
    
    private val TAG = "ProductivaApplication"
    
    // Database instance
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    
    // Application scope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "Inicializando aplicación Productiva")
        
        // Inicializar el gestor de sesión
        SessionManager.init(applicationContext)
        
        // Programar sincronización periódica
        SyncJobService.scheduleSyncJob(applicationContext)
        
        // Iniciar sincronización inicial en segundo plano
        scheduleInitialSync()
        
        Log.d(TAG, "Aplicación Productiva inicializada correctamente")
    }
    
    /**
     * Programa una sincronización inicial en segundo plano.
     */
    private fun scheduleInitialSync() {
        applicationScope.launch {
            try {
                // Si hay sesión activa, programar sincronización inmediata
                if (SessionManager.getInstance().isLoggedIn()) {
                    Log.d(TAG, "Sesión activa detectada, programando sincronización inicial")
                    SyncJobService.scheduleImmediateSync(applicationContext)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al programar sincronización inicial", e)
            }
        }
    }
}