package com.productiva.android.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.productiva.android.utils.ConnectionState

/**
 * Componente que muestra una interfaz adaptada al estado de la conexión a Internet.
 * Muestra un banner de advertencia cuando no hay conexión y el contenido normal cuando hay conexión.
 * También puede mostrar un indicador de progreso mientras se comprueba la conexión.
 */
@Composable
fun NetworkAwareWrapper(
    connectionState: ConnectionState,
    isCheckingConnection: Boolean = false,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Banner de estado de conexión
        AnimatedVisibility(
            visible = connectionState is ConnectionState.Disconnected,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            NetworkStatusBanner(connectionState)
        }
        
        // Contenido principal
        Box(modifier = Modifier.weight(1f)) {
            when {
                isCheckingConnection -> {
                    // Mostrando indicador de progreso mientras se comprueba la conexión
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Comprobando conexión...",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    // Mostrando contenido normal
                    content()
                }
            }
        }
    }
}

/**
 * Banner que muestra el estado de la conexión a Internet.
 */
@Composable
private fun NetworkStatusBanner(connectionState: ConnectionState) {
    val (backgroundColor, textColor, icon, message) = when (connectionState) {
        ConnectionState.Connected -> {
            // No se muestra cuando está conectado
            return
        }
        ConnectionState.Disconnected -> {
            Color(0xFFFFCC00) to Color.Black to Icons.Default.CloudOff to
                    "Sin conexión a Internet. Trabajando en modo offline."
        }
        ConnectionState.Checking -> {
            Color(0xFF64B5F6) to Color.White to Icons.Default.Sync to
                    "Comprobando conexión a Internet..."
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Estado de conexión",
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = message,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Componente que muestra un mensaje de advertencia cuando la conexión está offline
 * y el contenido normal cuando hay conexión.
 */
@Composable
fun OfflineWarning(
    connectionState: ConnectionState,
    message: String = "Esta funcionalidad requiere conexión a Internet",
    content: @Composable () -> Unit
) {
    when (connectionState) {
        ConnectionState.Connected -> {
            content()
        }
        else -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Advertencia",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}