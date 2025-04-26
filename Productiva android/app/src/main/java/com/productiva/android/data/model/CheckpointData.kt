package com.productiva.android.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.productiva.android.data.converter.DateConverter
import java.util.Date

/**
 * Entidad que representa un registro de fichaje (entrada o salida) de un empleado.
 *
 * @property id Identificador único del registro de fichaje.
 * @property employeeId ID del empleado que realiza el fichaje.
 * @property locationId ID del punto de fichaje donde se realiza.
 * @property companyId ID de la empresa a la que pertenece el empleado.
 * @property checkInTime Fecha y hora de entrada.
 * @property checkOutTime Fecha y hora de salida (opcional).
 * @property notes Notas o comentarios sobre el fichaje (opcional).
 * @property checkInLatitude Latitud donde se realizó la entrada (opcional).
 * @property checkInLongitude Longitud donde se realizó la entrada (opcional).
 * @property checkOutLatitude Latitud donde se realizó la salida (opcional).
 * @property checkOutLongitude Longitud donde se realizó la salida (opcional).
 * @property hoursWorked Número de horas trabajadas calculadas (opcional).
 * @property status Estado del fichaje ("pending", "completed", "auto_completed", "error").
 * @property createdAt Fecha de creación del registro.
 * @property updatedAt Fecha de última actualización del registro.
 * @property syncStatus Estado de sincronización con el servidor.
 * @property lastSyncTime Marca de tiempo de la última sincronización.
 * @property pendingChanges Indica si hay cambios locales pendientes de sincronizar.
 */
@Entity(
    tableName = "checkpoints",
    indices = [
        Index("employeeId"),
        Index("locationId"),
        Index("companyId"),
        Index("status"),
        Index("syncStatus")
    ]
)
@TypeConverters(DateConverter::class)
data class CheckpointData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val employeeId: Int,
    val locationId: Int,
    val companyId: Int,
    
    val checkInTime: Date,
    val checkOutTime: Date? = null,
    
    val notes: String? = null,
    
    val checkInLatitude: Double? = null,
    val checkInLongitude: Double? = null,
    val checkOutLatitude: Double? = null,
    val checkOutLongitude: Double? = null,
    
    val hoursWorked: Double? = null,
    
    val status: String, // "pending", "completed", "auto_completed", "error"
    
    val createdAt: Date,
    val updatedAt: Date,
    
    // Campos para sincronización
    val syncStatus: String = SyncStatus.SYNCED, // "synced", "pending_upload", "pending_update", "conflict"
    val lastSyncTime: Long = 0,
    val pendingChanges: Boolean = false
) {
    /**
     * Comprueba si el fichaje está pendiente (sin fecha de salida).
     *
     * @return true si el fichaje está pendiente, false en caso contrario.
     */
    fun isPending(): Boolean {
        return checkOutTime == null && status == "pending"
    }
    
    /**
     * Comprueba si el fichaje está completo (con fecha de entrada y salida).
     *
     * @return true si el fichaje está completo, false en caso contrario.
     */
    fun isCompleted(): Boolean {
        return checkOutTime != null && (status == "completed" || status == "auto_completed")
    }
    
    /**
     * Crea una copia del fichaje con salida registrada.
     *
     * @param checkOutTime Fecha y hora de salida.
     * @param latitude Latitud donde se realizó la salida (opcional).
     * @param longitude Longitud donde se realizó la salida (opcional).
     * @param autoCompleted Indica si la salida fue registrada automáticamente.
     * @return Fichaje actualizado con salida registrada.
     */
    fun registerCheckOut(
        checkOutTime: Date,
        latitude: Double? = null,
        longitude: Double? = null,
        autoCompleted: Boolean = false
    ): CheckpointData {
        val status = if (autoCompleted) "auto_completed" else "completed"
        val hoursWorked = calculateHoursWorked(this.checkInTime, checkOutTime)
        
        return copy(
            checkOutTime = checkOutTime,
            checkOutLatitude = latitude,
            checkOutLongitude = longitude,
            hoursWorked = hoursWorked,
            status = status,
            updatedAt = Date(),
            syncStatus = SyncStatus.PENDING_UPDATE,
            pendingChanges = true
        )
    }
    
    /**
     * Crea una copia del fichaje con estado de sincronización actualizado.
     *
     * @param newSyncStatus Nuevo estado de sincronización.
     * @return Fichaje actualizado con nuevo estado de sincronización.
     */
    fun withSyncStatus(newSyncStatus: String, lastSyncTime: Long = System.currentTimeMillis()): CheckpointData {
        return copy(
            syncStatus = newSyncStatus,
            lastSyncTime = lastSyncTime,
            pendingChanges = newSyncStatus != SyncStatus.SYNCED
        )
    }
    
    /**
     * Calcula las horas trabajadas entre dos fechas.
     *
     * @param checkIn Fecha y hora de entrada.
     * @param checkOut Fecha y hora de salida.
     * @return Número de horas trabajadas calculadas.
     */
    private fun calculateHoursWorked(checkIn: Date, checkOut: Date): Double {
        val diffMillis = checkOut.time - checkIn.time
        return (diffMillis / (1000.0 * 60 * 60)) // Convertir milisegundos a horas
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
    
    /**
     * Clase auxiliar que define constantes para los estados de fichaje.
     */
    object Status {
        const val PENDING = "pending"
        const val COMPLETED = "completed"
        const val AUTO_COMPLETED = "auto_completed"
        const val ERROR = "error"
    }
}