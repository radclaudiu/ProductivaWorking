package com.productiva.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.productiva.android.model.Task
import com.productiva.android.model.User
import com.productiva.android.repository.TaskRepository
import com.productiva.android.repository.TaskRepository.Resource
import com.productiva.android.repository.UserRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla principal
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val userRepository = UserRepository(application)
    private val taskRepository = TaskRepository(application)
    
    // Usuario actual
    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> get() = _currentUser
    
    // Tareas
    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> get() = _tasks
    
    // Estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading
    
    // Mensaje de error
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
    
    // Filtro de estado
    private val _currentStatusFilter = MutableLiveData<String>()
    val currentStatusFilter: LiveData<String> get() = _currentStatusFilter
    
    // Tarea seleccionada
    private val _selectedTask = MutableLiveData<Task>()
    val selectedTask: LiveData<Task> get() = _selectedTask
    
    init {
        // Por defecto, mostrar tareas pendientes
        _currentStatusFilter.value = Task.STATUS_PENDING
    }
    
    /**
     * Carga el usuario actual y sus tareas
     */
    fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val result = userRepository.getCurrentUser()
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    if (user != null) {
                        _currentUser.value = user
                        loadTasks(user.id)
                    }
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Error al obtener usuario"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error desconocido"
            }
        }
    }
    
    /**
     * Carga el usuario por ID y sus tareas
     */
    fun loadUserById(userId: Int) {
        viewModelScope.launch {
            try {
                val user = userRepository.getUserById(userId)
                if (user != null) {
                    _currentUser.value = user
                    loadTasks(user.id)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al obtener usuario"
            }
        }
    }
    
    /**
     * Carga las tareas del usuario con el estado actual
     */
    fun loadTasks(userId: Int) {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                taskRepository.loadTasksWithStatusFlow(
                    userId = userId,
                    status = _currentStatusFilter.value
                ).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _isLoading.value = true
                            resource.data?.let { _tasks.value = it }
                        }
                        is Resource.Success -> {
                            _isLoading.value = false
                            _tasks.value = resource.data ?: emptyList()
                        }
                        is Resource.Error -> {
                            _isLoading.value = false
                            _errorMessage.value = resource.message ?: "Error desconocido"
                            resource.data?.let { _tasks.value = it }
                        }
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Error desconocido"
            }
        }
    }
    
    /**
     * Cambia el filtro de estado y recarga las tareas
     */
    fun setStatusFilter(status: String) {
        if (_currentStatusFilter.value != status) {
            _currentStatusFilter.value = status
            _currentUser.value?.let { loadTasks(it.id) }
        }
    }
    
    /**
     * Actualiza el estado de una tarea
     */
    fun updateTaskStatus(taskId: Int, newStatus: String) {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val result = taskRepository.updateTaskStatus(taskId, newStatus)
                if (result.isFailure) {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Error al actualizar tarea"
                } else {
                    // Recargar las tareas si el estado cambió
                    _currentUser.value?.let { loadTasks(it.id) }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Selecciona una tarea para ver sus detalles
     */
    fun selectTask(task: Task) {
        _selectedTask.value = task
    }
    
    /**
     * Limpia la tarea seleccionada
     */
    fun clearSelectedTask() {
        _selectedTask.value = null
    }
    
    /**
     * Refresca las tareas desde el servidor
     */
    fun refreshTasks() {
        _currentUser.value?.let { loadTasks(it.id) }
    }
    
    /**
     * Cierra la sesión del usuario actual
     */
    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }
}