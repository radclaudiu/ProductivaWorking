package com.productiva.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.productiva.android.session.SessionManager
import com.productiva.android.sync.SyncManager
import com.productiva.android.utils.NOTIFICATION_CHANNEL_ID
import com.productiva.android.utils.SYNC_NOTIFICATION_CHANNEL_ID
import com.productiva.android.utils.TASKS_NOTIFICATION_CHANNEL_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Clase de aplicación personalizada para inicializar componentes clave
 * y configurar recursos globales al inicio de la aplicación.
 */
class ProductivaApplication : Application() {
    
    private val applicationScope = CoroutineScope(Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar componentes en segundo plano
        applicationScope.launch {
            // Crear canales de notificación
            createNotificationChannels()
            
            // Inicializar el gestor de sesión
            initSessionManager()
            
            // Inicializar el administrador de sincronización
            initSyncManager()
            
            Log.d(TAG, "Inicialización completada")
        }
    }
    
    /**
     * Inicializa el gestor de sesión.
     */
    private fun initSessionManager() {
        try {
            val sessionManager = SessionManager.getInstance()
            sessionManager.init(this)
            Log.d(TAG, "Gestor de sesión inicializado")
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar gestor de sesión", e)
        }
    }
    
    /**
     * Inicializa el administrador de sincronización y programa sincronizaciones periódicas.
     */
    private fun initSyncManager() {
        try {
            val syncManager = SyncManager.getInstance(this)
            
            // Programar sincronizaciones periódicas en segundo plano
            syncManager.schedulePeriodicalSync()
            
            Log.d(TAG, "Administrador de sincronización inicializado")
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar administrador de sincronización", e)
        }
    }
    
    /**
     * Crea los canales de notificación necesarios para Android 8.0 (API 26) y superior.
     */
    private fun createNotificationChannels() {
        // Solo crear canales en Android 8.0 (API 26) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Canal para notificaciones generales
            val generalChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Notificaciones generales",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones generales de la aplicación"
                enableVibration(true)
                enableLights(true)
            }
            
            // Canal para notificaciones de sincronización
            val syncChannel = NotificationChannel(
                SYNC_NOTIFICATION_CHANNEL_ID,
                "Sincronización",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones sobre sincronización de datos"
                enableVibration(false)
                enableLights(false)
            }
            
            // Canal para notificaciones de tareas
            val tasksChannel = NotificationChannel(
                TASKS_NOTIFICATION_CHANNEL_ID,
                "Tareas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones sobre tareas y vencimientos"
                enableVibration(true)
                enableLights(true)
            }
            
            // Registrar todos los canales
            notificationManager.createNotificationChannels(
                listOf(generalChannel, syncChannel, tasksChannel)
            )
            
            Log.d(TAG, "Canales de notificación creados")
        }
    }
    
    companion object {
        private const val TAG = "ProductivaApplication"
    }
}