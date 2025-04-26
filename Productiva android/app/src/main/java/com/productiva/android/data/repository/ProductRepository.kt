package com.productiva.android.data.repository

import android.content.Context
import android.util.Log
import com.productiva.android.data.database.AppDatabase
import com.productiva.android.data.model.Product
import com.productiva.android.network.NetworkResult
import com.productiva.android.network.model.SyncResponse
import com.productiva.android.network.safeApiCall
import com.productiva.android.repository.ResourceState
import com.productiva.android.session.SessionManager
import com.productiva.android.sync.SyncResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.collections.HashMap

/**
 * Repositorio que gestiona el acceso a datos de productos,
 * tanto desde la base de datos local como desde el servidor remoto.
 */
class ProductRepository private constructor(context: Context) : BaseRepository(context) {
    
    private val productDao = AppDatabase.getDatabase(context, kotlinx.coroutines.MainScope()).productDao()
    private val sessionManager = SessionManager.getInstance()
    
    companion object {
        private const val TAG = "ProductRepository"
        
        @Volatile
        private var instance: ProductRepository? = null
        
        /**
         * Obtiene la instancia única del repositorio.
         *
         * @param context Contexto de la aplicación.
         * @return Instancia del repositorio.
         */
        fun getInstance(context: Context): ProductRepository {
            return instance ?: synchronized(this) {
                instance ?: ProductRepository(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Obtiene todos los productos como flujo de ResourceState.
     *
     * @param forceRefresh Si es true, fuerza una actualización desde el servidor.
     * @return Flujo de ResourceState con los productos.
     */
    fun getAllProducts(forceRefresh: Boolean = false): Flow<ResourceState<List<Product>>> {
        return networkBoundResource(
            shouldFetch = { products -> forceRefresh || products.isNullOrEmpty() },
            remoteFetch = {
                val companyId = sessionManager.getCurrentCompanyId()
                safeApiCall {
                    apiService.getProducts(companyId = companyId)
                }
            },
            localFetch = {
                productDao.getAllProducts()
            },
            saveFetchResult = { response ->
                withContext(Dispatchers.IO) {
                    // Guardar productos añadidos y actualizados
                    val productsToSave = mutableListOf<Product>()
                    
                    response.added.forEach { product ->
                        productsToSave.add(product.withSyncStatus(Product.SyncStatus.SYNCED))
                    }
                    
                    response.updated.forEach { product ->
                        productsToSave.add(product.withSyncStatus(Product.SyncStatus.SYNCED))
                    }
                    
                    if (productsToSave.isNotEmpty()) {
                        productDao.insertAll(productsToSave)
                    }
                    
                    // Procesar eliminaciones
                    if (response.deleted.isNotEmpty()) {
                        for (productId in response.deleted) {
                            productDao.markAsDeleted(productId)
                        }
                        productDao.deleteMarkedProducts()
                    }
                }
            }
        )
    }
    
    /**
     * Obtiene un producto por su ID como flujo de ResourceState.
     *
     * @param productId ID del producto.
     * @param forceRefresh Si es true, fuerza una actualización desde el servidor.
     * @return Flujo de ResourceState con el producto.
     */
    fun getProductById(productId: Int, forceRefresh: Boolean = false): Flow<ResourceState<Product>> {
        return networkBoundResource(
            shouldFetch = { product -> forceRefresh || product == null },
            remoteFetch = {
                safeApiCall {
                    apiService.getProductById(productId)
                }
            },
            localFetch = {
                productDao.getProductById(productId)
            },
            saveFetchResult = { product ->
                withContext(Dispatchers.IO) {
                    productDao.insert(product.withSyncStatus(Product.SyncStatus.SYNCED))
                }
            }
        )
    }
    
    /**
     * Busca productos por nombre o descripción.
     *
     * @param query Texto a buscar.
     * @return Flujo de ResourceState con los productos que coinciden con la búsqueda.
     */
    fun searchProducts(query: String): Flow<ResourceState<List<Product>>> {
        return networkBoundResource(
            shouldFetch = { false }, // No buscar en el servidor, solo localmente
            remoteFetch = {
                // No se usa, pero es necesario para el tipo
                NetworkResult.Success(emptyList())
            },
            localFetch = {
                productDao.searchProducts(query)
            },
            saveFetchResult = { /* No se guarda nada */ }
        )
    }
    
    /**
     * Crea un nuevo producto.
     *
     * @param product Producto a crear.
     * @return Flujo de ResourceState con el producto creado.
     */
    suspend fun createProduct(product: Product): ResourceState<Product> = withContext(Dispatchers.IO) {
        try {
            // Preparar el producto para creación local
            val productWithStatus = product.copy(
                syncStatus = Product.SyncStatus.PENDING_UPLOAD
            )
            
            // Guardar el producto en la base de datos local
            val productId = productDao.insert(productWithStatus).toInt()
            val savedProduct = productDao.getProductById(productId)
                ?: return@withContext ResourceState.Error("Error al guardar el producto")
            
            // Intentar sincronizar si hay conexión
            if (connectivityMonitor.isNetworkAvailable()) {
                when (val result = safeApiCall { apiService.createProduct(savedProduct) }) {
                    is NetworkResult.Success -> {
                        // Actualizar el producto con los datos del servidor
                        val serverProduct = result.data
                        productDao.insert(serverProduct.withSyncStatus(Product.SyncStatus.SYNCED))
                        
                        return@withContext ResourceState.Success(serverProduct)
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Error al sincronizar producto creado: ${result.message}")
                        return@withContext ResourceState.Success(savedProduct, isFromCache = true)
                    }
                    is NetworkResult.Loading -> {
                        return@withContext ResourceState.Success(savedProduct, isFromCache = true)
                    }
                }
            } else {
                // Sin conexión, devolver éxito pero con datos locales
                return@withContext ResourceState.Success(savedProduct, isFromCache = true)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear producto", e)
            ResourceState.Error(e.message ?: "Error desconocido")
        }
    }
    
    /**
     * Actualiza un producto existente.
     *
     * @param product Producto actualizado.
     * @return Flujo de ResourceState con el producto actualizado.
     */
    suspend fun updateProduct(product: Product): ResourceState<Product> = withContext(Dispatchers.IO) {
        try {
            // Preparar el producto para actualización local
            val productWithStatus = product.copy(
                syncStatus = Product.SyncStatus.PENDING_UPDATE,
                pendingChanges = true,
                updatedAt = Date()
            )
            
            // Actualizar el producto en la base de datos local
            productDao.update(productWithStatus)
            val savedProduct = productDao.getProductById(product.id)
                ?: return@withContext ResourceState.Error("Producto no encontrado")
            
            // Intentar sincronizar si hay conexión
            if (connectivityMonitor.isNetworkAvailable()) {
                when (val result = safeApiCall { apiService.updateProduct(product.id, savedProduct) }) {
                    is NetworkResult.Success -> {
                        // Actualizar el producto con los datos del servidor
                        val serverProduct = result.data
                        productDao.insert(serverProduct.withSyncStatus(Product.SyncStatus.SYNCED))
                        
                        return@withContext ResourceState.Success(serverProduct)
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Error al sincronizar producto actualizado: ${result.message}")
                        return@withContext ResourceState.Success(savedProduct, isFromCache = true)
                    }
                    is NetworkResult.Loading -> {
                        return@withContext ResourceState.Success(savedProduct, isFromCache = true)
                    }
                }
            } else {
                // Sin conexión, devolver éxito pero con datos locales
                return@withContext ResourceState.Success(savedProduct, isFromCache = true)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar producto", e)
            ResourceState.Error(e.message ?: "Error desconocido")
        }
    }
    
    /**
     * Actualiza el stock de un producto.
     *
     * @param productId ID del producto.
     * @param newStock Nuevo nivel de stock.
     * @return Flujo de ResourceState con el producto actualizado.
     */
    suspend fun updateStock(productId: Int, newStock: Int): ResourceState<Product> = withContext(Dispatchers.IO) {
        try {
            // Actualizar el stock en la base de datos local
            productDao.updateStock(productId, newStock)
            val updatedProduct = productDao.getProductById(productId)
                ?: return@withContext ResourceState.Error("Producto no encontrado")
            
            // Intentar sincronizar si hay conexión
            if (connectivityMonitor.isNetworkAvailable()) {
                when (val result = safeApiCall { apiService.updateProduct(productId, updatedProduct) }) {
                    is NetworkResult.Success -> {
                        // Actualizar el producto con los datos del servidor
                        val serverProduct = result.data
                        productDao.insert(serverProduct.withSyncStatus(Product.SyncStatus.SYNCED))
                        
                        return@withContext ResourceState.Success(serverProduct)
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Error al sincronizar actualización de stock: ${result.message}")
                        return@withContext ResourceState.Success(updatedProduct, isFromCache = true)
                    }
                    is NetworkResult.Loading -> {
                        return@withContext ResourceState.Success(updatedProduct, isFromCache = true)
                    }
                }
            } else {
                // Sin conexión, devolver éxito pero con datos locales
                return@withContext ResourceState.Success(updatedProduct, isFromCache = true)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar stock", e)
            ResourceState.Error(e.message ?: "Error desconocido")
        }
    }
    
    /**
     * Elimina un producto.
     *
     * @param productId ID del producto a eliminar.
     * @return Flujo de ResourceState con el resultado de la operación.
     */
    suspend fun deleteProduct(productId: Int): ResourceState<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Marcar el producto como eliminado localmente
            productDao.markAsDeleted(productId)
            
            // Intentar sincronizar si hay conexión
            if (connectivityMonitor.isNetworkAvailable()) {
                when (val result = safeApiCall { apiService.deleteProduct(productId) }) {
                    is NetworkResult.Success -> {
                        // El producto se eliminó correctamente en el servidor, eliminarlo físicamente
                        val product = productDao.getProductById(productId)
                        if (product != null) {
                            productDao.updateSyncStatus(productId, Product.SyncStatus.SYNCED)
                            productDao.deleteMarkedProducts()
                        }
                        
                        return@withContext ResourceState.Success(true)
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Error al sincronizar eliminación de producto: ${result.message}")
                        return@withContext ResourceState.Success(true, isFromCache = true)
                    }
                    is NetworkResult.Loading -> {
                        return@withContext ResourceState.Success(true, isFromCache = true)
                    }
                }
            } else {
                // Sin conexión, devolver éxito pero con datos locales
                return@withContext ResourceState.Success(true, isFromCache = true)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar producto", e)
            ResourceState.Error(e.message ?: "Error desconocido")
        }
    }
    
    /**
     * Sincroniza todos los productos pendientes con el servidor.
     *
     * @param lastSyncTime Marca de tiempo de la última sincronización.
     * @return Resultado de la sincronización.
     */
    suspend fun syncWithServer(lastSyncTime: Long): SyncResult = executeSyncOperation {
        val companyId = sessionManager.getCurrentCompanyId()
        
        // 1. Obtener todos los productos pendientes de sincronización
        val pendingProducts = productDao.getPendingSyncProducts()
        
        // 2. Preparar los datos para la sincronización
        val syncData = HashMap<String, Any>()
        
        // 2.1. Añadir productos a crear/actualizar
        val productsToUpload = pendingProducts.filter { 
            it.syncStatus == Product.SyncStatus.PENDING_UPLOAD || 
            it.syncStatus == Product.SyncStatus.PENDING_UPDATE 
        }
        syncData["products"] = productsToUpload
        
        // 2.2. Añadir productos a eliminar
        val productsToDelete = pendingProducts.filter { 
            it.syncStatus == Product.SyncStatus.PENDING_DELETE 
        }.map { it.id }
        syncData["deleted_ids"] = productsToDelete
        
        // 2.3. Añadir última vez sincronizado para recibir actualizaciones del servidor
        syncData["last_sync"] = lastSyncTime
        
        // 2.4. Añadir ID de empresa
        syncData["company_id"] = companyId
        
        // 3. Realizar la sincronización con el servidor
        val result = safeApiCall {
            apiService.syncProducts(syncData)
        }
        
        when (result) {
            is NetworkResult.Success -> {
                // 4. Procesar la respuesta del servidor
                val response = result.data.data ?: throw Exception("Respuesta vacía del servidor")
                
                // 4.1. Guardar productos añadidos y actualizados
                val productsToSave = mutableListOf<Product>()
                
                response.added.forEach { product ->
                    productsToSave.add(product.withSyncStatus(Product.SyncStatus.SYNCED))
                }
                
                response.updated.forEach { product ->
                    productsToSave.add(product.withSyncStatus(Product.SyncStatus.SYNCED))
                }
                
                if (productsToSave.isNotEmpty()) {
                    productDao.insertAll(productsToSave)
                }
                
                // 4.2. Marcar como sincronizados los productos que enviamos
                val syncedIds = productsToUpload.map { it.id }
                if (syncedIds.isNotEmpty()) {
                    productDao.markAsSynced(syncedIds)
                }
                
                // 4.3. Procesar eliminaciones
                response.deleted.forEach { productId ->
                    productDao.markAsDeleted(productId)
                }
                
                // 4.4. Eliminar físicamente los productos marcados como eliminados y ya sincronizados
                productDao.deleteMarkedProducts()
                
                SyncResult.Success(
                    addedCount = response.added.size,
                    updatedCount = response.updated.size,
                    deletedCount = response.deleted.size
                )
            }
            is NetworkResult.Error -> {
                throw Exception(result.message)
            }
            is NetworkResult.Loading -> {
                throw Exception("Estado de carga inesperado")
            }
        }
    }
    
    /**
     * Obtiene el número de productos pendientes de sincronización.
     *
     * @return Número de productos pendientes de sincronización.
     */
    suspend fun getPendingSyncCount(): Int = withContext(Dispatchers.IO) {
        productDao.getPendingSyncProductsCount()
    }
}