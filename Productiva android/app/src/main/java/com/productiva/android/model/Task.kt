package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Ignore
import java.util.Date

/**
 * Modelo de datos para tareas
 */
@Entity(
    tableName = "tasks",
    indices = [
        Index("userId"),
        Index("companyId"),
        Index("locationId")
    ]
)
data class Task(
    @PrimaryKey
    val id: Int,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "description")
    val description: String?,
    
    @ColumnInfo(name = "status")
    val status: String,
    
    @ColumnInfo(name = "userId")
    val userId: Int,
    
    @ColumnInfo(name = "userName")
    val userName: String?,
    
    @ColumnInfo(name = "companyId")
    val companyId: Int,
    
    @ColumnInfo(name = "companyName")
    val companyName: String?,
    
    @ColumnInfo(name = "locationId")
    val locationId: Int?,
    
    @ColumnInfo(name = "locationName")
    val locationName: String?,
    
    @ColumnInfo(name = "priority")
    val priority: Int = 0,
    
    @ColumnInfo(name = "dueDate")
    val dueDate: Date?,
    
    @ColumnInfo(name = "completionDate")
    val completionDate: Date? = null,
    
    @ColumnInfo(name = "repeatFrequency")
    val repeatFrequency: String? = null,
    
    @ColumnInfo(name = "tags")
    val tags: List<String>? = null,
    
    @ColumnInfo(name = "requiredSignature")
    val requiredSignature: Boolean = false,
    
    @ColumnInfo(name = "requiredPhoto")
    val requiredPhoto: Boolean = false,
    
    @ColumnInfo(name = "createdAt")
    val createdAt: Date?,
    
    @ColumnInfo(name = "updatedAt")
    val updatedAt: Date?,
    
    @ColumnInfo(name = "createdById")
    val createdById: Int?,
    
    @ColumnInfo(name = "updatedById")
    val updatedById: Int?,
    
    @ColumnInfo(name = "lastSyncedAt")
    val lastSyncedAt: Date? = null,
    
    @ColumnInfo(name = "localId")
    val localId: Long? = null,
    
    @ColumnInfo(name = "locallyModified")
    val locallyModified: Boolean = false
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_IN_PROGRESS = "in_progress"
        const val STATUS_COMPLETED = "completed"
        const val STATUS_CANCELLED = "cancelled"
        
        val STATUS_OPTIONS = listOf(
            STATUS_PENDING,
            STATUS_IN_PROGRESS,
            STATUS_COMPLETED,
            STATUS_CANCELLED
        )
    }
    
    fun isCompleted(): Boolean {
        return status == STATUS_COMPLETED
    }
    
    fun isCancelled(): Boolean {
        return status == STATUS_CANCELLED
    }
    
    fun isActive(): Boolean {
        return status == STATUS_PENDING || status == STATUS_IN_PROGRESS
    }
    
    fun isPastDue(): Boolean {
        if (dueDate == null) return false
        return dueDate.before(Date()) && isActive()
    }
}