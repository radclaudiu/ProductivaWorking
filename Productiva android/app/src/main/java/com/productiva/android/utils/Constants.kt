package com.productiva.android.utils

/**
 * URL base de la API del portal web.
 * Se debe reemplazar por la URL real del servidor de producción.
 */
const val API_BASE_URL = "https://productiva.replit.app/api/"

/**
 * Valor constante para manejar permisos.
 */
const val PERMISSION_REQUEST_CODE = 123

/**
 * Valor constante para solicitar permisos de cámara.
 */
const val CAMERA_PERMISSION_REQUEST_CODE = 124

/**
 * Valor constante para solicitar permisos de almacenamiento.
 */
const val STORAGE_PERMISSION_REQUEST_CODE = 125

/**
 * Valor constante para el canal de notificaciones principal.
 */
const val NOTIFICATION_CHANNEL_ID = "com.productiva.android.NOTIFICATIONS"

/**
 * Valor constante para el canal de notificaciones de sincronización.
 */
const val SYNC_NOTIFICATION_CHANNEL_ID = "com.productiva.android.SYNC"

/**
 * Valor constante para el canal de notificaciones de tareas.
 */
const val TASKS_NOTIFICATION_CHANNEL_ID = "com.productiva.android.TASKS"

/**
 * Valor constante para el ID de notificación de sincronización.
 */
const val SYNC_NOTIFICATION_ID = 1001

/**
 * Valor constante para preferencias compartidas.
 */
const val PREFS_NAME = "com.productiva.android.PREFS"

/**
 * Valor constante para la clave de token de autenticación en preferencias.
 */
const val PREF_AUTH_TOKEN = "auth_token"

/**
 * Valor constante para la clave de datos de usuario en preferencias.
 */
const val PREF_USER_DATA = "user_data"

/**
 * Valor constante para la clave de último tiempo de sincronización en preferencias.
 */
const val PREF_LAST_SYNC_TIME = "last_sync_time"

/**
 * Intervalo mínimo entre sincronizaciones automáticas (en milisegundos).
 * Por defecto, 15 minutos.
 */
const val MIN_SYNC_INTERVAL = 15 * 60 * 1000L

/**
 * Intervalo de sincronización periódica (en milisegundos).
 * Por defecto, 30 minutos.
 */
const val SYNC_INTERVAL = 30 * 60 * 1000L

/**
 * Máximo tamaño de archivo para fotos (en bytes).
 * Por defecto, 10 MB.
 */
const val MAX_FILE_SIZE = 10 * 1024 * 1024L

/**
 * Calidad de compresión para imágenes JPEG.
 * Rango de 0 a 100, donde 100 es la mejor calidad.
 */
const val IMAGE_COMPRESSION_QUALITY = 85

/**
 * Ancho máximo para imágenes redimensionadas.
 */
const val MAX_IMAGE_WIDTH = 1200

/**
 * Alto máximo para imágenes redimensionadas.
 */
const val MAX_IMAGE_HEIGHT = 1200