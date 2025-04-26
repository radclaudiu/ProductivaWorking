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
 * DAO para acceder y manipular productos en la base de datos.
 */
@Dao
interface ProductDao {
    
    /**
     * Obtiene todos los productos.
     *
     * @return Flow con la lista de productos.
     */
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>
    
    /**
     * Obtiene un producto por su ID.
     *
     * @param productId ID del producto.
     * @return Flow con el producto (o null si no existe).
     */
    @Query("SELECT * FROM products WHERE id = :productId")
    fun getProductById(productId: Int): Flow<Product?>
    
    /**
     * Obtiene un producto por su ID de forma síncrona.
     *
     * @param productId ID del producto.
     * @return El producto (o null si no existe).
     */
    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductByIdSync(productId: Int): Product?
    
    /**
     * Obtiene un producto por su código de barras.
     *
     * @param barcode Código de barras del producto.
     * @return Flow con el producto (o null si no existe).
     */
    @Query("SELECT * FROM products WHERE barcode = :barcode")
    fun getProductByBarcode(barcode: String): Flow<Product?>
    
    /**
     * Obtiene un producto por su código de barras de forma síncrona.
     *
     * @param barcode Código de barras del producto.
     * @return El producto (o null si no existe).
     */
    @Query("SELECT * FROM products WHERE barcode = :barcode")
    suspend fun getProductByBarcodeSync(barcode: String): Product?
    
    /**
     * Obtiene productos por categoría.
     *
     * @param category Categoría de los productos.
     * @return Flow con la lista de productos.
     */
    @Query("SELECT * FROM products WHERE category = :category ORDER BY name ASC")
    fun getProductsByCategory(category: String): Flow<List<Product>>
    
    /**
     * Busca productos por nombre, descripción, código de barras o SKU.
     *
     * @param query Texto de búsqueda.
     * @return Flow con la lista de productos que coinciden con la búsqueda.
     */
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<Product>>
    
    /**
     * Obtiene todas las categorías de productos disponibles.
     *
     * @return Flow con la lista de categorías.
     */
    @Query("SELECT DISTINCT category FROM products WHERE category IS NOT NULL ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>
    
    /**
     * Inserta un producto en la base de datos.
     *
     * @param product Producto a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)
    
    /**
     * Inserta varios productos en la base de datos.
     *
     * @param products Lista de productos a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)
    
    /**
     * Actualiza un producto existente.
     *
     * @param product Producto con los datos actualizados.
     * @return Número de filas actualizadas.
     */
    @Update
    suspend fun updateProduct(product: Product): Int
    
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
     * Elimina un producto por su ID.
     *
     * @param productId ID del producto a eliminar.
     * @return Número de filas eliminadas.
     */
    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: Int): Int
    
    /**
     * Obtiene todos los productos modificados localmente que requieren sincronización.
     *
     * @return Lista de productos pendientes de sincronización.
     */
    @Query("SELECT * FROM products WHERE isLocallyModified = 1")
    suspend fun getProductsToSync(): List<Product>
    
    /**
     * Marca varios productos como sincronizados.
     *
     * @param productIds Lista de IDs de productos a marcar.
     * @param syncTime Timestamp de la sincronización.
     */
    @Query("UPDATE products SET isLocallyModified = 0, lastSyncTime = :syncTime WHERE id IN (:productIds)")
    suspend fun markProductsAsSynced(productIds: List<Int>, syncTime: Long)
    
    /**
     * Sincroniza los productos con los datos del servidor.
     * Inserta o actualiza los productos recibidos, y elimina los que ya no existen en el servidor.
     *
     * @param products Productos recibidos del servidor.
     * @param productsToDelete IDs de productos a eliminar (opcional).
     * @param syncTime Timestamp de la sincronización.
     */
    @Transaction
    suspend fun syncProductsFromServer(products: List<Product>, productsToDelete: List<Int>, syncTime: Long) {
        // Insertar o actualizar productos recibidos
        for (product in products) {
            val existingProduct = getProductByIdSync(product.id)
            if (existingProduct != null && existingProduct.isLocallyModified) {
                // Si el producto existe y fue modificado localmente, preservar esa marca
                insertProduct(product.copy(isLocallyModified = true, lastSyncTime = syncTime))
            } else {
                // En caso contrario, usar los datos del servidor
                insertProduct(product.copy(isLocallyModified = false, lastSyncTime = syncTime))
            }
        }
        
        // Eliminar productos que ya no existen en el servidor
        if (productsToDelete.isNotEmpty()) {
            deleteProductsByIds(productsToDelete)
        }
    }
    
    /**
     * Elimina varios productos por sus IDs.
     *
     * @param productIds Lista de IDs de productos a eliminar.
     */
    @Query("DELETE FROM products WHERE id IN (:productIds)")
    suspend fun deleteProductsByIds(productIds: List<Int>)
}