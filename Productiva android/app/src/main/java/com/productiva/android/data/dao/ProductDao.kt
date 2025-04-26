package com.productiva.android.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.data.model.Product

/**
 * DAO (Data Access Object) para las operaciones de base de datos relacionadas con productos.
 */
@Dao
interface ProductDao {
    
    /**
     * Inserta un producto en la base de datos.
     * Si ya existe un registro con el mismo ID, lo reemplaza.
     *
     * @param product Producto a insertar.
     * @return ID generado para el producto insertado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product): Long
    
    /**
     * Inserta varios productos en la base de datos.
     * Si ya existen registros con los mismos IDs, los reemplaza.
     *
     * @param products Lista de productos a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)
    
    /**
     * Actualiza un producto existente en la base de datos.
     *
     * @param product Producto a actualizar.
     */
    @Update
    suspend fun update(product: Product)
    
    /**
     * Obtiene todos los productos de la base de datos.
     *
     * @return Lista de todos los productos.
     */
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name")
    suspend fun getAllProducts(): List<Product>
    
    /**
     * Obtiene un producto por su ID.
     *
     * @param productId ID del producto.
     * @return Producto con el ID especificado o null si no existe.
     */
    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: Int): Product?
    
    /**
     * Busca productos por nombre, descripción o código de barras.
     *
     * @param query Texto a buscar.
     * @return Lista de productos que coinciden con la búsqueda.
     */
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' ORDER BY name")
    suspend fun searchProducts(query: String): List<Product>
    
    /**
     * Actualiza el stock de un producto.
     *
     * @param productId ID del producto.
     * @param newStock Nuevo valor de stock.
     */
    @Query("UPDATE products SET stock = :newStock, syncStatus = 'PENDING_UPDATE', pendingChanges = 1, updatedAt = CURRENT_TIMESTAMP WHERE id = :productId")
    suspend fun updateStock(productId: Int, newStock: Int)
    
    /**
     * Marca un producto como eliminado (para sincronización posterior).
     * No elimina físicamente el producto, solo actualiza su estado.
     *
     * @param productId ID del producto a marcar.
     */
    @Query("UPDATE products SET syncStatus = 'PENDING_DELETE', pendingChanges = 1, updatedAt = CURRENT_TIMESTAMP WHERE id = :productId")
    suspend fun markAsDeleted(productId: Int)
    
    /**
     * Obtiene todos los productos con cambios pendientes de sincronización.
     *
     * @return Lista de productos pendientes de sincronizar.
     */
    @Query("SELECT * FROM products WHERE syncStatus != 'SYNCED' OR pendingChanges = 1")
    suspend fun getPendingSyncProducts(): List<Product>
    
    /**
     * Obtiene la cantidad de productos pendientes de sincronización.
     *
     * @return Número de productos pendientes de sincronizar.
     */
    @Query("SELECT COUNT(*) FROM products WHERE syncStatus != 'SYNCED' OR pendingChanges = 1")
    suspend fun getPendingSyncProductsCount(): Int
    
    /**
     * Marca varios productos como sincronizados.
     *
     * @param productIds Lista de IDs de productos a marcar.
     */
    @Query("UPDATE products SET syncStatus = 'SYNCED', pendingChanges = 0 WHERE id IN (:productIds)")
    suspend fun markAsSynced(productIds: List<Int>)
    
    /**
     * Actualiza el estado de sincronización de un producto.
     *
     * @param productId ID del producto.
     * @param syncStatus Nuevo estado de sincronización.
     */
    @Query("UPDATE products SET syncStatus = :syncStatus, pendingChanges = 0 WHERE id = :productId")
    suspend fun updateSyncStatus(productId: Int, syncStatus: Product.SyncStatus)
    
    /**
     * Elimina físicamente todos los productos marcados para eliminación y ya sincronizados.
     */
    @Query("DELETE FROM products WHERE syncStatus = 'PENDING_DELETE' OR (syncStatus = 'SYNCED' AND pendingChanges = 1)")
    suspend fun deleteMarkedProducts()
}