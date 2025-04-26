package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * Entidad que representa una impresora guardada en la aplicación.
 */
@Entity(tableName = "saved_printers")
data class SavedPrinter(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "model")
    val model: String,
    
    @ColumnInfo(name = "address")
    val address: String,
    
    @ColumnInfo(name = "connection_type")
    val connectionType: String,
    
    @ColumnInfo(name = "paper_width")
    val paperWidth: Int = 62,  // Ancho en mm
    
    @ColumnInfo(name = "paper_height")
    val paperHeight: Int = 29, // Alto en mm
    
    @ColumnInfo(name = "orientation")
    val orientation: Int = ORIENTATION_PORTRAIT,
    
    @ColumnInfo(name = "print_quality")
    val printQuality: Int = QUALITY_NORMAL,
    
    @ColumnInfo(name = "is_default")
    val isDefault: Boolean = false,
    
    @ColumnInfo(name = "last_used")
    val lastUsed: Long = 0
) {
    companion object {
        // Tipos de conexión
        const val TYPE_WIFI = "wifi"
        const val TYPE_BLUETOOTH = "bluetooth"
        const val TYPE_USB = "usb"
        
        // Orientaciones
        const val ORIENTATION_PORTRAIT = 0
        const val ORIENTATION_LANDSCAPE = 1
        
        // Calidades de impresión
        const val QUALITY_NORMAL = 0
        const val QUALITY_HIGH = 1
        
        // Tamaños de papel comunes para Brother
        val PAPER_SIZE_62_29 = PaperSize(62, 29, "Address Label 62x29mm")
        val PAPER_SIZE_29_90 = PaperSize(29, 90, "Shipping Label 29x90mm")
        val PAPER_SIZE_17_54 = PaperSize(17, 54, "Name Badge 17x54mm")
        val PAPER_SIZE_62_100 = PaperSize(62, 100, "Shipping Label 62x100mm")
        
        // Lista de tamaños disponibles
        val AVAILABLE_SIZES = listOf(
            PAPER_SIZE_62_29,
            PAPER_SIZE_29_90,
            PAPER_SIZE_17_54,
            PAPER_SIZE_62_100
        )
    }
    
    /**
     * Clase para representar un tamaño de papel.
     */
    data class PaperSize(
        val width: Int,
        val height: Int,
        val description: String
    )
    
    /**
     * Obtiene la descripción del tipo de conexión.
     */
    fun getConnectionTypeString(): String {
        return when (connectionType) {
            TYPE_WIFI -> "WiFi"
            TYPE_BLUETOOTH -> "Bluetooth"
            TYPE_USB -> "USB"
            else -> "Desconocido"
        }
    }
    
    /**
     * Obtiene la descripción de la orientación.
     */
    fun getOrientationString(): String {
        return when (orientation) {
            ORIENTATION_PORTRAIT -> "Vertical"
            ORIENTATION_LANDSCAPE -> "Horizontal"
            else -> "Desconocido"
        }
    }
}