package com.productiva.android.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Modelo para tareas.
 * Representa una tarea en el sistema con todos sus campos y métodos auxiliares.
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
    val status: String, // "pending", "completed", "cancelled"
    
    @SerializedName("priority")
    val priority: Int = 0, // 0 = normal, 1 = alta, 2 = urgente
    
    @SerializedName("due_date")
    val dueDate: String? = null,
    
    @SerializedName("assigned_to")
    val assignedTo: Int? = null, // ID del usuario asignado
    
    @SerializedName("assigned_to_name")
    val assignedToName: String? = null, // Nombre del usuario asignado
    
    @SerializedName("created_by")
    val createdBy: Int? = null, // ID del usuario que creó la tarea
    
    @SerializedName("created_by_name")
    val createdByName: String? = null, // Nombre del usuario que creó la tarea
    
    @SerializedName("company_id")
    val companyId: Int? = null,
    
    @SerializedName("location_id")
    val locationId: Int? = null,
    
    @SerializedName("location_name")
    val locationName: String? = null,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    
    @SerializedName("completed_at")
    val completedAt: String? = null,
    
    @SerializedName("completed_by")
    val completedBy: Int? = null,
    
    @SerializedName("completed_by_name")
    val completedByName: String? = null,
    
    @SerializedName("completion_notes")
    val completionNotes: String? = null,
    
    @SerializedName("signature_image")
    val signatureImage: String? = null,
    
    @SerializedName("photo_image")
    val photoImage: String? = null,
    
    @SerializedName("requires_signature")
    val requiresSignature: Boolean = false,
    
    @SerializedName("requires_photo")
    val requiresPhoto: Boolean = false,
    
    @SerializedName("category")
    val category: String? = null,
    
    @SerializedName("tags")
    val tags: List<String> = emptyList(),
    
    // Campos locales (no se envían al servidor)
    var localSignaturePath: String? = null,
    var localPhotoPath: String? = null,
    var needsSync: Boolean = false,
    var lastSyncTime: Long = 0
) {
    @Ignore
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    /**
     * Formatea la fecha de vencimiento para mostrar.
     */
    fun formatDueDate(): String {
        if (dueDate.isNullOrEmpty()) return "Sin fecha límite"
        
        return try {
            val date = dateFormat.parse(dueDate)
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            date?.let { outputFormat.format(it) } ?: "Fecha inválida"
        } catch (e: ParseException) {
            "Fecha inválida"
        }
    }
    
    /**
     * Comprueba si la tarea está vencida.
     */
    fun isOverdue(): Boolean {
        if (status != "pending" || dueDate.isNullOrEmpty()) return false
        
        return try {
            val dueDateObj = dateFormat.parse(dueDate)
            val currentDate = Date()
            dueDateObj?.before(currentDate) ?: false
        } catch (e: ParseException) {
            false
        }
    }
    
    /**
     * Calcula los días restantes hasta la fecha de vencimiento.
     */
    fun getDaysRemaining(): Int {
        if (dueDate.isNullOrEmpty()) return Int.MAX_VALUE
        
        return try {
            val dueDateObj = dateFormat.parse(dueDate)
            val currentDate = Date()
            
            if (dueDateObj != null) {
                val diff = dueDateObj.time - currentDate.time
                TimeUnit.MILLISECONDS.toDays(diff).toInt()
            } else {
                Int.MAX_VALUE
            }
        } catch (e: ParseException) {
            Int.MAX_VALUE
        }
    }
    
    /**
     * Marca la tarea como completada.
     */
    fun complete(userId: Int, userName: String, notes: String? = null, signaturePath: String? = null, photoPath: String? = null): Task {
        val now = dateFormat.format(Date())
        
        return this.copy(
            status = "completed",
            completedAt = now,
            completedBy = userId,
            completedByName = userName,
            completionNotes = notes,
            signatureImage = null, // La imagen de firma se enviará aparte
            photoImage = null, // La imagen de foto se enviará aparte
            localSignaturePath = signaturePath,
            localPhotoPath = photoPath,
            needsSync = true,
            lastSyncTime = System.currentTimeMillis()
        )
    }
    
    /**
     * Marca la tarea como cancelada.
     */
    fun cancel(userId: Int, userName: String, reason: String): Task {
        val now = dateFormat.format(Date())
        
        return this.copy(
            status = "cancelled",
            completedAt = now,
            completedBy = userId,
            completedByName = userName,
            completionNotes = reason,
            needsSync = true,
            lastSyncTime = System.currentTimeMillis()
        )
    }
    
    /**
     * Actualiza la tarea con datos del servidor, preservando cambios locales.
     */
    fun updateFromServer(serverTask: Task): Task {
        // Si la tarea tiene cambios locales pendientes, preservarlos
        return if (needsSync) {
            this.copy(
                title = serverTask.title,
                description = serverTask.description,
                priority = serverTask.priority,
                dueDate = serverTask.dueDate,
                assignedTo = serverTask.assignedTo,
                assignedToName = serverTask.assignedToName,
                createdBy = serverTask.createdBy,
                createdByName = serverTask.createdByName,
                companyId = serverTask.companyId,
                locationId = serverTask.locationId,
                locationName = serverTask.locationName,
                createdAt = serverTask.createdAt,
                updatedAt = serverTask.updatedAt,
                category = serverTask.category,
                tags = serverTask.tags,
                requiresSignature = serverTask.requiresSignature,
                requiresPhoto = serverTask.requiresPhoto,
                // No actualizamos status, completedAt, completedBy, completionNotes
                // porque tenemos cambios locales pendientes
                lastSyncTime = System.currentTimeMillis()
            )
        } else {
            // Si no hay cambios locales, actualizar todo
            serverTask.copy(
                localSignaturePath = this.localSignaturePath,
                localPhotoPath = this.localPhotoPath,
                lastSyncTime = System.currentTimeMillis()
            )
        }
    }
    
    /**
     * Obtiene el código de color para la prioridad.
     */
    fun getPriorityColorCode(): Int {
        return when (priority) {
            0 -> 0xFF4CAF50.toInt() // Verde para normal
            1 -> 0xFFFFC107.toInt() // Amarillo para alta
            else -> 0xFFF44336.toInt() // Rojo para urgente
        }
    }
    
    /**
     * Obtiene el texto descriptivo de la prioridad.
     */
    fun getPriorityText(): String {
        return when (priority) {
            0 -> "Normal"
            1 -> "Alta"
            else -> "Urgente"
        }
    }
}