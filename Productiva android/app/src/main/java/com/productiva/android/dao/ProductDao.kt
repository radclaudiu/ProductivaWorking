package com.productiva.android.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.productiva.android.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para los productos.
 * Proporciona métodos para acceder y manipular la tabla de productos.
 */
@Dao
interface ProductDao {
    /**
     * Inserta un producto en la base de datos.
     * Si ya existe un producto con el mismo ID, lo reemplaza.
     *
     * @param product Producto a insertar.
     * @return ID del producto insertado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long
    
    /**
     * Inserta múltiples productos en la base de datos.
     * Si ya existe algún producto con el mismo ID, lo reemplaza.
     *
     * @param products Lista de productos a insertar.
     * @return Lista de IDs de los productos insertados.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>): List<Long>
    
    /**
     * Actualiza un producto existente.
     *
     * @param product Producto a actualizar.
     * @return Número de filas actualizadas.
     */
    @Update
    suspend fun updateProduct(product: Product): Int
    
    /**
     * Obtiene todos los productos.
     *
     * @return Flow con la lista de todos los productos.
     */
    @Query("SELECT * FROM products ORDER BY name")
    fun getAllProducts(): Flow<List<Product>>
    
    /**
     * Obtiene un producto por su ID.
     *
     * @param productId ID del producto.
     * @return Flow con el producto, o null si no existe.
     */
    @Query("SELECT * FROM products WHERE id = :productId")
    fun getProductById(productId: Int): Flow<Product?>
    
    /**
     * Obtiene un producto por su SKU.
     *
     * @param sku SKU del producto.
     * @return Flow con el producto, o null si no existe.
     */
    @Query("SELECT * FROM products WHERE sku = :sku")
    fun getProductBySku(sku: String): Flow<Product?>
    
    /**
     * Obtiene un producto por su código de barras.
     *
     * @param barcode Código de barras del producto.
     * @return Flow con el producto, o null si no existe.
     */
    @Query("SELECT * FROM products WHERE barcode = :barcode")
    fun getProductByBarcode(barcode: String): Flow<Product?>
    
    /**
     * Obtiene todos los productos de una categoría específica.
     *
     * @param category Categoría de los productos.
     * @return Flow con la lista de productos de la categoría.
     */
    @Query("SELECT * FROM products WHERE category = :category ORDER BY name")
    fun getProductsByCategory(category: String): Flow<List<Product>>
    
    /**
     * Obtiene todos los productos con existencias bajas.
     *
     * @return Flow con la lista de productos con existencias bajas.
     */
    @Query("SELECT * FROM products WHERE stock <= minStock AND stock > 0 ORDER BY stock")
    fun getLowStockProducts(): Flow<List<Product>>
    
    /**
     * Obtiene todos los productos agotados.
     *
     * @return Flow con la lista de productos agotados.
     */
    @Query("SELECT * FROM products WHERE stock <= 0 ORDER BY name")
    fun getOutOfStockProducts(): Flow<List<Product>>
    
    /**
     * Busca productos por nombre, descripción, SKU, código de barras o categoría.
     *
     * @param query Texto a buscar.
     * @return Flow con la lista de productos que coinciden con la búsqueda.
     */
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY name")
    fun searchProducts(query: String): Flow<List<Product>>
    
    /**
     * Obtiene todos los productos que requieren sincronización.
     *
     * @return Lista de productos que necesitan sincronizarse.
     */
    @Query("SELECT * FROM products WHERE isLocallyModified = 1")
    suspend fun getProductsToSync(): List<Product>
    
    /**
     * Marca todos los productos como sincronizados.
     *
     * @param productIds Lista de IDs de productos.
     * @param syncTime Timestamp de la sincronización.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE products SET isLocallyModified = 0, lastSyncTime = :syncTime WHERE id IN (:productIds)")
    suspend fun markProductsAsSynced(productIds: List<Int>, syncTime: Long): Int
    
    /**
     * Elimina un producto por su ID.
     *
     * @param productId ID del producto a eliminar.
     * @return Número de filas eliminadas.
     */
    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: Int): Int
    
    /**
     * Elimina todos los productos.
     */
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
    
    /**
     * Obtiene un producto por su ID de forma síncrona.
     *
     * @param productId ID del producto.
     * @return El producto, o null si no existe.
     */
    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductByIdSync(productId: Int): Product?
    
    /**
     * Obtiene un producto por su SKU de forma síncrona.
     *
     * @param sku SKU del producto.
     * @return El producto, o null si no existe.
     */
    @Query("SELECT * FROM products WHERE sku = :sku")
    suspend fun getProductBySkuSync(sku: String): Product?
    
    /**
     * Obtiene un producto por su código de barras de forma síncrona.
     *
     * @param barcode Código de barras del producto.
     * @return El producto, o null si no existe.
     */
    @Query("SELECT * FROM products WHERE barcode = :barcode")
    suspend fun getProductByBarcodeSync(barcode: String): Product?
    
    /**
     * Elimina los productos por IDs.
     *
     * @param productIds Lista de IDs de productos a eliminar.
     * @return Número de filas eliminadas.
     */
    @Query("DELETE FROM products WHERE id IN (:productIds)")
    suspend fun deleteProductsByIds(productIds: List<Int>): Int
    
    /**
     * Actualiza el stock de un producto.
     *
     * @param productId ID del producto.
     * @param newStock Nuevo valor de stock.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE products SET stock = :newStock, isLocallyModified = 1 WHERE id = :productId")
    suspend fun updateProductStock(productId: Int, newStock: Int): Int
    
    /**
     * Obtiene todas las categorías de productos distintas.
     *
     * @return Flow con la lista de categorías.
     */
    @Query("SELECT DISTINCT category FROM products WHERE category IS NOT NULL ORDER BY category")
    fun getAllCategories(): Flow<List<String>>
    
    /**
     * Transacción para sincronizar productos desde el servidor.
     * Inserta nuevos productos, actualiza existentes y elimina los que ya no existen.
     *
     * @param products Lista de productos del servidor.
     * @param deletedIds Lista de IDs de productos eliminados en el servidor.
     * @param syncTime Timestamp de la sincronización.
     */
    @Transaction
    suspend fun syncProductsFromServer(products: List<Product>, deletedIds: List<Int>, syncTime: Long) {
        // Eliminar productos marcados como eliminados
        if (deletedIds.isNotEmpty()) {
            deleteProductsByIds(deletedIds)
        }
        
        // Insertar o actualizar productos
        val productsWithSyncTime = products.map { product ->
            product.copy(lastSyncTime = syncTime, isLocallyModified = false)
        }
        
        if (productsWithSyncTime.isNotEmpty()) {
            insertProducts(productsWithSyncTime)
        }
    }
}