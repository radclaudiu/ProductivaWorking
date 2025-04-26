package com.productiva.android.sync

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.productiva.android.R
import com.productiva.android.ui.MainActivity
import com.productiva.android.utils.SYNC_NOTIFICATION_CHANNEL_ID
import com.productiva.android.utils.SYNC_NOTIFICATION_ID

/**
 * Clase auxiliar para mostrar notificaciones relacionadas con la sincronización.
 * Muestra notificaciones de progreso, finalización y errores durante el proceso.
 */
class SyncNotificationHelper(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        private const val TAG = "SyncNotifHelper"
    }
    
    /**
     * Muestra una notificación de sincronización en progreso.
     */
    fun showSyncInProgressNotification() {
        try {
            val notification = NotificationCompat.Builder(context, SYNC_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setContentTitle("Sincronizando datos")
                .setContentText("Sincronizando con el servidor...")
                .setProgress(100, 0, true)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(createMainActivityPendingIntent())
                .build()
            
            notificationManager.notify(SYNC_NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error al mostrar notificación de sincronización", e)
        }
    }
    
    /**
     * Actualiza la notificación de progreso con un porcentaje específico.
     *
     * @param progress Porcentaje de progreso (0-100).
     */
    fun updateSyncProgressNotification(progress: Int) {
        try {
            val notification = NotificationCompat.Builder(context, SYNC_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setContentTitle("Sincronizando datos")
                .setContentText("Progreso: $progress%")
                .setProgress(100, progress, false)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(createMainActivityPendingIntent())
                .build()
            
            notificationManager.notify(SYNC_NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar notificación de progreso", e)
        }
    }
    
    /**
     * Muestra una notificación de sincronización completada.
     *
     * @param addedCount Número de elementos añadidos.
     * @param updatedCount Número de elementos actualizados.
     * @param deletedCount Número de elementos eliminados.
     */
    fun showSyncCompletedNotification(
        addedCount: Int,
        updatedCount: Int,
        deletedCount: Int
    ) {
        try {
            val totalItems = addedCount + updatedCount + deletedCount
            
            val contentText = if (totalItems > 0) {
                "Se sincronizaron $totalItems elementos"
            } else {
                "Todos los datos están al día"
            }
            
            val notification = NotificationCompat.Builder(context, SYNC_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("Sincronización completada")
                .setContentText(contentText)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(createMainActivityPendingIntent())
                .build()
            
            notificationManager.notify(SYNC_NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error al mostrar notificación de sincronización completada", e)
        }
    }
    
    /**
     * Muestra una notificación de error de sincronización.
     *
     * @param errorMessage Mensaje de error.
     */
    fun showSyncErrorNotification(errorMessage: String) {
        try {
            val notification = NotificationCompat.Builder(context, SYNC_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle("Error de sincronización")
                .setContentText(errorMessage)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(createMainActivityPendingIntent())
                .build()
            
            notificationManager.notify(SYNC_NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error al mostrar notificación de error", e)
        }
    }
    
    /**
     * Cancela cualquier notificación de sincronización activa.
     */
    fun cancelSyncNotification() {
        try {
            notificationManager.cancel(SYNC_NOTIFICATION_ID)
        } catch (e: Exception) {
            Log.e(TAG, "Error al cancelar notificación", e)
        }
    }
    
    /**
     * Crea un PendingIntent para abrir la actividad principal.
     *
     * @return PendingIntent configurado.
     */
    private fun createMainActivityPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        return PendingIntent.getActivity(context, 0, intent, flags)
    }
}