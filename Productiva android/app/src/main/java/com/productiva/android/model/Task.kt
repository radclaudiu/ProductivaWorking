package com.productiva.android.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.productiva.android.database.Converters

/**
 * Modelo para tareas.
 * Representa una tarea asignada a un usuario o ubicación.
 */
@Entity(
    tableName = "tasks",
    indices = [
        Index("assignedTo"),
        Index("locationId")
    ]
)
@TypeConverters(Converters::class)
data class Task(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("assigned_to")
    val assignedTo: Int? = null, // ID del usuario asignado
    
    @SerializedName("location_id")
    val locationId: Int? = null, // ID de la ubicación
    
    @SerializedName("due_date")
    val dueDate: String? = null, // Fecha de vencimiento (YYYY-MM-DD)
    
    @SerializedName("due_time")
    val dueTime: String? = null, // Hora de vencimiento (HH:MM)
    
    @SerializedName("priority")
    val priority: Int = 1, // 1-3 (baja, media, alta)
    
    @SerializedName("status")
    val status: String = "pending", // pending, in_progress, completed, cancelled
    
    @SerializedName("tags")
    val tags: List<String> = emptyList(),
    
    @SerializedName("requires_photo")
    val requiresPhoto: Boolean = false,
    
    @SerializedName("requires_signature")
    val requiresSignature: Boolean = false,
    
    @SerializedName("requires_scan")
    val requiresScan: Boolean = false,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    
    @SerializedName("company_id")
    val companyId: Int? = null,
    
    @SerializedName("recurrence")
    val recurrence: String? = null, // daily, weekly, monthly, custom
    
    @SerializedName("recurrence_config")
    val recurrenceConfig: Map<String, String> = emptyMap(),
    
    @SerializedName("completion_data")
    val completionData: TaskCompletion? = null,
    
    // Campos locales
    var isLocallyModified: Boolean = false,
    var lastSyncTime: Long = 0
) {
    /**
     * Verifica si la tarea está completada.
     */
    fun isCompleted(): Boolean {
        return status == "completed"
    }
    
    /**
     * Verifica si la tarea está vencida.
     */
    fun isOverdue(): Boolean {
        // Implementar lógica de verificación de vencimiento
        return false
    }
    
    /**
     * Verifica si la tarea está pendiente.
     */
    fun isPending(): Boolean {
        return status == "pending"
    }
    
    /**
     * Verifica si la tarea está en progreso.
     */
    fun isInProgress(): Boolean {
        return status == "in_progress"
    }
    
    /**
     * Verifica si la tarea está cancelada.
     */
    fun isCancelled(): Boolean {
        return status == "cancelled"
    }
    
    /**
     * Obtiene el color basado en la prioridad.
     */
    fun getPriorityColor(): String {
        return when (priority) {
            1 -> "#28a745" // Verde
            2 -> "#ffc107" // Amarillo
            3 -> "#dc3545" // Rojo
            else -> "#6c757d" // Gris
        }
    }
    
    /**
     * Obtiene el nombre de la prioridad.
     */
    fun getPriorityName(): String {
        return when (priority) {
            1 -> "Baja"
            2 -> "Media"
            3 -> "Alta"
            else -> "Desconocida"
        }
    }
}