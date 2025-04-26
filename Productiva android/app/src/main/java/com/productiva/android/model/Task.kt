package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.productiva.android.database.Converters
import java.util.Date

/**
 * Modelo de datos para una tarea.
 * Representa las tareas asignadas a los empleados.
 */
@Entity(tableName = "tasks")
@TypeConverters(Converters::class)
data class Task(
    @PrimaryKey
    val id: Int,
    
    // Información básica de la tarea
    val title: String,
    val description: String,
    val status: String, // "pending", "in_progress", "completed", "cancelled"
    
    // Datos de asignación
    val assignedToUserId: Int,
    val assignedToUserName: String,
    val assignedByUserId: Int,
    val assignedByUserName: String,
    
    // Prioridad y categoría
    val priority: Int, // 1 (baja) a 5 (alta)
    val category: String,
    
    // Plazos y fechas
    val createdAt: Date,
    val updatedAt: Date,
    val dueDate: Date?,
    val completedAt: Date?,
    
    // Datos de empresa y ubicación
    val companyId: Int,
    val locationId: Int?,
    val locationName: String?,
    
    // Archivos adjuntos y referencias
    val attachments: List<String>?, // URLs o rutas a archivos adjuntos
    val relatedTaskIds: List<Int>?, // IDs de tareas relacionadas
    
    // Metadatos adicionales
    val requiresPhoto: Boolean,
    val requiresSignature: Boolean,
    val requiresConfirmation: Boolean,
    val isRecurring: Boolean,
    val recurringSchedule: String?, // Expresión cron o similar para tareas recurrentes
    
    // Datos para sincronización
    val lastSyncTime: Long, // Timestamp de última sincronización
    val isLocalOnly: Boolean = false // Indica si fue creado solo localmente
)