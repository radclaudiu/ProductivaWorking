package com.productiva.android.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.productiva.android.utils.ApiConnectionChecker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect

/**
 * Componente wrapper que muestra estado de conexión y mensaje apropiado para trabajar offline.
 * Este componente gestiona automáticamente la visualización de banners de estado de conexión.
 */
@Composable
fun NetworkAwareWrapper(
    connectionState: ApiConnectionChecker.ConnectionResult,
    isCheckingConnection: Boolean = false,
    showOfflineBanner: Boolean = true,
    showConnectionStatusBar: Boolean = true,
    content: @Composable () -> Unit
) {
    // Determinar si estamos offline basado en el estado de conexión
    val isOffline = when (connectionState) {
        is ApiConnectionChecker.ConnectionResult.NoInternet,
        is ApiConnectionChecker.ConnectionResult.ServerUnavailable,
        is ApiConnectionChecker.ConnectionResult.Timeout -> true
        else -> false
    }
    
    // Determinar si el servidor está caído específicamente
    val isServerDown = connectionState is ApiConnectionChecker.ConnectionResult.ServerUnavailable
    
    // UI principal con banners de estado
    Column {
        // Barra de estado de conexión (si está activada)
        if (showConnectionStatusBar) {
            ConnectionStatusBar(
                connectionStatus = connectionState,
                visible = connectionState !is ApiConnectionChecker.ConnectionResult.Connected
            )
        }
        
        // Banner de modo offline (si está activado)
        if (showOfflineBanner) {
            OfflineBanner(
                isOffline = isOffline,
                isServerUnavailable = isServerDown
            )
        }
        
        // Contenido principal
        Box(modifier = Modifier.fillMaxSize()) {
            if (isCheckingConnection) {
                // Mostrar indicador de carga mientras se verifica la conexión
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                // Mostrar el contenido principal
                content()
            }
        }
    }
}

/**
 * Versión del componente NetworkAwareWrapper que verifica la conexión automáticamente.
 * Realiza una verificación de estado de conexión y muestra indicadores apropiados.
 */
@Composable
fun AutoCheckNetworkAwareWrapper(
    connectionChecker: ApiConnectionChecker,
    checkOnAppear: Boolean = true,
    showOfflineBanner: Boolean = true,
    showConnectionStatusBar: Boolean = true,
    checkInterval: Long = 0, // 0 = sin verificación periódica
    content: @Composable () -> Unit
) {
    var connectionState by remember { 
        mutableStateOf<ApiConnectionChecker.ConnectionResult>(
            ApiConnectionChecker.ConnectionResult.Checking
        ) 
    }
    var isCheckingConnection by remember { mutableStateOf(checkOnAppear) }
    
    // Efecto para verificar la conexión al aparecer
    LaunchedEffect(checkOnAppear) {
        if (checkOnAppear) {
            isCheckingConnection = true
            connectionChecker.checkApiConnection().collect { result ->
                connectionState = result
                isCheckingConnection = false
            }
        }
    }
    
    // Efecto para verificación periódica (si está habilitada)
    if (checkInterval > 0) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(checkInterval)
                
                // No iniciar nueva verificación si ya hay una en curso
                if (!isCheckingConnection) {
                    isCheckingConnection = true
                    connectionChecker.checkApiConnection().collect { result ->
                        connectionState = result
                        isCheckingConnection = false
                    }
                }
            }
        }
    }
    
    // Renderizar el wrapper con el estado actual
    NetworkAwareWrapper(
        connectionState = connectionState,
        isCheckingConnection = isCheckingConnection,
        showOfflineBanner = showOfflineBanner,
        showConnectionStatusBar = showConnectionStatusBar,
        content = content
    )
}