package com.productiva.android

import android.app.Application
import android.os.StrictMode
import android.util.Log
import com.brother.sdk.BrotherPrintLibrary
import com.productiva.android.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Clase de aplicación principal que se inicializa cuando la app inicia.
 * Se encarga de inicializar componentes globales como la base de datos y el SDK de Brother.
 */
class ProductivaApplication : Application() {
    
    // Crea un CoroutineScope para la aplicación
    private val applicationScope = CoroutineScope(SupervisorJob())
    
    // Base de datos Room de la aplicación
    val database by lazy { AppDatabase.getInstance(this) }
    
    companion object {
        private const val TAG = "ProductivaApplication"
        
        // Instancia singleton de la aplicación
        lateinit var instance: ProductivaApplication
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Configura la instancia singleton
        instance = this
        
        // Configura StrictMode para desarrollo
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )
            
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }
        
        // Inicializa el SDK de impresión Brother
        try {
            BrotherPrintLibrary.initialize(this)
            Log.d(TAG, "Brother SDK inicializado correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar Brother SDK", e)
        }
        
        // Otras inicializaciones aquí...
        
        Log.d(TAG, "Aplicación Productiva inicializada")
    }
}