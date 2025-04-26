package com.productiva.android.sync

/**
 * Clase sellada que representa el estado de una operación de sincronización.
 * Se utiliza para actualizar la UI según el estado actual del proceso.
 */
sealed class SyncState {
    /**
     * Estado inicial o en reposo.
     */
    object Idle : SyncState()
    
    /**
     * Sincronización en progreso.
     *
     * @property progress Porcentaje de progreso (0-100).
     */
    data class Syncing(val progress: Int = 0) : SyncState()
    
    /**
     * Sincronización completada con éxito.
     *
     * @property result Resultado detallado de la sincronización.
     */
    data class Completed(val result: SyncResult.Success) : SyncState()
    
    /**
     * Error durante la sincronización.
     *
     * @property message Mensaje de error.
     * @property exception Excepción que causó el error (opcional).
     */
    data class Error(val message: String, val exception: Throwable? = null) : SyncState()
}