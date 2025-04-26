package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para completados de tareas
 * Representa el registro de una tarea completada en el sistema Productiva
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
    
    @SerializedName("task_id")
    val taskId: Int,
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("location_id")
    val locationId: Int,
    
    @SerializedName("completion_date")
    val completionDate: String,
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("photo_path")
    val photoPath: String? = null,
    
    @SerializedName("signature_path")
    val signaturePath: String? = null,
    
    @SerializedName("sync_status")
    var syncStatus: Int = SYNC_PENDING,
    
    @SerializedName("server_id")
    var serverId: Int? = null,
    
    @SerializedName("last_sync")
    var lastSync: Long = System.currentTimeMillis()
) {
    companion object {
        const val SYNC_PENDING = 0
        const val SYNC_COMPLETE = 1
        const val SYNC_ERROR = 2
    }
}