package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modelo de datos que representa un registro de completado de tarea.
 * Se almacena en la base de datos local y se sincroniza con el servidor.
 * Se utiliza para registrar los completados de tareas offline que luego serán sincronizados.
 */
@Entity(tableName = "task_completions")
data class TaskCompletion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @SerializedName("task_id")
    val taskId: Int,
    
    @SerializedName("status")
    val status: String, // COMPLETED, CANCELLED
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("notes")
    val notes: String?,
    
    @SerializedName("completion_date")
    val completionDate: String,
    
    @SerializedName("has_signature")
    val hasSignature: Boolean,
    
    @SerializedName("has_photo")
    val hasPhoto: Boolean,
    
    @SerializedName("signature_data")
    val signatureData: String?,
    
    @SerializedName("photo_data")
    val photoData: String?,
    
    @SerializedName("local_signature_path")
    val localSignaturePath: String?,
    
    @SerializedName("local_photo_path")
    val localPhotoPath: String?,
    
    @SerializedName("synced")
    val synced: Boolean = false
) {
    /**
     * Marca este completado como sincronizado.
     */
    fun markAsSynced(): TaskCompletion {
        return this.copy(synced = true)
    }
    
    companion object {
        /**
         * Crea un nuevo objeto de completado de tarea.
         */
        fun create(
            taskId: Int,
            status: String,
            userId: Int,
            notes: String? = null,
            signatureData: String? = null,
            photoData: String? = null,
            localSignaturePath: String? = null,
            localPhotoPath: String? = null
        ): TaskCompletion {
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
            
            return TaskCompletion(
                taskId = taskId,
                status = status,
                userId = userId,
                notes = notes,
                completionDate = now,
                hasSignature = !localSignaturePath.isNullOrEmpty() || !signatureData.isNullOrEmpty(),
                hasPhoto = !localPhotoPath.isNullOrEmpty() || !photoData.isNullOrEmpty(),
                signatureData = signatureData,
                photoData = photoData,
                localSignaturePath = localSignaturePath,
                localPhotoPath = localPhotoPath,
                synced = false
            )
        }
    }
}