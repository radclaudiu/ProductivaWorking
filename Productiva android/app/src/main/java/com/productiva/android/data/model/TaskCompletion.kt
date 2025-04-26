package com.productiva.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.productiva.android.data.converter.DateConverter
import java.util.Date

/**
 * Modelo que representa el registro de completado de una tarea.
 *
 * @property id ID único del registro de completado.
 * @property taskId ID de la tarea completada.
 * @property userId ID del usuario que completó la tarea.
 * @property completedAt Fecha y hora de completado.
 * @property notes Notas adicionales sobre el completado (opcional).
 * @property signaturePath Ruta a la imagen de la firma (opcional).
 * @property photoPath Ruta a la foto de completado (opcional).
 * @property latitude Latitud de la ubicación de completado (opcional).
 * @property longitude Longitud de la ubicación de completado (opcional).
 * @property syncStatus Estado de sincronización del registro.
 */
@Entity(tableName = "task_completions")
@TypeConverters(DateConverter::class)
data class TaskCompletion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val taskId: Int = 0,
    val userId: Int,
    val completedAt: Date = Date(),
    val notes: String? = null,
    val signaturePath: String? = null,
    val photoPath: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD
) {
    /**
     * Estado de sincronización del registro de completado.
     */
    enum class SyncStatus {
        /** Sincronizado con el servidor. */
        SYNCED,
        /** Pendiente de subir al servidor. */
        PENDING_UPLOAD
    }
    
    /**
     * Verifica si la tarea tiene una firma.
     *
     * @return True si tiene firma, False en caso contrario.
     */
    fun hasSignature(): Boolean {
        return !signaturePath.isNullOrEmpty()
    }
    
    /**
     * Verifica si la tarea tiene una foto.
     *
     * @return True si tiene foto, False en caso contrario.
     */
    fun hasPhoto(): Boolean {
        return !photoPath.isNullOrEmpty()
    }
    
    /**
     * Verifica si la tarea tiene datos de ubicación.
     *
     * @return True si tiene ubicación, False en caso contrario.
     */
    fun hasLocation(): Boolean {
        return latitude != null && longitude != null
    }
    
    /**
     * Crea una copia del registro con un estado de sincronización específico.
     *
     * @param syncStatus Nuevo estado de sincronización.
     * @return Copia del registro con el nuevo estado.
     */
    fun withSyncStatus(syncStatus: SyncStatus): TaskCompletion {
        return this.copy(syncStatus = syncStatus)
    }
}