package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modelo de datos que representa la información de completado de una tarea.
 * Se almacena en la base de datos local y se sincroniza con el servidor.
 */
@Entity(tableName = "task_completions")
data class TaskCompletion(
    @PrimaryKey
    @SerializedName("task_id")
    val taskId: Int,
    
    @SerializedName("status")
    val status: String,  // "COMPLETED", "CANCELLED"
    
    @SerializedName("completed_by")
    val completedBy: Int,
    
    @SerializedName("completed_at")
    val completedAt: String = getCurrentTimestamp(),
    
    @SerializedName("notes")
    val notes: String?,
    
    @SerializedName("signature_data")
    val signatureData: String?,  // Base64 de la firma
    
    @SerializedName("photo_data")
    val photoData: String?,  // Base64 de la foto
    
    @SerializedName("location_latitude")
    val locationLatitude: Double?,
    
    @SerializedName("location_longitude")
    val locationLongitude: Double?,
    
    @SerializedName("device_info")
    val deviceInfo: String?,
    
    // Campos locales (no se envían al servidor)
    val needsSync: Boolean = true,
    val syncError: String? = null,
    val localSignaturePath: String? = null,
    val localPhotoPath: String? = null,
    val lastSyncAttempt: Long = 0,
    val syncAttempts: Int = 0
) {
    companion object {
        /**
         * Genera una marca de tiempo en formato ISO 8601.
         */
        fun getCurrentTimestamp(): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            return dateFormat.format(Date())
        }
        
        /**
         * Crea una instancia de completado de tarea.
         */
        fun create(
            taskId: Int,
            status: String,
            userId: Int,
            notes: String? = null,
            signatureData: String? = null,
            photoData: String? = null,
            latitude: Double? = null,
            longitude: Double? = null,
            deviceInfo: String? = null,
            localSignaturePath: String? = null,
            localPhotoPath: String? = null
        ): TaskCompletion {
            return TaskCompletion(
                taskId = taskId,
                status = status,
                completedBy = userId,
                notes = notes,
                signatureData = signatureData,
                photoData = photoData,
                locationLatitude = latitude,
                locationLongitude = longitude,
                deviceInfo = deviceInfo,
                localSignaturePath = localSignaturePath,
                localPhotoPath = localPhotoPath
            )
        }
    }
    
    /**
     * Verifica si el completado tiene información de geolocalización.
     */
    fun hasLocationInfo(): Boolean {
        return locationLatitude != null && locationLongitude != null
    }
    
    /**
     * Formatea la información de geolocalización para mostrar en UI.
     */
    fun getLocationDisplay(): String {
        return if (hasLocationInfo()) {
            String.format("%.6f, %.6f", locationLatitude, locationLongitude)
        } else {
            "No disponible"
        }
    }
    
    /**
     * Verifica si el completado está listo para sincronización.
     */
    fun isReadyForSync(): Boolean {
        return needsSync && syncAttempts < 5
    }
}