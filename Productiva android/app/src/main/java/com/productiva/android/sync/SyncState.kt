package com.productiva.android.sync

/**
 * Clase que representa el estado de sincronización actual.
 * Almacena información sobre las sincronizaciones realizadas y pendientes.
 *
 * @property isRunning Indica si la sincronización está en curso.
 * @property lastSyncAttemptTime Última vez que se intentó sincronizar (timestamp).
 * @property lastSuccessfulSyncTime Última vez que se sincronizó correctamente (timestamp).
 * @property lastError Último error ocurrido durante la sincronización.
 * @property pendingChangesCount Número de cambios pendientes de sincronización.
 */
data class SyncState(
    var isRunning: Boolean = false,
    var lastSyncAttemptTime: Long = 0,
    var lastSuccessfulSyncTime: Long = 0,
    var lastError: String? = null,
    var pendingChangesCount: Int = 0
) {
    /**
     * Indica si hay una sincronización en curso.
     *
     * @return true si hay una sincronización en curso, false en caso contrario.
     */
    fun isSyncing(): Boolean = isRunning
    
    /**
     * Indica si nunca se ha sincronizado.
     *
     * @return true si nunca se ha sincronizado, false en caso contrario.
     */
    fun neverSynced(): Boolean = lastSuccessfulSyncTime == 0L
    
    /**
     * Indica si hay cambios pendientes de sincronización.
     *
     * @return true si hay cambios pendientes, false en caso contrario.
     */
    fun hasPendingChanges(): Boolean = pendingChangesCount > 0
    
    /**
     * Indica si la última sincronización falló.
     *
     * @return true si la última sincronización falló, false en caso contrario.
     */
    fun lastSyncFailed(): Boolean = lastError != null
    
    /**
     * Obtiene el último error de sincronización.
     *
     * @return Mensaje de error o null si no hay error.
     */
    fun getLastErrorMessage(): String? = lastError
    
    /**
     * Formatea el estado de sincronización como un texto legible.
     *
     * @return Texto descriptivo del estado de sincronización.
     */
    fun getStatusText(): String {
        return when {
            isRunning -> "Sincronizando..."
            neverSynced() -> "Nunca sincronizado"
            lastSyncFailed() -> "Error: $lastError"
            else -> {
                val syncTime = android.text.format.DateUtils.getRelativeTimeSpanString(
                    lastSuccessfulSyncTime,
                    System.currentTimeMillis(),
                    android.text.format.DateUtils.MINUTE_IN_MILLIS
                )
                "Última sincronización: $syncTime"
            }
        }
    }
}