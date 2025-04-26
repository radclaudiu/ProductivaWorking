package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
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
        Index("taskId"),
        Index("userId")
    ]
)
data class TaskCompletion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "taskId")
    val taskId: Int,
    
    @ColumnInfo(name = "userId")
    val userId: Int,
    
    @ColumnInfo(name = "userName")
    val userName: String?,
    
    @ColumnInfo(name = "notes")
    val notes: String?,
    
    @ColumnInfo(name = "completionDate")
    val completionDate: Date = Date(),
    
    @ColumnInfo(name = "locationId")
    val locationId: Int?,
    
    @ColumnInfo(name = "locationName")
    val locationName: String?,
    
    @ColumnInfo(name = "latitude")
    val latitude: Double? = null,
    
    @ColumnInfo(name = "longitude")
    val longitude: Double? = null,
    
    @ColumnInfo(name = "hasSignature")
    val hasSignature: Boolean = false,
    
    @ColumnInfo(name = "signaturePath")
    val signaturePath: String? = null,
    
    @ColumnInfo(name = "hasPhoto")
    val hasPhoto: Boolean = false,
    
    @ColumnInfo(name = "photoPath")
    val photoPath: String? = null,
    
    @ColumnInfo(name = "remoteId")
    val remoteId: Int? = null,
    
    @ColumnInfo(name = "synced")
    val synced: Boolean = false,
    
    @ColumnInfo(name = "pendingUpload")
    val pendingUpload: Boolean = true,
    
    @ColumnInfo(name = "lastSyncedAt")
    val lastSyncedAt: Date? = null,
    
    @ColumnInfo(name = "createdAt")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updatedAt")
    val updatedAt: Date = Date()
)