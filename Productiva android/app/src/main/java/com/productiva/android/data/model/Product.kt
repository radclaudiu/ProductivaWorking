package com.productiva.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.productiva.android.data.converter.DateConverter
import java.util.Date

/**
 * Modelo que representa un producto en el sistema.
 *
 * @property id ID único del producto.
 * @property name Nombre del producto.
 * @property description Descripción del producto (opcional).
 * @property sku Código SKU del producto (opcional).
 * @property barcode Código de barras del producto (opcional).
 * @property price Precio del producto.
 * @property stock Cantidad en stock del producto.
 * @property minStock Stock mínimo antes de alertar (opcional).
 * @property imageUrl URL de la imagen del producto (opcional).
 * @property companyId ID de la empresa a la que pertenece el producto.
 * @property categoryId ID de la categoría del producto (opcional).
 * @property isActive Indica si el producto está activo.
 * @property createdAt Fecha de creación del producto.
 * @property updatedAt Fecha de última actualización del producto.
 * @property syncStatus Estado de sincronización del producto.
 * @property pendingChanges Indica si hay cambios pendientes de sincronización.
 */
@Entity(tableName = "products")
@TypeConverters(DateConverter::class)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String? = null,
    val sku: String? = null,
    val barcode: String? = null,
    val price: Double = 0.0,
    val stock: Int = 0,
    val minStock: Int? = null,
    val imageUrl: String? = null,
    val companyId: Int,
    val categoryId: Int? = null,
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD,
    val pendingChanges: Boolean = false
) {
    /**
     * Estado de sincronización del producto.
     */
    enum class SyncStatus {
        /** Sincronizado con el servidor. */
        SYNCED,
        /** Pendiente de subir al servidor. */
        PENDING_UPLOAD,
        /** Pendiente de actualizar en el servidor. */
        PENDING_UPDATE,
        /** Pendiente de eliminar en el servidor. */
        PENDING_DELETE
    }
    
    /**
     * Crea una copia del producto con un estado de sincronización específico.
     *
     * @param syncStatus Nuevo estado de sincronización.
     * @return Copia del producto con el nuevo estado.
     */
    fun withSyncStatus(syncStatus: SyncStatus): Product {
        return this.copy(syncStatus = syncStatus)
    }
    
    /**
     * Marca el producto como eliminado.
     *
     * @return Copia del producto marcado para eliminación.
     */
    fun markAsDeleted(): Product {
        return this.copy(
            syncStatus = SyncStatus.PENDING_DELETE,
            pendingChanges = true,
            updatedAt = Date()
        )
    }
    
    /**
     * Actualiza el stock del producto.
     *
     * @param newStock Nuevo nivel de stock.
     * @return Copia del producto con el stock actualizado.
     */
    fun updateStock(newStock: Int): Product {
        return this.copy(
            stock = newStock,
            syncStatus = SyncStatus.PENDING_UPDATE,
            pendingChanges = true,
            updatedAt = Date()
        )
    }
}