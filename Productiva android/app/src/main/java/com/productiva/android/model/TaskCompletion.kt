package com.productiva.android.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Modelo para el registro de completado de tareas.
 * Se utiliza para almacenar localmente los datos de completado de tareas
 * y sincronizarlos con el servidor cuando hay conexión a internet.
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
    @PrimaryKey
    val id: String,
    
    @SerializedName("task_id")
    val taskId: Int,
    
    @SerializedName("completion_date")
    val completionDate: String,
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("signature_image")
    val signatureImage: String? = null, // Base64 de la imagen de firma
    
    @SerializedName("photo_image")
    val photoImage: String? = null, // Ruta local o Base64 de foto
    
    @SerializedName("completed_by_user_id")
    val completedByUserId: Int,
    
    @SerializedName("completed_by_username")
    val completedByUsername: String,
    
    @SerializedName("latitude")
    val latitude: Double? = null,
    
    @SerializedName("longitude")
    val longitude: Double? = null,
    
    // Campo local para seguimiento de sincronización
    var synced: Boolean = false,
    
    // Campo local para seguimiento de intentos de sincronización
    var syncAttempts: Int = 0,
    
    // Último intento de sincronización (timestamp)
    var lastSyncAttempt: Long = 0
) {
    companion object {
        /**
         * Crea una nueva instancia de TaskCompletion con valores por defecto.
         */
        fun create(
            taskId: Int,
            userId: Int,
            username: String,
            notes: String? = null,
            signatureImage: String? = null,
            photoImage: String? = null,
            latitude: Double? = null,
            longitude: Double? = null
        ): TaskCompletion {
            // Generar ID único para completado local
            val id = UUID.randomUUID().toString()
            
            // Formatear fecha actual
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val currentDate = dateFormat.format(Date())
            
            return TaskCompletion(
                id = id,
                taskId = taskId,
                completionDate = currentDate,
                notes = notes,
                signatureImage = signatureImage,
                photoImage = photoImage,
                completedByUserId = userId,
                completedByUsername = username,
                latitude = latitude,
                longitude = longitude,
                synced = false,
                syncAttempts = 0,
                lastSyncAttempt = 0
            )
        }
    }
    
    /**
     * Comprueba si este completado está listo para sincronizar.
     * Se considera listo si no está sincronizado y han pasado al menos
     * 5 minutos desde el último intento (para evitar demasiados intentos seguidos).
     */
    fun isReadyToSync(): Boolean {
        if (synced) return false
        
        val now = System.currentTimeMillis()
        val minDelayBetweenAttempts = 5 * 60 * 1000 // 5 minutos en milisegundos
        
        return lastSyncAttempt == 0L || (now - lastSyncAttempt) > minDelayBetweenAttempts
    }
    
    /**
     * Incrementa el contador de intentos de sincronización y actualiza el timestamp.
     */
    fun incrementSyncAttempt(): TaskCompletion {
        return this.copy(
            syncAttempts = syncAttempts + 1,
            lastSyncAttempt = System.currentTimeMillis()
        )
    }
    
    /**
     * Marca el completado como sincronizado.
     */
    fun markAsSynced(): TaskCompletion {
        return this.copy(
            synced = true,
            lastSyncAttempt = System.currentTimeMillis()
        )
    }
}