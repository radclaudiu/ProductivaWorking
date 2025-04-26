package com.productiva.android.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.productiva.android.data.converter.DateConverter
import com.productiva.android.data.converter.ListConverter
import java.util.Date

/**
 * Entidad que representa una tarea en el sistema.
 *
 * @property id Identificador único de la tarea.
 * @property title Título descriptivo de la tarea.
 * @property description Descripción detallada de la tarea (opcional).
 * @property status Estado actual de la tarea (pendiente, en progreso, completada, etc.).
 * @property priority Prioridad de la tarea (baja, media, alta).
 * @property assignedTo ID del empleado asignado a la tarea (opcional).
 * @property companyId ID de la empresa a la que pertenece la tarea.
 * @property createdBy ID del usuario que creó la tarea.
 * @property productId ID del producto relacionado con la tarea (opcional).
 * @property dueDate Fecha límite para completar la tarea (opcional).
 * @property createdAt Fecha de creación de la tarea.
 * @property updatedAt Fecha de última actualización de la tarea.
 * @property completedAt Fecha de completado de la tarea (opcional).
 * @property tags Lista de etiquetas asociadas a la tarea (opcional).
 * @property requireSignature Indica si se requiere firma al completar.
 * @property requirePhoto Indica si se requiere foto al completar.
 * @property requireNotes Indica si se requieren notas al completar.
 * @property location Ubicación asociada a la tarea (opcional).
 * @property syncStatus Estado de sincronización con el servidor.
 * @property lastSyncTime Marca de tiempo de la última sincronización.
 * @property isDeleted Indica si la tarea ha sido marcada para eliminación.
 * @property pendingChanges Indica si hay cambios locales pendientes de sincronizar.
 * @property completionData Datos de completado de la tarea (opcional).
 */
@Entity(
    tableName = "tasks",
    indices = [
        Index("companyId"),
        Index("productId"),
        Index("assignedTo"),
        Index("syncStatus")
    ]
)
@TypeConverters(DateConverter::class, ListConverter::class)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val title: String,
    val description: String? = null,
    val status: String, // "pending", "in_progress", "completed", "cancelled"
    val priority: String, // "low", "medium", "high"
    
    val assignedTo: Int? = null,
    val companyId: Int,
    val createdBy: Int,
    val productId: Int? = null,
    
    val dueDate: Date? = null,
    val createdAt: Date,
    val updatedAt: Date,
    val completedAt: Date? = null,
    
    val tags: List<String> = emptyList(),
    
    val requireSignature: Boolean = false,
    val requirePhoto: Boolean = false,
    val requireNotes: Boolean = true,
    
    val location: String? = null,
    
    // Campos para sincronización
    val syncStatus: String = SyncStatus.SYNCED, // "synced", "pending_upload", "pending_update", "conflict"
    val lastSyncTime: Long = 0,
    val isDeleted: Boolean = false,
    val pendingChanges: Boolean = false,
    
    @Ignore
    val completionData: TaskCompletion? = null
) {
    /**
     * Constructor secundario que incluye el ID para creación desde Room.
     */
    constructor(
        id: Int,
        title: String,
        description: String?,
        status: String,
        priority: String,
        assignedTo: Int?,
        companyId: Int,
        createdBy: Int,
        productId: Int?,
        dueDate: Date?,
        createdAt: Date,
        updatedAt: Date,
        completedAt: Date?,
        tags: List<String>,
        requireSignature: Boolean,
        requirePhoto: Boolean,
        requireNotes: Boolean,
        location: String?,
        syncStatus: String,
        lastSyncTime: Long,
        isDeleted: Boolean,
        pendingChanges: Boolean
    ) : this(
        id, title, description, status, priority, assignedTo, companyId, createdBy, productId,
        dueDate, createdAt, updatedAt, completedAt, tags, requireSignature, requirePhoto, 
        requireNotes, location, syncStatus, lastSyncTime, isDeleted, pendingChanges, null
    )
    
    /**
     * Comprueba si la tarea está completada.
     *
     * @return true si el estado es "completed", false en caso contrario.
     */
    fun isCompleted(): Boolean {
        return status == "completed"
    }
    
    /**
     * Comprueba si la tarea está vencida.
     *
     * @return true si la fecha límite ha pasado y la tarea no está completada, false en caso contrario.
     */
    fun isOverdue(): Boolean {
        return dueDate != null && Date().after(dueDate) && status != "completed"
    }
    
    /**
     * Crea una copia de la tarea con estado completado.
     *
     * @param completion Datos de completado.
     * @return Tarea actualizada con estado completado.
     */
    fun complete(completion: TaskCompletion): Task {
        return copy(
            status = "completed",
            completedAt = Date(),
            updatedAt = Date(),
            syncStatus = SyncStatus.PENDING_UPDATE,
            pendingChanges = true,
            completionData = completion
        )
    }
    
    /**
     * Crea una copia de la tarea marcada para eliminación.
     *
     * @return Tarea actualizada marcada para eliminación.
     */
    fun markForDeletion(): Task {
        return copy(
            isDeleted = true,
            syncStatus = SyncStatus.PENDING_DELETE,
            pendingChanges = true,
            updatedAt = Date()
        )
    }
    
    /**
     * Crea una copia de la tarea con estado de sincronización actualizado.
     *
     * @param newSyncStatus Nuevo estado de sincronización.
     * @return Tarea actualizada con nuevo estado de sincronización.
     */
    fun withSyncStatus(newSyncStatus: String, lastSyncTime: Long = System.currentTimeMillis()): Task {
        return copy(
            syncStatus = newSyncStatus,
            lastSyncTime = lastSyncTime,
            pendingChanges = newSyncStatus != SyncStatus.SYNCED
        )
    }
    
    /**
     * Clase auxiliar que define constantes para los estados de sincronización.
     */
    object SyncStatus {
        const val SYNCED = "synced"
        const val PENDING_UPLOAD = "pending_upload"
        const val PENDING_UPDATE = "pending_update"
        const val PENDING_DELETE = "pending_delete"
        const val CONFLICT = "conflict"
    }
}