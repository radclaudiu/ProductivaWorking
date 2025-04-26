package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.text.NumberFormat
import java.util.Locale

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
    
    @SerializedName("code")
    val code: String?,
    
    @SerializedName("barcode")
    val barcode: String?,
    
    @SerializedName("sku")
    val sku: String?,
    
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("cost")
    val cost: Double?,
    
    @SerializedName("stock")
    val stock: Int?,
    
    @SerializedName("reorder_level")
    val reorderLevel: Int?,
    
    @SerializedName("category")
    val category: String?,
    
    @SerializedName("category_id")
    val categoryId: Int?,
    
    @SerializedName("brand")
    val brand: String?,
    
    @SerializedName("brand_id")
    val brandId: Int?,
    
    @SerializedName("supplier")
    val supplier: String?,
    
    @SerializedName("supplier_id")
    val supplierId: Int?,
    
    @SerializedName("tax_rate")
    val taxRate: Double?,
    
    @SerializedName("weight")
    val weight: Double?,
    
    @SerializedName("dimensions")
    val dimensions: String?,
    
    @SerializedName("image_url")
    val imageUrl: String?,
    
    @SerializedName("is_active")
    val isActive: Boolean,
    
    @SerializedName("created_at")
    val createdAt: String?,
    
    @SerializedName("updated_at")
    val updatedAt: String?,
    
    @SerializedName("company_id")
    val companyId: Int?,
    
    @SerializedName("location_id")
    val locationId: Int?,
    
    // Campos locales
    val needsSync: Boolean = false,
    val localImagePath: String? = null
) {
    /**
     * Devuelve el precio formateado con símbolo de moneda.
     */
    fun formattedPrice(): String {
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        return format.format(price)
    }
    
    /**
     * Devuelve el costo formateado con símbolo de moneda.
     */
    fun formattedCost(): String {
        if (cost == null) return ""
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        return format.format(cost)
    }
    
    /**
     * Determina si el producto tiene stock bajo.
     */
    fun hasLowStock(): Boolean {
        if (stock == null || reorderLevel == null) return false
        return stock <= reorderLevel
    }
    
    /**
     * Calcula el margen de beneficio en porcentaje.
     */
    fun profitMargin(): Double? {
        if (cost == null || cost <= 0) return null
        return ((price - cost) / cost) * 100
    }
    
    /**
     * Formatea el margen de beneficio como texto.
     */
    fun formattedProfitMargin(): String {
        val margin = profitMargin() ?: return ""
        return String.format("%.2f%%", margin)
    }
    
    /**
     * Actualiza este producto con información del servidor.
     */
    fun updateFromServer(serverProduct: Product): Product {
        return this.copy(
            name = serverProduct.name,
            description = serverProduct.description,
            code = serverProduct.code,
            barcode = serverProduct.barcode,
            sku = serverProduct.sku,
            price = serverProduct.price,
            cost = serverProduct.cost,
            stock = serverProduct.stock,
            reorderLevel = serverProduct.reorderLevel,
            category = serverProduct.category,
            categoryId = serverProduct.categoryId,
            brand = serverProduct.brand,
            brandId = serverProduct.brandId,
            supplier = serverProduct.supplier,
            supplierId = serverProduct.supplierId,
            taxRate = serverProduct.taxRate,
            weight = serverProduct.weight,
            dimensions = serverProduct.dimensions,
            imageUrl = serverProduct.imageUrl,
            isActive = serverProduct.isActive,
            updatedAt = serverProduct.updatedAt,
            needsSync = false
        )
    }
    
    /**
     * Marca este producto para sincronización.
     */
    fun markForSync(): Product {
        return this.copy(needsSync = true)
    }
    
    companion object {
        /**
         * Crea un producto vacío para usar como comodín.
         */
        fun createEmpty(): Product {
            return Product(
                id = 0,
                name = "",
                description = null,
                code = null,
                barcode = null,
                sku = null,
                price = 0.0,
                cost = null,
                stock = null,
                reorderLevel = null,
                category = null,
                categoryId = null,
                brand = null,
                brandId = null,
                supplier = null,
                supplierId = null,
                taxRate = null,
                weight = null,
                dimensions = null,
                imageUrl = null,
                isActive = true,
                createdAt = null,
                updatedAt = null,
                companyId = null,
                locationId = null,
                needsSync = false,
                localImagePath = null
            )
        }
    }
}