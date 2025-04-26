package com.productiva.android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.productiva.android.utils.ConnectivityMonitor

/**
 * Componente contenedor que envuelve el contenido para mostrar indicadores
 * de estado de red y adaptarse a cambios de conectividad.
 *
 * @param content Contenido principal a mostrar.
 * @param offlineIndicator Indicador a mostrar cuando no hay conexión.
 * @param loadingIndicator Indicador a mostrar durante la carga (opcional).
 * @param errorView Vista a mostrar en caso de error (opcional).
 * @param isLoading Estado de carga.
 * @param error Mensaje de error si hay alguno.
 * @param onRetry Acción a realizar al intentar de nuevo (opcional).
 */
@Composable
fun NetworkAwareWrapper(
    content: @Composable () -> Unit,
    offlineIndicator: @Composable () -> Unit = { OfflineIndicator() },
    loadingIndicator: @Composable (() -> Unit)? = null,
    errorView: @Composable ((error: String, onRetry: () -> Unit) -> Unit)? = { error, onRetry ->
        ErrorView(
            error = error,
            onRetry = onRetry
        )
    },
    isLoading: Boolean = false,
    error: String? = null,
    onRetry: () -> Unit = {}
) {
    val context = LocalContext.current
    val connectivityMonitor = ConnectivityMonitor.getInstance(context)
    val isOnline by connectivityMonitor.networkAvailable.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Contenido principal
        content()
        
        // Indicador de estado sin conexión
        AnimatedVisibility(
            visible = !isOnline,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            enter = slideInVertically { -it } + expandVertically() + fadeIn(),
            exit = slideOutVertically { -it } + shrinkVertically() + fadeOut()
        ) {
            offlineIndicator()
        }
        
        // Indicador de carga
        if (loadingIndicator != null && isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                loadingIndicator()
            }
        }
        
        // Vista de error
        if (errorView != null && !error.isNullOrBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                errorView(error, onRetry)
            }
        }
    }
}

/**
 * Indicador que se muestra cuando no hay conexión a Internet.
 */
@Composable
fun OfflineIndicator() {
    Surface(
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Text(
                text = "Sin conexión a Internet",
                color = MaterialTheme.colorScheme.onError,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Vista de error que se muestra cuando hay algún problema.
 *
 * @param error Mensaje de error a mostrar.
 * @param onRetry Acción a realizar al pulsar el botón de reintentar.
 */
@Composable
fun ErrorView(
    error: String,
    onRetry: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Text(
                text = "Error",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            androidx.compose.material3.Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
            
            androidx.compose.material3.Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                androidx.compose.material3.Text("Reintentar")
            }
        }
    }
}