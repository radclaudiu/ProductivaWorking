package com.productiva.android.sync

/**
 * Clase sellada que representa el resultado de una operación de sincronización.
 */
sealed class SyncResult {
    /**
     * Sincronización exitosa.
     *
     * @property addedCount Número de elementos añadidos.
     * @property updatedCount Número de elementos actualizados.
     * @property deletedCount Número de elementos eliminados.
     * @property timestamp Marca de tiempo de la sincronización.
     */
    data class Success(
        val addedCount: Int = 0,
        val updatedCount: Int = 0,
        val deletedCount: Int = 0,
        val timestamp: Long = System.currentTimeMillis()
    ) : SyncResult()
    
    /**
     * Error durante la sincronización.
     *
     * @property message Mensaje de error.
     * @property exception Excepción que causó el error (opcional).
     */
    data class Error(
        val message: String,
        val exception: Throwable? = null
    ) : SyncResult()
}