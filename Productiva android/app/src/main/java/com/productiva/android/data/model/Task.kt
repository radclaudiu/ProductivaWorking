package com.productiva.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.productiva.android.data.converter.DateConverter
import com.productiva.android.data.converter.ListConverter
import java.util.Date

/**
 * Modelo que representa una tarea en el sistema.
 *
 * @property id ID único de la tarea.
 * @property title Título de la tarea.
 * @property description Descripción detallada de la tarea (opcional).
 * @property assignedToId ID del empleado asignado (opcional).
 * @property assignedToName Nombre del empleado asignado (opcional).
 * @property companyId ID de la empresa a la que pertenece la tarea.
 * @property dueDate Fecha de vencimiento de la tarea (opcional).
 * @property priority Prioridad de la tarea (1-5, siendo 5 la más alta).
 * @property status Estado actual de la tarea.
 * @property tags Lista de etiquetas asociadas a la tarea.
 * @property requiresSignature Indica si la tarea requiere firma al completarse.
 * @property requiresPhoto Indica si la tarea requiere foto al completarse.
 * @property completionId ID del registro de completado asociado (si está completada).
 * @property createdAt Fecha de creación de la tarea.
 * @property updatedAt Fecha de última actualización de la tarea.
 * @property syncStatus Estado de sincronización de la tarea.
 * @property pendingChanges Indica si hay cambios pendientes de sincronización.
 */
@Entity(tableName = "tasks")
@TypeConverters(DateConverter::class, ListConverter::class)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String? = null,
    val assignedToId: Int? = null,
    val assignedToName: String? = null,
    val companyId: Int,
    val dueDate: Date? = null,
    val priority: Int = 3,
    val status: TaskStatus = TaskStatus.PENDING,
    val tags: List<String> = emptyList(),
    val requiresSignature: Boolean = false,
    val requiresPhoto: Boolean = false,
    val completionId: Int? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD,
    val pendingChanges: Boolean = false
) {
    /**
     * Estado de la tarea.
     */
    enum class TaskStatus {
        /** Pendiente de iniciar. */
        PENDING,
        /** En progreso. */
        IN_PROGRESS,
        /** Completada. */
        COMPLETED,
        /** Cancelada. */
        CANCELLED
    }
    
    /**
     * Estado de sincronización de la tarea.
     */
    enum class SyncStatus {
        /** Sincronizada con el servidor. */
        SYNCED,
        /** Pendiente de subir al servidor. */
        PENDING_UPLOAD,
        /** Pendiente de actualizar en el servidor. */
        PENDING_UPDATE,
        /** Pendiente de eliminar en el servidor. */
        PENDING_DELETE
    }
    
    /**
     * Verifica si la tarea está completada.
     *
     * @return True si la tarea está completada, False en caso contrario.
     */
    fun isCompleted(): Boolean {
        return status == TaskStatus.COMPLETED && completionId != null
    }
    
    /**
     * Marca la tarea como completada con los datos de completado proporcionados.
     *
     * @param completion Datos de completado.
     * @return Copia de la tarea marcada como completada.
     */
    fun complete(completion: TaskCompletion): Task {
        return this.copy(
            status = TaskStatus.COMPLETED,
            completionId = completion.id,
            syncStatus = SyncStatus.PENDING_UPDATE,
            pendingChanges = true,
            updatedAt = Date()
        )
    }
    
    /**
     * Crea una copia de la tarea con un estado de sincronización específico.
     *
     * @param syncStatus Nuevo estado de sincronización.
     * @return Copia de la tarea con el nuevo estado.
     */
    fun withSyncStatus(syncStatus: SyncStatus): Task {
        return this.copy(syncStatus = syncStatus)
    }
    
    /**
     * Actualiza el estado de la tarea.
     *
     * @param newStatus Nuevo estado de la tarea.
     * @return Copia de la tarea con el estado actualizado.
     */
    fun updateStatus(newStatus: TaskStatus): Task {
        return this.copy(
            status = newStatus,
            syncStatus = SyncStatus.PENDING_UPDATE,
            pendingChanges = true,
            updatedAt = Date()
        )
    }
}