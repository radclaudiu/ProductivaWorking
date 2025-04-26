package com.productiva.android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.productiva.android.sync.SyncManager
import com.productiva.android.sync.SyncState

/**
 * Componente que muestra el estado actual de sincronización.
 * Cambia de apariencia según si está sincronizando, hubo errores o está idle.
 *
 * @param modifier Modificador para el componente.
 * @param onSyncRequest Callback para solicitar una sincronización manual.
 */
@Composable
fun SyncStatusIndicator(
    modifier: Modifier = Modifier,
    onSyncRequest: () -> Unit = {}
) {
    val context = LocalContext.current
    val syncManager = SyncManager.getInstance(context)
    val syncState by syncManager.syncState.collectAsState()
    
    Row(
        modifier = modifier
            .padding(8.dp)
            .clip(CircleShape)
            .clickable { onSyncRequest() }
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Icono de estado
        when (syncState) {
            is SyncState.Idle -> {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Sincronizar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            is SyncState.Syncing -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            }
            is SyncState.Completed -> {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sincronizado",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(20.dp)
                    )
                }
            }
            is SyncState.Error -> {
                Icon(
                    imageVector = Icons.Default.SyncProblem,
                    contentDescription = "Error de sincronización",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Texto de estado (opcional, solo visible cuando hay acción o error)
        AnimatedVisibility(
            visible = syncState is SyncState.Syncing || syncState is SyncState.Error,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            when (val state = syncState) {
                is SyncState.Syncing -> {
                    val progress = (state.progress).coerceIn(0, 100)
                    Text(
                        text = if (progress > 0) "${progress}%" else "Sincronizando...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is SyncState.Error -> {
                    Text(
                        text = "Error",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> { /* No mostrar texto */ }
            }
        }
    }
}