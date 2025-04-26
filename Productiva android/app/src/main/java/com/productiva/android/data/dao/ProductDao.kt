package com.productiva.android.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.data.model.Product
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * DAO (Data Access Object) para operaciones relacionadas con productos en la base de datos Room.
 */
@Dao
interface ProductDao {
    
    /**
     * Inserta un producto en la base de datos.
     *
     * @param product Producto a insertar.
     * @return ID del producto insertado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product): Long
    
    /**
     * Inserta varios productos en la base de datos.
     *
     * @param products Lista de productos a insertar.
     * @return Lista de IDs de los productos insertados.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>): List<Long>
    
    /**
     * Actualiza un producto existente en la base de datos.
     *
     * @param product Producto a actualizar.
     * @return Número de filas actualizadas.
     */
    @Update
    suspend fun update(product: Product): Int
    
    /**
     * Actualiza varios productos existentes en la base de datos.
     *
     * @param products Lista de productos a actualizar.
     * @return Número de filas actualizadas.
     */
    @Update
    suspend fun updateAll(products: List<Product>): Int
    
    /**
     * Elimina un producto de la base de datos.
     *
     * @param product Producto a eliminar.
     * @return Número de filas eliminadas.
     */
    @Delete
    suspend fun delete(product: Product): Int
    
    /**
     * Obtiene todos los productos como flujo observable.
     *
     * @return Flujo de lista de todos los productos.
     */
    @Query("SELECT * FROM products WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllProductsFlow(): Flow<List<Product>>
    
    /**
     * Obtiene todos los productos.
     *
     * @return Lista de todos los productos.
     */
    @Query("SELECT * FROM products WHERE isDeleted = 0 ORDER BY name ASC")
    suspend fun getAllProducts(): List<Product>
    
    /**
     * Obtiene un producto por su ID.
     *
     * @param productId ID del producto.
     * @return Producto correspondiente al ID o null si no existe.
     */
    @Query("SELECT * FROM products WHERE id = :productId AND isDeleted = 0")
    suspend fun getProductById(productId: Int): Product?
    
    /**
     * Obtiene un producto por su SKU.
     *
     * @param sku SKU del producto.
     * @return Producto correspondiente al SKU o null si no existe.
     */
    @Query("SELECT * FROM products WHERE sku = :sku AND isDeleted = 0")
    suspend fun getProductBySku(sku: String): Product?
    
    /**
     * Obtiene un producto por su código de barras.
     *
     * @param barcode Código de barras del producto.
     * @return Producto correspondiente al código de barras o null si no existe.
     */
    @Query("SELECT * FROM products WHERE barcode = :barcode AND isDeleted = 0")
    suspend fun getProductByBarcode(barcode: String): Product?
    
    /**
     * Busca productos por nombre o descripción.
     *
     * @param query Texto a buscar.
     * @return Lista de productos que coinciden con la búsqueda.
     */
    @Query("SELECT * FROM products WHERE (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') AND isDeleted = 0 ORDER BY name ASC")
    suspend fun searchProducts(query: String): List<Product>
    
    /**
     * Busca productos por nombre o descripción como flujo observable.
     *
     * @param query Texto a buscar.
     * @return Flujo de lista de productos que coinciden con la búsqueda.
     */
    @Query("SELECT * FROM products WHERE (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') AND isDeleted = 0 ORDER BY name ASC")
    fun searchProductsFlow(query: String): Flow<List<Product>>
    
    /**
     * Obtiene todos los productos de una categoría.
     *
     * @param category Categoría de los productos.
     * @return Lista de productos de la categoría.
     */
    @Query("SELECT * FROM products WHERE category = :category AND isDeleted = 0 ORDER BY name ASC")
    suspend fun getProductsByCategory(category: String): List<Product>
    
    /**
     * Obtiene todos los productos de una categoría como flujo observable.
     *
     * @param category Categoría de los productos.
     * @return Flujo de lista de productos de la categoría.
     */
    @Query("SELECT * FROM products WHERE category = :category AND isDeleted = 0 ORDER BY name ASC")
    fun getProductsByCategoryFlow(category: String): Flow<List<Product>>
    
    /**
     * Obtiene todos los productos de una empresa.
     *
     * @param companyId ID de la empresa.
     * @return Lista de productos de la empresa.
     */
    @Query("SELECT * FROM products WHERE companyId = :companyId AND isDeleted = 0 ORDER BY name ASC")
    suspend fun getProductsByCompany(companyId: Int): List<Product>
    
    /**
     * Obtiene todos los productos de una empresa como flujo observable.
     *
     * @param companyId ID de la empresa.
     * @return Flujo de lista de productos de la empresa.
     */
    @Query("SELECT * FROM products WHERE companyId = :companyId AND isDeleted = 0 ORDER BY name ASC")
    fun getProductsByCompanyFlow(companyId: Int): Flow<List<Product>>
    
    /**
     * Obtiene todos los productos con stock bajo.
     *
     * @return Lista de productos con stock bajo.
     */
    @Query("SELECT * FROM products WHERE stock <= minimumStock AND minimumStock > 0 AND isDeleted = 0 ORDER BY stock ASC")
    suspend fun getLowStockProducts(): List<Product>
    
    /**
     * Obtiene todos los productos con stock bajo como flujo observable.
     *
     * @return Flujo de lista de productos con stock bajo.
     */
    @Query("SELECT * FROM products WHERE stock <= minimumStock AND minimumStock > 0 AND isDeleted = 0 ORDER BY stock ASC")
    fun getLowStockProductsFlow(): Flow<List<Product>>
    
    /**
     * Obtiene todos los productos activos.
     *
     * @return Lista de productos activos.
     */
    @Query("SELECT * FROM products WHERE active = 1 AND isDeleted = 0 ORDER BY name ASC")
    suspend fun getActiveProducts(): List<Product>
    
    /**
     * Obtiene todos los productos activos como flujo observable.
     *
     * @return Flujo de lista de productos activos.
     */
    @Query("SELECT * FROM products WHERE active = 1 AND isDeleted = 0 ORDER BY name ASC")
    fun getActiveProductsFlow(): Flow<List<Product>>
    
    /**
     * Obtiene todas las categorías distintas de productos.
     *
     * @return Lista de categorías distintas.
     */
    @Query("SELECT DISTINCT category FROM products WHERE category IS NOT NULL AND category != '' AND isDeleted = 0 ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>
    
    /**
     * Obtiene todos los productos pendientes de sincronizar con el servidor.
     *
     * @return Lista de productos pendientes de sincronizar.
     */
    @Query("SELECT * FROM products WHERE syncStatus != 'synced' ORDER BY updatedAt DESC")
    suspend fun getPendingSyncProducts(): List<Product>
    
    /**
     * Obtiene el número de productos pendientes de sincronizar con el servidor.
     *
     * @return Número de productos pendientes de sincronizar.
     */
    @Query("SELECT COUNT(*) FROM products WHERE syncStatus != 'synced'")
    suspend fun getPendingSyncProductsCount(): Int
    
    /**
     * Obtiene todos los productos pendientes de sincronizar con el servidor como flujo observable.
     *
     * @return Flujo de lista de productos pendientes de sincronizar.
     */
    @Query("SELECT * FROM products WHERE syncStatus != 'synced' ORDER BY updatedAt DESC")
    fun getPendingSyncProductsFlow(): Flow<List<Product>>
    
    /**
     * Actualiza el estado de sincronización de un producto.
     *
     * @param productId ID del producto.
     * @param syncStatus Nuevo estado de sincronización.
     * @param lastSyncTime Marca de tiempo de la última sincronización.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE products SET syncStatus = :syncStatus, lastSyncTime = :lastSyncTime, pendingChanges = :syncStatus != 'synced' WHERE id = :productId")
    suspend fun updateSyncStatus(productId: Int, syncStatus: String, lastSyncTime: Long = System.currentTimeMillis()): Int
    
    /**
     * Marca varios productos como sincronizados.
     *
     * @param productIds Lista de IDs de productos.
     * @param lastSyncTime Marca de tiempo de la última sincronización.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE products SET syncStatus = 'synced', lastSyncTime = :lastSyncTime, pendingChanges = 0 WHERE id IN (:productIds)")
    suspend fun markAsSynced(productIds: List<Int>, lastSyncTime: Long = System.currentTimeMillis()): Int
    
    /**
     * Marca un producto como eliminado (borrado lógico).
     *
     * @param productId ID del producto.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE products SET isDeleted = 1, syncStatus = 'pending_delete', pendingChanges = 1, updatedAt = :updatedAt WHERE id = :productId")
    suspend fun markAsDeleted(productId: Int, updatedAt: Date = Date()): Int
    
    /**
     * Elimina físicamente los productos marcados como eliminados y ya sincronizados.
     *
     * @return Número de filas eliminadas.
     */
    @Query("DELETE FROM products WHERE isDeleted = 1 AND syncStatus = 'synced'")
    suspend fun deleteMarkedProducts(): Int
    
    /**
     * Actualiza el stock de un producto.
     *
     * @param productId ID del producto.
     * @param newStock Nuevo nivel de stock.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE products SET stock = :newStock, updatedAt = :updatedAt, syncStatus = 'pending_update', pendingChanges = 1 WHERE id = :productId")
    suspend fun updateStock(productId: Int, newStock: Int, updatedAt: Date = Date()): Int
    
    /**
     * Obtiene los productos actualizados después de una fecha específica.
     *
     * @param timestamp Marca de tiempo a partir de la cual buscar actualizaciones.
     * @return Lista de productos actualizados después de la fecha especificada.
     */
    @Query("SELECT * FROM products WHERE updatedAt >= :timestamp AND isDeleted = 0")
    suspend fun getProductsUpdatedAfter(timestamp: Date): List<Product>
    
    /**
     * Limpia la base de datos de productos (elimina todos los productos).
     * Utilizar con precaución.
     */
    @Query("DELETE FROM products")
    suspend fun clearAll()
}