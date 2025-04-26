package com.productiva.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalPrintshop
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.productiva.android.components.NetworkAwareWrapper
import com.productiva.android.model.Product
import com.productiva.android.ui.theme.Error
import com.productiva.android.ui.theme.ProductivaBlue
import com.productiva.android.ui.theme.ProductivaGreen
import com.productiva.android.utils.ApiConnectionChecker
import com.productiva.android.viewmodel.ProductViewModel

/**
 * Pantalla de listado y gestión de productos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    productViewModel: ProductViewModel,
    apiConnectionChecker: ApiConnectionChecker,
    companyId: Int,
    onBackClick: () -> Unit,
    onProductClick: (Product) -> Unit,
    onPrintProduct: (Product) -> Unit
) {
    val products by productViewModel.products.collectAsState(initial = emptyList())
    val productState by productViewModel.productState.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }
    var filterExpanded by remember { mutableStateOf(false) }
    
    // Categorías disponibles para filtrar (se obtendrán dinámicamente de los productos)
    val categories = remember(products) {
        products.mapNotNull { it.category }
            .distinct()
            .sorted()
            .toMutableList()
            .apply { add(0, "Todas") }
    }
    
    var selectedCategory by remember { mutableStateOf("Todas") }
    
    // Mostrar errores como snackbar
    LaunchedEffect(productState) {
        if (productState is ProductViewModel.ProductState.Error) {
            val errorState = productState as ProductViewModel.ProductState.Error
            snackbarHostState.showSnackbar(errorState.message)
        }
    }
    
    // Buscar productos cuando cambia la consulta
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            productViewModel.searchProducts(searchQuery)
        } else {
            productViewModel.clearSearch()
        }
    }
    
    // Filtrar por categoría
    LaunchedEffect(selectedCategory) {
        if (selectedCategory != "Todas") {
            productViewModel.filterByCategory(selectedCategory)
        } else {
            productViewModel.clearFilter()
        }
    }
    
    // Cargar productos de la empresa al inicio
    LaunchedEffect(Unit) {
        productViewModel.loadProductsByCompany(companyId)
    }
    
    // Estructura principal
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
                    IconButton(onClick = { filterExpanded = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
                    }
                    
                    DropdownMenu(
                        expanded = filterExpanded,
                        onDismissRequest = { filterExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    filterExpanded = false
                                }
                            )
                        }
                    }
                    
                    IconButton(onClick = { productViewModel.syncProducts(companyId = companyId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sincronizar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Buscar productos...") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Limpiar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )
                
                // Filtro activo (si hay)
                if (selectedCategory != "Todas") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Categoría: $selectedCategory",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ProductivaBlue
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        IconButton(
                            onClick = { selectedCategory = "Todas" },
                            modifier = Modifier.size(16.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Limpiar filtro",
                                tint = ProductivaBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
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
                                IconButton(onClick = { 
                                    searchQuery = ""
                                    selectedCategory = "Todas"
                                    productViewModel.syncProducts(companyId = companyId) 
                                }) {
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
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 160.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = paddingValues
                        ) {
                            items(products) { product ->
                                ProductItem(
                                    product = product,
                                    onClick = { onProductClick(product) },
                                    onPrintClick = { onPrintProduct(product) }
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
 * Item individual para mostrar un producto en la cuadrícula.
 */
@Composable
fun ProductItem(
    product: Product,
    onClick: () -> Unit,
    onPrintClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Imagen del producto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                if (product.imageUrl != null || product.localImagePath != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(product.localImagePath ?: product.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                } else {
                    // Imagen por defecto
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = product.name.take(2).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Indicador de código de barras
                if (!product.barcode.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.QrCode,
                            contentDescription = "Tiene código de barras",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // Indicador de stock bajo
                if (product.hasLowStock()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Error.copy(alpha = 0.8f))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Stock bajo",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // Botón de impresión
                IconButton(
                    onClick = onPrintClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(
                            color = ProductivaBlue.copy(alpha = 0.8f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.LocalPrintshop,
                        contentDescription = "Imprimir etiqueta",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Información del producto
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Nombre del producto
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Códigos
                if (!product.code.isNullOrEmpty()) {
                    Text(
                        text = "Cód: ${product.code}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Precio
                Text(
                    text = product.formattedPrice(),
                    style = MaterialTheme.typography.titleMedium,
                    color = ProductivaGreen,
                    fontWeight = FontWeight.Bold
                )
                
                // Stock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    product.stock?.let {
                        Text(
                            text = "Stock: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (product.hasLowStock()) Error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}