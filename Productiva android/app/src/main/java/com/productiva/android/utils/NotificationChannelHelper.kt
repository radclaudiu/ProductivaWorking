package com.productiva.android.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Utilidad para crear y gestionar canales de notificación para Android O (API 26) y superiores.
 * En Android 8.0+, las notificaciones requieren canales para ser mostradas.
 */
object NotificationChannelHelper {
    
    // Canal para notificaciones de sincronización
    const val CHANNEL_SYNC = "channel_sync"
    
    // Canal para notificaciones de tareas
    const val CHANNEL_TASKS = "channel_tasks"
    
    // Canal para notificaciones de fichajes
    const val CHANNEL_CHECKPOINTS = "channel_checkpoints"
    
    /**
     * Crea todos los canales de notificación necesarios para la aplicación
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createSyncChannel(context)
            createTasksChannel(context)
            createCheckpointsChannel(context)
        }
    }
    
    /**
     * Crea el canal para notificaciones relacionadas con la sincronización
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createSyncChannel(context: Context) {
        val name = "Sincronización"
        val description = "Notificaciones sobre el estado de sincronización de datos"
        val importance = NotificationManager.IMPORTANCE_LOW
        
        val channel = NotificationChannel(CHANNEL_SYNC, name, importance).apply {
            this.description = description
            enableVibration(false)
            enableLights(false)
        }
        
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    
    /**
     * Crea el canal para notificaciones relacionadas con tareas
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createTasksChannel(context: Context) {
        val name = "Tareas"
        val description = "Notificaciones sobre tareas asignadas y pendientes"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        
        val channel = NotificationChannel(CHANNEL_TASKS, name, importance).apply {
            this.description = description
            enableVibration(true)
            enableLights(true)
        }
        
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    
    /**
     * Crea el canal para notificaciones relacionadas con fichajes
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createCheckpointsChannel(context: Context) {
        val name = "Fichajes"
        val description = "Notificaciones sobre fichajes pendientes y recordatorios"
        val importance = NotificationManager.IMPORTANCE_HIGH
        
        val channel = NotificationChannel(CHANNEL_CHECKPOINTS, name, importance).apply {
            this.description = description
            enableVibration(true)
            enableLights(true)
        }
        
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}