package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.productiva.android.database.Converters
import java.util.Date

/**
 * Modelo de datos para un producto.
 * Representa los productos del inventario que pueden imprimirse como etiquetas.
 */
@Entity(tableName = "products")
@TypeConverters(Converters::class)
data class Product(
    @PrimaryKey
    val id: Int,
    
    // Información básica del producto
    val name: String,
    val description: String?,
    val sku: String?,
    val barcode: String?,
    
    // Datos de categorización
    val category: String?,
    val subcategory: String?,
    val brand: String?,
    val supplier: String?,
    
    // Datos de inventario
    val stock: Int,
    val minStock: Int?,
    val maxStock: Int?,
    val location: String?,
    
    // Datos económicos
    val price: Double?,
    val cost: Double?,
    val tax: Double?,
    
    // Datos de dimensiones y medidas
    val weight: Double?,
    val weightUnit: String?,
    val dimensions: String?, // Formato: "ancho x alto x profundidad"
    val dimensionUnit: String?,
    
    // Datos adicionales
    val imageUrl: String?,
    val isActive: Boolean,
    val notes: String?,
    val attributes: Map<String, String>?, // Atributos adicionales como clave-valor
    
    // Fechas importantes
    val createdAt: Date,
    val updatedAt: Date,
    val expiryDate: Date?,
    
    // Datos de empresa
    val companyId: Int,
    
    // Datos para sincronización
    val lastSyncTime: Long,
    val isLocallyModified: Boolean = false // Indica si fue modificado localmente y requiere sincronización
)