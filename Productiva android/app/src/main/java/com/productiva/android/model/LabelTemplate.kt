package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos que representa una plantilla de etiqueta en la aplicación.
 * Se almacena en la base de datos local y se sincroniza con el servidor.
 */
@Entity(tableName = "label_templates")
data class LabelTemplate(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("printer_type")
    val printerType: String,  // Tipos: "BROTHER_QL", "ZEBRA", "DYMO", etc.
    
    @SerializedName("template_data")
    val templateData: String,  // Estructura JSON o XML con la definición de la etiqueta
    
    @SerializedName("width")
    val width: Int,  // Ancho en puntos o píxeles
    
    @SerializedName("height")
    val height: Int,  // Alto en puntos o píxeles
    
    @SerializedName("is_default")
    val isDefault: Boolean,
    
    @SerializedName("usage_count")
    val usageCount: Int,
    
    @SerializedName("last_used")
    val lastUsed: String?,
    
    @SerializedName("created_by")
    val createdBy: Int?,
    
    @SerializedName("created_at")
    val createdAt: String?,
    
    @SerializedName("updated_at")
    val updatedAt: String?,
    
    @SerializedName("paper_type")
    val paperType: String?,  // Para impresoras Brother: "W29H90", "W62H100", etc.
    
    @SerializedName("orientation")
    val orientation: String?,  // "PORTRAIT" o "LANDSCAPE"
    
    @SerializedName("dpi")
    val dpi: Int?,  // Resolución de la etiqueta
    
    @SerializedName("fields")
    val fields: List<LabelField>?,  // Campos variables de la etiqueta
    
    // Campos locales (no se envían al servidor)
    val needsSync: Boolean = false,
    val localUsageCount: Int = 0,
    val lastSyncTimestamp: Long = 0
) {
    /**
     * Obtiene el nombre formateado para mostrar en UI.
     */
    fun getDisplayName(): String {
        return if (isDefault) "$name (Predeterminada)" else name
    }
    
    /**
     * Obtiene la descripción para mostrar en UI.
     */
    fun getDisplayDescription(): String {
        return description ?: "Sin descripción"
    }
    
    /**
     * Verifica si la plantilla es compatible con el tipo de impresora especificado.
     */
    fun isCompatibleWith(printerType: String): Boolean {
        return this.printerType.equals(printerType, ignoreCase = true)
    }
    
    /**
     * Clase para representar un campo variable en una plantilla de etiqueta.
     */
    data class LabelField(
        @SerializedName("id")
        val id: String,
        
        @SerializedName("name")
        val name: String,
        
        @SerializedName("type")
        val type: String,  // "TEXT", "BARCODE", "IMAGE", "QR", etc.
        
        @SerializedName("x")
        val x: Int,  // Posición X en la etiqueta
        
        @SerializedName("y")
        val y: Int,  // Posición Y en la etiqueta
        
        @SerializedName("width")
        val width: Int?,
        
        @SerializedName("height")
        val height: Int?,
        
        @SerializedName("font_size")
        val fontSize: Int?,
        
        @SerializedName("font_name")
        val fontName: String?,
        
        @SerializedName("alignment")
        val alignment: String?,  // "LEFT", "CENTER", "RIGHT"
        
        @SerializedName("default_value")
        val defaultValue: String?
    )
}