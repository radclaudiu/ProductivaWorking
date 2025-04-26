package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val status: String, // PENDING, COMPLETED, CANCELLED
    
    @SerializedName("created_at")
    val createdAt: String?,
    
    @SerializedName("due_date")
    val dueDate: String?,
    
    @SerializedName("priority")
    val priority: Int, // 1-5 (1: baja, 5: alta)
    
    @SerializedName("recurring")
    val recurring: Boolean,
    
    @SerializedName("assigned_to")
    val assignedTo: Int?,
    
    @SerializedName("assigned_name")
    val assignedName: String?,
    
    @SerializedName("created_by")
    val createdBy: Int?,
    
    @SerializedName("created_by_name")
    val createdByName: String?,
    
    @SerializedName("location_id")
    val locationId: Int?,
    
    @SerializedName("location_name")
    val locationName: String?,
    
    @SerializedName("requires_signature")
    val requiresSignature: Boolean,
    
    @SerializedName("requires_photo")
    val requiresPhoto: Boolean,
    
    @SerializedName("completed_at")
    val completedAt: String?,
    
    @SerializedName("completed_by")
    val completedBy: Int?,
    
    @SerializedName("completed_by_name")
    val completedByName: String?,
    
    @SerializedName("completion_notes")
    val completionNotes: String?,
    
    @SerializedName("has_signature")
    val hasSignature: Boolean,
    
    @SerializedName("has_photo")
    val hasPhoto: Boolean,
    
    @SerializedName("signature_url")
    val signatureUrl: String?,
    
    @SerializedName("photo_url")
    val photoUrl: String?,
    
    @SerializedName("local_signature_path")
    val localSignaturePath: String?,
    
    @SerializedName("local_photo_path")
    val localPhotoPath: String?,
    
    // Campo para indicar si la tarea necesita sincronizarse con el servidor
    val needsSync: Boolean = false
) {
    /**
     * Determina si la tarea está vencida.
     */
    fun isOverdue(): Boolean {
        if (status != "PENDING" || dueDate.isNullOrEmpty()) {
            return false
        }
        
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val dueDateParsed = inputFormat.parse(dueDate)
            val currentDate = Date()
            
            return dueDateParsed != null && dueDateParsed.before(currentDate)
        } catch (e: Exception) {
            // En caso de error al parsear la fecha, asumir que no está vencida
            return false
        }
    }
    
    /**
     * Obtiene el texto de visualización del estado.
     */
    fun getStatusDisplay(): String {
        return when (status) {
            "PENDING" -> if (isOverdue()) "Vencida" else "Pendiente"
            "COMPLETED" -> "Completada"
            "CANCELLED" -> "Cancelada"
            else -> status
        }
    }
    
    /**
     * Obtiene el texto de visualización del asignado.
     */
    fun getAssignedDisplay(): String {
        return assignedName ?: "Sin asignar"
    }
    
    /**
     * Obtiene el texto de visualización de la fecha de vencimiento.
     */
    fun getDueDateDisplay(): String {
        if (dueDate.isNullOrEmpty()) {
            return "Sin fecha"
        }
        
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dueDate)
            
            return date?.let { outputFormat.format(it) } ?: "Formato incorrecto"
        } catch (e: Exception) {
            return "Error en fecha"
        }
    }
    
    /**
     * Obtiene el texto de visualización de la fecha de completado.
     */
    fun getCompletedAtDisplay(): String {
        if (completedAt.isNullOrEmpty()) {
            return "No completada"
        }
        
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(completedAt)
            
            return date?.let { outputFormat.format(it) } ?: "Formato incorrecto"
        } catch (e: Exception) {
            return "Error en fecha"
        }
    }
    
    /**
     * Crea un duplicado de esta tarea con estado modificado para indicar que necesita sincronización.
     */
    fun markForSync(): Task {
        return this.copy(needsSync = true)
    }
    
    /**
     * Crea un duplicado de esta tarea con estado completado.
     */
    fun markAsCompleted(
        userId: Int,
        userName: String,
        notes: String?,
        hasSignature: Boolean,
        hasPhoto: Boolean,
        localSignaturePath: String?,
        localPhotoPath: String?
    ): Task {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
        
        return this.copy(
            status = "COMPLETED",
            completedAt = now,
            completedBy = userId,
            completedByName = userName,
            completionNotes = notes,
            hasSignature = hasSignature,
            hasPhoto = hasPhoto,
            localSignaturePath = localSignaturePath,
            localPhotoPath = localPhotoPath,
            needsSync = true
        )
    }
    
    /**
     * Crea un duplicado de esta tarea con estado cancelado.
     */
    fun markAsCancelled(userId: Int, userName: String, notes: String?): Task {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
        
        return this.copy(
            status = "CANCELLED",
            completedAt = now,
            completedBy = userId,
            completedByName = userName,
            completionNotes = notes,
            needsSync = true
        )
    }
    
    /**
     * Actualiza esta tarea con información del servidor.
     */
    fun updateFromServer(serverTask: Task): Task {
        return this.copy(
            title = serverTask.title,
            description = serverTask.description,
            status = serverTask.status,
            dueDate = serverTask.dueDate,
            priority = serverTask.priority,
            recurring = serverTask.recurring,
            assignedTo = serverTask.assignedTo,
            assignedName = serverTask.assignedName,
            locationId = serverTask.locationId,
            locationName = serverTask.locationName,
            requiresSignature = serverTask.requiresSignature,
            requiresPhoto = serverTask.requiresPhoto,
            completedAt = serverTask.completedAt,
            completedBy = serverTask.completedBy,
            completedByName = serverTask.completedByName,
            completionNotes = serverTask.completionNotes,
            hasSignature = serverTask.hasSignature,
            hasPhoto = serverTask.hasPhoto,
            signatureUrl = serverTask.signatureUrl,
            photoUrl = serverTask.photoUrl,
            needsSync = false
        )
    }
    
    companion object {
        /**
         * Crea una tarea vacía para usar como comodín.
         */
        fun createEmpty(): Task {
            return Task(
                id = 0,
                title = "",
                description = null,
                status = "PENDING",
                createdAt = null,
                dueDate = null,
                priority = 3,
                recurring = false,
                assignedTo = null,
                assignedName = null,
                createdBy = null,
                createdByName = null,
                locationId = null,
                locationName = null,
                requiresSignature = false,
                requiresPhoto = false,
                completedAt = null,
                completedBy = null,
                completedByName = null,
                completionNotes = null,
                hasSignature = false,
                hasPhoto = false,
                signatureUrl = null,
                photoUrl = null,
                localSignaturePath = null,
                localPhotoPath = null,
                needsSync = false
            )
        }
    }
}