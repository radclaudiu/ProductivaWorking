package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Entidad que representa una tarea en el sistema.
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("status")
    val status: String = STATUS_PENDING,
    
    @SerializedName("created_date")
    val createdDate: Date? = null,
    
    @SerializedName("due_date")
    val dueDate: Date? = null,
    
    @SerializedName("completed_date")
    val completedDate: Date? = null,
    
    @SerializedName("priority")
    val priority: Int = 0, // 0: Baja, 1: Normal, 2: Alta
    
    @SerializedName("user_id")
    val userId: Int? = null,
    
    @SerializedName("user_name")
    val userName: String? = null,
    
    @SerializedName("location_id")
    val locationId: Int? = null,
    
    @SerializedName("location_name")
    val locationName: String? = null,
    
    @SerializedName("company_id")
    val companyId: Int? = null,
    
    @SerializedName("company_name")
    val companyName: String? = null,
    
    @SerializedName("requires_signature")
    val requiresSignature: Boolean = false,
    
    @SerializedName("requires_photo")
    val requiresPhoto: Boolean = false,
    
    @SerializedName("has_attachments")
    val hasAttachments: Boolean = false,
    
    @SerializedName("tags")
    val tags: List<String>? = null,
    
    // Campo para sincronización, no se envía al servidor
    var syncPending: Boolean = false
) {
    /**
     * Verifica si la tarea está completada
     */
    fun isCompleted(): Boolean {
        return status == STATUS_COMPLETED
    }
    
    /**
     * Verifica si la tarea está en progreso
     */
    fun isInProgress(): Boolean {
        return status == STATUS_IN_PROGRESS
    }
    
    /**
     * Verifica si la tarea está cancelada
     */
    fun isCancelled(): Boolean {
        return status == STATUS_CANCELLED
    }
    
    /**
     * Verifica si la tarea está pendiente
     */
    fun isPending(): Boolean {
        return status == STATUS_PENDING
    }
    
    /**
     * Verifica si la tarea está vencida
     */
    fun isPastDue(): Boolean {
        val now = Date()
        return dueDate != null && dueDate.before(now) && !isCompleted() && !isCancelled()
    }
    
    /**
     * Obtiene el color de la prioridad
     */
    fun getPriorityColor(): Int {
        return when (priority) {
            2 -> 0xFFE53935.toInt() // Rojo para alta prioridad
            1 -> 0xFFFB8C00.toInt() // Naranja para prioridad media
            else -> 0xFF4CAF50.toInt() // Verde para prioridad baja
        }
    }
    
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_IN_PROGRESS = "in_progress"
        const val STATUS_COMPLETED = "completed"
        const val STATUS_CANCELLED = "cancelled"
        
        val ALL_STATUSES = listOf(
            STATUS_PENDING,
            STATUS_IN_PROGRESS,
            STATUS_COMPLETED,
            STATUS_CANCELLED
        )
    }
}