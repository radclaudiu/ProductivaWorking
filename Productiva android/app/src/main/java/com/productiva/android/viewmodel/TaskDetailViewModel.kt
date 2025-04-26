package com.productiva.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.repository.TaskCompletionRepository
import com.productiva.android.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

/**
 * ViewModel para la pantalla de detalle de tarea
 */
class TaskDetailViewModel(application: Application) : AndroidViewModel(application) {
    
    private val taskRepository = TaskRepository(application)
    private val taskCompletionRepository = TaskCompletionRepository(application)
    
    // Tarea actual
    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task
    
    // Completaciones de la tarea
    private val _completions = MutableLiveData<List<TaskCompletion>>()
    val completions: LiveData<List<TaskCompletion>> = _completions
    
    // Estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Mensaje de error
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    // Estado de completado
    private val _completionState = MutableLiveData<CompletionState>()
    val completionState: LiveData<CompletionState> = _completionState
    
    /**
     * Carga una tarea por su ID
     */
    fun loadTask(taskId: Int) {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val loadedTask = taskRepository.getTaskById(taskId)
                
                if (loadedTask != null) {
                    _task.value = loadedTask
                    
                    // Cargar completaciones
                    _completions.value = taskCompletionRepository.getCompletionsByTaskId(taskId).value ?: emptyList()
                    
                    _isLoading.value = false
                } else {
                    _errorMessage.value = "No se encontró la tarea"
                    _isLoading.value = false
                    
                    // Intentar cargar desde el servidor
                    fetchTaskFromServer(taskId)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al cargar la tarea"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Obtiene una tarea desde el servidor
     */
    private fun fetchTaskFromServer(taskId: Int) {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val response = taskRepository.fetchTaskFromServer(taskId)
                
                response.fold(
                    onSuccess = { fetchedTask ->
                        _task.value = fetchedTask
                        _isLoading.value = false
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.message ?: "Error al obtener la tarea del servidor"
                        _isLoading.value = false
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al obtener la tarea del servidor"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Actualiza el estado de una tarea
     */
    fun updateTaskStatus(status: String) {
        val currentTask = _task.value ?: return
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val updatedTask = currentTask.copy(
                    status = status,
                    updatedAt = Date()
                )
                
                val result = taskRepository.updateTask(updatedTask)
                
                result.fold(
                    onSuccess = { task ->
                        _task.value = task
                        _isLoading.value = false
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.message ?: "Error al actualizar la tarea"
                        _isLoading.value = false
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al actualizar la tarea"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Completa una tarea
     */
    fun completeTask(notes: String?, timeSpent: Int?) {
        val currentTask = _task.value ?: return
        val userId = getSelectedUserId()
        
        if (userId == -1) {
            _errorMessage.value = "No hay un usuario seleccionado"
            return
        }
        
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // Crear objeto de completación
                val completion = TaskCompletion(
                    taskId = currentTask.id,
                    userId = userId,
                    notes = notes,
                    status = "completed",
                    timeSpent = timeSpent,
                    completionDate = Date()
                )
                
                val result = taskCompletionRepository.createTaskCompletion(completion)
                
                result.fold(
                    onSuccess = { savedCompletion ->
                        // Actualizar estado de la tarea
                        updateTaskStatus("completed")
                        
                        _completionState.value = CompletionState.Success(savedCompletion)
                        _isLoading.value = false
                        
                        // Recargar completaciones
                        _completions.value = taskCompletionRepository.getCompletionsByTaskId(currentTask.id).value ?: emptyList()
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.message ?: "Error al completar la tarea"
                        _completionState.value = CompletionState.Error(error.message ?: "Error al completar la tarea")
                        _isLoading.value = false
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al completar la tarea"
                _completionState.value = CompletionState.Error(e.message ?: "Error al completar la tarea")
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Completa una tarea con firma
     */
    fun completeTaskWithSignature(notes: String?, timeSpent: Int?, signatureFile: File, clientName: String?) {
        val currentTask = _task.value ?: return
        val userId = getSelectedUserId()
        
        if (userId == -1) {
            _errorMessage.value = "No hay un usuario seleccionado"
            return
        }
        
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // Crear objeto de completación
                val completion = TaskCompletion(
                    taskId = currentTask.id,
                    userId = userId,
                    notes = notes,
                    status = "completed",
                    timeSpent = timeSpent,
                    completionDate = Date(),
                    clientName = clientName
                )
                
                val result = taskCompletionRepository.createTaskCompletionWithSignature(completion, signatureFile)
                
                result.fold(
                    onSuccess = { savedCompletion ->
                        // Actualizar estado de la tarea
                        updateTaskStatus("completed")
                        
                        _completionState.value = CompletionState.Success(savedCompletion)
                        _isLoading.value = false
                        
                        // Recargar completaciones
                        _completions.value = taskCompletionRepository.getCompletionsByTaskId(currentTask.id).value ?: emptyList()
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.message ?: "Error al completar la tarea con firma"
                        _completionState.value = CompletionState.Error(error.message ?: "Error al completar la tarea con firma")
                        _isLoading.value = false
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al completar la tarea con firma"
                _completionState.value = CompletionState.Error(e.message ?: "Error al completar la tarea con firma")
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Completa una tarea con foto
     */
    fun completeTaskWithPhoto(notes: String?, timeSpent: Int?, photoFile: File) {
        val currentTask = _task.value ?: return
        val userId = getSelectedUserId()
        
        if (userId == -1) {
            _errorMessage.value = "No hay un usuario seleccionado"
            return
        }
        
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // Crear objeto de completación
                val completion = TaskCompletion(
                    taskId = currentTask.id,
                    userId = userId,
                    notes = notes,
                    status = "completed",
                    timeSpent = timeSpent,
                    completionDate = Date()
                )
                
                val result = taskCompletionRepository.createTaskCompletionWithPhoto(completion, photoFile)
                
                result.fold(
                    onSuccess = { savedCompletion ->
                        // Actualizar estado de la tarea
                        updateTaskStatus("completed")
                        
                        _completionState.value = CompletionState.Success(savedCompletion)
                        _isLoading.value = false
                        
                        // Recargar completaciones
                        _completions.value = taskCompletionRepository.getCompletionsByTaskId(currentTask.id).value ?: emptyList()
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.message ?: "Error al completar la tarea con foto"
                        _completionState.value = CompletionState.Error(error.message ?: "Error al completar la tarea con foto")
                        _isLoading.value = false
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al completar la tarea con foto"
                _completionState.value = CompletionState.Error(e.message ?: "Error al completar la tarea con foto")
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Inicia una tarea (cambia el estado a "in_progress")
     */
    fun startTask() {
        updateTaskStatus("in_progress")
    }
    
    /**
     * Obtiene el ID del usuario seleccionado
     */
    private fun getSelectedUserId(): Int {
        return getApplication<Application>().getSharedPreferences("productiva_prefs", Application.MODE_PRIVATE)
            .getInt("selected_user_id", -1)
    }
    
    /**
     * Resetea el estado de completado
     */
    fun resetCompletionState() {
        _completionState.value = CompletionState.Initial
    }
    
    /**
     * Estados posibles del proceso de completado
     */
    sealed class CompletionState {
        object Initial : CompletionState()
        data class Success(val completion: TaskCompletion) : CompletionState()
        data class Error(val message: String) : CompletionState()
    }
}