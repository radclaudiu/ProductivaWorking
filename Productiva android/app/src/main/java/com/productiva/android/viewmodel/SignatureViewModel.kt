package com.productiva.android.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.productiva.android.model.TaskCompletion
import com.productiva.android.repository.TaskCompletionRepository
import com.productiva.android.repository.TaskRepository
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel para la pantalla de captura de firma
 */
class SignatureViewModel(application: Application) : AndroidViewModel(application) {
    
    private val taskRepository = TaskRepository(application)
    private val taskCompletionRepository = TaskCompletionRepository(application)
    
    // Estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Mensaje de error
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    // Estado de envío de firma
    private val _signatureState = MutableLiveData<SignatureState>()
    val signatureState: LiveData<SignatureState> = _signatureState
    
    // Información para la completación
    private var taskId: Int = -1
    private var notes: String? = null
    private var timeSpent: Int? = null
    private var clientName: String? = null
    
    /**
     * Establece los datos para la completación de tarea
     */
    fun setTaskCompletionData(taskId: Int, notes: String?, timeSpent: Int?, clientName: String?) {
        this.taskId = taskId
        this.notes = notes
        this.timeSpent = timeSpent
        this.clientName = clientName
    }
    
    /**
     * Guarda la firma y completa la tarea
     */
    fun saveSignatureAndCompleteTask(signatureBitmap: Bitmap) {
        if (taskId == -1) {
            _errorMessage.value = "No hay una tarea seleccionada"
            return
        }
        
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // Guardar la firma como archivo
                val signatureFile = saveSignatureToFile(signatureBitmap)
                
                if (signatureFile != null) {
                    // Completar la tarea con la firma
                    val completion = TaskCompletion(
                        taskId = taskId,
                        userId = getSelectedUserId(),
                        notes = notes,
                        status = "completed",
                        timeSpent = timeSpent,
                        completionDate = Date(),
                        clientName = clientName
                    )
                    
                    val result = taskCompletionRepository.createTaskCompletionWithSignature(completion, signatureFile)
                    
                    result.fold(
                        onSuccess = { savedCompletion ->
                            // Actualizar el estado de la tarea a completada
                            val taskResult = taskRepository.updateTaskStatus(taskId, "completed")
                            
                            taskResult.fold(
                                onSuccess = { _ ->
                                    _signatureState.value = SignatureState.Success(signatureFile)
                                    _isLoading.value = false
                                },
                                onFailure = { error ->
                                    _errorMessage.value = "La tarea se completó pero hubo un error al actualizar su estado: ${error.message}"
                                    _signatureState.value = SignatureState.Success(signatureFile)
                                    _isLoading.value = false
                                }
                            )
                        },
                        onFailure = { error ->
                            _errorMessage.value = error.message ?: "Error al guardar la firma"
                            _signatureState.value = SignatureState.Error(error.message ?: "Error al guardar la firma")
                            _isLoading.value = false
                        }
                    )
                } else {
                    _errorMessage.value = "Error al guardar la firma como archivo"
                    _signatureState.value = SignatureState.Error("Error al guardar la firma como archivo")
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al guardar la firma"
                _signatureState.value = SignatureState.Error(e.message ?: "Error al guardar la firma")
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Guarda la imagen de la firma como un archivo
     */
    private fun saveSignatureToFile(bitmap: Bitmap): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "SIGN_${timeStamp}_${taskId}.jpg"
        
        val storageDir = getApplication<Application>().getExternalFilesDir("Signatures")
        storageDir?.mkdirs()
        
        val imageFile = File(storageDir, fileName)
        
        return try {
            val fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.close()
            imageFile
        } catch (e: IOException) {
            _errorMessage.value = "Error al guardar la firma: ${e.message}"
            null
        }
    }
    
    /**
     * Obtiene el ID del usuario seleccionado
     */
    private fun getSelectedUserId(): Int {
        return getApplication<Application>().getSharedPreferences("productiva_prefs", Application.MODE_PRIVATE)
            .getInt("selected_user_id", -1)
    }
    
    /**
     * Estados posibles del proceso de firma
     */
    sealed class SignatureState {
        object Initial : SignatureState()
        data class Success(val signatureFile: File) : SignatureState()
        data class Error(val message: String) : SignatureState()
    }
}