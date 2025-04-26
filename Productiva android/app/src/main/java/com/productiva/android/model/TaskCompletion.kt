package com.productiva.android.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Modelo para completación de tareas.
 * Representa la información sobre cómo se completó una tarea.
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
    
    @SerializedName("completed_by")
    val completedBy: Int, // ID del usuario que completó la tarea
    
    @SerializedName("completed_at")
    val completedAt: String, // Fecha y hora de completación
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("location_id")
    val locationId: Int? = null,
    
    @SerializedName("photo_path")
    val photoPath: String? = null,
    
    @SerializedName("signature_path")
    val signaturePath: String? = null,
    
    @SerializedName("scan_data")
    val scanData: String? = null,
    
    @SerializedName("latitude")
    val latitude: Double? = null,
    
    @SerializedName("longitude")
    val longitude: Double? = null,
    
    @SerializedName("status")
    val status: String = "completed", // completed, partial, failed
    
    @SerializedName("rating")
    val rating: Int? = null, // 1-5 estrellas
    
    // Campos locales
    var isLocalOnly: Boolean = false,
    var isSynced: Boolean = false,
    var retryCount: Int = 0,
    var lastSyncAttempt: Long = 0,
    var localPhotoPath: String? = null,     // Ruta local a la foto
    var localSignaturePath: String? = null  // Ruta local a la firma
) {
    /**
     * Verifica si la completación requiere ser sincronizada.
     */
    fun needsSync(): Boolean {
        return isLocalOnly && !isSynced
    }
    
    /**
     * Verifica si la completación tiene foto.
     */
    fun hasPhoto(): Boolean {
        return !photoPath.isNullOrEmpty() || !localPhotoPath.isNullOrEmpty()
    }
    
    /**
     * Verifica si la completación tiene firma.
     */
    fun hasSignature(): Boolean {
        return !signaturePath.isNullOrEmpty() || !localSignaturePath.isNullOrEmpty()
    }
    
    /**
     * Verifica si la completación tiene datos de escaneo.
     */
    fun hasScanData(): Boolean {
        return !scanData.isNullOrEmpty()
    }
    
    /**
     * Verifica si la completación tiene coordenadas de ubicación.
     */
    fun hasLocation(): Boolean {
        return latitude != null && longitude != null
    }
}