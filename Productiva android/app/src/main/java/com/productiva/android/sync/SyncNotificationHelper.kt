package com.productiva.android.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.productiva.android.ProductivaApplication
import com.productiva.android.R
import com.productiva.android.ui.MainActivity
import com.productiva.android.utils.SYNC_NOTIFICATION_CHANNEL_ID
import com.productiva.android.utils.SYNC_NOTIFICATION_ID

/**
 * Helper para manejar las notificaciones relacionadas con la sincronización.
 */
class SyncNotificationHelper(private val context: Context) {
    
    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Crea el canal de notificaciones para las sincronizaciones.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Sincronización"
            val descriptionText = "Notificaciones sobre el estado de sincronización"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(SYNC_NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Muestra una notificación de sincronización en progreso.
     */
    fun showSyncInProgressNotification() {
        val notification = NotificationCompat.Builder(context, SYNC_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sync)
            .setContentTitle("Sincronizando datos")
            .setContentText("Estamos sincronizando los datos con el servidor...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setProgress(0, 0, true)
            .setContentIntent(createContentIntent())
            .build()
        
        notificationManager.notify(SYNC_NOTIFICATION_ID, notification)
    }
    
    /**
     * Muestra una notificación de sincronización completada.
     */
    fun showSyncCompletedNotification() {
        val notification = NotificationCompat.Builder(context, SYNC_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sync_done)
            .setContentTitle("Sincronización completada")
            .setContentText("Todos los datos están actualizados")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(createContentIntent())
            .build()
        
        notificationManager.notify(SYNC_NOTIFICATION_ID, notification)
    }
    
    /**
     * Muestra una notificación de error en la sincronización.
     *
     * @param errorMessage Mensaje de error para mostrar.
     */
    fun showSyncErrorNotification(errorMessage: String) {
        val notification = NotificationCompat.Builder(context, SYNC_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sync_error)
            .setContentTitle("Error de sincronización")
            .setContentText(errorMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(errorMessage))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(createContentIntent())
            .build()
        
        notificationManager.notify(SYNC_NOTIFICATION_ID, notification)
    }
    
    /**
     * Crea un PendingIntent para abrir la aplicación cuando se toca la notificación.
     */
    private fun createContentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}