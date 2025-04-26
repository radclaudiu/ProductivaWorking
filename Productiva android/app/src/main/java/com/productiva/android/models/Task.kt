package com.productiva.android.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("location_id")
    val locationId: Int,
    
    @SerializedName("group_id")
    val groupId: Int? = null,
    
    @SerializedName("frequency")
    val frequency: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("priority")
    val priority: Int = 0,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    
    @SerializedName("due_date")
    val dueDate: String? = null,
    
    @SerializedName("print_label")
    val printLabel: Boolean = false,
    
    @SerializedName("needs_signature")
    val needsSignature: Boolean = false,
    
    @SerializedName("needs_photo")
    val needsPhoto: Boolean = false,
    
    // Campos locales no sincronizados con la API
    var localPhotoPath: String? = null,
    var localSyncStatus: Int = SYNC_STATUS_SYNCED, // 0 = synced, 1 = pending, 2 = error
    var lastSyncAttempt: Long? = null
) {
    companion object {
        const val SYNC_STATUS_SYNCED = 0
        const val SYNC_STATUS_PENDING = 1
        const val SYNC_STATUS_ERROR = 2
    }
}