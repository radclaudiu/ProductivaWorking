package com.productiva.android.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Clase de entidad para representar una tarea en la base de datos
 */
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["assignedTo"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("assignedTo"),
        Index("status"),
        Index("location_id")
    ]
)
data class Task(
    @PrimaryKey
    @ColumnInfo(name = "id")
    @SerializedName("id")
    val id: Int = 0,
    
    @ColumnInfo(name = "title")
    @SerializedName("title")
    val title: String,
    
    @ColumnInfo(name = "description")
    @SerializedName("description")
    val description: String? = null,
    
    @ColumnInfo(name = "assignedTo")
    @SerializedName("assigned_to")
    val assignedTo: Int? = null,
    
    @ColumnInfo(name = "assignedToName")
    @SerializedName("assigned_to_name")
    val assignedToName: String? = null,
    
    @ColumnInfo(name = "status")
    @SerializedName("status")
    val status: String = "pending", // pending, in_progress, completed, cancelled
    
    @ColumnInfo(name = "priority")
    @SerializedName("priority")
    val priority: Int = 1, // 1-5, donde 5 es la más alta
    
    @ColumnInfo(name = "createdAt")
    @SerializedName("created_at")
    val createdAt: Date? = null,
    
    @ColumnInfo(name = "updatedAt")
    @SerializedName("updated_at")
    val updatedAt: Date? = null,
    
    @ColumnInfo(name = "dueDate")
    @SerializedName("due_date")
    val dueDate: Date? = null,
    
    @ColumnInfo(name = "completedAt")
    @SerializedName("completed_at")
    val completedAt: Date? = null,
    
    @ColumnInfo(name = "company_id")
    @SerializedName("company_id")
    val companyId: Int? = null,
    
    @ColumnInfo(name = "company_name")
    @SerializedName("company_name")
    val companyName: String? = null,
    
    @ColumnInfo(name = "location_id")
    @SerializedName("location_id")
    val locationId: Int? = null,
    
    @ColumnInfo(name = "location_name")
    @SerializedName("location_name")
    val locationName: String? = null,
    
    @ColumnInfo(name = "requires_signature")
    @SerializedName("requires_signature")
    val requiresSignature: Boolean = false,
    
    @ColumnInfo(name = "requires_photo")
    @SerializedName("requires_photo")
    val requiresPhoto: Boolean = false,
    
    @ColumnInfo(name = "requires_notes")
    @SerializedName("requires_notes")
    val requiresNotes: Boolean = false,
    
    @ColumnInfo(name = "has_label")
    @SerializedName("has_label")
    val hasLabel: Boolean = false,
    
    @ColumnInfo(name = "label_template_id")
    @SerializedName("label_template_id")
    val labelTemplateId: Int? = null,
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = true,
    
    @ColumnInfo(name = "last_sync")
    val lastSync: Long = System.currentTimeMillis()
)