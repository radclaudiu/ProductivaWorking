package com.productiva.android.data.model

/**
 * Clase de datos para solicitudes de sincronización con el servidor.
 * 
 * @property deviceId Identificador único del dispositivo
 * @property lastSyncTimestamp Timestamp de la última sincronización exitosa
 * @property pendingOperations Lista de operaciones pendientes de sincronizar
 */
data class SyncRequest(
    val deviceId: String,
    val lastSyncTimestamp: Long,
    val pendingOperations: List<SyncOperation>
)

/**
 * Clase de datos para representar una operación pendiente de sincronización.
 * 
 * @property entityType Tipo de entidad (checkpoint_record, task, etc.)
 * @property entityId ID de la entidad afectada
 * @property operation Tipo de operación (CREATE, UPDATE, DELETE)
 * @property data Datos de la operación en formato JSON
 * @property timestamp Timestamp de cuando se realizó la operación
 */
data class SyncOperation(
    val entityType: String,
    val entityId: Long,
    val operation: String,
    val data: Map<String, Any>,
    val timestamp: Long = System.currentTimeMillis()
)