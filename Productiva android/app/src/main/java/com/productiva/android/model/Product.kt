package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

/**
 * Modelo de datos que representa un producto en la aplicación.
 * Se almacena en la base de datos local y se sincroniza con el servidor.
 */
@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("sku")
    val sku: String?,
    
    @SerializedName("barcode")
    val barcode: String?,
    
    @SerializedName("price")
    val price: BigDecimal,
    
    @SerializedName("cost")
    val cost: BigDecimal?,
    
    @SerializedName("stock")
    val stock: Int,
    
    @SerializedName("category_id")
    val categoryId: Int?,
    
    @SerializedName("image_url")
    val imageUrl: String?,
    
    @SerializedName("tax_rate")
    val taxRate: Float?,
    
    @SerializedName("location")
    val location: String?,
    
    @SerializedName("weight")
    val weight: Float?,
    
    @SerializedName("dimensions")
    val dimensions: String?,
    
    @SerializedName("last_updated")
    val lastUpdated: String?,
    
    @SerializedName("active")
    val isActive: Boolean,
    
    // Campos locales (no se envían al servidor)
    val needsSync: Boolean = false,
    val lastSyncTimestamp: Long = 0
) {
    /**
     * Obtiene una versión formateada del precio para mostrar en UI.
     */
    fun getFormattedPrice(): String {
        return String.format("%.2f €", price)
    }
    
    /**
     * Determina si el producto tiene stock disponible.
     */
    fun hasStock(): Boolean {
        return stock > 0
    }
    
    /**
     * Obtiene el estado del stock para mostrar en UI.
     */
    fun getStockStatus(): StockStatus {
        return when {
            stock <= 0 -> StockStatus.OUT_OF_STOCK
            stock < 5 -> StockStatus.LOW_STOCK
            else -> StockStatus.IN_STOCK
        }
    }
    
    /**
     * Obtiene el margen de beneficio.
     */
    fun getProfitMargin(): Float {
        if (cost == null || cost.compareTo(BigDecimal.ZERO) == 0) {
            return 0f
        }
        
        val profit = price.subtract(cost)
        return profit.toFloat() / cost.toFloat() * 100
    }
    
    /**
     * Estados posibles del stock.
     */
    enum class StockStatus {
        IN_STOCK,
        LOW_STOCK,
        OUT_OF_STOCK
    }
}