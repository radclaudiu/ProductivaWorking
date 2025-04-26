package com.productiva.android.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.productiva.android.R
import com.productiva.android.ui.MainActivity

/**
 * Clase de ayuda para mostrar notificaciones relacionadas con la sincronización.
 * Gestiona el canal de notificaciones y ofrece métodos para mostrar diferentes
 * tipos de notificaciones.
 */
class SyncNotificationHelper(private val context: Context) {
    
    companion object {
        private const val CHANNEL_ID = "sync_notification_channel"
        private const val SYNC_NOTIFICATION_ID = 1001
        private const val SYNC_PROGRESS_NOTIFICATION_ID = 1002
    }
    
    init {
        // Crear canal de notificación (obligatorio en Android 8.0+)
        createNotificationChannel()
    }
    
    /**
     * Crea el canal de notificación para Android 8.0+.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Sincronización"
            val descriptionText = "Notificaciones de sincronización de datos"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            
            // Registrar el canal con el sistema
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Muestra una notificación indicando que la sincronización ha comenzado.
     */
    fun showSyncStartedNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sync)
            .setContentTitle("Sincronizando")
            .setContentText("Actualizando datos con el servidor...")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setProgress(0, 0, true)
        
        // Mostrar notificación
        NotificationManagerCompat.from(context).notify(
            SYNC_PROGRESS_NOTIFICATION_ID, builder.build()
        )
    }
    
    /**
     * Muestra una notificación indicando que la sincronización ha finalizado con éxito.
     */
    fun showSyncCompletedNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sync_success)
            .setContentTitle("Sincronización completada")
            .setContentText("Los datos se han sincronizado correctamente")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Cancelar notificación de progreso
        NotificationManagerCompat.from(context).cancel(SYNC_PROGRESS_NOTIFICATION_ID)
        
        // Mostrar notificación de éxito
        NotificationManagerCompat.from(context).notify(
            SYNC_NOTIFICATION_ID, builder.build()
        )
    }
    
    /**
     * Muestra una notificación indicando que la sincronización ha fallado.
     *
     * @param errorMessage Mensaje de error que describe el fallo.
     */
    fun showSyncFailedNotification(errorMessage: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sync_error)
            .setContentTitle("Error de sincronización")
            .setContentText(errorMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(errorMessage))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Cancelar notificación de progreso
        NotificationManagerCompat.from(context).cancel(SYNC_PROGRESS_NOTIFICATION_ID)
        
        // Mostrar notificación de error
        NotificationManagerCompat.from(context).notify(
            SYNC_NOTIFICATION_ID, builder.build()
        )
    }
}