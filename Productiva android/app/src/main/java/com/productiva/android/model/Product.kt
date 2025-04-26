package com.productiva.android.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.productiva.android.database.Converters

/**
 * Modelo para productos.
 * Representa un producto en el sistema con toda su información.
 */
@Entity(
    tableName = "products",
    indices = [
        Index("sku", unique = true),
        Index("barcode")
    ]
)
@TypeConverters(Converters::class)
data class Product(
    @PrimaryKey
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("sku")
    val sku: String,
    
    @SerializedName("barcode")
    val barcode: String? = null,
    
    @SerializedName("price")
    val price: Double? = null,
    
    @SerializedName("cost")
    val cost: Double? = null,
    
    @SerializedName("stock")
    val stock: Int = 0,
    
    @SerializedName("min_stock")
    val minStock: Int = 0,
    
    @SerializedName("category")
    val category: String? = null,
    
    @SerializedName("supplier")
    val supplier: String? = null,
    
    @SerializedName("supplier_id")
    val supplierId: Int? = null,
    
    @SerializedName("location_id")
    val locationId: Int? = null,
    
    @SerializedName("company_id")
    val companyId: Int? = null,
    
    @SerializedName("image_url")
    val imageUrl: String? = null,
    
    @SerializedName("tags")
    val tags: List<String> = emptyList(),
    
    @SerializedName("attributes")
    val attributes: Map<String, String> = emptyMap(),
    
    @SerializedName("is_active")
    val isActive: Boolean = true,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    
    // Campos locales
    var isLocallyModified: Boolean = false,
    var lastSyncTime: Long = 0,
    var localImagePath: String? = null
) {
    /**
     * Verifica si el producto tiene existencias disponibles.
     */
    fun hasStock(): Boolean {
        return stock > 0
    }
    
    /**
     * Verifica si el producto está en nivel bajo de existencias.
     */
    fun isLowStock(): Boolean {
        return stock <= minStock && stock > 0
    }
    
    /**
     * Verifica si el producto está agotado.
     */
    fun isOutOfStock(): Boolean {
        return stock <= 0
    }
    
    /**
     * Obtiene el margen de ganancia en porcentaje.
     */
    fun getMarginPercent(): Double {
        if (cost == null || price == null || cost <= 0) return 0.0
        return ((price - cost) / cost) * 100
    }
    
    /**
     * Obtiene el margen de ganancia en valor absoluto.
     */
    fun getMarginValue(): Double {
        if (cost == null || price == null) return 0.0
        return price - cost
    }
    
    /**
     * Obtiene la URL de la imagen o la ruta local si existe.
     */
    fun getImageSource(): String? {
        return localImagePath ?: imageUrl
    }
    
    /**
     * Verifica si el producto tiene imagen.
     */
    fun hasImage(): Boolean {
        return !imageUrl.isNullOrEmpty() || !localImagePath.isNullOrEmpty()
    }
    
    /**
     * Verifica si el producto necesita ser sincronizado con el servidor.
     */
    fun needsSync(): Boolean {
        return isLocallyModified
    }
}