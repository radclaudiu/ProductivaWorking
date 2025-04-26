package com.productiva.android.data.model

/**
 * Clase de datos que representa el estado actual de sincronización.
 * 
 * @property isSuccessful Indica si la última sincronización fue exitosa
 * @property lastSyncTimestamp Marca de tiempo de la última sincronización (en milisegundos)
 * @property pendingChanges Número de cambios pendientes por sincronizar (opcional)
 */
data class SyncState(
    val isSuccessful: Boolean,
    val lastSyncTimestamp: Long,
    val pendingChanges: Int = 0
) {
    /**
     * Calcula si la sincronización es reciente (menos de 1 hora)
     */
    fun isSyncRecent(): Boolean {
        val currentTime = System.currentTimeMillis()
        val oneHourInMillis = 60 * 60 * 1000
        return (currentTime - lastSyncTimestamp) < oneHourInMillis
    }
    
    /**
     * Calcula el tiempo transcurrido desde la última sincronización en minutos
     */
    fun getLastSyncTimeInMinutes(): Int {
        val currentTime = System.currentTimeMillis()
        val diffInMillis = currentTime - lastSyncTimestamp
        return (diffInMillis / (60 * 1000)).toInt()
    }
    
    /**
     * Obtiene un mensaje de estado legible sobre la sincronización
     */
    fun getStatusMessage(): String {
        return when {
            !isSuccessful -> "Última sincronización: Error"
            pendingChanges > 0 -> "Pendiente: $pendingChanges cambios"
            isSyncRecent() -> "Sincronizado hace ${getLastSyncTimeInMinutes()} minutos"
            else -> "Última sincronización: ${formatTimestamp(lastSyncTimestamp)}"
        }
    }
    
    /**
     * Formatea una marca de tiempo en formato legible
     */
    private fun formatTimestamp(timestamp: Long): String {
        // Implementación simplificada - en una app real usaríamos DateFormat
        val minutes = getLastSyncTimeInMinutes()
        
        return when {
            minutes < 60 -> "$minutes minutos"
            minutes < 24 * 60 -> "${minutes / 60} horas"
            else -> "${minutes / (24 * 60)} días"
        }
    }
}