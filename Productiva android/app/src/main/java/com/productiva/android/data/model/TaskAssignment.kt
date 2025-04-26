package com.productiva.android.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para asignaciones de tareas.
 * Representa la asignación de una tarea a un empleado.
 * 
 * @property id Identificador único de la asignación
 * @property taskId ID de la tarea asignada
 * @property employeeId ID del empleado asignado
 * @property assignedAt Fecha y hora de asignación
 * @property assignedBy ID del usuario que realizó la asignación
 * @property startDate Fecha de inicio prevista
 * @property endDate Fecha de finalización prevista
 * @property status Estado de la asignación (ASSIGNED, IN_PROGRESS, COMPLETED, REJECTED)
 * @property completedAt Fecha y hora de completado (si aplica)
 * @property notes Notas sobre la asignación
 * @property signatureUrl URL o ruta a la firma de completado (si aplica)
 * @property photoUrl URL o ruta a la foto de completado (si aplica)
 * @property syncStatus Estado de sincronización con el servidor
 */
@Entity(
    tableName = "task_assignments",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Employee::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("taskId"),
        Index("employeeId")
    ]
)
data class TaskAssignment(
    @PrimaryKey
    @SerializedName("id")
    val id: Int = 0,  // 0 para asignaciones locales aún no sincronizadas
    
    @SerializedName("task_id")
    val taskId: Int,
    
    @SerializedName("employee_id")
    val employeeId: Int,
    
    @SerializedName("assigned_at")
    val assignedAt: String,
    
    @SerializedName("assigned_by")
    val assignedBy: Int? = null,
    
    @SerializedName("start_date")
    val startDate: String? = null,
    
    @SerializedName("end_date")
    val endDate: String? = null,
    
    @SerializedName("status")
    val status: String = "ASSIGNED",  // ASSIGNED, IN_PROGRESS, COMPLETED, REJECTED
    
    @SerializedName("completed_at")
    val completedAt: String? = null,
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("signature_url")
    val signatureUrl: String? = null,
    
    @SerializedName("photo_url")
    val photoUrl: String? = null,
    
    // Campos locales (no se envían al servidor)
    val syncStatus: String = "SYNCED",  // SYNCED, PENDING_SYNC, SYNC_ERROR
    
    // Rutas locales para recursos aún no sincronizados
    val localSignaturePath: String? = null,
    val localPhotoPath: String? = null
)