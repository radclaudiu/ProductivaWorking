package com.productiva.android.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.productiva.android.ui.theme.Error
import com.productiva.android.ui.theme.Success
import com.productiva.android.ui.theme.Warning
import com.productiva.android.utils.ApiConnectionChecker
import com.productiva.android.utils.ConnectivityMonitor

/**
 * Barra de estado que muestra información sobre la conexión de red y API.
 */
@Composable
fun ConnectionStatusBar(
    connectionStatus: ApiConnectionChecker.ConnectionResult,
    visible: Boolean = true
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        val (backgroundColor, textColor, icon, message) = when (connectionStatus) {
            is ApiConnectionChecker.ConnectionResult.Connected -> {
                Quadruple(
                    Success.copy(alpha = 0.9f),
                    Color.White,
                    Icons.Default.SignalWifi4Bar,
                    "Conectado al servidor"
                )
            }
            is ApiConnectionChecker.ConnectionResult.NoInternet -> {
                Quadruple(
                    Error.copy(alpha = 0.9f),
                    Color.White,
                    Icons.Default.SignalWifiOff,
                    "Sin conexión a Internet"
                )
            }
            is ApiConnectionChecker.ConnectionResult.ServerUnavailable -> {
                Quadruple(
                    Error.copy(alpha = 0.9f),
                    Color.White,
                    Icons.Default.CloudOff,
                    "Servidor no disponible"
                )
            }
            is ApiConnectionChecker.ConnectionResult.Timeout -> {
                Quadruple(
                    Error.copy(alpha = 0.9f),
                    Color.White,
                    Icons.Default.SyncProblem,
                    "Tiempo de espera agotado"
                )
            }
            is ApiConnectionChecker.ConnectionResult.AuthError -> {
                Quadruple(
                    Warning.copy(alpha = 0.9f),
                    Color.White,
                    Icons.Default.Error,
                    "Error de autenticación"
                )
            }
            is ApiConnectionChecker.ConnectionResult.ServerError -> {
                Quadruple(
                    Error.copy(alpha = 0.9f),
                    Color.White,
                    Icons.Default.Error,
                    "Error del servidor: ${connectionStatus.statusCode}"
                )
            }
            is ApiConnectionChecker.ConnectionResult.Error -> {
                Quadruple(
                    Error.copy(alpha = 0.9f),
                    Color.White,
                    Icons.Default.Error,
                    connectionStatus.message
                )
            }
            is ApiConnectionChecker.ConnectionResult.NotAuthenticated -> {
                Quadruple(
                    Warning.copy(alpha = 0.9f),
                    Color.White,
                    Icons.Default.Error,
                    "No autenticado"
                )
            }
            is ApiConnectionChecker.ConnectionResult.TokenExpired -> {
                Quadruple(
                    Warning.copy(alpha = 0.9f),
                    Color.White,
                    Icons.Default.Error,
                    "Sesión expirada"
                )
            }
            is ApiConnectionChecker.ConnectionResult.Checking -> {
                Quadruple(
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
                    Color.White,
                    Icons.Default.SyncProblem,
                    "Verificando conexión..."
                )
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Estado de conexión",
                    tint = textColor,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Clase auxiliar para retornar cuatro valores.
 */
private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)