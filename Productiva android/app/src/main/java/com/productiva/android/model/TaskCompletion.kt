package com.productiva.android.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Modelo de datos para completaciones de tareas
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
    indices = [
        Index("taskId")
    ]
)
data class TaskCompletion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @SerializedName("task_id")
    val taskId: Int,
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("user_name")
    val userName: String? = null,
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("completion_date")
    val completionDate: Date? = null,
    
    @SerializedName("time_spent")
    val timeSpent: Int? = null,
    
    @SerializedName("client_name")
    val clientName: String? = null,
    
    @SerializedName("has_signature")
    val hasSignature: Boolean? = false,
    
    @SerializedName("signature_path")
    val signaturePath: String? = null,
    
    @SerializedName("has_photo")
    val hasPhoto: Boolean? = false,
    
    @SerializedName("photo_path")
    val photoPath: String? = null,
    
    @SerializedName("created_at")
    val createdAt: Date? = null,
    
    @SerializedName("updated_at")
    val updatedAt: Date? = null,
    
    // Campos para sincronización local
    val lastSyncedAt: Date? = null,
    val synced: Boolean = false,
    val pendingUpload: Boolean = true
)