package com.productiva.android.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import android.util.Log

/**
 * Utilidad para crear y gestionar canales de notificación en Android 8.0+.
 */
object NotificationChannelHelper {
    
    // Constantes para canales de notificación
    const val CHANNEL_SYNC = "sync_channel"
    const val CHANNEL_TASKS = "tasks_channel"
    const val CHANNEL_PRODUCTS = "products_channel"
    const val CHANNEL_PRINTER = "printer_channel"
    const val CHANNEL_CHECKPOINTS = "checkpoints_channel"
    
    /**
     * Crea todos los canales de notificación necesarios para la aplicación.
     * Solo tiene efecto en Android 8.0 (API 26) y superior.
     *
     * @param context Contexto de la aplicación.
     */
    fun createNotificationChannels(context: Context) {
        // Solo crear canales en Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createSyncChannel(context)
            createTasksChannel(context)
            createProductsChannel(context)
            createPrinterChannel(context)
            createCheckpointsChannel(context)
            
            Log.d("NotificationChannel", "Canales de notificación creados")
        }
    }
    
    /**
     * Crea el canal de notificaciones para sincronización.
     *
     * @param context Contexto de la aplicación.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createSyncChannel(context: Context) {
        val name = "Sincronización"
        val description = "Notificaciones sobre sincronización de datos"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        
        val channel = NotificationChannel(CHANNEL_SYNC, name, importance).apply {
            this.description = description
            enableVibration(true)
        }
        
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    
    /**
     * Crea el canal de notificaciones para tareas.
     *
     * @param context Contexto de la aplicación.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createTasksChannel(context: Context) {
        val name = "Tareas"
        val description = "Notificaciones sobre tareas pendientes y completadas"
        val importance = NotificationManager.IMPORTANCE_HIGH // Alta prioridad para tareas
        
        val channel = NotificationChannel(CHANNEL_TASKS, name, importance).apply {
            this.description = description
            enableVibration(true)
        }
        
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    
    /**
     * Crea el canal de notificaciones para productos.
     *
     * @param context Contexto de la aplicación.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createProductsChannel(context: Context) {
        val name = "Productos"
        val description = "Notificaciones sobre inventario y actualización de productos"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        
        val channel = NotificationChannel(CHANNEL_PRODUCTS, name, importance).apply {
            this.description = description
            enableVibration(true)
        }
        
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    
    /**
     * Crea el canal de notificaciones para la impresora.
     *
     * @param context Contexto de la aplicación.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPrinterChannel(context: Context) {
        val name = "Impresora"
        val description = "Notificaciones sobre el estado de impresión de etiquetas"
        val importance = NotificationManager.IMPORTANCE_HIGH // Alta prioridad para impresión
        
        val channel = NotificationChannel(CHANNEL_PRINTER, name, importance).apply {
            this.description = description
            enableVibration(true)
        }
        
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    
    /**
     * Crea el canal de notificaciones para fichajes.
     *
     * @param context Contexto de la aplicación.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createCheckpointsChannel(context: Context) {
        val name = "Fichajes"
        val description = "Notificaciones sobre fichajes pendientes y registros de jornada"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        
        val channel = NotificationChannel(CHANNEL_CHECKPOINTS, name, importance).apply {
            this.description = description
            enableVibration(true)
        }
        
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}