package com.productiva.android.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel para la captura de firmas
 */
class SignatureViewModel(application: Application) : AndroidViewModel(application) {
    
    // Ruta donde se guardará la firma
    private val _signatureFilePath = MutableLiveData<String>()
    val signatureFilePath: LiveData<String> get() = _signatureFilePath
    
    // Estado de guardado
    private val _isSaved = MutableLiveData<Boolean>()
    val isSaved: LiveData<Boolean> get() = _isSaved
    
    // Mensaje de error
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
    
    init {
        _isSaved.value = false
    }
    
    /**
     * Guarda la firma como imagen PNG
     */
    fun saveSignature(bitmap: Bitmap, taskId: Int): File? {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "SIGN_${taskId}_$timeStamp.png"
            
            // Crear directorio si no existe
            val storageDir = getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            storageDir?.mkdirs()
            
            val file = File(storageDir, fileName)
            val outputStream = FileOutputStream(file)
            
            // Crear copia del bitmap con fondo blanco para mejor visibilidad
            val signatureBitmap = createWhiteBackgroundBitmap(bitmap)
            
            // Guardar como PNG
            signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            
            _signatureFilePath.value = file.absolutePath
            _isSaved.value = true
            
            return file
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Error al guardar firma"
            return null
        }
    }
    
    /**
     * Crea un bitmap con fondo blanco y la firma encima
     */
    private fun createWhiteBackgroundBitmap(originalBitmap: Bitmap): Bitmap {
        val resultBitmap = Bitmap.createBitmap(
            originalBitmap.width,
            originalBitmap.height,
            Bitmap.Config.ARGB_8888
        )
        
        val canvas = Canvas(resultBitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(originalBitmap, 0f, 0f, null)
        
        return resultBitmap
    }
    
    /**
     * Obtiene un archivo de firma guardado
     */
    fun getSignatureFile(): File? {
        val path = _signatureFilePath.value ?: return null
        val file = File(path)
        return if (file.exists()) file else null
    }
    
    /**
     * Carga una firma guardada
     */
    fun loadSignature(path: String): Bitmap? {
        try {
            val file = File(path)
            if (file.exists()) {
                return BitmapFactory.decodeFile(file.absolutePath)
            }
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Error al cargar firma"
        }
        return null
    }
    
    /**
     * Elimina una firma guardada
     */
    fun deleteSignature() {
        try {
            val path = _signatureFilePath.value ?: return
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
            _signatureFilePath.value = null
            _isSaved.value = false
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Error al eliminar firma"
        }
    }
    
    /**
     * Resetea el estado de guardado
     */
    fun resetSaveState() {
        _isSaved.value = false
    }
}