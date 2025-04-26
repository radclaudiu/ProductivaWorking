package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Entidad que representa la finalización de una tarea en la aplicación.
 */
@Entity(tableName = "task_completions")
data class TaskCompletion(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    
    @ColumnInfo(name = "server_id")
    @SerializedName("id") 
    val serverId: Int? = null, // ID del servidor después de sincronizar
    
    @ColumnInfo(name = "task_id")
    @SerializedName("task_id") 
    val taskId: Int,
    
    @ColumnInfo(name = "completed_by")
    @SerializedName("completed_by") 
    val completedBy: Int,
    
    @ColumnInfo(name = "completion_date")
    @SerializedName("completion_date") 
    val completionDate: Date = Date(),
    
    @ColumnInfo(name = "comments")
    @SerializedName("comments") 
    val comments: String? = null,
    
    @ColumnInfo(name = "signature_file")
    @SerializedName("signature_file") 
    val signatureFile: String? = null,
    
    @ColumnInfo(name = "photo_file")
    @SerializedName("photo_file") 
    val photoFile: String? = null,
    
    @ColumnInfo(name = "location_latitude")
    @SerializedName("location_latitude") 
    val locationLatitude: Double? = null,
    
    @ColumnInfo(name = "location_longitude")
    @SerializedName("location_longitude") 
    val locationLongitude: Double? = null,
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,
    
    @ColumnInfo(name = "sync_date")
    val syncDate: Long? = null,
    
    @ColumnInfo(name = "local_files")
    val localFiles: String? = null, // JSON array de rutas de archivos locales
    
    @ColumnInfo(name = "labels_printed")
    @SerializedName("labels_printed") 
    val labelsPrinted: String? = null, // JSON array de IDs de etiquetas impresas
    
    @ColumnInfo(name = "completion_status")
    @SerializedName("completion_status") 
    val completionStatus: String = "ok" // "ok", "partial", "issue"
) {
    /**
     * Determina si esta finalización de tarea tiene una firma adjunta.
     */
    fun hasSignature(): Boolean {
        return !signatureFile.isNullOrEmpty()
    }
    
    /**
     * Determina si esta finalización de tarea tiene una foto adjunta.
     */
    fun hasPhoto(): Boolean {
        return !photoFile.isNullOrEmpty()
    }
    
    /**
     * Determina si esta finalización de tarea tiene coordenadas de ubicación.
     */
    fun hasLocation(): Boolean {
        return locationLatitude != null && locationLongitude != null
    }
    
    /**
     * Obtiene un color para representar el estado de finalización.
     */
    fun getCompletionStatusColor(): Int {
        return when (completionStatus) {
            "ok" -> 0xFF4CAF50.toInt() // Verde para OK
            "partial" -> 0xFFFF9800.toInt() // Naranja para parcial
            "issue" -> 0xFFF44336.toInt() // Rojo para problemas
            else -> 0xFF9E9E9E.toInt() // Gris por defecto
        }
    }
    
    /**
     * Devuelve un estado de finalización localizado para su presentación.
     */
    fun getLocalizedCompletionStatus(): String {
        return when (completionStatus) {
            "ok" -> "Completada"
            "partial" -> "Parcial"
            "issue" -> "Con problemas"
            else -> completionStatus
        }
    }
}