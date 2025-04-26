package com.productiva.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.productiva.android.components.NetworkAwareWrapper
import com.productiva.android.model.Task
import com.productiva.android.ui.theme.Error
import com.productiva.android.ui.theme.ProductivaBlue
import com.productiva.android.ui.theme.ProductivaGreen
import com.productiva.android.ui.theme.ProductivaOrange
import com.productiva.android.utils.ApiConnectionChecker
import com.productiva.android.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla de listado y gestión de tareas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    taskViewModel: TaskViewModel,
    apiConnectionChecker: ApiConnectionChecker,
    userId: Int,
    onBackClick: () -> Unit,
    onTaskClick: (Task) -> Unit
) {
    val tasks by taskViewModel.tasks.collectAsState(initial = emptyList())
    val taskState by taskViewModel.taskState.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    var filterExpanded by remember { mutableStateOf(false) }
    
    // Filtros disponibles
    val filters = listOf("Todas", "Pendientes", "Completadas", "Canceladas")
    var selectedFilter by remember { mutableStateOf("Pendientes") }
    
    // Mostrar errores como snackbar
    LaunchedEffect(taskState) {
        if (taskState is TaskViewModel.TaskState.Error) {
            val errorState = taskState as TaskViewModel.TaskState.Error
            snackbarHostState.showSnackbar(errorState.message)
        }
    }
    
    // Cargar tareas del usuario al inicio
    LaunchedEffect(Unit) {
        taskViewModel.loadTasksForUser(userId)
    }
    
    // Aplicar filtro
    LaunchedEffect(selectedFilter) {
        when (selectedFilter) {
            "Pendientes" -> taskViewModel.filterTasks("PENDING")
            "Completadas" -> taskViewModel.filterTasks("COMPLETED")
            "Canceladas" -> taskViewModel.filterTasks("CANCELLED")
            else -> taskViewModel.clearFilter()
        }
    }
    
    // Estructura principal
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tareas") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { filterExpanded = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
                    }
                    
                    DropdownMenu(
                        expanded = filterExpanded,
                        onDismissRequest = { filterExpanded = false }
                    ) {
                        filters.forEach { filter ->
                            DropdownMenuItem(
                                text = { Text(filter) },
                                onClick = {
                                    selectedFilter = filter
                                    filterExpanded = false
                                }
                            )
                        }
                    }
                    
                    IconButton(onClick = { taskViewModel.syncTasks() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sincronizar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        NetworkAwareWrapper(
            connectionState = apiConnectionChecker.getCurrentConnectionState(),
            isCheckingConnection = taskState is TaskViewModel.TaskState.Loading
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Filtros activos
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "Pendientes",
                        onClick = { selectedFilter = "Pendientes" },
                        label = { Text("Pendientes") }
                    )
                    
                    FilterChip(
                        selected = selectedFilter == "Completadas",
                        onClick = { selectedFilter = "Completadas" },
                        label = { Text("Completadas") }
                    )
                    
                    FilterChip(
                        selected = selectedFilter == "Canceladas",
                        onClick = { selectedFilter = "Canceladas" },
                        label = { Text("Canceladas") }
                    )
                }
                
                // Lista de tareas
                when {
                    taskState is TaskViewModel.TaskState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    tasks.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "No hay tareas disponibles",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                IconButton(onClick = { taskViewModel.syncTasks() }) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Sincronizar",
                                        tint = ProductivaBlue
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = paddingValues
                        ) {
                            items(tasks) { task ->
                                TaskItem(
                                    task = task,
                                    onClick = { onTaskClick(task) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Item individual para mostrar una tarea en la lista.
 */
@Composable
fun TaskItem(
    task: Task,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Encabezado con estado y prioridad
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Indicador de estado
                val (statusColor, statusIcon) = when (task.status) {
                    "COMPLETED" -> Pair(ProductivaGreen, Icons.Default.Check)
                    "CANCELLED" -> Pair(Error, Icons.Default.Info)
                    "PENDING" -> {
                        if (task.isOverdue()) Pair(Error, Icons.Default.Warning)
                        else Pair(ProductivaBlue, Icons.Default.Info)
                    }
                    else -> Pair(ProductivaOrange, Icons.Default.Info)
                }
                
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.1f))
                        .border(1.dp, statusColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Título y detalles
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = task.getStatusDisplay(),
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor
                    )
                }
                
                // Indicador de prioridad
                val priorityColor = when (task.priority) {
                    5 -> Error
                    4 -> ProductivaOrange
                    3 -> Color(0xFFFFC107)
                    else -> Color.Gray
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(priorityColor.copy(alpha = 0.1f))
                        .border(1.dp, priorityColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "P${task.priority}",
                        style = MaterialTheme.typography.bodySmall,
                        color = priorityColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Descripción
            if (!task.description.isNullOrBlank()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Información adicional
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Ubicación
                Text(
                    text = task.locationName ?: "Sin ubicación",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Fecha de vencimiento
                task.dueDate?.let { dueDate ->
                    try {
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        val date = inputFormat.parse(dueDate)
                        val formattedDate = outputFormat.format(date)
                        
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (task.isOverdue()) Error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } catch (e: Exception) {
                        Text(
                            text = dueDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Información de asignación
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Asignada a: ${task.getAssignedDisplay()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Información de sincronización
                if (task.needsSync) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            tint = ProductivaOrange,
                            modifier = Modifier.size(12.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = "Pendiente de sincronizar",
                            style = MaterialTheme.typography.bodySmall,
                            color = ProductivaOrange
                        )
                    }
                }
            }
        }
    }
}