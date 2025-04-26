package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Entidad que representa una tarea en la aplicación.
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    @ColumnInfo(name = "id")
    @SerializedName("id") 
    val id: Int,
    
    @ColumnInfo(name = "title")
    @SerializedName("title") 
    val title: String,
    
    @ColumnInfo(name = "description")
    @SerializedName("description") 
    val description: String?,
    
    @ColumnInfo(name = "status")
    @SerializedName("status") 
    val status: String, // "pending", "in_progress", "completed", "cancelled"
    
    @ColumnInfo(name = "priority")
    @SerializedName("priority") 
    val priority: Int, // 1-5, 5 es máxima prioridad
    
    @ColumnInfo(name = "created_by")
    @SerializedName("created_by") 
    val createdBy: Int,
    
    @ColumnInfo(name = "assigned_to")
    @SerializedName("assigned_to") 
    val assignedTo: Int?,
    
    @ColumnInfo(name = "company_id")
    @SerializedName("company_id") 
    val companyId: Int,
    
    @ColumnInfo(name = "location_id")
    @SerializedName("location_id") 
    val locationId: Int?,
    
    @ColumnInfo(name = "due_date")
    @SerializedName("due_date") 
    val dueDate: Date?,
    
    @ColumnInfo(name = "created_at")
    @SerializedName("created_at") 
    val createdAt: String,
    
    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at") 
    val updatedAt: String?,
    
    @ColumnInfo(name = "category")
    @SerializedName("category") 
    val category: String?,
    
    @ColumnInfo(name = "requires_signature")
    @SerializedName("requires_signature") 
    val requiresSignature: Boolean = false,
    
    @ColumnInfo(name = "requires_photo")
    @SerializedName("requires_photo") 
    val requiresPhoto: Boolean = false,
    
    @ColumnInfo(name = "is_recurrent")
    @SerializedName("is_recurrent") 
    val isRecurrent: Boolean = false,
    
    @ColumnInfo(name = "recurrence_days")
    @SerializedName("recurrence_days") 
    val recurrenceDays: Int? = null, // Cada cuántos días se repite
    
    @ColumnInfo(name = "is_deleted")
    @SerializedName("is_deleted") 
    val isDeleted: Boolean = false,
    
    @ColumnInfo(name = "last_completion_date")
    @SerializedName("last_completion_date") 
    val lastCompletionDate: Date? = null,
    
    @ColumnInfo(name = "completions_count")
    @SerializedName("completions_count") 
    val completionsCount: Int = 0,
    
    @ColumnInfo(name = "notes")
    @SerializedName("notes") 
    val notes: String? = null,
    
    @ColumnInfo(name = "attached_files")
    @SerializedName("attached_files") 
    val attachedFiles: String? = null, // JSON array de rutas de archivos
    
    @ColumnInfo(name = "labels_to_print")
    @SerializedName("labels_to_print") 
    val labelsToPrint: String? = null, // JSON array de IDs de etiquetas a imprimir
    
    @ColumnInfo(name = "last_sync")
    val lastSync: Long = System.currentTimeMillis()
) {
    /**
     * Determina si la tarea está vencida.
     */
    fun isOverdue(): Boolean {
        if (dueDate == null) return false
        if (status == "completed" || status == "cancelled") return false
        
        return dueDate.before(Date())
    }
    
    /**
     * Obtiene un color para representar la prioridad de la tarea.
     */
    fun getPriorityColor(): Int {
        return when (priority) {
            5 -> 0xFFE53935.toInt() // Rojo para máxima prioridad
            4 -> 0xFFF57C00.toInt() // Naranja para alta prioridad
            3 -> 0xFFFFB300.toInt() // Ámbar para prioridad media
            2 -> 0xFF43A047.toInt() // Verde para prioridad baja
            else -> 0xFF78909C.toInt() // Gris para prioridad mínima
        }
    }
    
    /**
     * Obtiene un color para representar el estado de la tarea.
     */
    fun getStatusColor(): Int {
        return when (status) {
            "pending" -> 0xFF90A4AE.toInt() // Gris-azul para pendiente
            "in_progress" -> 0xFF2196F3.toInt() // Azul para en progreso
            "completed" -> 0xFF4CAF50.toInt() // Verde para completada
            "cancelled" -> 0xFFE57373.toInt() // Rojo claro para cancelada
            else -> 0xFF78909C.toInt() // Gris por defecto
        }
    }
    
    /**
     * Devuelve el estado localizado para su presentación.
     */
    fun getLocalizedStatus(): String {
        return when (status) {
            "pending" -> "Pendiente"
            "in_progress" -> "En progreso"
            "completed" -> "Completada"
            "cancelled" -> "Cancelada"
            else -> status
        }
    }
    
    /**
     * Determina si la tarea puede ser editada.
     */
    fun canBeEdited(): Boolean {
        return status != "completed" && status != "cancelled" && !isDeleted
    }
}