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
     * Inserta nuevos productos en la base de datos.
     * Si ya existe un producto con el mismo ID, lo reemplaza.
     *
     * @param products Lista de productos a insertar.
     * @return Lista de IDs de los productos insertados.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>): List<Long>
    
    /**
     * Inserta un nuevo producto en la base de datos.
     * Si ya existe un producto con el mismo ID, lo reemplaza.
     *
     * @param product Producto a insertar.
     * @return ID del producto insertado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long
    
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
    @Query("SELECT * FROM products ORDER BY name ASC")
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
     * Obtiene productos por compañía.
     *
     * @param companyId ID de la compañía.
     * @return Flow con la lista de productos de la compañía.
     */
    @Query("SELECT * FROM products WHERE companyId = :companyId ORDER BY name ASC")
    fun getProductsByCompany(companyId: Int): Flow<List<Product>>
    
    /**
     * Obtiene productos por ubicación.
     *
     * @param locationId ID de la ubicación.
     * @return Flow con la lista de productos de la ubicación.
     */
    @Query("SELECT * FROM products WHERE locationId = :locationId ORDER BY name ASC")
    fun getProductsByLocation(locationId: Int): Flow<List<Product>>
    
    /**
     * Obtiene productos por categoría.
     *
     * @param category Categoría de los productos.
     * @return Flow con la lista de productos de la categoría.
     */
    @Query("SELECT * FROM products WHERE category = :category ORDER BY name ASC")
    fun getProductsByCategory(category: String): Flow<List<Product>>
    
    /**
     * Busca productos por nombre, código o código de barras.
     *
     * @param query Texto a buscar.
     * @return Flow con la lista de productos que coinciden con la búsqueda.
     */
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<Product>>
    
    /**
     * Obtiene productos con stock bajo.
     *
     * @return Flow con la lista de productos con stock bajo.
     */
    @Query("SELECT * FROM products WHERE stock <= stockMin AND stock IS NOT NULL AND stockMin IS NOT NULL ORDER BY stock ASC")
    fun getLowStockProducts(): Flow<List<Product>>
    
    /**
     * Obtiene productos pendientes de sincronización.
     *
     * @return Lista de productos pendientes de sincronización.
     */
    @Query("SELECT * FROM products WHERE needsSync = 1")
    suspend fun getProductsPendingSync(): List<Product>
    
    /**
     * Actualiza un producto desde el servidor, preservando los cambios locales.
     * Si el producto no existe, lo inserta.
     *
     * @param serverProduct Producto recibido del servidor.
     * @return Producto actualizado o insertado.
     */
    @Transaction
    suspend fun upsertFromServer(serverProduct: Product): Product {
        val existingProduct = getProductByIdSync(serverProduct.id)
        
        val productToSave = if (existingProduct != null) {
            // Si el producto local tiene cambios pendientes de sincronización,
            // actualizamos solo los campos que no modifican los cambios locales
            if (existingProduct.needsSync) {
                existingProduct.copy(
                    // Actualizar solo campos que no afectan a los cambios locales
                    name = serverProduct.name,
                    description = serverProduct.description,
                    imageUrl = serverProduct.imageUrl,
                    category = serverProduct.category,
                    supplier = serverProduct.supplier,
                    isActive = serverProduct.isActive,
                    createdAt = serverProduct.createdAt,
                    updatedAt = serverProduct.updatedAt,
                    lastSyncTime = System.currentTimeMillis()
                    // Mantener los demás campos con los valores locales
                )
            } else {
                // Si no hay cambios locales pendientes, actualizamos todo
                // pero preservamos la ruta de imagen local
                serverProduct.copy(
                    localImagePath = existingProduct.localImagePath,
                    lastSyncTime = System.currentTimeMillis()
                )
            }
        } else {
            // Si no existe, insertamos el nuevo producto
            serverProduct.copy(lastSyncTime = System.currentTimeMillis())
        }
        
        // Guardar el producto actualizado o nuevo
        insertProduct(productToSave)
        
        return productToSave
    }
    
    /**
     * Obtiene un producto por su ID de forma síncrona.
     *
     * @param productId ID del producto.
     * @return El producto, o null si no existe.
     */
    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductByIdSync(productId: Int): Product?
    
    /**
     * Marca todos los productos como sincronizados.
     *
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE products SET needsSync = 0 WHERE needsSync = 1")
    suspend fun markAllAsSynced(): Int
    
    /**
     * Elimina todos los productos.
     */
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
}