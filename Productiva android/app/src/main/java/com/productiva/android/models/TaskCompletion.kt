package com.productiva.android.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "task_completions")
data class TaskCompletion(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    
    @SerializedName("id")
    val id: Int? = null, // Puede ser null si aún no está sincronizado
    
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
    
    @SerializedName("signature_path")
    val signaturePath: String? = null,
    
    @SerializedName("photo_path")
    val photoPath: String? = null,
    
    // Campos locales no sincronizados con la API
    var localSignaturePath: String? = null,
    var localPhotoPath: String? = null,
    var localSyncStatus: Int = Task.SYNC_STATUS_PENDING,
    var lastSyncAttempt: Long? = null
)