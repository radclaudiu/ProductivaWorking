package com.productiva.android.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.productiva.android.utils.ConnectivityMonitor
import kotlinx.coroutines.delay

/**
 * Componente que muestra un contenido diferente según el estado de la conexión.
 * Muestra una barra superior cuando está trabajando sin conexión.
 *
 * @param content Contenido principal a mostrar.
 * @param offlineContent Contenido opcional a mostrar cuando no hay conexión.
 * Si es null, se mostrará el contenido principal con una barra de aviso.
 */
@Composable
fun NetworkAwareWrapper(
    content: @Composable () -> Unit,
    offlineContent: (@Composable () -> Unit)? = null,
    syncStatusContent: (@Composable () -> Unit)? = null
) {
    val context = LocalContext.current
    val connectivityMonitor = remember { ConnectivityMonitor.getInstance(context) }
    
    // Observar el estado de la conexión
    val isNetworkAvailable by connectivityMonitor.isNetworkAvailableFlow.collectAsState(initial = true)
    
    // Estado para la animación de aparición/desaparición del banner
    var showOfflineBanner by remember { mutableStateOf(false) }
    
    // Mostrar el banner con un pequeño retraso cuando se pierde la conexión
    // y ocultarlo inmediatamente cuando se recupera
    LaunchedEffect(isNetworkAvailable) {
        if (!isNetworkAvailable) {
            delay(300) // Pequeño retraso para evitar parpadeos en reconexiones rápidas
            showOfflineBanner = true
        } else {
            showOfflineBanner = false
        }
    }
    
    // Contenedor principal
    Column {
        // Banner de modo sin conexión
        AnimatedVisibility(
            visible = showOfflineBanner,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            OfflineBanner()
        }
        
        // Contenido de estado de sincronización, si se proporciona
        syncStatusContent?.invoke()
        
        // Contenido principal o alternativo según el estado de conexión
        if (offlineContent != null && !isNetworkAvailable) {
            offlineContent()
        } else {
            content()
        }
    }
    
    // Registrar/desregistrar listeners cuando el composable entra/sale de la composición
    DisposableEffect(context) {
        // No es necesario registrar/desregistrar aquí, ya que ConnectivityMonitor
        // se inicializa al nivel de aplicación y maneja su ciclo de vida
        
        onDispose {
            // Limpieza si fuera necesaria
        }
    }
}

/**
 * Banner que se muestra cuando la aplicación está trabajando sin conexión.
 */
@Composable
private fun OfflineBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.error,
        elevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF44336))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "Sin conexión",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Trabajando sin conexión",
                    color = Color.White,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Componente que muestra un banner con el estado de sincronización.
 */
@Composable
fun SyncStatusBanner(
    isSyncing: Boolean,
    lastSyncTime: Long? = null
) {
    AnimatedVisibility(
        visible = isSyncing,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colors.primary,
            elevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2196F3))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sincronizando",
                        tint = Color.White,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Sincronizando datos...",
                        color = Color.White,
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}