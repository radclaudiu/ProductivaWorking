package com.productiva.android.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Clase de entidad para representar una completación de tarea en la base de datos
 */
@Entity(
    tableName = "task_completions",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("task_id"),
        Index("user_id"),
        Index("completion_date")
    ]
)
data class TaskCompletion(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @SerializedName("id")
    val id: Int = 0,
    
    @ColumnInfo(name = "task_id")
    @SerializedName("task_id")
    val taskId: Int,
    
    @ColumnInfo(name = "user_id")
    @SerializedName("user_id")
    val userId: Int? = null,
    
    @ColumnInfo(name = "user_name")
    @SerializedName("user_name")
    val userName: String? = null,
    
    @ColumnInfo(name = "completion_date")
    @SerializedName("completion_date")
    val completionDate: Date = Date(),
    
    @ColumnInfo(name = "notes")
    @SerializedName("notes")
    val notes: String? = null,
    
    @ColumnInfo(name = "status")
    @SerializedName("status")
    val status: String = "completed", // completed, partial, failed
    
    @ColumnInfo(name = "signature_path")
    @SerializedName("signature_path")
    val signaturePath: String? = null,
    
    @ColumnInfo(name = "client_name")
    @SerializedName("client_name")
    val clientName: String? = null,
    
    @ColumnInfo(name = "photo_path")
    @SerializedName("photo_path")
    val photoPath: String? = null,
    
    @ColumnInfo(name = "time_spent")
    @SerializedName("time_spent")
    val timeSpent: Int? = null, // en minutos
    
    @ColumnInfo(name = "latitude")
    @SerializedName("latitude")
    val latitude: Double? = null,
    
    @ColumnInfo(name = "longitude")
    @SerializedName("longitude")
    val longitude: Double? = null,
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,
    
    @ColumnInfo(name = "last_sync")
    val lastSync: Long = System.currentTimeMillis()
)