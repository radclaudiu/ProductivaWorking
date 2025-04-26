package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Entidad que representa una impresora guardada en el sistema.
 * Guarda configuración de impresoras Brother para etiquetas.
 */
@Entity(tableName = "saved_printers")
data class SavedPrinter(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("address")
    val address: String, // MAC o IP según tipo
    
    @SerializedName("model")
    val model: String = "QL-720NW", // Modelo por defecto Brother QL-720NW
    
    @SerializedName("connection_type")
    val connectionType: String = TYPE_BLUETOOTH, // bluetooth, wifi, usb
    
    @SerializedName("paper_width")
    val paperWidth: Int = 62, // Ancho papel en mm (62mm es estándar)
    
    @SerializedName("paper_height")
    val paperHeight: Int = 29, // Alto papel en mm (29mm para etiquetas direcciones)
    
    @SerializedName("orientation")
    val orientation: String = ORIENTATION_LANDSCAPE,
    
    @SerializedName("print_quality")
    val printQuality: String = QUALITY_HIGH,
    
    @SerializedName("is_default")
    val isDefault: Boolean = false,
    
    @SerializedName("last_used")
    val lastUsed: Long = 0
) {
    /**
     * Genera una representación en texto de la impresora
     */
    override fun toString(): String {
        return name
    }
    
    companion object {
        // Tipos de conexión
        const val TYPE_BLUETOOTH = "bluetooth"
        const val TYPE_WIFI = "wifi"
        const val TYPE_USB = "usb"
        
        // Orientación
        const val ORIENTATION_PORTRAIT = "portrait"
        const val ORIENTATION_LANDSCAPE = "landscape"
        
        // Calidad de impresión
        const val QUALITY_HIGH = "high"
        const val QUALITY_NORMAL = "normal"
        const val QUALITY_DRAFT = "draft"
        
        // Modelos comunes Brother
        val BROTHER_MODELS = listOf(
            "QL-720NW",
            "QL-820NWB",
            "QL-1110NWB",
            "PT-P910BT",
            "PT-P950NW"
        )
    }
}