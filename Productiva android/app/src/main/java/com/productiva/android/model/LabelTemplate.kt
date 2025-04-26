package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Modelo de datos para plantillas de etiquetas
 */
@Entity(tableName = "label_templates")
data class LabelTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("width")
    val width: Int? = null, // Ancho de etiqueta en puntos
    
    @SerializedName("height")
    val height: Int? = null, // Alto de etiqueta en puntos
    
    @SerializedName("created_at")
    val createdAt: Date? = null,
    
    @SerializedName("updated_at")
    val updatedAt: Date? = null,
    
    @SerializedName("user_id")
    val userId: Int? = null,
    
    @SerializedName("company_id")
    val companyId: Int? = null,
    
    // Campos para sincronización local
    val lastSyncedAt: Date? = null,
    val synced: Boolean = false,
    val pendingUpload: Boolean = false
)