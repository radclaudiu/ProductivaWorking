package com.productiva.android.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.productiva.android.ProductivaApplication
import com.productiva.android.model.Product
import com.productiva.android.network.RetrofitClient
import com.productiva.android.repository.ProductRepository
import com.productiva.android.repository.ResourceState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de productos.
 */
class ProductViewModel(application: Application) : AndroidViewModel(application) {
    
    private val TAG = "ProductViewModel"
    
    // Repositorio de productos
    private val productRepository: ProductRepository
    
    // Estado de productos
    private val _productState = MutableStateFlow<ProductState>(ProductState.Idle)
    val productState: StateFlow<ProductState> = _productState
    
    // Filtros
    private val _categoryFilter = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow<String?>(null)
    private val _companyFilter = MutableStateFlow<Int?>(null)
    private val _locationFilter = MutableStateFlow<Int?>(null)
    
    // Producto seleccionado
    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct
    
    // Lista de productos filtrados
    @OptIn(ExperimentalCoroutinesApi::class)
    val products: Flow<List<Product>> = combine(
        _searchQuery,
        _categoryFilter,
        _companyFilter,
        _locationFilter
    ) { search, category, company, location ->
        ProductFilter(search, category, company, location)
    }.flatMapLatest { filter ->
        when {
            filter.search != null -> productRepository.searchProducts(filter.search)
            filter.category != null -> productRepository.getProductsByCategory(filter.category)
            filter.company != null -> productRepository.getProductsByCompany(filter.company)
            filter.location != null -> productRepository.getProductsByLocation(filter.location)
            else -> productRepository.getAllProducts()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Lista de productos con stock bajo
    val lowStockProducts = productRepository.getLowStockProducts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    init {
        val app = getApplication<ProductivaApplication>()
        val apiService = RetrofitClient.getApiService(app)
        
        // Inicializar el repositorio
        productRepository = ProductRepository(app.database.productDao(), apiService)
    }
    
    /**
     * Carga productos de una empresa específica.
     */
    fun loadProductsByCompany(companyId: Int) {
        _productState.value = ProductState.Loading
        _companyFilter.value = companyId
        
        viewModelScope.launch {
            try {
                // Sincronizar productos con el servidor
                productRepository.syncProducts(companyId = companyId).collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            _productState.value = ProductState.Synced(state.data)
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
                Log.e(TAG, "Error al cargar productos por empresa", e)
                _productState.value = ProductState.Error("Error al cargar productos: ${e.message}")
            }
        }
    }
    
    /**
     * Carga productos de una ubicación específica.
     */
    fun loadProductsByLocation(locationId: Int) {
        _productState.value = ProductState.Loading
        _locationFilter.value = locationId
        
        viewModelScope.launch {
            try {
                // Sincronizar productos con el servidor
                productRepository.syncProducts(locationId = locationId).collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            _productState.value = ProductState.Synced(state.data)
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
                Log.e(TAG, "Error al cargar productos por ubicación", e)
                _productState.value = ProductState.Error("Error al cargar productos: ${e.message}")
            }
        }
    }
    
    /**
     * Busca productos por nombre, código o código de barras.
     */
    fun searchProducts(query: String) {
        if (query.isBlank()) {
            clearSearch()
            return
        }
        
        _searchQuery.value = query
    }
    
    /**
     * Limpia la búsqueda actual.
     */
    fun clearSearch() {
        _searchQuery.value = null
    }
    
    /**
     * Filtra productos por categoría.
     */
    fun filterByCategory(category: String) {
        _categoryFilter.value = category
    }
    
    /**
     * Limpia todos los filtros.
     */
    fun clearFilter() {
        _categoryFilter.value = null
    }
    
    /**
     * Sincroniza productos con el servidor.
     */
    fun syncProducts(locationId: Int? = null, companyId: Int? = null) {
        _productState.value = ProductState.Loading
        
        viewModelScope.launch {
            try {
                productRepository.syncProducts(locationId, companyId).collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            _productState.value = ProductState.Synced(state.data)
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
                _productState.value = ProductState.Error("Error al sincronizar productos: ${e.message}")
            }
        }
    }
    
    /**
     * Obtiene un producto específico por su ID.
     */
    fun getProduct(productId: Int) {
        _productState.value = ProductState.Loading
        
        viewModelScope.launch {
            try {
                productRepository.syncProduct(productId).collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            _selectedProduct.value = state.data
                            _productState.value = ProductState.ProductLoaded(state.data)
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
                Log.e(TAG, "Error al obtener producto", e)
                _productState.value = ProductState.Error("Error al obtener producto: ${e.message}")
            }
        }
    }
    
    /**
     * Actualiza un producto.
     */
    fun updateProduct(product: Product) {
        _productState.value = ProductState.Loading
        
        viewModelScope.launch {
            try {
                productRepository.updateProduct(product).collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            _selectedProduct.value = state.data
                            _productState.value = ProductState.ProductUpdated(state.data)
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
                Log.e(TAG, "Error al actualizar producto", e)
                _productState.value = ProductState.Error("Error al actualizar producto: ${e.message}")
            }
        }
    }
    
    /**
     * Selecciona un producto para verlo en detalle.
     */
    fun selectProduct(product: Product) {
        _selectedProduct.value = product
    }
    
    /**
     * Clases para estados y filtros de productos.
     */
    sealed class ProductState {
        object Idle : ProductState()
        object Loading : ProductState()
        data class Synced(val products: List<Product>) : ProductState()
        data class ProductLoaded(val product: Product) : ProductState()
        data class ProductUpdated(val product: Product) : ProductState()
        data class Error(val message: String) : ProductState()
    }
    
    data class ProductFilter(
        val search: String? = null,
        val category: String? = null,
        val company: Int? = null,
        val location: Int? = null
    )
}