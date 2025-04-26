package com.productiva.android.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para productos.
 * Representa la estructura de datos de un producto en el sistema.
 * 
 * @property id Identificador único del producto
 * @property companyId ID de la empresa a la que pertenece
 * @property name Nombre del producto
 * @property description Descripción detallada del producto
 * @property sku Código SKU (Stock Keeping Unit)
 * @property barcode Código de barras (EAN/UPC)
 * @property price Precio del producto
 * @property cost Coste del producto
 * @property category Categoría del producto
 * @property imageUrl URL o ruta a la imagen del producto
 * @property active Indica si el producto está activo
 * @property stock Cantidad en stock
 * @property createdAt Fecha de creación
 * @property updatedAt Fecha de última actualización
 * @property syncStatus Estado de sincronización con el servidor
 */
@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = Company::class,
            parentColumns = ["id"],
            childColumns = ["companyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("companyId"),
        Index("sku"),
        Index("barcode")
    ]
)
data class Product(
    @PrimaryKey
    @SerializedName("id")
    val id: Int = 0,  // 0 para productos locales aún no sincronizados
    
    @SerializedName("company_id")
    val companyId: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("sku")
    val sku: String? = null,
    
    @SerializedName("barcode")
    val barcode: String? = null,
    
    @SerializedName("price")
    val price: Double = 0.0,
    
    @SerializedName("cost")
    val cost: Double? = null,
    
    @SerializedName("category")
    val category: String? = null,
    
    @SerializedName("image_url")
    val imageUrl: String? = null,
    
    @SerializedName("active")
    val active: Boolean = true,
    
    @SerializedName("stock")
    val stock: Int? = null,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    
    // Campos locales (no se envían al servidor)
    val syncStatus: String = "SYNCED"  // SYNCED, PENDING_SYNC, SYNC_ERROR
)