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
import com.productiva.android.repository.UserRepository
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

/**
 * ViewModel para la pantalla de detalle de tarea
 */
class TaskDetailViewModel(application: Application) : AndroidViewModel(application) {
    
    private val taskRepository = TaskRepository(application)
    private val completionRepository = TaskCompletionRepository(application)
    private val userRepository = UserRepository(application)
    
    // Tarea actual
    private val _currentTask = MutableLiveData<Task>()
    val currentTask: LiveData<Task> get() = _currentTask
    
    // Completaciones de la tarea
    private val _completions = MutableLiveData<List<TaskCompletion>>()
    val completions: LiveData<List<TaskCompletion>> get() = _completions
    
    // Completación actual
    private val _currentCompletion = MutableLiveData<TaskCompletion>()
    val currentCompletion: LiveData<TaskCompletion> get() = _currentCompletion
    
    // Estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading
    
    // Mensaje de error
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
    
    // Estado de completación (éxito)
    private val _completionSuccess = MutableLiveData<Boolean>()
    val completionSuccess: LiveData<Boolean> get() = _completionSuccess
    
    // Ruta del archivo de firma
    private val _signatureFilePath = MutableLiveData<String>()
    val signatureFilePath: LiveData<String> get() = _signatureFilePath
    
    // Ruta del archivo de foto
    private val _photoFilePath = MutableLiveData<String>()
    val photoFilePath: LiveData<String> get() = _photoFilePath
    
    /**
     * Carga una tarea por su ID
     */
    fun loadTask(taskId: Int) {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // Intentar primero desde la base de datos local
                var task = taskRepository.getTaskById(taskId)
                
                if (task == null) {
                    // Si no está en la BD local, buscar en el servidor
                    val result = taskRepository.fetchTaskById(taskId)
                    if (result.isSuccess) {
                        task = result.getOrNull()
                    } else {
                        _errorMessage.value = result.exceptionOrNull()?.message ?: "Error al cargar tarea"
                    }
                }
                
                task?.let {
                    _currentTask.value = it
                    loadCompletions(it.id)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Carga las completaciones de una tarea
     */
    private fun loadCompletions(taskId: Int) {
        viewModelScope.launch {
            try {
                // Obtener completaciones locales primero
                val localCompletions = completionRepository.getCompletionsByTaskId(taskId)
                
                // Obtener de la API
                // Nota: Deberíamos implementar un método en el repositorio para esto
                
                // Por ahora, usar los datos locales
                _completions.value = localCompletions.value ?: emptyList()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al cargar completaciones"
            }
        }
    }
    
    /**
     * Crea una nueva completación para la tarea
     */
    fun createCompletion(notes: String, currentUserId: Int) {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val currentTask = _currentTask.value ?: return@launch
                
                // Buscar información del usuario
                val user = userRepository.getUserById(currentUserId)
                
                val completion = TaskCompletion(
                    taskId = currentTask.id,
                    userId = currentUserId,
                    userName = user?.name,
                    notes = notes,
                    completionDate = Date(),
                    locationId = currentTask.locationId,
                    locationName = currentTask.locationName
                )
                
                val result = completionRepository.createTaskCompletion(currentTask.id, completion)
                
                if (result.isSuccess) {
                    _currentCompletion.value = result.getOrNull()
                    _completionSuccess.value = true
                    
                    // Cambiar estado de la tarea a completada
                    taskRepository.updateTaskStatus(currentTask.id, Task.STATUS_COMPLETED)
                    
                    // Recargar tarea y completaciones
                    loadTask(currentTask.id)
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Error al crear completación"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Crea una completación con firma
     */
    fun createCompletionWithSignature(notes: String, currentUserId: Int, signatureFile: File) {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val currentTask = _currentTask.value ?: return@launch
                
                // Buscar información del usuario
                val user = userRepository.getUserById(currentUserId)
                
                val completion = TaskCompletion(
                    taskId = currentTask.id,
                    userId = currentUserId,
                    userName = user?.name,
                    notes = notes,
                    completionDate = Date(),
                    locationId = currentTask.locationId,
                    locationName = currentTask.locationName,
                    hasSignature = true
                )
                
                val result = completionRepository.createTaskCompletionWithSignature(
                    currentTask.id,
                    completion,
                    signatureFile
                )
                
                if (result.isSuccess) {
                    _currentCompletion.value = result.getOrNull()
                    _signatureFilePath.value = signatureFile.absolutePath
                    _completionSuccess.value = true
                    
                    // Cambiar estado de la tarea a completada
                    taskRepository.updateTaskStatus(currentTask.id, Task.STATUS_COMPLETED)
                    
                    // Recargar tarea y completaciones
                    loadTask(currentTask.id)
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Error al crear completación con firma"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Crea una completación con foto
     */
    fun createCompletionWithPhoto(notes: String, currentUserId: Int, photoFile: File) {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val currentTask = _currentTask.value ?: return@launch
                
                // Buscar información del usuario
                val user = userRepository.getUserById(currentUserId)
                
                val completion = TaskCompletion(
                    taskId = currentTask.id,
                    userId = currentUserId,
                    userName = user?.name,
                    notes = notes,
                    completionDate = Date(),
                    locationId = currentTask.locationId,
                    locationName = currentTask.locationName,
                    hasPhoto = true
                )
                
                val result = completionRepository.createTaskCompletionWithPhoto(
                    currentTask.id,
                    completion,
                    photoFile
                )
                
                if (result.isSuccess) {
                    _currentCompletion.value = result.getOrNull()
                    _photoFilePath.value = photoFile.absolutePath
                    _completionSuccess.value = true
                    
                    // Cambiar estado de la tarea a completada
                    taskRepository.updateTaskStatus(currentTask.id, Task.STATUS_COMPLETED)
                    
                    // Recargar tarea y completaciones
                    loadTask(currentTask.id)
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Error al crear completación con foto"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Cambia el estado de la tarea actual
     */
    fun updateTaskStatus(newStatus: String) {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val currentTask = _currentTask.value ?: return@launch
                
                val result = taskRepository.updateTaskStatus(currentTask.id, newStatus)
                
                if (result.isSuccess) {
                    // Recargar tarea
                    loadTask(currentTask.id)
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Error al actualizar estado"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Establece la ruta del archivo de firma
     */
    fun setSignatureFilePath(path: String) {
        _signatureFilePath.value = path
    }
    
    /**
     * Establece la ruta del archivo de foto
     */
    fun setPhotoFilePath(path: String) {
        _photoFilePath.value = path
    }
    
    /**
     * Limpia el estado de completación
     */
    fun clearCompletionState() {
        _completionSuccess.value = false
        _currentCompletion.value = null
    }
}