package com.productiva.android

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.multidex.MultiDex
import androidx.work.Configuration
import com.productiva.android.data.AppDatabase
import com.productiva.android.sync.SyncManager
import com.productiva.android.utils.NotificationChannelHelper

/**
 * Clase principal de la aplicación.
 * Inicializa componentes y configuraciones globales.
 */
class ProductivaApplication : Application(), Configuration.Provider {
    
    companion object {
        private const val TAG = "ProductivaApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "Inicializando aplicación Productiva")
        
        // Inicializar MultiDex para dispositivos con API < 21
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            MultiDex.install(this)
        }
        
        // Inicializar canales de notificación para Android 8.0+
        NotificationChannelHelper.createNotificationChannels(this)
        
        // Inicializar base de datos
        AppDatabase.getDatabase(this)
        
        // Inicializar y programar sincronización
        val syncManager = SyncManager.getInstance(this)
        syncManager.startPeriodicSync()
        
        Log.d(TAG, "Aplicación Productiva inicializada")
    }
    
    /**
     * Proporciona la configuración para WorkManager.
     * Necesario para la interfaz Configuration.Provider.
     */
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
    }
}