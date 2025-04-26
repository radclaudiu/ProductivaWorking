package com.productiva.android.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.productiva.android.data.converter.DateConverter
import com.productiva.android.data.converter.ListConverter
import java.util.Date

/**
 * Entidad que representa un producto en el sistema.
 *
 * @property id Identificador único del producto.
 * @property name Nombre del producto.
 * @property description Descripción detallada del producto (opcional).
 * @property sku Código SKU (Stock Keeping Unit) del producto.
 * @property barcode Código de barras del producto (opcional).
 * @property category Categoría del producto (opcional).
 * @property price Precio del producto.
 * @property cost Costo de producción o adquisición del producto (opcional).
 * @property stock Cantidad en stock del producto.
 * @property minimumStock Stock mínimo antes de reordenar (opcional).
 * @property unit Unidad de medida del producto (unidad, kg, litro, etc.).
 * @property weight Peso del producto (opcional).
 * @property dimensions Dimensiones del producto en formato "LxAxA" (opcional).
 * @property imagePath Ruta a la imagen del producto (opcional).
 * @property tags Lista de etiquetas asociadas al producto (opcional).
 * @property active Indica si el producto está activo y disponible.
 * @property companyId ID de la empresa a la que pertenece el producto.
 * @property createdAt Fecha de creación del producto.
 * @property updatedAt Fecha de última actualización del producto.
 * @property syncStatus Estado de sincronización con el servidor.
 * @property lastSyncTime Marca de tiempo de la última sincronización.
 * @property isDeleted Indica si el producto ha sido marcado para eliminación.
 * @property pendingChanges Indica si hay cambios locales pendientes de sincronizar.
 */
@Entity(
    tableName = "products",
    indices = [
        Index("sku", unique = true),
        Index("barcode"),
        Index("companyId"),
        Index("syncStatus")
    ]
)
@TypeConverters(DateConverter::class, ListConverter::class)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val name: String,
    val description: String? = null,
    val sku: String,
    val barcode: String? = null,
    
    val category: String? = null,
    val price: Double,
    val cost: Double? = null,
    val stock: Int,
    val minimumStock: Int? = null,
    val unit: String, // "unit", "kg", "liter", etc.
    
    val weight: Double? = null,
    val dimensions: String? = null, // Format: "LxWxH"
    val imagePath: String? = null,
    
    val tags: List<String> = emptyList(),
    val active: Boolean = true,
    
    val companyId: Int,
    val createdAt: Date,
    val updatedAt: Date,
    
    // Campos para sincronización
    val syncStatus: String = SyncStatus.SYNCED, // "synced", "pending_upload", "pending_update", "conflict"
    val lastSyncTime: Long = 0,
    val isDeleted: Boolean = false,
    val pendingChanges: Boolean = false
) {
    /**
     * Verifica si el stock está por debajo del mínimo.
     *
     * @return true si el stock está por debajo del mínimo establecido, false en caso contrario.
     */
    fun isLowStock(): Boolean {
        return minimumStock != null && stock <= minimumStock
    }
    
    /**
     * Crea una copia del producto con stock actualizado.
     *
     * @param newStock Nuevo nivel de stock.
     * @return Producto actualizado con nuevo nivel de stock.
     */
    fun updateStock(newStock: Int): Product {
        return copy(
            stock = newStock,
            updatedAt = Date(),
            syncStatus = SyncStatus.PENDING_UPDATE,
            pendingChanges = true
        )
    }
    
    /**
     * Crea una copia del producto con precio actualizado.
     *
     * @param newPrice Nuevo precio.
     * @return Producto actualizado con nuevo precio.
     */
    fun updatePrice(newPrice: Double): Product {
        return copy(
            price = newPrice,
            updatedAt = Date(),
            syncStatus = SyncStatus.PENDING_UPDATE,
            pendingChanges = true
        )
    }
    
    /**
     * Crea una copia del producto marcado para eliminación.
     *
     * @return Producto actualizado marcado para eliminación.
     */
    fun markForDeletion(): Product {
        return copy(
            isDeleted = true,
            syncStatus = SyncStatus.PENDING_DELETE,
            pendingChanges = true,
            updatedAt = Date()
        )
    }
    
    /**
     * Crea una copia del producto con estado de sincronización actualizado.
     *
     * @param newSyncStatus Nuevo estado de sincronización.
     * @return Producto actualizado con nuevo estado de sincronización.
     */
    fun withSyncStatus(newSyncStatus: String, lastSyncTime: Long = System.currentTimeMillis()): Product {
        return copy(
            syncStatus = newSyncStatus,
            lastSyncTime = lastSyncTime,
            pendingChanges = newSyncStatus != SyncStatus.SYNCED
        )
    }
    
    /**
     * Clase auxiliar que define constantes para los estados de sincronización.
     */
    object SyncStatus {
        const val SYNCED = "synced"
        const val PENDING_UPLOAD = "pending_upload"
        const val PENDING_UPDATE = "pending_update"
        const val PENDING_DELETE = "pending_delete"
        const val CONFLICT = "conflict"
    }
}