package com.productiva.android.data.model

/**
 * Clase de datos para respuestas de sincronización desde el servidor.
 * 
 * @property success Indica si la sincronización fue exitosa
 * @property timestamp Timestamp del servidor para esta sincronización
 * @property message Mensaje descriptivo sobre el resultado
 * @property updates Datos actualizados desde el servidor
 * @property operationResults Resultados de las operaciones pendientes enviadas
 * @property conflicts Conflictos detectados durante la sincronización
 */
data class SyncResponse(
    val success: Boolean,
    val timestamp: Long,
    val message: String? = null,
    val updates: SyncUpdates? = null,
    val operationResults: List<SyncOperationResult>? = null,
    val conflicts: List<SyncConflict>? = null
)

/**
 * Clase de datos para las actualizaciones enviadas desde el servidor.
 * 
 * @property companies Empresas actualizadas
 * @property employees Empleados actualizados
 * @property checkpoints Puntos de fichaje actualizados
 * @property tasks Tareas actualizadas
 * @property products Productos actualizados
 * @property labelTemplates Plantillas de etiquetas actualizadas
 */
data class SyncUpdates(
    val companies: List<Company>? = null,
    val employees: List<Employee>? = null,
    val checkpoints: List<Checkpoint>? = null,
    val tasks: List<Task>? = null,
    val products: List<Product>? = null,
    val labelTemplates: List<LabelTemplate>? = null
)

/**
 * Clase de datos para los resultados de operaciones pendientes.
 * 
 * @property id ID de la operación en el cliente
 * @property success Indica si la operación se aplicó exitosamente
 * @property serverId ID asignado por el servidor (para operaciones CREATE)
 * @property message Mensaje descriptivo sobre el resultado
 */
data class SyncOperationResult(
    val id: Long,
    val success: Boolean,
    val serverId: Long? = null,
    val message: String? = null
)

/**
 * Clase de datos para conflictos de sincronización.
 * 
 * @property entityType Tipo de entidad en conflicto
 * @property entityId ID de la entidad en conflicto
 * @property localData Datos locales que están en conflicto
 * @property serverData Datos del servidor que están en conflicto
 * @property conflictType Tipo de conflicto (UPDATE_CONFLICT, DELETE_CONFLICT, etc.)
 */
data class SyncConflict(
    val entityType: String,
    val entityId: Long,
    val localData: Map<String, Any>? = null,
    val serverData: Map<String, Any>? = null,
    val conflictType: String
)