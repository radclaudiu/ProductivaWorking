package com.productiva.android.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Clase de entidad para representar una plantilla de etiqueta en la base de datos
 */
@Entity(
    tableName = "label_templates",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("user_id"),
        Index("company_id")
    ]
)
data class LabelTemplate(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @SerializedName("id")
    val id: Int = 0,
    
    @ColumnInfo(name = "name")
    @SerializedName("name")
    val name: String,
    
    @ColumnInfo(name = "description")
    @SerializedName("description")
    val description: String? = null,
    
    @ColumnInfo(name = "template_data")
    @SerializedName("template_data")
    val templateData: String, // JSON con la configuración de la plantilla
    
    @ColumnInfo(name = "user_id")
    @SerializedName("user_id")
    val userId: Int? = null,
    
    @ColumnInfo(name = "company_id")
    @SerializedName("company_id")
    val companyId: Int? = null,
    
    @ColumnInfo(name = "width")
    @SerializedName("width")
    val width: Int = 62, // ancho en mm
    
    @ColumnInfo(name = "height")
    @SerializedName("height")
    val height: Int = 29, // alto en mm
    
    @ColumnInfo(name = "orientation")
    @SerializedName("orientation")
    val orientation: String = "landscape", // portrait o landscape
    
    @ColumnInfo(name = "is_default")
    @SerializedName("is_default")
    val isDefault: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,
    
    @ColumnInfo(name = "last_sync")
    val lastSync: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "preview_image_path")
    @SerializedName("preview_image_path")
    val previewImagePath: String? = null
)