package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Entidad que representa una plantilla de etiqueta en el sistema.
 */
@Entity(tableName = "label_templates")
data class LabelTemplate(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("html_template")
    val htmlTemplate: String? = null,
    
    @SerializedName("fields")
    val fields: String? = null, // Lista separada por comas de campos en la plantilla
    
    @SerializedName("width")
    val width: Int = 62, // Ancho en mm
    
    @SerializedName("height")
    val height: Int = 29, // Alto en mm
    
    @SerializedName("image_path")
    val imagePath: String? = null, // Vista previa de la etiqueta
    
    @SerializedName("company_id")
    val companyId: Int? = null,
    
    @SerializedName("company_name")
    val companyName: String? = null,
    
    @SerializedName("created_by")
    val createdBy: Int? = null,
    
    @SerializedName("created_date")
    val createdDate: String? = null,
    
    @SerializedName("is_system")
    val isSystem: Boolean = false,
    
    @SerializedName("times_used")
    val timesUsed: Int = 0,
    
    @SerializedName("is_favorite")
    val isFavorite: Boolean = false,
    
    @SerializedName("last_used")
    val lastUsed: Long = 0
) {
    /**
     * Genera una representación en texto de la plantilla
     */
    override fun toString(): String {
        return name
    }
    
    /**
     * Obtiene la lista de campos como lista
     */
    fun getFieldsList(): List<String> {
        return fields?.split(",")?.map { it.trim() } ?: emptyList()
    }
    
    /**
     * Verifica si la plantilla tiene campos definidos
     */
    fun hasFields(): Boolean {
        return !fields.isNullOrBlank()
    }
    
    /**
     * Calcula el aspecto (ratio) de la etiqueta
     */
    fun getAspectRatio(): Float {
        return if (height > 0) width.toFloat() / height.toFloat() else 1f
    }
    
    companion object {
        // Tipos de plantillas predefinidas
        const val TYPE_ADDRESS = "address"
        const val TYPE_PRODUCT = "product"
        const val TYPE_BARCODE = "barcode"
        const val TYPE_CUSTOM = "custom"
    }
}