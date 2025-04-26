package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa una plantilla de etiqueta para impresión
 */
@Entity(tableName = "label_templates")
data class LabelTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val isDefault: Boolean = false,
    
    // Tamaño de la etiqueta
    val widthMm: Int = 62, // Ancho del papel en mm
    val heightMm: Int = 100, // Alto del papel en mm
    
    // Contenido y formato
    val showTitle: Boolean = true,
    val showDate: Boolean = true,
    val showExtraText: Boolean = true,
    val showQrCode: Boolean = false,
    val showBarcode: Boolean = false,
    
    // Tamaños de fuente (1-5, donde 3 es normal)
    val titleFontSize: Int = 4,
    val dateFontSize: Int = 2,
    val extraTextFontSize: Int = 3,
    
    // Márgenes en mm
    val marginTop: Int = 3,
    val marginLeft: Int = 3,
    val marginRight: Int = 3,
    val marginBottom: Int = 3,
    
    // Formato de fecha (por defecto: dd/MM/yyyy HH:mm)
    val dateFormat: String = "dd/MM/yyyy HH:mm"
)