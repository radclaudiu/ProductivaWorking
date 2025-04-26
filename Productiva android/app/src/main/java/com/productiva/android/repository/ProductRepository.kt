package com.productiva.android.repository

import android.util.Log
import com.productiva.android.dao.ProductDao
import com.productiva.android.model.Product
import com.productiva.android.network.ApiService
import com.productiva.android.network.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Repositorio para la gestión de productos.
 * Proporciona métodos para obtener, sincronizar y actualizar productos.
 */
class ProductRepository(
    private val productDao: ProductDao,
    private val apiService: ApiService
) {
    private val TAG = "ProductRepository"
    
    /**
     * Obtiene todos los productos de la base de datos local.
     */
    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
    }
    
    /**
     * Obtiene un producto específico por su ID.
     */
    fun getProductById(productId: Int): Flow<Product?> {
        return productDao.getProductById(productId)
    }
    
    /**
     * Obtiene productos por compañía.
     */
    fun getProductsByCompany(companyId: Int): Flow<List<Product>> {
        return productDao.getProductsByCompany(companyId)
    }
    
    /**
     * Obtiene productos por ubicación.
     */
    fun getProductsByLocation(locationId: Int): Flow<List<Product>> {
        return productDao.getProductsByLocation(locationId)
    }
    
    /**
     * Obtiene productos por categoría.
     */
    fun getProductsByCategory(category: String): Flow<List<Product>> {
        return productDao.getProductsByCategory(category)
    }
    
    /**
     * Busca productos por nombre, código o código de barras.
     */
    fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query)
    }
    
    /**
     * Obtiene productos con stock bajo.
     */
    fun getLowStockProducts(): Flow<List<Product>> {
        return productDao.getLowStockProducts()
    }
    
    /**
     * Sincroniza productos con el servidor.
     * Primero sube los cambios locales pendientes y luego obtiene los últimos datos del servidor.
     */
    fun syncProducts(locationId: Int? = null, companyId: Int? = null): Flow<ResourceState<List<Product>>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // 1. Subir cambios locales pendientes
            val pendingProducts = productDao.getProductsPendingSync()
            if (pendingProducts.isNotEmpty()) {
                Log.d(TAG, "Sincronizando ${pendingProducts.size} productos pendientes")
                
                for (product in pendingProducts) {
                    try {
                        val response = apiService.updateProduct(product.id, product)
                        if (response.success) {
                            Log.d(TAG, "Producto sincronizado con éxito: ${product.id}")
                        } else {
                            Log.e(TAG, "Error al sincronizar producto: ${response.message}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al subir producto ${product.id}", e)
                        // Continuamos con el siguiente producto aunque haya error
                    }
                }
                
                // Marcar todos como sincronizados
                productDao.markAllAsSynced()
            }
            
            // 2. Obtener productos del servidor según los filtros
            val response = when {
                locationId != null -> apiService.getProductsByLocation(locationId)
                companyId != null -> apiService.getProductsByCompany(companyId)
                else -> apiService.getAllProducts()
            }
            
            // 3. Insertar o actualizar los productos en la base de datos local
            if (response.success && response.data != null) {
                val products = response.data
                
                withContext(Dispatchers.IO) {
                    for (product in products) {
                        productDao.upsertFromServer(product)
                    }
                }
                
                // Emitir los productos actualizados
                val updatedProducts = when {
                    locationId != null -> productDao.getProductsByLocation(locationId).value ?: emptyList()
                    companyId != null -> productDao.getProductsByCompany(companyId).value ?: emptyList()
                    else -> productDao.getAllProducts().value ?: emptyList()
                }
                
                emit(ResourceState.Success(updatedProducts))
            } else {
                emit(ResourceState.Error(response.message ?: "Error desconocido al obtener productos"))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "Error HTTP al sincronizar productos", e)
            emit(ResourceState.Error("Error de red: ${e.message()}"))
        } catch (e: IOException) {
            Log.e(TAG, "Error IO al sincronizar productos", e)
            emit(ResourceState.Error("Error de conexión: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar productos", e)
            emit(ResourceState.Error("Error al sincronizar: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Sincroniza un producto específico con el servidor.
     */
    fun syncProduct(productId: Int): Flow<ResourceState<Product>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // 1. Verificar si hay cambios locales pendientes
            val existingProduct = productDao.getProductByIdSync(productId)
            
            if (existingProduct?.needsSync == true) {
                // Subir cambios al servidor
                try {
                    val response = apiService.updateProduct(productId, existingProduct)
                    if (response.success) {
                        // Marcar como sincronizado
                        val updated = existingProduct.copy(needsSync = false)
                        productDao.updateProduct(updated)
                    } else {
                        Log.e(TAG, "Error al sincronizar producto: ${response.message}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al subir producto $productId", e)
                    // Continuamos aunque haya error
                }
            }
            
            // 2. Obtener la última versión del producto desde el servidor
            val response = apiService.getProductById(productId)
            
            if (response.success && response.data != null) {
                val serverProduct = response.data
                
                // 3. Actualizar en la base de datos local
                val updatedProduct = productDao.upsertFromServer(serverProduct)
                
                emit(ResourceState.Success(updatedProduct))
            } else {
                // Si no se puede obtener del servidor pero existe localmente,
                // devolvemos la versión local
                if (existingProduct != null) {
                    emit(ResourceState.Success(existingProduct))
                } else {
                    emit(ResourceState.Error(response.message ?: "Error al obtener el producto"))
                }
            }
        } catch (e: HttpException) {
            Log.e(TAG, "Error HTTP al sincronizar producto $productId", e)
            emit(ResourceState.Error("Error de red: ${e.message()}"))
        } catch (e: IOException) {
            Log.e(TAG, "Error IO al sincronizar producto $productId", e)
            emit(ResourceState.Error("Error de conexión: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar producto $productId", e)
            emit(ResourceState.Error("Error al sincronizar: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Actualiza un producto localmente y lo marca para sincronización.
     */
    fun updateProduct(product: Product): Flow<ResourceState<Product>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // Marcar para sincronización
            val productToUpdate = product.markForSync()
            
            // Actualizar en la base de datos local
            productDao.updateProduct(productToUpdate)
            
            emit(ResourceState.Success(productToUpdate))
            
            // Intentar sincronizar inmediatamente si es posible
            try {
                val response = apiService.updateProduct(product.id, productToUpdate)
                if (response.success) {
                    // Marcar como sincronizado
                    val synced = productToUpdate.copy(needsSync = false)
                    productDao.updateProduct(synced)
                    emit(ResourceState.Success(synced, "Producto actualizado y sincronizado"))
                }
            } catch (e: Exception) {
                // No hacemos nada si falla la sincronización inmediata,
                // se sincronizará más tarde
                Log.d(TAG, "La sincronización inmediata falló, se intentará más tarde")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar producto localmente", e)
            emit(ResourceState.Error("Error al actualizar: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
}