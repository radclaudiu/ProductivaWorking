package com.productiva.android.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Modelo de completado de tarea
 * Representa el registro de finalización de una tarea, incluyendo firmas e imágenes
 */
@Entity(
    tableName = "task_completions",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId")]
)
data class TaskCompletion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    // Relación con tarea
    val taskId: Int,
    
    // Datos del completado
    @SerializedName("completed_by")
    val completedBy: Int? = null,
    @SerializedName("completed_at")
    val completedAt: Date? = null,
    val comments: String? = null,
    
    // Archivos adjuntos
    @SerializedName("signature_path")
    val signaturePath: String? = null,
    @SerializedName("photo_path")
    val photoPath: String? = null,
    
    // Geolocalización
    val latitude: Double? = null,
    val longitude: Double? = null,
    
    // Estado del registro
    val synced: Boolean = false,
    @SerializedName("is_local_only")
    val isLocalOnly: Boolean = true,
    @SerializedName("last_synced")
    val lastSynced: Date? = null
)