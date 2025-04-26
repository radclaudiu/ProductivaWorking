package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para tareas
 * Representa una tarea del sistema Productiva
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("company_id")
    val companyId: Int,
    
    @SerializedName("location_id")
    val locationId: Int,
    
    @SerializedName("frequency")
    val frequency: String,
    
    @SerializedName("needs_photo")
    val needsPhoto: Boolean = false,
    
    @SerializedName("needs_signature")
    val needsSignature: Boolean = false,
    
    @SerializedName("print_label")
    val printLabel: Boolean = false,
    
    @SerializedName("is_active")
    val isActive: Boolean = true,
    
    @SerializedName("created_date")
    val createdDate: String? = null,
    
    @SerializedName("priority")
    val priority: Int = 0,
    
    @SerializedName("label_template_id")
    val labelTemplateId: Int? = null,
    
    @SerializedName("last_sync")
    var lastSync: Long = System.currentTimeMillis()
)