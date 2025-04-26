package com.productiva.android.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.productiva.android.database.Converters
import java.util.Date

/**
 * Modelo de datos para la completación de una tarea.
 * Registra los detalles de cómo se completó una tarea específica.
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
@TypeConverters(Converters::class)
data class TaskCompletion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    // Relación con la tarea
    val taskId: Int,
    
    // Detalles de la completación
    val completedByUserId: Int,
    val completedByUserName: String,
    val completedAt: Date,
    val comments: String?,
    
    // Evidencias
    val photoUrl: String?, // Ruta a la foto de evidencia (local o remota)
    val signatureUrl: String?, // Ruta a la firma (local o remota)
    
    // Datos adicionales
    val latitude: Double?,
    val longitude: Double?,
    val accuracy: Float?,
    
    // Datos para sincronización
    val serverCompletionId: Int?, // ID en el servidor, null si aún no está sincronizado
    val isLocalOnly: Boolean = true, // Indica si fue creado solo localmente
    val isSynced: Boolean = false, // Indica si ya está sincronizado con el servidor
    val lastSyncAttempt: Long? = null, // Timestamp del último intento de sincronización
    val syncError: String? = null // Error durante la sincronización, si lo hubo
)