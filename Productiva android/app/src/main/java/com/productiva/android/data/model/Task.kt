package com.productiva.android.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para tareas.
 * Representa la estructura de datos de una tarea en el sistema.
 * 
 * @property id Identificador único de la tarea
 * @property companyId ID de la empresa a la que pertenece
 * @property title Título descriptivo de la tarea
 * @property description Descripción detallada de la tarea
 * @property status Estado de la tarea (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)
 * @property priority Prioridad de la tarea (LOW, MEDIUM, HIGH, URGENT)
 * @property dueDate Fecha límite para completar la tarea (formato "YYYY-MM-DD")
 * @property createdAt Fecha de creación de la tarea
 * @property createdBy ID del usuario que creó la tarea
 * @property requiresSignature Indica si requiere firma al completar
 * @property requiresPhoto Indica si requiere foto al completar
 * @property requiresNote Indica si requiere nota al completar
 * @property location Ubicación relacionada con la tarea
 * @property syncStatus Estado de sincronización con el servidor
 */
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Company::class,
            parentColumns = ["id"],
            childColumns = ["companyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("companyId")]
)
data class Task(
    @PrimaryKey
    @SerializedName("id")
    val id: Int = 0,  // 0 para tareas locales aún no sincronizadas
    
    @SerializedName("company_id")
    val companyId: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("status")
    val status: String = "PENDING",  // PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    
    @SerializedName("priority")
    val priority: String = "MEDIUM",  // LOW, MEDIUM, HIGH, URGENT
    
    @SerializedName("due_date")
    val dueDate: String? = null,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("created_by")
    val createdBy: Int? = null,
    
    @SerializedName("requires_signature")
    val requiresSignature: Boolean = false,
    
    @SerializedName("requires_photo")
    val requiresPhoto: Boolean = false,
    
    @SerializedName("requires_note")
    val requiresNote: Boolean = false,
    
    @SerializedName("location")
    val location: String? = null,
    
    // Campos locales (no se envían al servidor)
    val syncStatus: String = "SYNCED"  // SYNCED, PENDING_SYNC, SYNC_ERROR
)