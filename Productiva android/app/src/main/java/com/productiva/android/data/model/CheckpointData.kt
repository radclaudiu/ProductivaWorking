package com.productiva.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.productiva.android.data.converter.DateConverter
import java.util.Date

/**
 * Modelo que representa un registro de fichaje (entrada/salida) en el sistema.
 *
 * @property id ID único del fichaje.
 * @property employeeId ID del empleado.
 * @property companyId ID de la empresa.
 * @property locationId ID del punto de fichaje.
 * @property checkInTime Fecha y hora de entrada.
 * @property checkOutTime Fecha y hora de salida (opcional).
 * @property checkInLatitude Latitud de la ubicación de entrada (opcional).
 * @property checkInLongitude Longitud de la ubicación de entrada (opcional).
 * @property checkOutLatitude Latitud de la ubicación de salida (opcional).
 * @property checkOutLongitude Longitud de la ubicación de salida (opcional).
 * @property status Estado del fichaje.
 * @property hoursWorked Horas trabajadas (calculadas al registrar la salida).
 * @property notes Notas adicionales (opcional).
 * @property createdAt Fecha de creación del fichaje.
 * @property updatedAt Fecha de última actualización del fichaje.
 * @property syncStatus Estado de sincronización del fichaje.
 * @property pendingChanges Indica si hay cambios pendientes de sincronización.
 */
@Entity(tableName = "checkpoints")
@TypeConverters(DateConverter::class)
data class CheckpointData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val employeeId: Int,
    val companyId: Int,
    val locationId: Int,
    val checkInTime: Date,
    val checkOutTime: Date? = null,
    val checkInLatitude: Double? = null,
    val checkInLongitude: Double? = null,
    val checkOutLatitude: Double? = null,
    val checkOutLongitude: Double? = null,
    val status: Status = Status.PENDING,
    val hoursWorked: Double = 0.0,
    val notes: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD,
    val pendingChanges: Boolean = false
) {
    /**
     * Estado del fichaje.
     */
    enum class Status {
        /** Pendiente (solo check-in) */
        PENDING,
        /** Completado (check-in y check-out) */
        COMPLETED,
        /** Marcado como automático */
        AUTO_COMPLETED
    }
    
    /**
     * Estado de sincronización del fichaje.
     */
    enum class SyncStatus {
        /** Sincronizado con el servidor */
        SYNCED,
        /** Pendiente de subir al servidor */
        PENDING_UPLOAD,
        /** Pendiente de actualizar en el servidor */
        PENDING_UPDATE
    }
    
    /**
     * Verifica si el fichaje está pendiente (solo tiene check-in).
     *
     * @return True si está pendiente, False en caso contrario.
     */
    fun isPending(): Boolean {
        return status == Status.PENDING && checkOutTime == null
    }
    
    /**
     * Verifica si el fichaje está completo (tiene check-in y check-out).
     *
     * @return True si está completo, False en caso contrario.
     */
    fun isCompleted(): Boolean {
        return (status == Status.COMPLETED || status == Status.AUTO_COMPLETED) && checkOutTime != null
    }
    
    /**
     * Crea una copia del fichaje con un estado de sincronización específico.
     *
     * @param syncStatus Nuevo estado de sincronización.
     * @return Copia del fichaje con el nuevo estado.
     */
    fun withSyncStatus(syncStatus: SyncStatus): CheckpointData {
        return this.copy(syncStatus = syncStatus)
    }
    
    /**
     * Registra la salida del fichaje.
     *
     * @param checkOutTime Fecha y hora de salida.
     * @param checkOutLatitude Latitud de la ubicación de salida (opcional).
     * @param checkOutLongitude Longitud de la ubicación de salida (opcional).
     * @param hoursWorked Horas trabajadas calculadas.
     * @param isAutomatic Indica si el cierre es automático.
     * @param notes Notas adicionales (opcional).
     * @return Copia del fichaje con la salida registrada.
     */
    fun registerCheckOut(
        checkOutTime: Date,
        checkOutLatitude: Double? = null,
        checkOutLongitude: Double? = null,
        hoursWorked: Double,
        isAutomatic: Boolean = false,
        notes: String? = null
    ): CheckpointData {
        return this.copy(
            checkOutTime = checkOutTime,
            checkOutLatitude = checkOutLatitude,
            checkOutLongitude = checkOutLongitude,
            hoursWorked = hoursWorked,
            status = if (isAutomatic) Status.AUTO_COMPLETED else Status.COMPLETED,
            notes = notes ?: this.notes,
            syncStatus = SyncStatus.PENDING_UPDATE,
            pendingChanges = true,
            updatedAt = Date()
        )
    }
}