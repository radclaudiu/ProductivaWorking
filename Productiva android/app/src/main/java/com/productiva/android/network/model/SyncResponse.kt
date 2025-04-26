package com.productiva.android.network.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de respuesta para sincronización de datos.
 * Contiene las entidades actualizadas del servidor.
 *
 * @param T Tipo de la entidad sincronizada.
 * @property timestamp Marca de tiempo de la sincronización actual.
 * @property serverChanges Lista de entidades con cambios desde el servidor.
 * @property conflictResolutions Lista de resoluciones de conflictos.
 * @property appliedChanges Lista de IDs de entidades cuyas modificaciones fueron aplicadas en el servidor.
 * @property failedChanges Lista de IDs de entidades cuyas modificaciones fallaron en el servidor.
 */
data class SyncResponse<T>(
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("server_changes") val serverChanges: List<T>,
    @SerializedName("conflict_resolutions") val conflictResolutions: List<ConflictResolution<T>>,
    @SerializedName("applied_changes") val appliedChanges: List<Int>,
    @SerializedName("failed_changes") val failedChanges: List<FailedChange>
)

/**
 * Modelo para resolución de conflictos durante la sincronización.
 *
 * @param T Tipo de la entidad con conflicto.
 * @property id ID de la entidad en conflicto.
 * @property resolution Entidad resultante tras la resolución del conflicto.
 * @property conflictType Tipo de conflicto.
 */
data class ConflictResolution<T>(
    @SerializedName("id") val id: Int,
    @SerializedName("resolution") val resolution: T,
    @SerializedName("conflict_type") val conflictType: String
)

/**
 * Modelo para cambios fallidos durante la sincronización.
 *
 * @property id ID de la entidad que falló al sincronizar.
 * @property reason Motivo del fallo.
 * @property code Código de error.
 */
data class FailedChange(
    @SerializedName("id") val id: Int,
    @SerializedName("reason") val reason: String,
    @SerializedName("code") val code: String
)