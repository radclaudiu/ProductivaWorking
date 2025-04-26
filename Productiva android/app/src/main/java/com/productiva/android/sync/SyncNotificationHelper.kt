package com.productiva.android.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.productiva.android.ui.MainActivity
import com.productiva.android.utils.SYNC_NOTIFICATION_CHANNEL_ID
import com.productiva.android.utils.SYNC_NOTIFICATION_ID

/**
 * Helper para gestionar las notificaciones relacionadas con la sincronización.
 */
class SyncNotificationHelper(private val context: Context) {
    
    companion object {
        private const val TAG = "SyncNotificationHelper"
    }
    
    /**
     * Crea el canal de notificaciones necesario para Android 8.0 (API 26) y superior.
     */
    init {
        createNotificationChannel()
    }
    
    /**
     * Crea el canal de notificaciones para la sincronización.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Sincronización"
            val description = "Notificaciones de sincronización de datos"
            val importance = NotificationManager.IMPORTANCE_LOW
            
            val channel = NotificationChannel(SYNC_NOTIFICATION_CHANNEL_ID, name, importance).apply {
                this.description = description
                enableVibration(false)
                enableLights(false)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                    as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Muestra una notificación de progreso durante la sincronización.
     *
     * @param message Mensaje a mostrar.
     * @param progress Porcentaje de progreso (0-100).
     */
    fun showSyncProgressNotification(message: String, progress: Int) {
        val pendingIntent = createPendingIntent()
        
        val builder = NotificationCompat.Builder(context, SYNC_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle("Sincronizando Productiva")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
        
        if (progress > 0) {
            builder.setProgress(100, progress, false)
        } else {
            builder.setProgress(0, 0, true)
        }
        
        NotificationManagerCompat.from(context).apply {
            notify(SYNC_NOTIFICATION_ID, builder.build())
        }
    }
    
    /**
     * Actualiza una notificación de progreso existente.
     *
     * @param message Mensaje a mostrar.
     * @param progress Porcentaje de progreso (0-100).
     */
    fun updateSyncProgressNotification(message: String, progress: Int) {
        showSyncProgressNotification(message, progress)
    }
    
    /**
     * Muestra una notificación de sincronización completada.
     *
     * @param message Mensaje a mostrar.
     */
    fun showSyncCompletedNotification(message: String) {
        val pendingIntent = createPendingIntent()
        
        val builder = NotificationCompat.Builder(context, SYNC_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle("Sincronización completada")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        
        NotificationManagerCompat.from(context).apply {
            notify(SYNC_NOTIFICATION_ID, builder.build())
        }
    }
    
    /**
     * Muestra una notificación de error durante la sincronización.
     *
     * @param message Mensaje de error a mostrar.
     */
    fun showSyncErrorNotification(message: String) {
        val pendingIntent = createPendingIntent()
        
        val builder = NotificationCompat.Builder(context, SYNC_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Error de sincronización")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        
        NotificationManagerCompat.from(context).apply {
            notify(SYNC_NOTIFICATION_ID, builder.build())
        }
    }
    
    /**
     * Crea un PendingIntent para abrir la actividad principal al tocar la notificación.
     *
     * @return PendingIntent configurado.
     */
    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or 
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
    }
    
    /**
     * Cancela todas las notificaciones de sincronización.
     */
    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancel(SYNC_NOTIFICATION_ID)
    }
}