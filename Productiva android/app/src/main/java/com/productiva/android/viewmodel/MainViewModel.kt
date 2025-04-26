package com.productiva.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.productiva.android.model.Task
import com.productiva.android.model.User
import com.productiva.android.repository.TaskRepository
import com.productiva.android.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel para la pantalla principal
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val taskRepository = TaskRepository(application)
    private val userRepository = UserRepository(application)
    
    // Usuario actual
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser
    
    // Estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Mensaje de error
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    // Filtro de tareas
    private val _taskFilter = MutableStateFlow(TaskFilter.ALL)
    val taskFilter: StateFlow<TaskFilter> = _taskFilter
    
    // Tareas
    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> = _tasks
    
    // Tareas filtradas
    private val _filteredTasks = MutableLiveData<List<Task>>()
    val filteredTasks: LiveData<List<Task>> = _filteredTasks
    
    // Consulta de búsqueda
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    init {
        loadCurrentUser()
    }
    
    /**
     * Carga el usuario actual
     */
    private fun loadCurrentUser() {
        val userId = getSelectedUserId()
        
        if (userId != -1) {
            viewModelScope.launch {
                val user = userRepository.getUserById(userId)
                _currentUser.value = user
                
                if (user != null) {
                    // Cargar tareas para el usuario actual
                    loadTasksForUser(userId)
                }
            }
        }
    }
    
    /**
     * Carga las tareas para un usuario específico
     */
    fun loadTasksForUser(userId: Int) {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // Obtener tareas del servidor
                val result = taskRepository.fetchTasksByUser(userId)
                
                result.fold(
                    onSuccess = {
                        _isLoading.value = false
                        applyFilter()
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.message ?: "Error al cargar tareas"
                        _isLoading.value = false
                        
                        // Si hay un error, intentamos cargar desde la base de datos local
                        val localTasks = taskRepository.getTasksByUser(userId).value
                        if (localTasks != null) {
                            _tasks.value = localTasks
                            applyFilter()
                        }
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al cargar tareas"
                _isLoading.value = false
                
                // Si hay un error, intentamos cargar desde la base de datos local
                val localTasks = taskRepository.getTasksByUser(userId).value
                if (localTasks != null) {
                    _tasks.value = localTasks
                    applyFilter()
                }
            }
        }
    }
    
    /**
     * Sincroniza las tareas con el servidor
     */
    fun syncTasks() {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val result = taskRepository.syncTasks()
                
                result.fold(
                    onSuccess = { count ->
                        _isLoading.value = false
                        loadCurrentUser() // Recargar tareas después de sincronizar
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.message ?: "Error al sincronizar tareas"
                        _isLoading.value = false
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al sincronizar tareas"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Establece el filtro de tareas
     */
    fun setTaskFilter(filter: TaskFilter) {
        _taskFilter.value = filter
        applyFilter()
    }
    
    /**
     * Aplica el filtro y la búsqueda a las tareas
     */
    private fun applyFilter() {
        val userId = getSelectedUserId()
        if (userId == -1) return
        
        viewModelScope.launch {
            val tasksToFilter = when (taskFilter.value) {
                TaskFilter.ALL -> taskRepository.getTasksByUser(userId).value ?: emptyList()
                TaskFilter.PENDING -> taskRepository.getTasksByUserAndStatus(userId, "pending").value ?: emptyList()
                TaskFilter.IN_PROGRESS -> taskRepository.getTasksByUserAndStatus(userId, "in_progress").value ?: emptyList()
                TaskFilter.COMPLETED -> taskRepository.getTasksByUserAndStatus(userId, "completed").value ?: emptyList()
                TaskFilter.TODAY -> {
                    // Filtrar tareas para hoy
                    val startOfDay = getStartOfDay(Date())
                    val endOfDay = getEndOfDay(Date())
                    taskRepository.getTasksByDateRange(startOfDay, endOfDay).value ?: emptyList()
                }
                TaskFilter.OVERDUE -> {
                    // Filtrar tareas vencidas (con fecha de vencimiento anterior a hoy y no completadas)
                    val today = Date()
                    (taskRepository.getTasksByUser(userId).value ?: emptyList()).filter { task ->
                        task.dueDate?.before(today) == true && task.status != "completed"
                    }
                }
            }
            
            // Aplicar búsqueda si existe
            val query = searchQuery.value
            _filteredTasks.value = if (query.isNotEmpty()) {
                val lowercaseQuery = query.lowercase()
                tasksToFilter.filter { task ->
                    task.title.lowercase().contains(lowercaseQuery) ||
                            (task.description?.lowercase()?.contains(lowercaseQuery) ?: false) ||
                            (task.locationName?.lowercase()?.contains(lowercaseQuery) ?: false)
                }
            } else {
                tasksToFilter
            }
        }
    }
    
    /**
     * Establece la consulta de búsqueda
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilter()
    }
    
    /**
     * Obtiene el ID del usuario seleccionado
     */
    private fun getSelectedUserId(): Int {
        return getApplication<Application>().getSharedPreferences("productiva_prefs", Application.MODE_PRIVATE)
            .getInt("selected_user_id", -1)
    }
    
    /**
     * Cierra la sesión
     */
    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }
    
    /**
     * Obtiene la fecha de inicio del día
     */
    private fun getStartOfDay(date: Date): Date {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.time
    }
    
    /**
     * Obtiene la fecha de fin del día
     */
    private fun getEndOfDay(date: Date): Date {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        calendar.set(java.util.Calendar.MINUTE, 59)
        calendar.set(java.util.Calendar.SECOND, 59)
        calendar.set(java.util.Calendar.MILLISECOND, 999)
        return calendar.time
    }
    
    /**
     * Filtros posibles para las tareas
     */
    enum class TaskFilter {
        ALL, PENDING, IN_PROGRESS, COMPLETED, TODAY, OVERDUE
    }
}