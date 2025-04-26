package com.productiva.android.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.productiva.android.ProductivaApplication
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.network.RetrofitClient
import com.productiva.android.repository.ResourceState
import com.productiva.android.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * ViewModel para la gestión de tareas.
 */
class TaskViewModel(application: Application) : AndroidViewModel(application) {
    
    private val TAG = "TaskViewModel"
    
    // Repositorio de tareas
    private val taskRepository: TaskRepository
    
    // Estado de tareas
    private val _taskState = MutableStateFlow<TaskState>(TaskState.Idle)
    val taskState: StateFlow<TaskState> = _taskState
    
    // Filtro actual
    private val _filter = MutableStateFlow<String?>(null)
    
    // Usuario actual
    private val _currentUserId = MutableStateFlow<Int?>(null)
    
    // Tarea seleccionada
    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask
    
    // Lista de tareas filtradas
    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: Flow<List<Task>> = combine(_currentUserId, _filter) { userId, filter ->
        Pair(userId, filter)
    }.flatMapLatest { (userId, filter) ->
        when {
            userId != null && filter != null -> taskRepository.getTasksByStatusForUser(userId, filter)
            userId != null -> taskRepository.getTasksAssignedToUser(userId)
            filter != null -> taskRepository.getTasksByStatus(filter)
            else -> taskRepository.getAllTasks()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    init {
        val app = getApplication<ProductivaApplication>()
        val apiService = RetrofitClient.getApiService(app)
        
        // Inicializar el repositorio
        taskRepository = TaskRepository(app.database.taskDao(), apiService)
    }
    
    /**
     * Carga tareas asignadas a un usuario específico.
     */
    fun loadTasksForUser(userId: Int) {
        _currentUserId.value = userId
        
        // Si hay un filtro activo, aplicarlo para este usuario
        if (_filter.value != null) {
            _filter.value?.let { filter ->
                loadTasksByStatusForUser(userId, filter)
            }
        } else {
            // Carga por defecto tareas pendientes
            loadTasksByStatusForUser(userId, "PENDING")
        }
    }
    
    /**
     * Carga tareas por estado para un usuario específico.
     */
    private fun loadTasksByStatusForUser(userId: Int, status: String) {
        _taskState.value = TaskState.Loading
        
        viewModelScope.launch {
            try {
                _currentUserId.value = userId
                _filter.value = status
                _taskState.value = TaskState.Idle
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar tareas por estado para usuario", e)
                _taskState.value = TaskState.Error("Error al cargar tareas: ${e.message}")
            }
        }
    }
    
    /**
     * Filtra tareas por estado.
     */
    fun filterTasks(status: String) {
        _filter.value = status
    }
    
    /**
     * Elimina el filtro de tareas.
     */
    fun clearFilter() {
        _filter.value = null
    }
    
    /**
     * Sincroniza tareas con el servidor.
     */
    fun syncTasks() {
        _taskState.value = TaskState.Loading
        
        viewModelScope.launch {
            try {
                // Primero sincronizar completados pendientes
                taskRepository.syncPendingTaskCompletions()
                
                // Luego obtener tareas actualizadas del servidor
                taskRepository.syncTasks(_currentUserId.value).collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            _taskState.value = TaskState.Synced(state.data ?: emptyList())
                        }
                        is ResourceState.Error -> {
                            _taskState.value = TaskState.Error(state.message ?: "Error desconocido")
                        }
                        is ResourceState.Loading -> {
                            _taskState.value = TaskState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al sincronizar tareas", e)
                _taskState.value = TaskState.Error("Error al sincronizar tareas: ${e.message}")
            }
        }
    }
    
    /**
     * Selecciona una tarea para ver detalles.
     */
    fun selectTask(task: Task) {
        _selectedTask.value = task
    }
    
    /**
     * Completa una tarea.
     */
    fun completeTask(taskId: Int, notes: String?, signature: Uri?, photo: Uri?) {
        _taskState.value = TaskState.Loading
        
        viewModelScope.launch {
            try {
                // Obtener la tarea
                val task = taskRepository.getTaskByIdSync(taskId)
                
                if (task == null) {
                    _taskState.value = TaskState.Error("Tarea no encontrada")
                    return@launch
                }
                
                // Procesar firma si existe
                var signaturePath: String? = null
                var signatureData: String? = null
                if (signature != null) {
                    signaturePath = saveSignatureToFile(signature, taskId)
                }
                
                // Procesar foto si existe
                var photoPath: String? = null
                var photoData: String? = null
                if (photo != null) {
                    photoPath = savePhotoToFile(photo, taskId)
                }
                
                // Crear objeto de completado
                val userId = _currentUserId.value ?: 0
                val completion = TaskCompletion.create(
                    taskId = taskId,
                    status = "COMPLETED",
                    userId = userId,
                    notes = notes,
                    signatureData = signatureData,
                    photoData = photoData,
                    localSignaturePath = signaturePath,
                    localPhotoPath = photoPath
                )
                
                // Completar tarea localmente
                taskRepository.completeTaskLocally(completion).collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            _taskState.value = TaskState.Completed(taskId)
                        }
                        is ResourceState.Error -> {
                            _taskState.value = TaskState.Error(state.message ?: "Error desconocido")
                        }
                        is ResourceState.Loading -> {
                            _taskState.value = TaskState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al completar tarea", e)
                _taskState.value = TaskState.Error("Error al completar tarea: ${e.message}")
            }
        }
    }
    
    /**
     * Cancela una tarea.
     */
    fun cancelTask(taskId: Int, notes: String?) {
        _taskState.value = TaskState.Loading
        
        viewModelScope.launch {
            try {
                // Obtener la tarea
                val task = taskRepository.getTaskByIdSync(taskId)
                
                if (task == null) {
                    _taskState.value = TaskState.Error("Tarea no encontrada")
                    return@launch
                }
                
                // Crear objeto de completado
                val userId = _currentUserId.value ?: 0
                val completion = TaskCompletion.create(
                    taskId = taskId,
                    status = "CANCELLED",
                    userId = userId,
                    notes = notes
                )
                
                // Cancelar tarea localmente
                taskRepository.completeTaskLocally(completion).collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            _taskState.value = TaskState.Cancelled(taskId)
                        }
                        is ResourceState.Error -> {
                            _taskState.value = TaskState.Error(state.message ?: "Error desconocido")
                        }
                        is ResourceState.Loading -> {
                            _taskState.value = TaskState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cancelar tarea", e)
                _taskState.value = TaskState.Error("Error al cancelar tarea: ${e.message}")
            }
        }
    }
    
    /**
     * Guarda una firma en un archivo local.
     */
    private fun saveSignatureToFile(signatureUri: Uri, taskId: Int): String? {
        return try {
            val context = getApplication<ProductivaApplication>()
            val inputStream = context.contentResolver.openInputStream(signatureUri)
            val fileName = "signature_${taskId}_${System.currentTimeMillis()}.png"
            val file = File(context.filesDir, fileName)
            
            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar firma", e)
            null
        }
    }
    
    /**
     * Guarda una foto en un archivo local.
     */
    private fun savePhotoToFile(photoUri: Uri, taskId: Int): String? {
        return try {
            val context = getApplication<ProductivaApplication>()
            val inputStream = context.contentResolver.openInputStream(photoUri)
            val fileName = "photo_${taskId}_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            
            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar foto", e)
            null
        }
    }
}

/**
 * Estados posibles de tareas.
 */
sealed class TaskState {
    object Idle : TaskState()
    object Loading : TaskState()
    data class Synced(val tasks: List<Task>) : TaskState()
    data class Completed(val taskId: Int) : TaskState()
    data class Cancelled(val taskId: Int) : TaskState()
    data class Error(val message: String) : TaskState()
}