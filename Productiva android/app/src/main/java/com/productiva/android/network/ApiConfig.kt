package com.productiva.android.network

/**
 * Configuración de la API.
 * Contiene constantes y URLs para la comunicación con el servidor.
 */
object ApiConfig {
    /**
     * URL base de la API.
     * La URL base es la misma que la de la aplicación web, añadiendo "/api".
     */
    const val BASE_URL = "https://productiva.replit.app/api/"
    
    /**
     * Versión de la API.
     */
    const val API_VERSION = "v1"
    
    /**
     * Tiempo de caché en segundos.
     * Define cuánto tiempo se almacenarán en caché las respuestas.
     */
    const val CACHE_TIME_SECONDS = 3600 // 1 hora
    
    /**
     * Tamaño máximo de caché en bytes.
     */
    const val CACHE_SIZE_BYTES = 10 * 1024 * 1024 // 10 MB
    
    /**
     * Tiempo de conexión en segundos.
     */
    const val CONNECTION_TIMEOUT_SECONDS = 30L
    
    /**
     * Tiempo de lectura en segundos.
     */
    const val READ_TIMEOUT_SECONDS = 30L
    
    /**
     * Tiempo de escritura en segundos.
     */
    const val WRITE_TIMEOUT_SECONDS = 30L
    
    /**
     * Rutas de la API.
     * Contiene las rutas para los diferentes endpoints de la API.
     */
    object Endpoints {
        // Autenticación
        const val LOGIN = "auth/login"
        const val LOGOUT = "auth/logout"
        
        // Tareas
        const val TASKS = "tasks"
        const val TASK_DETAIL = "tasks/{id}"
        const val TASK_COMPLETE = "tasks/{id}/complete"
        const val TASKS_SYNC = "tasks/sync"
        
        // Productos
        const val PRODUCTS = "products"
        const val PRODUCT_DETAIL = "products/{id}"
        const val PRODUCTS_SYNC = "products/sync"
        
        // Plantillas de etiquetas
        const val LABEL_TEMPLATES = "label-templates"
        const val LABEL_TEMPLATE_DETAIL = "label-templates/{id}"
        const val LABEL_TEMPLATES_SYNC = "label-templates/sync"
        
        // Fichajes
        const val CHECKPOINTS = "checkpoints"
        const val CHECKPOINT_DETAIL = "checkpoints/{id}"
        const val CHECKPOINTS_SYNC = "checkpoints/sync"
    }
}