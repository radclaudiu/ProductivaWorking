package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.text.NumberFormat
import java.util.Locale

/**
 * Modelo de datos para productos.
 * Incluye todos los campos necesarios para la sincronización con el portal web.
 */
@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("code")
    val code: String? = null,
    
    @SerializedName("barcode")
    val barcode: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("price")
    val price: Double = 0.0,
    
    @SerializedName("cost")
    val cost: Double? = null,
    
    @SerializedName("stock")
    val stock: Int? = null,
    
    @SerializedName("stock_min")
    val stockMin: Int? = null,
    
    @SerializedName("category")
    val category: String? = null,
    
    @SerializedName("supplier")
    val supplier: String? = null,
    
    @SerializedName("company_id")
    val companyId: Int,
    
    @SerializedName("location_id")
    val locationId: Int? = null,
    
    @SerializedName("image_url")
    val imageUrl: String? = null,
    
    @SerializedName("is_active")
    val isActive: Boolean = true,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    
    // Campos locales (no se envían al servidor)
    var localImagePath: String? = null,
    var needsSync: Boolean = false,
    var lastSyncTime: Long = 0
) {
    /**
     * Formatea el precio con el formato de moneda local.
     */
    fun formattedPrice(): String {
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        return format.format(price)
    }
    
    /**
     * Formatea el costo con el formato de moneda local.
     */
    fun formattedCost(): String {
        if (cost == null) return "N/A"
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        return format.format(cost)
    }
    
    /**
     * Calcula el margen de beneficio como porcentaje.
     */
    fun calculateMargin(): Double? {
        if (cost == null || cost <= 0 || price <= 0) return null
        return ((price - cost) / price) * 100.0
    }
    
    /**
     * Formatea el margen como porcentaje.
     */
    fun formattedMargin(): String {
        val margin = calculateMargin()
        return if (margin != null) {
            String.format("%.2f%%", margin)
        } else {
            "N/A"
        }
    }
    
    /**
     * Determina si el producto tiene stock bajo.
     */
    fun hasLowStock(): Boolean {
        if (stock == null || stockMin == null) return false
        return stock <= stockMin
    }
    
    /**
     * Actualiza el producto con datos del servidor preservando cambios locales.
     */
    fun updateFromServer(serverProduct: Product): Product {
        return this.copy(
            name = serverProduct.name,
            code = serverProduct.code,
            barcode = serverProduct.barcode,
            description = serverProduct.description,
            price = serverProduct.price,
            cost = serverProduct.cost,
            stock = serverProduct.stock,
            stockMin = serverProduct.stockMin,
            category = serverProduct.category,
            supplier = serverProduct.supplier,
            locationId = serverProduct.locationId,
            imageUrl = serverProduct.imageUrl,
            isActive = serverProduct.isActive,
            createdAt = serverProduct.createdAt,
            updatedAt = serverProduct.updatedAt,
            // Preservar datos locales
            localImagePath = this.localImagePath,
            needsSync = this.needsSync,
            lastSyncTime = System.currentTimeMillis()
        )
    }
    
    /**
     * Marca el producto como pendiente de sincronización.
     */
    fun markForSync(): Product {
        return this.copy(needsSync = true)
    }
}