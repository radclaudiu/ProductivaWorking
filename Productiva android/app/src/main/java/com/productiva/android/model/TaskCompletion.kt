package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import com.google.gson.annotations.SerializedName

/**
 * Entidad que representa la completación de una tarea en la aplicación.
 */
@Entity(
    tableName = "task_completions",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TaskCompletion(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    
    @ColumnInfo(name = "remote_id")
    @SerializedName("id") 
    val remoteId: Int? = null,
    
    @ColumnInfo(name = "task_id", index = true)
    @SerializedName("task_id") 
    val taskId: Int,
    
    @ColumnInfo(name = "user_id")
    @SerializedName("user_id") 
    val userId: Int,
    
    @ColumnInfo(name = "user_name")
    @SerializedName("user_name") 
    val userName: String,
    
    @ColumnInfo(name = "completion_date")
    @SerializedName("completion_date") 
    val completionDate: String,
    
    @ColumnInfo(name = "notes")
    @SerializedName("notes") 
    val notes: String?,
    
    @ColumnInfo(name = "signature_path")
    @SerializedName("signature_path") 
    val signaturePath: String?,
    
    @ColumnInfo(name = "photo_path")
    @SerializedName("photo_path") 
    val photoPath: String?,
    
    @ColumnInfo(name = "location_id")
    @SerializedName("location_id") 
    val locationId: Int?,
    
    @ColumnInfo(name = "location_name")
    @SerializedName("location_name") 
    val locationName: String?,
    
    @ColumnInfo(name = "latitude")
    @SerializedName("latitude") 
    val latitude: Double?,
    
    @ColumnInfo(name = "longitude")
    @SerializedName("longitude") 
    val longitude: Double?,
    
    @ColumnInfo(name = "sync_pending")
    val syncPending: Boolean = false,
    
    @ColumnInfo(name = "last_sync")
    val lastSync: Long = System.currentTimeMillis()
)