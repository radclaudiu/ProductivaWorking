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
    val status: String,
    
    @ColumnInfo(name = "priority")
    @SerializedName("priority") 
    val priority: Int,
    
    @ColumnInfo(name = "due_date")
    @SerializedName("due_date") 
    val dueDate: String?,
    
    @ColumnInfo(name = "created_at")
    @SerializedName("created_at") 
    val createdAt: String,
    
    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at") 
    val updatedAt: String,
    
    @ColumnInfo(name = "created_by")
    @SerializedName("created_by") 
    val createdBy: Int,
    
    @ColumnInfo(name = "assigned_to")
    @SerializedName("assigned_to") 
    val assignedTo: Int?,
    
    @ColumnInfo(name = "assignee_name")
    @SerializedName("assignee_name") 
    val assigneeName: String?,
    
    @ColumnInfo(name = "company_id")
    @SerializedName("company_id") 
    val companyId: Int,
    
    @ColumnInfo(name = "location_id")
    @SerializedName("location_id") 
    val locationId: Int?,
    
    @ColumnInfo(name = "location_name")
    @SerializedName("location_name") 
    val locationName: String?,
    
    @ColumnInfo(name = "is_deleted")
    @SerializedName("is_deleted") 
    val isDeleted: Boolean = false,
    
    @ColumnInfo(name = "last_sync")
    val lastSync: Long = System.currentTimeMillis()
) {
    /**
     * Estados posibles de una tarea.
     */
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_IN_PROGRESS = "in_progress"
        const val STATUS_COMPLETED = "completed"
        const val STATUS_CANCELLED = "cancelled"
        
        const val PRIORITY_LOW = 1
        const val PRIORITY_MEDIUM = 2
        const val PRIORITY_HIGH = 3
    }
    
    /**
     * Convierte la prioridad numérica a texto.
     */
    fun getPriorityText(): String {
        return when (priority) {
            PRIORITY_LOW -> "Baja"
            PRIORITY_MEDIUM -> "Media"
            PRIORITY_HIGH -> "Alta"
            else -> "Desconocida"
        }
    }
    
    /**
     * Verifica si la tarea está vencida.
     */
    fun isOverdue(): Boolean {
        if (dueDate.isNullOrEmpty() || status == STATUS_COMPLETED || status == STATUS_CANCELLED) {
            return false
        }
        
        try {
            // Formato esperado: DD-MM-YYYY
            val parts = dueDate.split("-")
            if (parts.size != 3) return false
            
            val day = parts[0].toInt()
            val month = parts[1].toInt() - 1  // Los meses en Java son 0-based
            val year = parts[2].toInt()
            
            val dueDateObj = java.util.Calendar.getInstance().apply {
                set(year, month, day, 23, 59, 59)
            }.time
            
            val now = Date()
            return now.after(dueDateObj)
        } catch (e: Exception) {
            return false
        }
    }
}