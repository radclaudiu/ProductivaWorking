package com.productiva.android.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para registros de fichaje.
 * Representa la estructura de un registro de entrada/salida en un punto de fichaje.
 * 
 * @property id Identificador único del registro
 * @property employeeId ID del empleado que realiza el fichaje
 * @property checkpointId ID del punto de fichaje donde se registra
 * @property checkInTime Fecha y hora de entrada (formato "YYYY-MM-DD HH:MM:SS")
 * @property checkOutTime Fecha y hora de salida, null si no ha salido
 * @property status Estado del registro (PENDING, COMPLETED, AUTO_COMPLETED)
 * @property notes Notas adicionales sobre el registro
 * @property hoursWorked Horas trabajadas calculadas (solo cuando está completado)
 * @property syncStatus Estado de sincronización con el servidor
 * @property localId ID local temporal para registros creados offline
 */
@Entity(
    tableName = "checkpoint_records",
    foreignKeys = [
        ForeignKey(
            entity = Employee::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Checkpoint::class,
            parentColumns = ["id"],
            childColumns = ["checkpointId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("employeeId"),
        Index("checkpointId")
    ]
)
data class CheckpointRecord(
    @PrimaryKey
    @SerializedName("id")
    val id: Int = 0,  // 0 para registros locales aún no sincronizados
    
    @SerializedName("employee_id")
    val employeeId: Int,
    
    @SerializedName("checkpoint_id")
    val checkpointId: Int,
    
    @SerializedName("check_in_time")
    val checkInTime: String,
    
    @SerializedName("check_out_time")
    val checkOutTime: String? = null,
    
    @SerializedName("status")
    val status: String = "PENDING",  // PENDING, COMPLETED, AUTO_COMPLETED
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("hours_worked")
    val hoursWorked: Float? = null,
    
    // Campos locales (no se envían al servidor)
    val syncStatus: String = "SYNCED",  // SYNCED, PENDING_SYNC, SYNC_ERROR
    
    // ID local temporal para registros creados offline
    val localId: Long? = null
)