package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Modelo de datos para tareas
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
    val status: String,
    
    @SerializedName("priority")
    val priority: Int? = 2,
    
    @SerializedName("due_date")
    val dueDate: Date? = null,
    
    @SerializedName("created_at")
    val createdAt: Date? = null,
    
    @SerializedName("updated_at")
    val updatedAt: Date? = null,
    
    @SerializedName("user_id")
    val userId: Int? = null,
    
    @SerializedName("created_by")
    val createdBy: Int? = null,
    
    @SerializedName("location_id")
    val locationId: Int? = null,
    
    @SerializedName("location_name")
    val locationName: String? = null,
    
    @SerializedName("company_id")
    val companyId: Int? = null,
    
    @SerializedName("company_name")
    val companyName: String? = null,
    
    @SerializedName("completion_date")
    val completionDate: Date? = null,
    
    @SerializedName("time_estimate")
    val timeEstimate: Int? = null,
    
    // Campos para sincronización local
    val lastSyncedAt: Date? = null,
    val locallyModified: Boolean = false
)