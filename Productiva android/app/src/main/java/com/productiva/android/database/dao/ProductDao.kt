package com.productiva.android.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para operaciones con productos en la base de datos local.
 */
@Dao
interface ProductDao {
    /**
     * Obtiene todos los productos.
     */
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>
    
    /**
     * Obtiene un producto por su ID.
     */
    @Query("SELECT * FROM products WHERE id = :productId")
    fun getProductById(productId: Int): Flow<Product?>
    
    /**
     * Obtiene un producto por su ID de forma síncrona.
     */
    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductByIdSync(productId: Int): Product?
    
    /**
     * Busca productos por nombre.
     */
    @Query("SELECT * FROM products WHERE name LIKE :query OR sku LIKE :query OR barcode LIKE :query ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<Product>>
    
    /**
     * Inserta un producto.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)
    
    /**
     * Inserta múltiples productos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)
    
    /**
     * Actualiza un producto.
     */
    @Update
    suspend fun updateProduct(product: Product)
    
    /**
     * Elimina todos los productos.
     */
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
    
    /**
     * Elimina un producto por su ID.
     */
    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProduct(productId: Int)
    
    /**
     * Obtiene los productos de una categoría.
     */
    @Query("SELECT * FROM products WHERE categoryId = :categoryId ORDER BY name ASC")
    fun getProductsByCategory(categoryId: Int): Flow<List<Product>>
    
    /**
     * Obtiene productos con stock bajo.
     */
    @Query("SELECT * FROM products WHERE stock < 5 AND stock > 0 ORDER BY stock ASC")
    fun getLowStockProducts(): Flow<List<Product>>
    
    /**
     * Obtiene productos sin stock.
     */
    @Query("SELECT * FROM products WHERE stock <= 0 ORDER BY name ASC")
    fun getOutOfStockProducts(): Flow<List<Product>>
    
    /**
     * Marca un producto para sincronización.
     */
    @Query("UPDATE products SET needsSync = :needsSync, lastSyncTimestamp = :timestamp WHERE id = :productId")
    suspend fun markForSync(productId: Int, needsSync: Boolean, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Obtiene productos que necesitan sincronización.
     */
    @Query("SELECT * FROM products WHERE needsSync = 1 ORDER BY lastSyncTimestamp ASC")
    suspend fun getProductsForSync(): List<Product>
    
    /**
     * Cuenta productos que necesitan sincronización.
     */
    @Query("SELECT COUNT(*) FROM products WHERE needsSync = 1")
    fun countProductsForSync(): Flow<Int>
    
    /**
     * Actualiza el stock de un producto.
     */
    @Query("UPDATE products SET stock = :newStock, needsSync = 1, lastSyncTimestamp = :timestamp WHERE id = :productId")
    suspend fun updateProductStock(productId: Int, newStock: Int, timestamp: Long = System.currentTimeMillis())
}