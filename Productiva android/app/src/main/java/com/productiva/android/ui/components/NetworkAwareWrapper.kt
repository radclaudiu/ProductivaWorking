package com.productiva.android.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.productiva.android.repository.ResourceState
import com.productiva.android.utils.ConnectivityMonitor
import kotlinx.coroutines.delay

/**
 * Componente que adapta el contenido de la UI al estado de la red y de los datos.
 * Muestra indicadores de carga, error o sin conexión según sea necesario.
 *
 * @param T Tipo de datos a mostrar
 * @param resourceState Estado del recurso (cargando, éxito, error)
 * @param onRefresh Acción a ejecutar para recargar los datos
 * @param content Contenido a mostrar cuando los datos están disponibles
 */
@Composable
fun <T> NetworkAwareWrapper(
    resourceState: ResourceState<T>,
    onRefresh: () -> Unit,
    canShowOfflineContent: Boolean = true,
    content: @Composable (T) -> Unit
) {
    val context = LocalContext.current
    val connectivityMonitor = remember { ConnectivityMonitor.getInstance(context) }
    
    // Observar el estado de la conexión
    val isNetworkAvailable by connectivityMonitor.isNetworkAvailableFlow.collectAsState(initial = true)
    
    // Variable para controlar si mostrar contenido en modo sin conexión
    var shouldShowOfflineContent by remember { mutableStateOf(false) }
    
    // Determinar si se debe mostrar contenido offline después de un tiempo sin conexión
    LaunchedEffect(isNetworkAvailable) {
        if (!isNetworkAvailable) {
            delay(2000) // Esperar 2 segundos sin conexión antes de mostrar contenido offline
            shouldShowOfflineContent = true
        } else {
            shouldShowOfflineContent = false
            // Si recuperamos la conexión, intentar sincronizar
            onRefresh()
        }
    }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Mostrar banner de modo sin conexión si es necesario
        OfflineIndicator()
        
        // Gestionar diferentes estados del recurso
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (resourceState) {
                is ResourceState.Loading -> {
                    if (resourceState.data != null && shouldShowOfflineContent && canShowOfflineContent) {
                        // Mostrar datos almacenados en caché mientras se carga, si están disponibles
                        content(resourceState.data)
                    } else {
                        // Mostrar indicador de carga
                        CircularProgressIndicator()
                    }
                }
                
                is ResourceState.Success -> {
                    // Mostrar contenido exitoso
                    content(resourceState.data)
                }
                
                is ResourceState.Error -> {
                    if (resourceState.data != null && (shouldShowOfflineContent || !isNetworkAvailable) && canShowOfflineContent) {
                        // Mostrar datos almacenados en caché en caso de error, si están disponibles
                        content(resourceState.data)
                    } else {
                        // Mostrar componente de error con opción de reintentar
                        ErrorView(
                            message = resourceState.message ?: "Error desconocido",
                            onRetry = onRefresh
                        )
                    }
                }
            }
        }
    }
}