package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

/**
 * Entidad que representa una plantilla de etiqueta en la aplicación.
 */
@Entity(tableName = "label_templates")
data class LabelTemplate(
    @PrimaryKey
    @ColumnInfo(name = "id")
    @SerializedName("id") 
    val id: Int,
    
    @ColumnInfo(name = "name")
    @SerializedName("name") 
    val name: String,
    
    @ColumnInfo(name = "description")
    @SerializedName("description") 
    val description: String?,
    
    @ColumnInfo(name = "html_content")
    @SerializedName("html_content") 
    val htmlContent: String,
    
    @ColumnInfo(name = "css_content")
    @SerializedName("css_content") 
    val cssContent: String?,
    
    @ColumnInfo(name = "preview_url")
    @SerializedName("preview_url") 
    val previewUrl: String?,
    
    @ColumnInfo(name = "width")
    @SerializedName("width") 
    val width: Int,
    
    @ColumnInfo(name = "height")
    @SerializedName("height") 
    val height: Int,
    
    @ColumnInfo(name = "company_id")
    @SerializedName("company_id") 
    val companyId: Int?,
    
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    
    @ColumnInfo(name = "times_used")
    val timesUsed: Int = 0,
    
    @ColumnInfo(name = "last_used")
    val lastUsed: Long = 0,
    
    @ColumnInfo(name = "last_sync")
    val lastSync: Long = System.currentTimeMillis()
) {
    /**
     * Obtiene la descripción del tamaño de la etiqueta.
     */
    fun getSizeDescription(): String {
        return "${width}mm x ${height}mm"
    }
    
    /**
     * Verifica si esta plantilla coincide con el tamaño de papel especificado.
     */
    fun matchesPaperSize(paperWidth: Int, paperHeight: Int): Boolean {
        // Si la plantilla es más pequeña o igual que el papel, puede imprimirse
        return width <= paperWidth && height <= paperHeight
    }
}