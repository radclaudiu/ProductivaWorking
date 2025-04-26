package com.productiva.android.repository

import android.util.Log
import com.productiva.android.dao.ProductDao
import com.productiva.android.model.Product
import com.productiva.android.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Repositorio para gestionar productos.
 */
class ProductRepository(
    private val productDao: ProductDao,
    private val apiService: ApiService
) {
    private val TAG = "ProductRepository"
    
    /**
     * Obtiene todos los productos.
     */
    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
    }
    
    /**
     * Obtiene un producto por su ID.
     */
    fun getProductById(productId: Int): Flow<Product?> {
        return productDao.getProductById(productId)
    }
    
    /**
     * Obtiene productos por categoría.
     */
    fun getProductsByCategory(category: String): Flow<List<Product>> {
        return productDao.getProductsByCategory(category)
    }
    
    /**
     * Obtiene productos por empresa.
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
     * Busca productos por nombre, código o código de barras.
     */
    fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query)
    }
    
    /**
     * Sincroniza productos con el servidor.
     */
    suspend fun syncProducts(locationId: Int? = null, companyId: Int? = null): Flow<ResourceState<List<Product>>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // Sincronizar productos del servidor según los filtros
            val response = when {
                locationId != null -> apiService.getProductsByLocation(locationId)
                companyId != null -> apiService.getProductsByCompany(companyId)
                else -> apiService.getAllProducts()
            }
            
            if (response.isSuccessful) {
                val serverProducts = response.body() ?: emptyList()
                
                // Obtener productos locales que necesitan sincronización
                val localProducts = productDao.getProductsNeedingSyncSync()
                
                // Actualizar productos locales con datos del servidor, preservando cambios locales pendientes
                withContext(Dispatchers.IO) {
                    // Filtrar solo productos que no están pendientes de sincronización
                    val productsToUpdate = serverProducts.filter { serverProduct ->
                        localProducts.none { it.id == serverProduct.id && it.needsSync }
                    }
                    
                    // Insertar o actualizar productos
                    productDao.upsertProducts(productsToUpdate)
                    
                    // También incluir los productos locales que necesitan sincronización
                    // en la lista de productos resultante
                    val resultProducts = ArrayList<Product>(productsToUpdate)
                    resultProducts.addAll(localProducts)
                    
                    emit(ResourceState.Success(resultProducts))
                }
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Sesión expirada"
                    403 -> "Sin permiso para acceder a los productos"
                    404 -> "No se encontraron productos"
                    else -> "Error del servidor: ${response.code()}"
                }
                
                emit(ResourceState.Error(errorMessage))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error de conexión al sincronizar productos", e)
            
            // En caso de error de conexión, devolver los productos locales
            val localProducts = productDao.getAllProductsSync()
            emit(ResourceState.Success(localProducts, "Usando datos locales"))
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar productos", e)
            emit(ResourceState.Error("Error al sincronizar productos: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Actualiza un producto.
     */
    suspend fun updateProduct(product: Product): Flow<ResourceState<Product>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // Marcar producto para sincronización
            val productToUpdate = product.markForSync()
            productDao.updateProduct(productToUpdate)
            
            // Intentar sincronizar con el servidor inmediatamente
            try {
                // Convertir a mapa para la API
                val productData = mapOf(
                    "name" to product.name,
                    "description" to (product.description ?: ""),
                    "code" to (product.code ?: ""),
                    "barcode" to (product.barcode ?: ""),
                    "sku" to (product.sku ?: ""),
                    "price" to product.price,
                    "cost" to (product.cost ?: 0.0),
                    "stock" to (product.stock ?: 0),
                    "reorder_level" to (product.reorderLevel ?: 0),
                    "category" to (product.category ?: ""),
                    "brand" to (product.brand ?: ""),
                    "supplier" to (product.supplier ?: ""),
                    "tax_rate" to (product.taxRate ?: 0.0),
                    "weight" to (product.weight ?: 0.0),
                    "dimensions" to (product.dimensions ?: ""),
                    "is_active" to product.isActive
                )
                
                val response = apiService.updateProduct(product.id, productData)
                
                if (response.isSuccessful) {
                    response.body()?.let { serverProduct ->
                        // Crear producto actualizado sin flag de sincronización
                        val syncedProduct = serverProduct.copy(needsSync = false)
                        productDao.updateProduct(syncedProduct)
                        emit(ResourceState.Success(syncedProduct))
                    } ?: emit(ResourceState.Success(productToUpdate, "Actualizado localmente"))
                } else {
                    // Error de servidor, mantener cambios locales
                    emit(ResourceState.Success(productToUpdate, "Actualizado localmente, pendiente de sincronizar"))
                }
            } catch (e: IOException) {
                // Error de conexión, mantener cambios locales
                Log.d(TAG, "No se pudo sincronizar producto, guardado localmente", e)
                emit(ResourceState.Success(productToUpdate, "Actualizado localmente, pendiente de sincronizar"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar producto", e)
            emit(ResourceState.Error("Error al actualizar producto: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Sincroniza un producto específico con el servidor.
     */
    suspend fun syncProduct(productId: Int): Flow<ResourceState<Product>> = flow {
        emit(ResourceState.Loading)
        
        try {
            val response = apiService.getProductById(productId)
            
            if (response.isSuccessful) {
                val serverProduct = response.body()
                
                if (serverProduct != null) {
                    // Obtener producto local
                    val localProduct = productDao.getProductByIdSync(productId)
                    
                    if (localProduct != null && localProduct.needsSync) {
                        // Si el producto local necesita sincronización, mantener cambios locales
                        val updatedProduct = localProduct.updateFromServer(serverProduct)
                        productDao.updateProduct(updatedProduct)
                        emit(ResourceState.Success(updatedProduct, "Actualizado con datos del servidor, manteniendo cambios locales"))
                    } else {
                        // Si no hay producto local o no necesita sincronización, usar datos del servidor
                        productDao.upsertProducts(listOf(serverProduct))
                        emit(ResourceState.Success(serverProduct))
                    }
                } else {
                    emit(ResourceState.Error("Producto no encontrado en el servidor"))
                }
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Sesión expirada"
                    403 -> "Sin permiso para acceder al producto"
                    404 -> "Producto no encontrado"
                    else -> "Error del servidor: ${response.code()}"
                }
                
                emit(ResourceState.Error(errorMessage))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error de conexión al sincronizar producto", e)
            
            // En caso de error de conexión, devolver el producto local
            val localProduct = productDao.getProductByIdSync(productId)
            if (localProduct != null) {
                emit(ResourceState.Success(localProduct, "Usando datos locales"))
            } else {
                emit(ResourceState.Error("Producto no encontrado localmente"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar producto", e)
            emit(ResourceState.Error("Error al sincronizar producto: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Obtiene productos con stock bajo.
     */
    fun getLowStockProducts(): Flow<List<Product>> {
        return productDao.getLowStockProducts()
    }
}