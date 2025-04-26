package com.productiva.android.sync

/**
 * Clase sellada que representa el resultado de una operación de sincronización.
 * Puede ser [Success] o [Error].
 */
sealed class SyncResult {
    
    /**
     * Resultado exitoso de sincronización.
     * Contiene estadísticas detalladas de los cambios aplicados en cada tipo de entidad.
     *
     * @property taskChanges Estadísticas de cambios de tareas.
     * @property productChanges Estadísticas de cambios de productos.
     * @property labelTemplateChanges Estadísticas de cambios de plantillas de etiquetas.
     * @property checkpointChanges Estadísticas de cambios de fichajes.
     */
    data class Success(
        val taskChanges: SyncChanges,
        val productChanges: SyncChanges,
        val labelTemplateChanges: SyncChanges,
        val checkpointChanges: SyncChanges
    ) : SyncResult() {
        
        /**
         * Calcula el número total de entidades enviadas en la sincronización.
         *
         * @return Total de entidades enviadas.
         */
        fun getTotalSent(): Int = taskChanges.sent + productChanges.sent + 
                            labelTemplateChanges.sent + checkpointChanges.sent
        
        /**
         * Calcula el número total de entidades recibidas en la sincronización.
         *
         * @return Total de entidades recibidas.
         */
        fun getTotalReceived(): Int = taskChanges.received + productChanges.received + 
                               labelTemplateChanges.received + checkpointChanges.received
        
        /**
         * Calcula el número total de entidades aplicadas en el servidor.
         *
         * @return Total de entidades aplicadas.
         */
        fun getTotalApplied(): Int = taskChanges.applied + productChanges.applied + 
                              labelTemplateChanges.applied + checkpointChanges.applied
        
        /**
         * Calcula el número total de entidades fallidas en el servidor.
         *
         * @return Total de entidades fallidas.
         */
        fun getTotalFailed(): Int = taskChanges.failed + productChanges.failed + 
                             labelTemplateChanges.failed + checkpointChanges.failed
        
        /**
         * Calcula el número total de conflictos resueltos.
         *
         * @return Total de conflictos resueltos.
         */
        fun getTotalConflicts(): Int = taskChanges.conflicts + productChanges.conflicts + 
                               labelTemplateChanges.conflicts + checkpointChanges.conflicts
    }
    
    /**
     * Resultado erróneo de sincronización.
     * Contiene el mensaje de error producido.
     *
     * @property message Mensaje de error.
     */
    data class Error(val message: String) : SyncResult()
}

/**
 * Estadísticas de cambios para un tipo de entidad durante la sincronización.
 *
 * @property sent Número de entidades enviadas al servidor.
 * @property received Número de entidades recibidas del servidor.
 * @property applied Número de entidades aplicadas correctamente en el servidor.
 * @property failed Número de entidades que fallaron al aplicarse en el servidor.
 * @property conflicts Número de conflictos resueltos.
 */
data class SyncChanges(
    val sent: Int = 0,
    val received: Int = 0,
    val applied: Int = 0,
    val failed: Int = 0,
    val conflicts: Int = 0
)