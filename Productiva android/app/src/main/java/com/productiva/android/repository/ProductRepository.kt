package com.productiva.android.repository

import android.util.Log
import com.productiva.android.dao.ProductDao
import com.productiva.android.model.Product
import com.productiva.android.network.ApiService
import com.productiva.android.network.NetworkResult
import com.productiva.android.network.safeApiCall
import com.productiva.android.utils.ConnectivityMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repositorio para gestionar los productos.
 * Proporciona métodos para acceder y manipular los productos, incluyendo sincronización con el servidor.
 */
class ProductRepository(
    private val productDao: ProductDao,
    private val apiService: ApiService,
    private val connectivityMonitor: ConnectivityMonitor
) {
    private val TAG = "ProductRepository"
    
    /**
     * Obtiene todos los productos.
     *
     * @return Flow con el estado del recurso que contiene la lista de productos.
     */
    fun getAllProducts(): Flow<ResourceState<List<Product>>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Emitir datos locales primero (caché)
            val localProducts = productDao.getAllProducts()
            localProducts.collect { products ->
                emit(ResourceState.CachedData(products))
                
                // Si hay conexión a Internet, intentar sincronizar
                if (connectivityMonitor.isNetworkAvailable()) {
                    fetchAndSyncProducts()
                } else {
                    emit(ResourceState.Offline<List<Product>>())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener productos", e)
            emit(ResourceState.Error("Error al obtener productos: ${e.message}", e))
        }
    }
    
    /**
     * Obtiene un producto por su ID.
     *
     * @param productId ID del producto.
     * @return Flow con el estado del recurso que contiene el producto.
     */
    fun getProductById(productId: Int): Flow<ResourceState<Product>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Emitir datos locales primero (caché)
            val localProduct = productDao.getProductById(productId)
            localProduct.collect { product ->
                if (product != null) {
                    emit(ResourceState.CachedData(product))
                    
                    // Si hay conexión a Internet, intentar obtener la versión actualizada
                    if (connectivityMonitor.isNetworkAvailable()) {
                        fetchProductFromServer(productId)
                    } else {
                        emit(ResourceState.Offline<Product>())
                    }
                } else {
                    // Si no existe localmente, intentar obtenerlo del servidor
                    if (connectivityMonitor.isNetworkAvailable()) {
                        fetchProductFromServer(productId)
                    } else {
                        emit(ResourceState.Error("Producto no encontrado y sin conexión a Internet"))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener producto por ID", e)
            emit(ResourceState.Error("Error al obtener producto: ${e.message}", e))
        }
    }
    
    /**
     * Obtiene un producto por su código de barras.
     *
     * @param barcode Código de barras del producto.
     * @return Flow con el estado del recurso que contiene el producto.
     */
    fun getProductByBarcode(barcode: String): Flow<ResourceState<Product>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Buscar localmente primero
            val localProduct = productDao.getProductByBarcode(barcode)
            localProduct.collect { product ->
                if (product != null) {
                    emit(ResourceState.Success(product))
                } else {
                    // Si hay conexión, buscar en el servidor
                    if (connectivityMonitor.isNetworkAvailable()) {
                        // Aquí se podría implementar una búsqueda específica por código de barras
                        // Por ahora, sincronizamos todos los productos para actualizar la base local
                        fetchAndSyncProducts()
                        
                        // Verificar si después de la sincronización ya existe el producto
                        val updatedProduct = productDao.getProductByBarcodeSync(barcode)
                        if (updatedProduct != null) {
                            emit(ResourceState.Success(updatedProduct))
                        } else {
                            emit(ResourceState.Error("Producto no encontrado"))
                        }
                    } else {
                        emit(ResourceState.Error("Producto no encontrado y sin conexión a Internet"))
                        emit(ResourceState.Offline<Product>())
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener producto por código de barras", e)
            emit(ResourceState.Error("Error al buscar producto: ${e.message}", e))
        }
    }
    
    /**
     * Obtiene productos por categoría.
     *
     * @param category Categoría de los productos.
     * @return Flow con el estado del recurso que contiene la lista de productos.
     */
    fun getProductsByCategory(category: String): Flow<ResourceState<List<Product>>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Emitir datos locales primero (caché)
            val localProducts = productDao.getProductsByCategory(category)
            localProducts.collect { products ->
                emit(ResourceState.CachedData(products))
                
                // Si hay conexión a Internet, intentar sincronizar
                if (connectivityMonitor.isNetworkAvailable()) {
                    fetchAndSyncProducts()
                } else {
                    emit(ResourceState.Offline<List<Product>>())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener productos por categoría", e)
            emit(ResourceState.Error("Error al obtener productos por categoría: ${e.message}", e))
        }
    }
    
    /**
     * Busca productos en la base de datos local.
     *
     * @param query Texto de búsqueda.
     * @return Flow con el estado del recurso que contiene la lista de productos.
     */
    fun searchProducts(query: String): Flow<ResourceState<List<Product>>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Emitir resultados de búsqueda local
            val localProducts = productDao.searchProducts(query)
            localProducts.collect { products ->
                emit(ResourceState.Success(products))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al buscar productos", e)
            emit(ResourceState.Error("Error al buscar productos: ${e.message}", e))
        }
    }
    
    /**
     * Crea un nuevo producto.
     *
     * @param product Datos del producto.
     * @return Flow con el estado del recurso que indica el resultado de la operación.
     */
    fun createProduct(product: Product): Flow<ResourceState<Product>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Guardar localmente primero con marca de modificación local
            val productWithLocalMark = product.copy(isLocallyModified = true)
            productDao.insertProduct(productWithLocalMark)
            
            // Si hay conexión, enviar al servidor
            if (connectivityMonitor.isNetworkAvailable()) {
                val result = safeApiCall {
                    apiService.createProduct(product)
                }
                
                when (result) {
                    is NetworkResult.Success -> {
                        // Actualizar en local con los datos del servidor
                        val serverProduct = result.data
                        val syncedProduct = serverProduct.copy(
                            isLocallyModified = false,
                            lastSyncTime = System.currentTimeMillis()
                        )
                        productDao.insertProduct(syncedProduct)
                        emit(ResourceState.Success(syncedProduct))
                    }
                    is NetworkResult.Error -> {
                        // Mantener la versión local para sincronizar más tarde
                        emit(ResourceState.Error("Error al sincronizar producto: ${result.message}"))
                    }
                    is NetworkResult.Loading -> {
                        // No debería ocurrir, pero por si acaso
                        Log.d(TAG, "Loading state in createProduct network call")
                    }
                }
            } else {
                // Sin conexión, se queda pendiente de sincronización
                emit(ResourceState.Success(productWithLocalMark))
                emit(ResourceState.Offline<Product>())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear producto", e)
            emit(ResourceState.Error("Error al crear producto: ${e.message}", e))
        }
    }
    
    /**
     * Actualiza un producto existente.
     *
     * @param product Datos del producto.
     * @return Flow con el estado del recurso que indica el resultado de la operación.
     */
    fun updateProduct(product: Product): Flow<ResourceState<Product>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Verificar que el producto existe
            val existingProduct = productDao.getProductByIdSync(product.id)
            if (existingProduct == null) {
                emit(ResourceState.Error("Producto no encontrado"))
                return@flow
            }
            
            // Actualizar localmente con marca de modificación local
            val productWithLocalMark = product.copy(isLocallyModified = true)
            productDao.updateProduct(productWithLocalMark)
            
            // Si hay conexión, enviar al servidor
            if (connectivityMonitor.isNetworkAvailable()) {
                val result = safeApiCall {
                    apiService.updateProduct(product.id, product)
                }
                
                when (result) {
                    is NetworkResult.Success -> {
                        // Actualizar en local con los datos del servidor
                        val serverProduct = result.data
                        val syncedProduct = serverProduct.copy(
                            isLocallyModified = false,
                            lastSyncTime = System.currentTimeMillis()
                        )
                        productDao.updateProduct(syncedProduct)
                        emit(ResourceState.Success(syncedProduct))
                    }
                    is NetworkResult.Error -> {
                        // Mantener la versión local para sincronizar más tarde
                        emit(ResourceState.Error("Error al sincronizar producto: ${result.message}"))
                    }
                    is NetworkResult.Loading -> {
                        // No debería ocurrir, pero por si acaso
                        Log.d(TAG, "Loading state in updateProduct network call")
                    }
                }
            } else {
                // Sin conexión, se queda pendiente de sincronización
                emit(ResourceState.Success(productWithLocalMark))
                emit(ResourceState.Offline<Product>())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar producto", e)
            emit(ResourceState.Error("Error al actualizar producto: ${e.message}", e))
        }
    }
    
    /**
     * Actualiza el stock de un producto.
     *
     * @param productId ID del producto.
     * @param newStock Nuevo valor de stock.
     * @return Flow con el estado del recurso que indica el resultado de la operación.
     */
    fun updateProductStock(productId: Int, newStock: Int): Flow<ResourceState<Boolean>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Actualizar localmente
            val updated = productDao.updateProductStock(productId, newStock)
            if (updated <= 0) {
                emit(ResourceState.Error("Producto no encontrado"))
                return@flow
            }
            
            // Obtener el producto actualizado
            val product = productDao.getProductByIdSync(productId)
            if (product == null) {
                emit(ResourceState.Error("Producto no encontrado después de actualizar"))
                return@flow
            }
            
            // Si hay conexión, sincronizar con el servidor
            if (connectivityMonitor.isNetworkAvailable()) {
                // Enviamos el producto completo al servidor
                val result = safeApiCall {
                    apiService.updateProduct(productId, product)
                }
                
                when (result) {
                    is NetworkResult.Success -> {
                        // Marcar como sincronizado
                        val syncTime = System.currentTimeMillis()
                        productDao.markProductsAsSynced(listOf(productId), syncTime)
                        emit(ResourceState.Success(true))
                    }
                    is NetworkResult.Error -> {
                        // Quedar pendiente de sincronización
                        emit(ResourceState.Error("Error al sincronizar stock: ${result.message}"))
                    }
                    is NetworkResult.Loading -> {
                        // No debería ocurrir, pero por si acaso
                        Log.d(TAG, "Loading state in updateProductStock network call")
                    }
                }
            } else {
                // Sin conexión, se queda pendiente de sincronización
                emit(ResourceState.Success(true))
                emit(ResourceState.Offline<Boolean>())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar stock", e)
            emit(ResourceState.Error("Error al actualizar stock: ${e.message}", e))
        }
    }
    
    /**
     * Elimina un producto.
     *
     * @param productId ID del producto.
     * @return Flow con el estado del recurso que indica el resultado de la operación.
     */
    fun deleteProduct(productId: Int): Flow<ResourceState<Boolean>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Eliminar localmente
            val deleted = productDao.deleteProductById(productId)
            if (deleted <= 0) {
                emit(ResourceState.Error("Producto no encontrado"))
                return@flow
            }
            
            // Si hay conexión, eliminar en el servidor
            if (connectivityMonitor.isNetworkAvailable()) {
                val result = safeApiCall {
                    apiService.deleteProduct(productId)
                }
                
                when (result) {
                    is NetworkResult.Success -> {
                        emit(ResourceState.Success(true))
                    }
                    is NetworkResult.Error -> {
                        // Ya se eliminó localmente, informar del error en sincronización
                        emit(ResourceState.Error("Producto eliminado localmente, pero ocurrió un error al sincronizar: ${result.message}"))
                    }
                    is NetworkResult.Loading -> {
                        // No debería ocurrir, pero por si acaso
                        Log.d(TAG, "Loading state in deleteProduct network call")
                    }
                }
            } else {
                // Sin conexión, producto eliminado localmente
                emit(ResourceState.Success(true))
                emit(ResourceState.Offline<Boolean>())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar producto", e)
            emit(ResourceState.Error("Error al eliminar producto: ${e.message}", e))
        }
    }
    
    /**
     * Sincroniza productos modificados localmente con el servidor.
     *
     * @return Flow con el estado del recurso que indica el resultado de la sincronización.
     */
    fun syncPendingProducts(): Flow<ResourceState<Int>> = flow {
        emit(ResourceState.Loading())
        
        if (!connectivityMonitor.isNetworkAvailable()) {
            emit(ResourceState.Offline<Int>())
            return@flow
        }
        
        try {
            // Obtener productos pendientes de sincronización
            val pendingProducts = productDao.getProductsToSync()
            if (pendingProducts.isEmpty()) {
                emit(ResourceState.Success(0))
                return@flow
            }
            
            // Sincronizar con el servidor
            val result = safeApiCall {
                apiService.syncProducts(pendingProducts)
            }
            
            when (result) {
                is NetworkResult.Success -> {
                    val syncResponse = result.data
                    val syncTime = System.currentTimeMillis()
                    
                    // Aplicar cambios del servidor
                    val addedAndUpdated = syncResponse.added + syncResponse.updated
                    val productsWithSyncTime = addedAndUpdated.map { it.copy(lastSyncTime = syncTime, isLocallyModified = false) }
                    
                    if (productsWithSyncTime.isNotEmpty()) {
                        productDao.insertProducts(productsWithSyncTime)
                    }
                    
                    // Marcar productos como sincronizados
                    val syncedIds = pendingProducts.map { it.id }
                    if (syncedIds.isNotEmpty()) {
                        productDao.markProductsAsSynced(syncedIds, syncTime)
                    }
                    
                    emit(ResourceState.Success(syncedIds.size))
                }
                is NetworkResult.Error -> {
                    emit(ResourceState.Error("Error al sincronizar productos: ${result.message}"))
                }
                is NetworkResult.Loading -> {
                    // No debería ocurrir, pero por si acaso
                    Log.d(TAG, "Loading state in syncPendingProducts network call")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar productos", e)
            emit(ResourceState.Error("Error al sincronizar productos: ${e.message}", e))
        }
    }
    
    /**
     * Obtiene categorías de productos.
     *
     * @return Flow con el estado del recurso que contiene la lista de categorías.
     */
    fun getAllCategories(): Flow<ResourceState<List<String>>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Emitir categorías locales
            val localCategories = productDao.getAllCategories()
            localCategories.collect { categories ->
                emit(ResourceState.Success(categories))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener categorías", e)
            emit(ResourceState.Error("Error al obtener categorías: ${e.message}", e))
        }
    }
    
    /**
     * Obtiene los productos desde el servidor y los sincroniza con la base de datos local.
     */
    private suspend fun fetchAndSyncProducts() {
        if (!connectivityMonitor.isNetworkAvailable()) {
            return
        }
        
        try {
            // Obtener el timestamp de la última sincronización
            val lastSync = findLastProductSyncTime()
            
            // Obtener productos actualizados desde el servidor
            val result = safeApiCall {
                apiService.getProducts(lastSync)
            }
            
            when (result) {
                is NetworkResult.Success -> {
                    val products = result.data
                    val currentTime = System.currentTimeMillis()
                    
                    // Sincronizar con la base de datos local
                    productDao.syncProductsFromServer(products, emptyList(), currentTime)
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Error al obtener productos del servidor: ${result.message}")
                }
                is NetworkResult.Loading -> {
                    // No debería ocurrir, pero por si acaso
                    Log.d(TAG, "Loading state in fetchAndSyncProducts network call")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar productos", e)
        }
    }
    
    /**
     * Obtiene un producto desde el servidor.
     *
     * @param productId ID del producto.
     */
    private suspend fun fetchProductFromServer(productId: Int) {
        if (!connectivityMonitor.isNetworkAvailable()) {
            return
        }
        
        try {
            val result = safeApiCall {
                apiService.getProductById(productId)
            }
            
            when (result) {
                is NetworkResult.Success -> {
                    val product = result.data
                    val syncedProduct = product.copy(
                        isLocallyModified = false,
                        lastSyncTime = System.currentTimeMillis()
                    )
                    productDao.insertProduct(syncedProduct)
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Error al obtener producto del servidor: ${result.message}")
                }
                is NetworkResult.Loading -> {
                    // No debería ocurrir, pero por si acaso
                    Log.d(TAG, "Loading state in fetchProductFromServer network call")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener producto del servidor", e)
        }
    }
    
    /**
     * Encuentra el timestamp de la última sincronización de productos.
     *
     * @return Timestamp de la última sincronización.
     */
    private suspend fun findLastProductSyncTime(): Long {
        try {
            // Aquí se podría implementar una lógica más sofisticada para guardar y recuperar
            // el timestamp de la última sincronización exitosa
            return 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener timestamp de última sincronización", e)
            return 0L
        }
    }
}