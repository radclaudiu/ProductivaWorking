package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

/**
 * Entidad que representa una impresora guardada en la aplicación.
 */
@Entity(tableName = "saved_printers")
data class SavedPrinter(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    
    @ColumnInfo(name = "name")
    @SerializedName("name") 
    val name: String,
    
    @ColumnInfo(name = "model")
    @SerializedName("model") 
    val model: String,
    
    @ColumnInfo(name = "mac_address")
    @SerializedName("mac_address") 
    val macAddress: String,
    
    @ColumnInfo(name = "ip_address")
    @SerializedName("ip_address") 
    val ipAddress: String? = null,
    
    @ColumnInfo(name = "connection_type")
    @SerializedName("connection_type") 
    val connectionType: String, // "bluetooth", "wifi", "usb"
    
    @ColumnInfo(name = "is_default")
    @SerializedName("is_default") 
    val isDefault: Boolean = false,
    
    @ColumnInfo(name = "paper_width")
    @SerializedName("paper_width") 
    val paperWidth: Int, // Ancho del papel en mm
    
    @ColumnInfo(name = "paper_height")
    @SerializedName("paper_height") 
    val paperHeight: Int, // Alto del papel en mm
    
    @ColumnInfo(name = "dpi")
    @SerializedName("dpi") 
    val dpi: Int = 203, // Resolución de la impresora en DPI
    
    @ColumnInfo(name = "last_used")
    val lastUsed: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "use_count")
    val useCount: Int = 0,
    
    @ColumnInfo(name = "company_id")
    @SerializedName("company_id") 
    val companyId: Int? = null,
    
    @ColumnInfo(name = "location_id")
    @SerializedName("location_id") 
    val locationId: Int? = null,
    
    @ColumnInfo(name = "print_settings")
    @SerializedName("print_settings") 
    val printSettings: String? = null // JSON de configuraciones adicionales
) {
    /**
     * Obtiene la descripción del tamaño del papel.
     */
    fun getPaperSizeDescription(): String {
        return "${paperWidth}mm x ${paperHeight}mm"
    }
    
    /**
     * Obtiene un icono basado en el tipo de conexión.
     */
    fun getConnectionIcon(): Int {
        return when (connectionType.lowercase()) {
            "bluetooth" -> android.R.drawable.stat_sys_data_bluetooth
            "wifi" -> android.R.drawable.stat_sys_data_wifi
            "usb" -> android.R.drawable.stat_sys_data_usb
            else -> android.R.drawable.stat_sys_download
        }
    }
    
    /**
     * Obtiene la descripción localizada del tipo de conexión.
     */
    fun getConnectionTypeName(): String {
        return when (connectionType.lowercase()) {
            "bluetooth" -> "Bluetooth"
            "wifi" -> "Wi-Fi"
            "usb" -> "USB"
            else -> connectionType
        }
    }
    
    /**
     * Comprueba si esta impresora puede imprimir una plantilla del tamaño especificado.
     */
    fun canPrintSize(width: Int, height: Int): Boolean {
        // Comprueba si el tamaño de la impresora es suficiente para la plantilla
        return width <= paperWidth && height <= paperHeight
    }
}