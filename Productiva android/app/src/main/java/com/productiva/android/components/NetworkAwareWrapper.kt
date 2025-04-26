package com.productiva.android.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.productiva.android.ui.theme.ProductivaRed
import com.productiva.android.utils.ConnectionState
import kotlinx.coroutines.delay

/**
 * Componente que envuelve el contenido y muestra una barra de estado de conexión
 * cuando el dispositivo está offline o cuando está verificando la conexión.
 */
@Composable
fun NetworkAwareWrapper(
    connectionState: ConnectionState,
    isCheckingConnection: Boolean = false,
    content: @Composable () -> Unit
) {
    // Estado de la barra de offline
    var showOfflineBanner by remember { mutableStateOf(false) }
    
    // Ocultar la barra después de un tiempo cuando se reconecta
    LaunchedEffect(connectionState.isConnected) {
        if (!connectionState.isConnected) {
            showOfflineBanner = true
        } else if (showOfflineBanner) {
            // Esperar un momento antes de ocultar la barra
            delay(3000)
            showOfflineBanner = false
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Barra de estado de conexión
        AnimatedVisibility(
            visible = !connectionState.isConnected || showOfflineBanner,
            enter = slideInVertically() + expandVertically() + fadeIn(),
            exit = slideOutVertically() + shrinkVertically() + fadeOut()
        ) {
            ConnectionStatusBar(connectionState = connectionState)
        }
        
        // Barra de verificación de conexión
        AnimatedVisibility(
            visible = isCheckingConnection,
            enter = slideInVertically() + expandVertically() + fadeIn(),
            exit = slideOutVertically() + shrinkVertically() + fadeOut()
        ) {
            SyncStatusIndicator()
        }
        
        // Contenido principal
        Box(modifier = Modifier.weight(1f)) {
            content()
            
            // Overlay cuando está offline
            if (!connectionState.isConnected) {
                OfflineBanner()
            }
        }
    }
}

/**
 * Barra que muestra el estado de la conexión.
 */
@Composable
fun ConnectionStatusBar(connectionState: ConnectionState) {
    val backgroundColor = if (connectionState.isConnected) {
        MaterialTheme.colorScheme.primary
    } else {
        ProductivaRed
    }
    
    val message = if (connectionState.isConnected) {
        "Conectado - ${connectionState.connectionType.name}"
    } else {
        "Sin conexión - Trabajando en modo offline"
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = backgroundColor,
        shadowElevation = 4.dp
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Banner translúcido que se muestra sobre el contenido cuando está offline.
 */
@Composable
fun OfflineBanner() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 4.dp
        ) {
            Text(
                text = "No hay conexión a Internet\nLos cambios se sincronizarán cuando vuelva la conexión",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Indicador de estado de sincronización.
 */
@Composable
fun SyncStatusIndicator() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiary,
        shadowElevation = 4.dp
    ) {
        Text(
            text = "Sincronizando datos...",
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onTertiary,
            textAlign = TextAlign.Center
        )
    }
}