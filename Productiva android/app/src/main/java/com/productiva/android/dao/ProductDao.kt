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
     */
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>
    
    /**
     * Obtiene todos los productos (versión sincrónica).
     */
    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllProductsSync(): List<Product>
    
    /**
     * Obtiene un producto por su ID.
     */
    @Query("SELECT * FROM products WHERE id = :productId")
    fun getProductById(productId: Int): Flow<Product?>
    
    /**
     * Obtiene un producto por su ID (versión sincrónica).
     */
    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductByIdSync(productId: Int): Product?
    
    /**
     * Obtiene productos por categoría.
     */
    @Query("SELECT * FROM products WHERE category = :category ORDER BY name ASC")
    fun getProductsByCategory(category: String): Flow<List<Product>>
    
    /**
     * Obtiene productos por empresa.
     */
    @Query("SELECT * FROM products WHERE company_id = :companyId ORDER BY name ASC")
    fun getProductsByCompany(companyId: Int): Flow<List<Product>>
    
    /**
     * Obtiene productos por ubicación.
     */
    @Query("SELECT * FROM products WHERE location_id = :locationId ORDER BY name ASC")
    fun getProductsByLocation(locationId: Int): Flow<List<Product>>
    
    /**
     * Busca productos por nombre, código o código de barras.
     */
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<Product>>
    
    /**
     * Obtiene productos con stock bajo.
     */
    @Query("SELECT * FROM products WHERE stock <= reorder_level AND stock IS NOT NULL AND reorder_level IS NOT NULL ORDER BY stock ASC")
    fun getLowStockProducts(): Flow<List<Product>>
    
    /**
     * Obtiene productos que necesitan sincronización.
     */
    @Query("SELECT * FROM products WHERE needs_sync = 1")
    fun getProductsNeedingSync(): Flow<List<Product>>
    
    /**
     * Obtiene productos que necesitan sincronización (versión sincrónica).
     */
    @Query("SELECT * FROM products WHERE needs_sync = 1")
    suspend fun getProductsNeedingSyncSync(): List<Product>
    
    /**
     * Inserta un nuevo producto.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProduct(product: Product)
    
    /**
     * Inserta múltiples productos.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProducts(products: List<Product>)
    
    /**
     * Actualiza un producto existente.
     */
    @Update
    suspend fun updateProduct(product: Product)
    
    /**
     * Actualiza múltiples productos.
     */
    @Update
    suspend fun updateProducts(products: List<Product>)
    
    /**
     * Inserta o actualiza productos (upsert).
     */
    @Transaction
    suspend fun upsertProducts(products: List<Product>) {
        for (product in products) {
            val existingProduct = getProductByIdSync(product.id)
            if (existingProduct == null) {
                insertProduct(product)
            } else if (!existingProduct.needsSync) {
                // Solo actualizar si el producto local no necesita sincronización
                updateProduct(product)
            }
        }
    }
    
    /**
     * Elimina todos los productos (solo para migraciones o resets).
     */
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
    
    /**
     * Actualiza un producto local con datos del servidor.
     */
    @Transaction
    suspend fun syncProduct(serverProduct: Product) {
        val localProduct = getProductByIdSync(serverProduct.id)
        if (localProduct == null) {
            insertProduct(serverProduct)
        } else if (!localProduct.needsSync) {
            updateProduct(serverProduct)
        } else {
            // Si el producto local necesita sincronización, actualizar solo campos no sensibles
            val updatedProduct = localProduct.copy(
                name = serverProduct.name,
                description = serverProduct.description,
                category = serverProduct.category,
                categoryId = serverProduct.categoryId,
                brand = serverProduct.brand,
                brandId = serverProduct.brandId,
                supplier = serverProduct.supplier,
                supplierId = serverProduct.supplierId,
                imageUrl = serverProduct.imageUrl,
                // Mantener campos que pueden haber sido editados localmente
                price = localProduct.price,
                cost = localProduct.cost,
                stock = localProduct.stock,
                // Mantener el flag de sincronización
                needsSync = true
            )
            updateProduct(updatedProduct)
        }
    }
}