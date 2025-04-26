package com.productiva.android.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.productiva.android.ProductivaApplication
import com.productiva.android.model.Product
import com.productiva.android.network.RetrofitClient
import com.productiva.android.repository.ProductRepository
import com.productiva.android.repository.ResourceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de productos.
 */
class ProductViewModel(application: Application) : AndroidViewModel(application) {
    
    private val TAG = "ProductViewModel"
    
    // Repositorio de productos
    private val productRepository: ProductRepository
    
    // Estado del producto, visible externamente
    private val _productState = MutableStateFlow<ProductState>(ProductState.Idle)
    val productState = _productState.asStateFlow()
    
    // Todos los productos
    val allProducts: LiveData<List<Product>>
    
    // Productos con stock bajo
    val lowStockProducts: LiveData<List<Product>>
    
    // Productos sin stock
    val outOfStockProducts: LiveData<List<Product>>
    
    // Contador de cambios pendientes
    val pendingSyncCount: LiveData<Int>
    
    init {
        val app = getApplication<ProductivaApplication>()
        val apiService = RetrofitClient.getApiService(app)
        val database = app.database
        
        // Inicializar el repositorio
        productRepository = ProductRepository(database.productDao(), apiService)
        
        // Inicializar observadores
        allProducts = productRepository.getAllProducts().asLiveData()
        lowStockProducts = database.productDao().getLowStockProducts().asLiveData()
        outOfStockProducts = database.productDao().getOutOfStockProducts().asLiveData()
        pendingSyncCount = database.productDao().countProductsForSync().asLiveData()
    }
    
    /**
     * Sincroniza productos con el servidor.
     */
    fun syncProducts() {
        _productState.value = ProductState.Loading
        
        viewModelScope.launch {
            try {
                productRepository.syncProducts().collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            _productState.value = ProductState.Synced(state.data ?: emptyList())
                        }
                        is ResourceState.Error -> {
                            _productState.value = ProductState.Error(state.message ?: "Error desconocido")
                        }
                        is ResourceState.Loading -> {
                            _productState.value = ProductState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al sincronizar productos", e)
                _productState.value = ProductState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Sincroniza cambios pendientes de productos con el servidor.
     */
    fun syncPendingChanges() {
        viewModelScope.launch {
            try {
                productRepository.syncPendingProductChanges().collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            val count = state.data ?: 0
                            Log.d(TAG, "Cambios pendientes sincronizados: $count")
                        }
                        is ResourceState.Error -> {
                            Log.e(TAG, "Error al sincronizar cambios pendientes: ${state.message}")
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al sincronizar cambios pendientes", e)
            }
        }
    }
    
    /**
     * Busca productos por texto.
     */
    fun searchProducts(query: String): LiveData<List<Product>> {
        return productRepository.searchProducts(query).asLiveData()
    }
    
    /**
     * Obtiene productos por categoría.
     */
    fun getProductsByCategory(categoryId: Int): LiveData<List<Product>> {
        return productRepository.getProductsByCategory(categoryId).asLiveData()
    }
    
    /**
     * Actualiza el stock de un producto.
     */
    fun updateProductStock(productId: Int, newStock: Int) {
        _productState.value = ProductState.Loading
        
        viewModelScope.launch {
            try {
                productRepository.updateProductStock(productId, newStock).collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            val product = state.data
                            if (product != null) {
                                _productState.value = ProductState.Updated(product)
                            } else {
                                _productState.value = ProductState.Error("No se pudo actualizar el producto")
                            }
                        }
                        is ResourceState.Error -> {
                            _productState.value = ProductState.Error(state.message ?: "Error desconocido")
                        }
                        is ResourceState.Loading -> {
                            _productState.value = ProductState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al actualizar stock", e)
                _productState.value = ProductState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Obtiene un producto por su ID.
     */
    fun getProductById(productId: Int): LiveData<Product?> {
        return productRepository.getProductById(productId).asLiveData()
    }
    
    /**
     * Estados posibles de productos.
     */
    sealed class ProductState {
        object Idle : ProductState()
        object Loading : ProductState()
        data class Synced(val products: List<Product>) : ProductState()
        data class Updated(val product: Product) : ProductState()
        data class Error(val message: String) : ProductState()
    }
}