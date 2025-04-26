package com.productiva.android.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Clase de entidad para representar una impresora guardada en la base de datos
 */
@Entity(
    tableName = "saved_printers",
    indices = [
        Index("address", unique = true)
    ]
)
data class SavedPrinter(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @SerializedName("id")
    val id: Int = 0,
    
    @ColumnInfo(name = "name")
    @SerializedName("name")
    val name: String,
    
    @ColumnInfo(name = "address")
    @SerializedName("address")
    val address: String,
    
    @ColumnInfo(name = "model")
    @SerializedName("model")
    val model: String? = null,
    
    @ColumnInfo(name = "printer_type")
    @SerializedName("printer_type")
    val printerType: String = "bluetooth", // bluetooth, wifi, usb
    
    @ColumnInfo(name = "is_default")
    @SerializedName("is_default")
    val isDefault: Boolean = false,
    
    @ColumnInfo(name = "last_used")
    @SerializedName("last_used")
    val lastUsed: Long = 0,
    
    @ColumnInfo(name = "paper_width")
    @SerializedName("paper_width")
    val paperWidth: Int = 62, // ancho en mm (62mm por defecto)
    
    @ColumnInfo(name = "paper_height")
    @SerializedName("paper_height")
    val paperHeight: Int = 29, // alto en mm (29mm por defecto)
    
    @ColumnInfo(name = "connection_params")
    @SerializedName("connection_params")
    val connectionParams: String? = null, // JSON con parámetros adicionales
    
    @ColumnInfo(name = "company_id")
    @SerializedName("company_id")
    val companyId: Int? = null,
    
    @ColumnInfo(name = "location_id")
    @SerializedName("location_id")
    val locationId: Int? = null
)