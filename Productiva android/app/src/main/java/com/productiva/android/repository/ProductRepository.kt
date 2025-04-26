package com.productiva.android.repository

import android.util.Log
import com.productiva.android.database.dao.ProductDao
import com.productiva.android.model.Product
import com.productiva.android.network.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import java.io.IOException

/**
 * Repositorio para gestionar productos, sincronizados con el servidor.
 */
class ProductRepository(
    private val productDao: ProductDao,
    private val apiService: ApiService
) {
    private val TAG = "ProductRepository"
    
    /**
     * Obtiene todos los productos almacenados localmente.
     * 
     * @return Flow con la lista de productos.
     */
    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
    }
    
    /**
     * Obtiene un producto por su ID.
     * 
     * @param productId ID del producto.
     * @return Flow con el producto o null si no existe.
     */
    fun getProductById(productId: Int): Flow<Product?> {
        return productDao.getProductById(productId)
    }
    
    /**
     * Busca productos por nombre.
     * 
     * @param query Cadena de búsqueda.
     * @return Flow con la lista de productos que coinciden con la búsqueda.
     */
    fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts("%$query%")
    }
    
    /**
     * Sincroniza los productos con el servidor.
     * 
     * @return Flow con el estado de la operación.
     */
    fun syncProducts(): Flow<ResourceState<List<Product>>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // Obtener productos del servidor
            val response = apiService.getProducts()
            
            if (response.isSuccessful) {
                val products = response.body()
                
                if (products != null) {
                    // Guardar productos en la base de datos local
                    productDao.deleteAllProducts() // Limpiar productos antiguos
                    productDao.insertProducts(products)
                    
                    Log.d(TAG, "Productos sincronizados correctamente: ${products.size}")
                    emit(ResourceState.Success(products))
                } else {
                    emit(ResourceState.Error("Respuesta vacía del servidor"))
                }
            } else {
                emit(ResourceState.Error("Error al obtener productos: ${response.code()}"))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error de red al sincronizar productos", e)
            emit(ResourceState.Error("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar productos", e)
            emit(ResourceState.Error("Error: ${e.message}"))
        }
    }
    
    /**
     * Obtiene los productos por categoría.
     * 
     * @param categoryId ID de la categoría.
     * @return Flow con la lista de productos de esa categoría.
     */
    fun getProductsByCategory(categoryId: Int): Flow<List<Product>> {
        return productDao.getProductsByCategory(categoryId)
    }
    
    /**
     * Actualiza el stock de un producto localmente.
     * 
     * @param productId ID del producto.
     * @param newStock Nueva cantidad de stock.
     * @return Flow con el estado de la operación.
     */
    fun updateProductStock(productId: Int, newStock: Int): Flow<ResourceState<Product>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // Obtener el producto actual
            val currentProduct = productDao.getProductByIdSync(productId)
            
            if (currentProduct != null) {
                // Actualizar el stock
                val updatedProduct = currentProduct.copy(stock = newStock)
                productDao.updateProduct(updatedProduct)
                
                // Marcar para sincronización posterior
                productDao.markForSync(productId, true)
                
                emit(ResourceState.Success(updatedProduct))
            } else {
                emit(ResourceState.Error("Producto no encontrado"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar stock del producto", e)
            emit(ResourceState.Error("Error: ${e.message}"))
        }
    }
    
    /**
     * Sincroniza los cambios pendientes de productos con el servidor.
     * 
     * @return Flow con el estado de la operación.
     */
    fun syncPendingProductChanges(): Flow<ResourceState<Int>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // Obtener productos pendientes de sincronización
            val pendingProducts = productDao.getProductsForSync()
            
            if (pendingProducts.isEmpty()) {
                emit(ResourceState.Success(0))
                return@flow
            }
            
            var syncedCount = 0
            
            for (product in pendingProducts) {
                // Enviar actualización al servidor
                val response = apiService.updateProduct(product.id, product)
                
                if (response.isSuccessful) {
                    // Marcar como sincronizado
                    productDao.markForSync(product.id, false)
                    syncedCount++
                } else {
                    Log.e(TAG, "Error al sincronizar producto ${product.id}: ${response.code()}")
                }
            }
            
            if (syncedCount == pendingProducts.size) {
                emit(ResourceState.Success(syncedCount))
            } else {
                emit(ResourceState.Error("Algunos productos no se pudieron sincronizar", null, Exception("Sync partially failed")))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error de red al sincronizar cambios de productos", e)
            emit(ResourceState.Error("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar cambios de productos", e)
            emit(ResourceState.Error("Error: ${e.message}"))
        }
    }
}