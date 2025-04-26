package com.productiva.android.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.productiva.android.components.AutoCheckNetworkAwareWrapper
import com.productiva.android.components.SyncStatusIndicator
import com.productiva.android.sync.SyncManager
import com.productiva.android.utils.ApiConnectionChecker
import java.util.concurrent.TimeUnit

/**
 * Pantalla principal de la aplicación con navegación inferior.
 */
@Composable
fun MainScreen(
    apiConnectionChecker: ApiConnectionChecker,
    syncManager: SyncManager,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var pendingTasksCount by remember { mutableIntStateOf(0) }
    var pendingLabelsCount by remember { mutableIntStateOf(0) }
    
    // Observar el estado de sincronización
    val syncState by syncManager.syncState.collectAsState(initial = SyncManager.SyncState.Idle)
    val lastSyncTime = remember { syncManager.getLastSyncTime() }
    
    // Función para iniciar sincronización manual
    val onSyncClick = {
        if (syncState != SyncManager.SyncState.Syncing) {
            syncManager.syncNow()
        }
    }
    
    // Scaffold con navegación inferior
    Scaffold(
        bottomBar = {
            NavigationBar {
                // Tab Dashboard
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) {
                                Icons.Filled.Dashboard
                            } else {
                                Icons.Outlined.Dashboard
                            },
                            contentDescription = "Dashboard"
                        )
                    },
                    label = { Text("Dashboard") }
                )
                
                // Tab Tareas
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (pendingTasksCount > 0) {
                                    Badge { Text(pendingTasksCount.toString()) }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 1) {
                                    Icons.Filled.Task
                                } else {
                                    Icons.Outlined.Task
                                },
                                contentDescription = "Tareas"
                            )
                        }
                    },
                    label = { Text("Tareas") }
                )
                
                // Tab Etiquetas
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (pendingLabelsCount > 0) {
                                    Badge { Text(pendingLabelsCount.toString()) }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 2) {
                                    Icons.Filled.Label
                                } else {
                                    Icons.Outlined.Label
                                },
                                contentDescription = "Etiquetas"
                            )
                        }
                    },
                    label = { Text("Etiquetas") }
                )
                
                // Tab Perfil
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) {
                                Icons.Filled.Person
                            } else {
                                Icons.Outlined.Person
                            },
                            contentDescription = "Perfil"
                        )
                    },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { paddingValues ->
        // Contenido principal con wrapper de red
        AutoCheckNetworkAwareWrapper(
            connectionChecker = apiConnectionChecker,
            checkOnAppear = true,
            checkInterval = TimeUnit.MINUTES.toMillis(5)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Contenido basado en la pestaña seleccionada
                when (selectedTab) {
                    0 -> DashboardContent(syncState, lastSyncTime, onSyncClick)
                    1 -> TasksContent()
                    2 -> LabelsContent()
                    3 -> ProfileContent(onLogout)
                }
            }
        }
    }
}

/**
 * Contenido para el tab de Dashboard.
 */
@Composable
private fun DashboardContent(
    syncState: SyncManager.SyncState,
    lastSyncTime: Long,
    onSyncClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Indicador de estado de sincronización en la parte superior
        SyncStatusIndicator(
            syncState = syncState,
            lastSyncTime = lastSyncTime,
            onSyncClick = onSyncClick
        )
        
        // Contenido del dashboard
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Dashboard de Productiva",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Contenido para el tab de Tareas.
 */
@Composable
private fun TasksContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Tareas",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Contenido para el tab de Etiquetas.
 */
@Composable
private fun LabelsContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Etiquetas",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Contenido para el tab de Perfil.
 */
@Composable
private fun ProfileContent(onLogout: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Perfil de Usuario",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            
            androidx.compose.material3.Button(
                onClick = onLogout,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Cerrar Sesión")
            }
        }
    }
}