package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Modelo de tarea
 * Representa las tareas asignadas a los empleados
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: Int,
    
    // Información básica
    val title: String,
    val description: String? = null,
    
    // Asignación
    @SerializedName("assigned_to")
    val assignedTo: Int? = null,
    @SerializedName("created_by")
    val createdBy: Int? = null,
    
    // Ubicación y empresa
    @SerializedName("location_id")
    val locationId: Int? = null,
    @SerializedName("company_id")
    val companyId: Int? = null,
    
    // Fechas
    @SerializedName("created_at")
    val createdAt: Date? = null,
    @SerializedName("updated_at")
    val updatedAt: Date? = null,
    @SerializedName("due_date")
    val dueDate: Date? = null,
    
    // Estado
    val status: String,
    val priority: Int = 1,
    
    // Metadatos
    val tags: String? = null,
    val category: String? = null,
    
    // Campos de control
    @SerializedName("last_synced")
    val lastSynced: Date? = null,
    val synced: Boolean = false,
    @SerializedName("is_local_only")
    val isLocalOnly: Boolean = false
)