package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * Modelo de datos para impresoras guardadas
 */
@Entity(tableName = "saved_printers")
data class SavedPrinter(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "address")
    val address: String,
    
    @ColumnInfo(name = "printerType")
    val printerType: String,
    
    @ColumnInfo(name = "isDefault")
    val isDefault: Boolean = false,
    
    @ColumnInfo(name = "lastUsed")
    val lastUsed: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "userId")
    val userId: Int? = null,
    
    @ColumnInfo(name = "paperWidth")
    val paperWidth: Int = 62,
    
    @ColumnInfo(name = "paperHeight")
    val paperHeight: Int = 29,
    
    @ColumnInfo(name = "orientation")
    val orientation: String = "landscape",
    
    @ColumnInfo(name = "dpi")
    val dpi: Int = 300,
    
    @ColumnInfo(name = "customSettings")
    val customSettings: String? = null
) {
    companion object {
        const val TYPE_BROTHER = "brother"
        const val TYPE_ZEBRA = "zebra"
        const val TYPE_GENERIC = "generic"
        
        val PRINTER_TYPES = listOf(
            TYPE_BROTHER,
            TYPE_ZEBRA,
            TYPE_GENERIC
        )
        
        const val ORIENTATION_PORTRAIT = "portrait"
        const val ORIENTATION_LANDSCAPE = "landscape"
        
        val ORIENTATIONS = listOf(
            ORIENTATION_PORTRAIT,
            ORIENTATION_LANDSCAPE
        )
    }
}