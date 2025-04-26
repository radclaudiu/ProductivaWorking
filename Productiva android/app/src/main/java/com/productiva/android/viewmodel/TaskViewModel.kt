package com.productiva.android.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.productiva.android.ProductivaApplication
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.model.User
import com.productiva.android.network.RetrofitClient
import com.productiva.android.repository.ResourceState
import com.productiva.android.repository.TaskRepository
import com.productiva.android.sync.SyncManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de tareas.
 */
class TaskViewModel(application: Application) : AndroidViewModel(application) {
    
    private val TAG = "TaskViewModel"
    
    // Repositorios
    private val taskRepository: TaskRepository
    private val syncManager: SyncManager
    
    // Estado de la operación con tareas
    private val _taskOperationState = MutableLiveData<TaskOperationState>()
    val taskOperationState: LiveData<TaskOperationState> = _taskOperationState
    
    // Lista de tareas filtrada por usuario actual
    private val _userTasks = MediatorLiveData<List<Task>>()
    val userTasks: LiveData<List<Task>> = _userTasks
    
    // Tarea seleccionada actualmente
    private val _selectedTask = MutableLiveData<Task?>()
    val selectedTask: LiveData<Task?> = _selectedTask
    
    // Finalizaciones de la tarea seleccionada
    private val _taskCompletions = MutableLiveData<List<TaskCompletion>>()
    val taskCompletions: LiveData<List<TaskCompletion>> = _taskCompletions
    
    // Estado de la sincronización
    private val _syncState = MutableLiveData<String>("No sincronizado")
    val syncState: LiveData<String> = _syncState
    
    // Filtros activos
    private var currentUserId: Int? = null
    private var statusFilter: String? = null
    
    /**
     * Estados posibles de las operaciones con tareas.
     */
    sealed class TaskOperationState {
        object Idle : TaskOperationState()
        object Loading : TaskOperationState()
        object Success : TaskOperationState()
        data class Error(val message: String) : TaskOperationState()
        data class CompletionSuccess(val completion: TaskCompletion) : TaskOperationState()
        data class OfflineSuccess(val message: String) : TaskOperationState()
    }
    
    init {
        // Obtener instancias de la aplicación
        val app = getApplication<ProductivaApplication>()
        val apiService = RetrofitClient.getApiService(app)
        val database = app.database
        
        // Inicializar repositorios
        taskRepository = TaskRepository(app, database.taskDao(), database.taskCompletionDao(), apiService)
        
        // Inicializar gestor de sincronización
        val labelTemplateRepository = LabelTemplateRepository(database.labelTemplateDao(), apiService)
        val userRepository = UserRepository(database.userDao(), apiService)
        syncManager = SyncManager(app, userRepository, taskRepository, labelTemplateRepository)
        
        // Configurar la sincronización periódica
        syncManager.setupPeriodicSync()
        
        // Iniciar en estado Idle
        _taskOperationState.value = TaskOperationState.Idle
        
        // Actualizar estado de sincronización
        updateSyncState()
    }
    
    /**
     * Establece el usuario actual y carga sus tareas.
     */
    fun setCurrentUser(user: User) {
        currentUserId = user.id
        refreshTasks()
    }
    
    /**
     * Actualiza el estado de sincronización.
     */
    private fun updateSyncState() {
        _syncState.value = syncManager.getFormattedLastSyncTime()
    }
    
    /**
     * Recarga las tareas según los filtros actuales.
     */
    fun refreshTasks() {
        currentUserId?.let { userId ->
            if (statusFilter != null) {
                // Aplicar ambos filtros: usuario y estado
                _userTasks.addSource(
                    taskRepository.getTasksAssignedToUserByStatus(userId, statusFilter!!)
                ) { tasks ->
                    _userTasks.value = tasks
                }
            } else {
                // Solo filtrar por usuario
                _userTasks.addSource(
                    taskRepository.getTasksAssignedToUser(userId)
                ) { tasks ->
                    _userTasks.value = tasks
                }
            }
        }
    }
    
    /**
     * Filtra las tareas por estado.
     */
    fun filterByStatus(status: String?) {
        // Remover fuentes anteriores
        _userTasks.value = emptyList()
        for (source in _userTasks.sources) {
            _userTasks.removeSource(source)
        }
        
        statusFilter = status
        refreshTasks()
    }
    
    /**
     * Selecciona una tarea y carga sus finalizaciones.
     */
    fun selectTask(task: Task) {
        _selectedTask.value = task
        
        // Cargar finalizaciones de la tarea
        viewModelScope.launch {
            _taskCompletions.value = taskRepository.getCompletionsForTask(task.id).value ?: emptyList()
        }
    }
    
    /**
     * Limpia la tarea seleccionada.
     */
    fun clearSelectedTask() {
        _selectedTask.value = null
        _taskCompletions.value = emptyList()
    }
    
    /**
     * Actualiza el estado de una tarea.
     */
    fun updateTaskStatus(taskId: Int, newStatus: String) {
        _taskOperationState.value = TaskOperationState.Loading
        
        viewModelScope.launch {
            try {
                taskRepository.updateTaskStatus(taskId, newStatus).collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            refreshTasks()
                            if (state.isFromCache) {
                                _taskOperationState.value = TaskOperationState.OfflineSuccess(
                                    "Tarea actualizada localmente. Se sincronizará cuando haya conexión."
                                )
                            } else {
                                _taskOperationState.value = TaskOperationState.Success
                            }
                        }
                        is ResourceState.Error -> {
                            _taskOperationState.value = TaskOperationState.Error(
                                state.message ?: "Error desconocido al actualizar la tarea"
                            )
                        }
                        is ResourceState.Loading -> {
                            _taskOperationState.value = TaskOperationState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al actualizar estado de tarea", e)
                _taskOperationState.value = TaskOperationState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Completa una tarea con firma opcional y foto opcional.
     */
    fun completeTask(
        taskId: Int,
        userId: Int,
        comments: String?,
        signatureUri: Uri?,
        photoUri: Uri?,
        status: String = "ok",
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        _taskOperationState.value = TaskOperationState.Loading
        
        viewModelScope.launch {
            try {
                taskRepository.completeTask(
                    taskId, userId, comments, signatureUri, photoUri, status, latitude, longitude
                ).collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            val completion = state.data!!
                            _taskCompletions.value = (_taskCompletions.value ?: emptyList()) + completion
                            refreshTasks()
                            
                            if (state.isFromCache) {
                                _taskOperationState.value = TaskOperationState.OfflineSuccess(
                                    "Tarea completada localmente. Se sincronizará cuando haya conexión."
                                )
                            } else {
                                _taskOperationState.value = TaskOperationState.CompletionSuccess(completion)
                            }
                        }
                        is ResourceState.Error -> {
                            _taskOperationState.value = TaskOperationState.Error(
                                state.message ?: "Error desconocido al completar la tarea"
                            )
                        }
                        is ResourceState.Loading -> {
                            _taskOperationState.value = TaskOperationState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al completar tarea", e)
                _taskOperationState.value = TaskOperationState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Inicia una sincronización manual con el servidor.
     */
    fun syncNow() {
        syncManager.syncNow()
        
        // Actualizar estado de sincronización después de un breve retraso
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000) // Esperar 2 segundos
            updateSyncState()
            refreshTasks()
        }
    }
    
    /**
     * Sincroniza las finalizaciones pendientes.
     */
    fun syncPendingCompletions() {
        _taskOperationState.value = TaskOperationState.Loading
        
        viewModelScope.launch {
            try {
                taskRepository.syncPendingCompletions().collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            val count = state.data ?: 0
                            refreshTasks()
                            
                            _taskOperationState.value = TaskOperationState.Success
                            if (count > 0) {
                                Log.d(TAG, "Se sincronizaron $count finalizaciones pendientes")
                            }
                        }
                        is ResourceState.Error -> {
                            _taskOperationState.value = TaskOperationState.Error(
                                state.message ?: "Error desconocido al sincronizar"
                            )
                        }
                        is ResourceState.Loading -> {
                            _taskOperationState.value = TaskOperationState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al sincronizar finalizaciones pendientes", e)
                _taskOperationState.value = TaskOperationState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Obtiene todos los estados de tareas disponibles.
     */
    fun getAvailableStatuses(): List<Pair<String, String>> {
        return listOf(
            Pair("pending", "Pendiente"),
            Pair("in_progress", "En progreso"),
            Pair("completed", "Completada"),
            Pair("cancelled", "Cancelada")
        )
    }
    
    /**
     * Formatea la fecha de una tarea para mostrar.
     */
    fun formatTaskDate(dueDate: java.util.Date?): String {
        if (dueDate == null) return "Sin fecha"
        
        return java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            .format(dueDate)
    }
}