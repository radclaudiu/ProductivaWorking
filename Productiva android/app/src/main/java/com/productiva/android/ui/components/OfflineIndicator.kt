package com.productiva.android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.runtime.Composable
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
 * Componente que muestra un indicador cuando la aplicación está trabajando en modo sin conexión.
 * Se muestra en la parte superior de la pantalla y se anima al aparecer/desaparecer.
 */
@Composable
fun OfflineIndicator() {
    val context = LocalContext.current
    val connectivityMonitor = remember { ConnectivityMonitor.getInstance(context) }
    
    // Observar el estado de la conexión
    val isNetworkAvailable by connectivityMonitor.isNetworkAvailableFlow.collectAsState(initial = true)
    
    // Estado para la animación de aparición/desaparición
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
    
    AnimatedVisibility(
        visible = showOfflineBanner,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it })
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "Sin conexión",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Trabajando sin conexión. Los cambios se sincronizarán cuando vuelva la conexión.",
                    color = Color.White,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}