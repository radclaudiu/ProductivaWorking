package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para impresoras guardadas
 */
@Entity(tableName = "saved_printers")
data class SavedPrinter(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("address")
    val address: String,
    
    @SerializedName("printer_type")
    val printerType: String, // BROTHER_CPCL, BROTHER_ESC_POS, GENERIC_ESC_POS
    
    @SerializedName("width")
    val width: Int? = null, // Ancho de etiqueta en puntos
    
    @SerializedName("height")
    val height: Int? = null, // Alto de etiqueta en puntos
    
    @SerializedName("density")
    val density: Int? = null, // Densidad de impresión (1-15)
    
    @SerializedName("is_default")
    val isDefault: Boolean = false,
    
    @SerializedName("last_used")
    val lastUsed: Long? = null // Timestamp de último uso
)