package com.productiva.android.utils

/**
 * Constantes utilizadas en toda la aplicación.
 */
object Constants {
    // Configuración
    const val DEBUG = true
    
    // API y red
    const val API_BASE_URL = "https://api.productiva.com/api/"
    const val CONNECTION_TIMEOUT = 30L // segundos
    
    // Preferencias
    const val PREFS_NAME = "productiva_prefs"
    const val PREF_AUTH_TOKEN = "auth_token"
    const val PREF_USER_ID = "user_id"
    const val PREF_USERNAME = "username"
    const val PREF_USER_ROLE = "user_role"
    const val PREF_COMPANY_ID = "company_id"
    const val PREF_COMPANY_NAME = "company_name"
    
    // Sincronización
    const val SYNC_INTERVAL_MINUTES = 15L
    
    // ID de notificaciones
    const val SYNC_NOTIFICATION_ID = 1001
    const val TASK_NOTIFICATION_ID = 1002
    
    // Impresora
    const val DEFAULT_PRINT_WIDTH = 62 // mm
    const val DEFAULT_PRINT_HEIGHT = 90 // mm
    const val DEFAULT_PRINT_DPI = 300
}

// IDs de canales de notificaciones
const val NOTIFICATION_CHANNEL_ID = "productiva_notification_channel"
const val SYNC_NOTIFICATION_CHANNEL_ID = "productiva_sync_notification_channel"
const val TASKS_NOTIFICATION_CHANNEL_ID = "productiva_tasks_notification_channel"