package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Modelo de datos que representa una tarea en la aplicación.
 * Se almacena en la base de datos local y se sincroniza con el servidor.
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("status")
    val status: String,  // "PENDING", "COMPLETED", "CANCELLED", "IN_PROGRESS"
    
    @SerializedName("priority")
    val priority: Int,  // 1-5, donde 5 es la más alta
    
    @SerializedName("due_date")
    val dueDate: String?,
    
    @SerializedName("assigned_to")
    val assignedTo: Int?,
    
    @SerializedName("assigned_to_name")
    val assignedToName: String?,
    
    @SerializedName("created_by")
    val createdBy: Int?,
    
    @SerializedName("created_by_name")
    val createdByName: String?,
    
    @SerializedName("created_at")
    val createdAt: String?,
    
    @SerializedName("updated_at")
    val updatedAt: String?,
    
    @SerializedName("completed_at")
    val completedAt: String?,
    
    @SerializedName("location_id")
    val locationId: Int?,
    
    @SerializedName("location_name")
    val locationName: String?,
    
    @SerializedName("company_id")
    val companyId: Int?,
    
    @SerializedName("company_name")
    val companyName: String?,
    
    @SerializedName("requires_signature")
    val requiresSignature: Boolean,
    
    @SerializedName("requires_photo")
    val requiresPhoto: Boolean,
    
    @SerializedName("signature_path")
    val signaturePath: String?,
    
    @SerializedName("photo_path")
    val photoPath: String?,
    
    @SerializedName("completion_notes")
    val completionNotes: String?,
    
    @SerializedName("tags")
    val tags: List<String>?,
    
    @SerializedName("attachments")
    val attachments: List<Attachment>?,
    
    // Campos locales (no se envían al servidor)
    val localSignaturePath: String? = null,
    val localPhotoPath: String? = null,
    val needsSync: Boolean = false,
    val syncError: String? = null,
    val lastSyncTimestamp: Long = 0
) {
    /**
     * Obtiene el estado de la tarea para mostrar en UI.
     */
    fun getStatusDisplay(): String {
        return when (status) {
            "PENDING" -> "Pendiente"
            "COMPLETED" -> "Completada"
            "CANCELLED" -> "Cancelada"
            "IN_PROGRESS" -> "En progreso"
            else -> status
        }
    }
    
    /**
     * Determina si la tarea está vencida.
     */
    fun isOverdue(): Boolean {
        if (dueDate == null || status == "COMPLETED" || status == "CANCELLED") {
            return false
        }
        
        // Formato de fecha esperado: "yyyy-MM-dd'T'HH:mm:ss"
        try {
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            val dueDateObj = dateFormat.parse(dueDate)
            
            return dueDateObj?.before(Date()) ?: false
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Obtiene el nombre formateado del asignado.
     */
    fun getAssignedDisplay(): String {
        return assignedToName ?: "Sin asignar"
    }
    
    /**
     * Verifica si la tarea está lista para sincronización.
     */
    fun isReadyForSync(): Boolean {
        return needsSync && (status == "COMPLETED" || status == "CANCELLED")
    }
    
    /**
     * Clase para representar un adjunto de tarea.
     */
    data class Attachment(
        @SerializedName("id")
        val id: Int,
        
        @SerializedName("name")
        val name: String,
        
        @SerializedName("file_path")
        val filePath: String,
        
        @SerializedName("file_type")
        val fileType: String,
        
        @SerializedName("upload_date")
        val uploadDate: String?
    )
}