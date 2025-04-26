package com.productiva.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.productiva.android.components.NetworkAwareWrapper
import com.productiva.android.model.Product
import com.productiva.android.ui.theme.Error
import com.productiva.android.ui.theme.ProductivaBlue
import com.productiva.android.ui.theme.ProductivaGreen
import com.productiva.android.ui.theme.ProductivaOrange
import com.productiva.android.ui.theme.Warning
import com.productiva.android.utils.ApiConnectionChecker
import com.productiva.android.viewmodel.ProductViewModel
import kotlinx.coroutines.flow.collect

/**
 * Pantalla principal de listado de productos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    productViewModel: ProductViewModel,
    apiConnectionChecker: ApiConnectionChecker,
    onBackClick: () -> Unit,
    onProductClick: (Product) -> Unit
) {
    val products by productViewModel.allProducts.collectAsState(initial = emptyList())
    val productState by productViewModel.productState.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    
    // Mostrar errores como snackbar
    LaunchedEffect(productState) {
        if (productState is ProductViewModel.ProductState.Error) {
            val errorState = productState as ProductViewModel.ProductState.Error
            snackbarHostState.showSnackbar(errorState.message)
        }
    }
    
    // Estructúra principal con scaffold
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productos") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { productViewModel.syncProducts() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sincronizar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            // En una implementación real, aquí se podría agregar un FAB para crear un nuevo producto
        }
    ) { paddingValues ->
        NetworkAwareWrapper(
            connectionState = apiConnectionChecker.getCurrentConnectionState(),
            isCheckingConnection = productState is ProductViewModel.ProductState.Loading
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Barra de búsqueda
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { isSearchActive = false },
                    active = isSearchActive,
                    onActiveChange = { isSearchActive = it },
                    placeholder = { Text("Buscar productos...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // No hay contenido en la barra de búsqueda expandida
                }
                
                // Lista de productos
                when {
                    productState is ProductViewModel.ProductState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    products.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "No hay productos disponibles",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                IconButton(onClick = { productViewModel.syncProducts() }) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Sincronizar",
                                        tint = ProductivaBlue
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        // Filtrar productos por búsqueda
                        val filteredProducts = if (searchQuery.isNotEmpty()) {
                            products.filter {
                                it.name.contains(searchQuery, ignoreCase = true) ||
                                (it.sku?.contains(searchQuery, ignoreCase = true) == true) ||
                                (it.barcode?.contains(searchQuery, ignoreCase = true) == true)
                            }
                        } else {
                            products
                        }
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = paddingValues
                        ) {
                            items(filteredProducts) { product ->
                                ProductItem(
                                    product = product,
                                    onClick = { onProductClick(product) },
                                    onStockChange = { newStock ->
                                        productViewModel.updateProductStock(product.id, newStock)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Item individual para mostrar un producto en la lista.
 */
@Composable
fun ProductItem(
    product: Product,
    onClick: () -> Unit,
    onStockChange: (Int) -> Unit
) {
    var currentStock by remember { mutableIntStateOf(product.stock) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (product.imageUrl != null) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = product.name.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del producto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Precio
                    Text(
                        text = product.getFormattedPrice(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Indicador de stock
                    val stockStatus = product.getStockStatus()
                    val stockColor = when (stockStatus) {
                        Product.StockStatus.IN_STOCK -> ProductivaGreen
                        Product.StockStatus.LOW_STOCK -> Warning
                        Product.StockStatus.OUT_OF_STOCK -> Error
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(stockColor)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = when (stockStatus) {
                            Product.StockStatus.IN_STOCK -> "En stock (${product.stock})"
                            Product.StockStatus.LOW_STOCK -> "Stock bajo (${product.stock})"
                            Product.StockStatus.OUT_OF_STOCK -> "Sin stock"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = stockColor
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = product.sku ?: "Sin SKU",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Controles de stock
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = {
                        currentStock = (currentStock + 1).coerceAtLeast(0)
                        onStockChange(currentStock)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Aumentar stock",
                        tint = ProductivaGreen
                    )
                }
                
                Text(
                    text = currentStock.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = {
                        if (currentStock > 0) {
                            currentStock = (currentStock - 1).coerceAtLeast(0)
                            onStockChange(currentStock)
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Disminuir stock",
                        tint = if (currentStock > 0) Error else Color.Gray
                    )
                }
            }
        }
    }
}