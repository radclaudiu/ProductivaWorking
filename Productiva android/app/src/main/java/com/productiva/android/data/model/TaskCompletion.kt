package com.productiva.android.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.productiva.android.data.converter.DateConverter
import java.util.Date

/**
 * Entidad que representa los datos de completado de una tarea.
 *
 * @property id Identificador único del registro de completado.
 * @property taskId ID de la tarea completada.
 * @property completedBy ID del usuario que completó la tarea.
 * @property completedAt Fecha y hora de completado.
 * @property notes Notas o comentarios sobre el completado (opcional).
 * @property photoPath Ruta al archivo de foto adjunta (opcional).
 * @property signaturePath Ruta al archivo de firma digital (opcional).
 * @property latitude Latitud donde se completó la tarea (opcional).
 * @property longitude Longitud donde se completó la tarea (opcional).
 * @property elapsedTimeSeconds Tiempo empleado en completar la tarea en segundos (opcional).
 * @property syncStatus Estado de sincronización con el servidor.
 * @property lastSyncTime Marca de tiempo de la última sincronización.
 * @property photoUploaded Indica si la foto ha sido subida al servidor.
 * @property signatureUploaded Indica si la firma ha sido subida al servidor.
 * @property pendingChanges Indica si hay cambios locales pendientes de sincronizar.
 * @property localPhotoUri URI local de la foto (para uso transitorio).
 * @property localSignatureUri URI local de la firma (para uso transitorio).
 */
@Entity(
    tableName = "task_completions",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("taskId"),
        Index("syncStatus")
    ]
)
@TypeConverters(DateConverter::class)
data class TaskCompletion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val taskId: Int,
    val completedBy: Int,
    val completedAt: Date,
    
    val notes: String? = null,
    val photoPath: String? = null,
    val signaturePath: String? = null,
    
    val latitude: Double? = null,
    val longitude: Double? = null,
    
    val elapsedTimeSeconds: Int? = null,
    
    // Campos para sincronización
    val syncStatus: String = SyncStatus.PENDING_UPLOAD,
    val lastSyncTime: Long = 0,
    val photoUploaded: Boolean = false,
    val signatureUploaded: Boolean = false,
    val pendingChanges: Boolean = true,
    
    // Campos transitorios (no persistidos)
    var localPhotoUri: String? = null,
    var localSignatureUri: String? = null
) {
    /**
     * Comprueba si el completado tiene una foto adjunta.
     *
     * @return true si hay una foto adjunta, false en caso contrario.
     */
    fun hasPhoto(): Boolean {
        return !photoPath.isNullOrEmpty() || !localPhotoUri.isNullOrEmpty()
    }
    
    /**
     * Comprueba si el completado tiene una firma digital.
     *
     * @return true si hay una firma digital, false en caso contrario.
     */
    fun hasSignature(): Boolean {
        return !signaturePath.isNullOrEmpty() || !localSignatureUri.isNullOrEmpty()
    }
    
    /**
     * Comprueba si el completado tiene información de geolocalización.
     *
     * @return true si hay información de geolocalización, false en caso contrario.
     */
    fun hasLocation(): Boolean {
        return latitude != null && longitude != null
    }
    
    /**
     * Crea una copia del completado con estado de sincronización actualizado.
     *
     * @param newSyncStatus Nuevo estado de sincronización.
     * @return Completado actualizado con nuevo estado de sincronización.
     */
    fun withSyncStatus(newSyncStatus: String, lastSyncTime: Long = System.currentTimeMillis()): TaskCompletion {
        return copy(
            syncStatus = newSyncStatus,
            lastSyncTime = lastSyncTime,
            pendingChanges = newSyncStatus != SyncStatus.SYNCED
        )
    }
    
    /**
     * Crea una copia del completado con estado de subida de archivos actualizado.
     *
     * @param photoUploaded true si la foto ha sido subida, false en caso contrario.
     * @param signatureUploaded true si la firma ha sido subida, false en caso contrario.
     * @return Completado actualizado con nuevos estados de subida de archivos.
     */
    fun withUploadStatus(photoUploaded: Boolean? = null, signatureUploaded: Boolean? = null): TaskCompletion {
        return copy(
            photoUploaded = photoUploaded ?: this.photoUploaded,
            signatureUploaded = signatureUploaded ?: this.signatureUploaded,
            pendingChanges = true
        )
    }
    
    /**
     * Clase auxiliar que define constantes para los estados de sincronización.
     */
    object SyncStatus {
        const val SYNCED = "synced"
        const val PENDING_UPLOAD = "pending_upload"
        const val PENDING_UPDATE = "pending_update"
        const val CONFLICT = "conflict"
    }
}