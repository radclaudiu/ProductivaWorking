package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Entidad que representa la completación de una tarea.
 */
@Entity(tableName = "task_completions")
data class TaskCompletion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @SerializedName("task_id")
    val taskId: Int,
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("user_name")
    val userName: String? = null,
    
    @SerializedName("completion_date")
    val completionDate: Date? = null,
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("location_id")
    val locationId: Int? = null,
    
    @SerializedName("location_name")
    val locationName: String? = null,
    
    @SerializedName("has_signature")
    val hasSignature: Boolean = false,
    
    @SerializedName("signature_path")
    val signaturePath: String? = null,
    
    @SerializedName("has_photo")
    val hasPhoto: Boolean = false,
    
    @SerializedName("photo_path")
    val photoPath: String? = null,
    
    @SerializedName("has_attachments")
    val hasAttachments: Boolean = false,
    
    @SerializedName("attachment_paths")
    val attachmentPaths: List<String>? = null,
    
    @SerializedName("geolocation")
    val geolocation: String? = null,
    
    // Campos para sincronización, no se envían al servidor
    var syncPending: Boolean = false,
    var remoteId: Int? = null
) {
    /**
     * Verifica si la completación tiene recursos adjuntos (firma, foto, etc)
     */
    fun hasResources(): Boolean {
        return hasSignature || hasPhoto || hasAttachments
    }
    
    /**
     * Verifica si la completación tiene suficiente información para ser enviada
     */
    fun isValid(): Boolean {
        return taskId > 0 && userId > 0 && completionDate != null
    }
    
    /**
     * Obtiene una descripción para la completación (para mostrar en la UI)
     */
    fun getDisplayDescription(): String {
        val description = StringBuilder()
        
        if (!notes.isNullOrBlank()) {
            description.append(notes)
        } else {
            description.append("Tarea completada")
        }
        
        val resources = mutableListOf<String>()
        if (hasSignature) resources.add("firma")
        if (hasPhoto) resources.add("foto")
        if (hasAttachments) resources.add("archivos adjuntos")
        
        if (resources.isNotEmpty()) {
            description.append(" (")
            description.append(resources.joinToString(", "))
            description.append(")")
        }
        
        return description.toString()
    }
}