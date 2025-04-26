package com.productiva.android.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.productiva.android.sync.SyncManager
import com.productiva.android.ui.theme.ProductivaBlue
import com.productiva.android.ui.theme.ProductivaGreen
import com.productiva.android.ui.theme.ProductivaRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Indicador del estado de sincronización que muestra cuando fue la última sincronización
 * y permite iniciar una sincronización manual.
 */
@Composable
fun SyncStatusIndicator(
    syncState: SyncManager.SyncState,
    lastSyncTime: Long,
    onSyncClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(8.dp)
    ) {
        // Texto de estado de sincronización
        Box(modifier = Modifier.weight(1f)) {
            when (syncState) {
                is SyncManager.SyncState.Syncing -> {
                    Text(
                        text = "Sincronizando...",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start
                    )
                }
                is SyncManager.SyncState.Success -> {
                    val formattedDate = formatDate(syncState.timestamp)
                    Text(
                        text = "Última sincronización: $formattedDate",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start
                    )
                }
                is SyncManager.SyncState.Error -> {
                    Text(
                        text = "Error de sincronización: ${syncState.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ProductivaRed,
                        textAlign = TextAlign.Start
                    )
                }
                else -> {
                    if (lastSyncTime > 0) {
                        val formattedDate = formatDate(lastSyncTime)
                        Text(
                            text = "Última sincronización: $formattedDate",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Start
                        )
                    } else {
                        Text(
                            text = "No sincronizado",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }

        // Botón de sincronización
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            when (syncState) {
                is SyncManager.SyncState.Syncing -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = ProductivaBlue,
                        strokeWidth = 2.dp
                    )
                }
                is SyncManager.SyncState.Success -> {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Sincronización exitosa",
                        tint = ProductivaGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                is SyncManager.SyncState.Error -> {
                    IconButton(onClick = onSyncClick) {
                        Icon(
                            imageVector = Icons.Default.SyncProblem,
                            contentDescription = "Error de sincronización",
                            tint = ProductivaRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                else -> {
                    IconButton(onClick = onSyncClick) {
                        // Animación de rotación
                        val rotation by animateFloatAsState(
                            targetValue = 0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 2000)
                            )
                        )
                        
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Sincronizar ahora",
                            tint = ProductivaBlue,
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(rotation)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Formatea una fecha timestamp a un formato legible.
 */
private fun formatDate(timestamp: Long): String {
    return if (timestamp <= 0) {
        "Nunca"
    } else {
        val date = Date(timestamp)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        format.format(date)
    }
}