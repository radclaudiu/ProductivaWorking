package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa una impresora Bluetooth guardada
 */
@Entity(tableName = "saved_printers")
data class SavedPrinter(
    @PrimaryKey
    val address: String,
    val name: String,
    val isDefault: Boolean = false,
    val printerModel: String = "BROTHER_GENERIC", // Tipo de impresora (por defecto Brother genérica)
    val paperWidth: Int = 62, // Ancho del papel en mm (estándar para Brother: 62mm)
    val paperLength: Int = 100, // Longitud del papel en mm (ajustable)
    val printDensity: Int = 0, // Densidad de impresión (-5 a 5, 0 es normal)
    val printSpeed: Int = 1 // Velocidad de impresión (0-2, donde 1 es normal)
)