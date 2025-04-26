package com.productiva.android.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.productiva.android.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel para la captura y gestión de firmas.
 */
class SignatureViewModel(application: Application) : AndroidViewModel(application) {
    
    private val TAG = "SignatureViewModel"
    
    // Estado de la operación con la firma
    private val _signatureState = MutableLiveData<SignatureState>()
    val signatureState: LiveData<SignatureState> = _signatureState
    
    // URI de la firma guardada
    private val _signatureUri = MutableLiveData<Uri?>()
    val signatureUri: LiveData<Uri?> = _signatureUri
    
    // Bitmap de la firma actual
    private val _currentSignature = MutableLiveData<Bitmap?>()
    val currentSignature: LiveData<Bitmap?> = _currentSignature
    
    /**
     * Estados posibles de las operaciones con firmas.
     */
    sealed class SignatureState {
        object Idle : SignatureState()
        object Loading : SignatureState()
        data class Success(val uri: Uri) : SignatureState()
        data class Error(val message: String) : SignatureState()
    }
    
    init {
        // Iniciar en estado Idle
        _signatureState.value = SignatureState.Idle
    }
    
    /**
     * Establece la firma actual.
     */
    fun setCurrentSignature(bitmap: Bitmap?) {
        _currentSignature.value = bitmap
    }
    
    /**
     * Guarda la firma actual como imagen.
     */
    fun saveSignature(taskId: Int, userId: Int) {
        val bitmap = _currentSignature.value
        
        if (bitmap == null) {
            _signatureState.value = SignatureState.Error("No hay firma para guardar")
            return
        }
        
        _signatureState.value = SignatureState.Loading
        
        viewModelScope.launch {
            try {
                // Generar nombre de archivo con timestamp
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "signature_${taskId}_${userId}_${timestamp}.png"
                
                // Guardar bitmap en archivo
                val file = withContext(Dispatchers.IO) {
                    val outputFile = File(getApplication<Application>().cacheDir, fileName)
                    FileUtils.createFileFromBitmap(getApplication(), bitmap, "signature_${taskId}_${userId}_")
                }
                
                if (file != null) {
                    val uri = Uri.fromFile(file)
                    _signatureUri.value = uri
                    _signatureState.value = SignatureState.Success(uri)
                    Log.d(TAG, "Firma guardada: ${uri.path}")
                } else {
                    _signatureState.value = SignatureState.Error("Error al guardar la firma")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al guardar firma", e)
                _signatureState.value = SignatureState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Borra la firma actual.
     */
    fun clearSignature() {
        _currentSignature.value = null
        _signatureUri.value = null
        _signatureState.value = SignatureState.Idle
    }
    
    /**
     * Carga una firma desde URI.
     */
    fun loadSignatureFromUri(uri: Uri) {
        _signatureState.value = SignatureState.Loading
        
        viewModelScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                    android.graphics.BitmapFactory.decodeStream(inputStream)
                }
                
                if (bitmap != null) {
                    _currentSignature.value = bitmap
                    _signatureUri.value = uri
                    _signatureState.value = SignatureState.Success(uri)
                } else {
                    _signatureState.value = SignatureState.Error("No se pudo cargar la imagen de firma")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar firma desde URI", e)
                _signatureState.value = SignatureState.Error("Error: ${e.message}")
            }
        }
    }
}