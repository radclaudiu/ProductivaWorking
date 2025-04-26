package com.productiva.android.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.SavedPrinter
import com.productiva.android.repository.LabelTemplateRepository
import com.productiva.android.repository.PrinterRepository
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel para la impresión de etiquetas
 */
class PrintLabelViewModel(application: Application) : AndroidViewModel(application) {
    
    private val printerRepository = PrinterRepository(application)
    private val templateRepository = LabelTemplateRepository(application)
    
    // Impresoras guardadas
    val savedPrinters = printerRepository.getAllPrinters()
    
    // Plantillas de etiquetas
    val labelTemplates = templateRepository.getAllTemplates()
    
    // Impresora seleccionada
    private val _selectedPrinter = MutableLiveData<SavedPrinter>()
    val selectedPrinter: LiveData<SavedPrinter> get() = _selectedPrinter
    
    // Plantilla seleccionada
    private val _selectedTemplate = MutableLiveData<LabelTemplate>()
    val selectedTemplate: LiveData<LabelTemplate> get() = _selectedTemplate
    
    // Estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading
    
    // Mensaje de error
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
    
    // Estado de impresión
    private val _printingState = MutableLiveData<PrintingState>()
    val printingState: LiveData<PrintingState> get() = _printingState
    
    // Ruta del archivo generado
    private val _generatedFilePath = MutableLiveData<String>()
    val generatedFilePath: LiveData<String> get() = _generatedFilePath
    
    init {
        loadDefaultPrinter()
        refreshTemplates()
    }
    
    /**
     * Carga la impresora predeterminada
     */
    private fun loadDefaultPrinter() {
        viewModelScope.launch {
            try {
                val defaultPrinter = printerRepository.getDefaultPrinter()
                _selectedPrinter.value = defaultPrinter
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al cargar impresora predeterminada"
            }
        }
    }
    
    /**
     * Refresca las plantillas desde el servidor
     */
    fun refreshTemplates() {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val result = templateRepository.syncTemplates()
                if (result.isFailure) {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Error al sincronizar plantillas"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Selecciona una impresora
     */
    fun selectPrinter(printer: SavedPrinter) {
        _selectedPrinter.value = printer
        
        viewModelScope.launch {
            try {
                // Actualizar timestamp de último uso
                printerRepository.updateLastUsed(printer.id)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al actualizar impresora"
            }
        }
    }
    
    /**
     * Selecciona una plantilla
     */
    fun selectTemplate(template: LabelTemplate) {
        _selectedTemplate.value = template
    }
    
    /**
     * Carga una plantilla por su ID
     */
    fun loadTemplateById(templateId: Int) {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // Buscar primero en la base de datos local
                var template = templateRepository.getTemplateById(templateId)
                
                if (template == null) {
                    // Si no está en la BD local, buscar en el servidor
                    val result = templateRepository.fetchTemplateById(templateId)
                    if (result.isSuccess) {
                        template = result.getOrNull()
                    } else {
                        _errorMessage.value = result.exceptionOrNull()?.message ?: "Error al cargar plantilla"
                    }
                }
                
                template?.let {
                    _selectedTemplate.value = it
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Guarda una impresora
     */
    fun savePrinter(printer: SavedPrinter) {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val id = printerRepository.savePrinter(printer)
                
                // Cargar la impresora guardada
                val savedPrinter = printerRepository.getPrinterById(id.toInt())
                _selectedPrinter.value = savedPrinter
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al guardar impresora"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Establece una impresora como predeterminada
     */
    fun setDefaultPrinter(printer: SavedPrinter) {
        viewModelScope.launch {
            try {
                printerRepository.setAsDefault(printer.id)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al establecer impresora predeterminada"
            }
        }
    }
    
    /**
     * Genera un PDF con la etiqueta para imprimir
     */
    fun generateLabelPdf(data: Map<String, String>): File? {
        try {
            val template = _selectedTemplate.value ?: return null
            val printer = _selectedPrinter.value ?: return null
            
            // TODO: Implementar generación real de PDF con los datos de la plantilla
            // Por ahora, generar un PDF simple
            
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(
                printer.paperWidth * 10,
                printer.paperHeight * 10,
                1
            ).create()
            
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            // Dibujar fondo blanco
            canvas.drawColor(Color.WHITE)
            
            // Dibujar algunos datos
            val paint = Paint()
            paint.color = Color.BLACK
            paint.textSize = 24f
            
            var y = 50f
            data.forEach { (key, value) ->
                canvas.drawText("$key: $value", 50f, y, paint)
                y += 40f
            }
            
            pdfDocument.finishPage(page)
            
            // Guardar PDF en almacenamiento
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "LABEL_${template.id}_$timeStamp.pdf"
            
            val storageDir = getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            storageDir?.mkdirs()
            
            val file = File(storageDir, fileName)
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            
            _generatedFilePath.value = file.absolutePath
            _printingState.value = PrintingState.GENERATED
            
            return file
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Error al generar etiqueta"
            _printingState.value = PrintingState.ERROR
            return null
        }
    }
    
    /**
     * Imprime una etiqueta
     */
    fun printLabel(data: Map<String, String>) {
        _printingState.value = PrintingState.PRINTING
        
        // Generar archivo primero
        val file = generateLabelPdf(data)
        
        if (file != null) {
            // TODO: Implementar integración real con la impresora
            // Por ahora, simular impresión
            
            viewModelScope.launch {
                try {
                    // Simular proceso de impresión
                    _printingState.value = PrintingState.SENT
                } catch (e: Exception) {
                    _errorMessage.value = e.message ?: "Error al imprimir etiqueta"
                    _printingState.value = PrintingState.ERROR
                }
            }
        } else {
            _printingState.value = PrintingState.ERROR
        }
    }
    
    /**
     * Elimina una impresora
     */
    fun deletePrinter(printerId: Int) {
        viewModelScope.launch {
            try {
                printerRepository.deletePrinter(printerId)
                
                // Si era la impresora seleccionada, limpiar selección
                if (_selectedPrinter.value?.id == printerId) {
                    _selectedPrinter.value = null
                    loadDefaultPrinter()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al eliminar impresora"
            }
        }
    }
    
    /**
     * Resetea el estado de impresión
     */
    fun resetPrintingState() {
        _printingState.value = PrintingState.IDLE
    }
    
    /**
     * Estados de impresión
     */
    enum class PrintingState {
        IDLE,
        GENERATING,
        GENERATED,
        PRINTING,
        SENT,
        ERROR
    }
}