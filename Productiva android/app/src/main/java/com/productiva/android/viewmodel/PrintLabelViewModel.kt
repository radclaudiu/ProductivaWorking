package com.productiva.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.SavedPrinter
import com.productiva.android.model.Task
import com.productiva.android.repository.LabelTemplateRepository
import com.productiva.android.repository.PrinterRepository
import com.productiva.android.repository.TaskRepository
import com.productiva.android.util.PrinterManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de impresión de etiquetas
 */
class PrintLabelViewModel(application: Application) : AndroidViewModel(application) {
    
    private val printerRepository = PrinterRepository(application)
    private val labelTemplateRepository = LabelTemplateRepository(application)
    private val taskRepository = TaskRepository(application)
    private val printerManager = PrinterManager(application)
    
    // Tarea actual
    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task
    
    // Impresoras disponibles
    private val _printers = MutableLiveData<List<SavedPrinter>>()
    val printers: LiveData<List<SavedPrinter>> = _printers
    
    // Plantillas disponibles
    private val _templates = MutableLiveData<List<LabelTemplate>>()
    val templates: LiveData<List<LabelTemplate>> = _templates
    
    // Impresora seleccionada
    private val _selectedPrinter = MutableLiveData<SavedPrinter?>()
    val selectedPrinter: LiveData<SavedPrinter?> = _selectedPrinter
    
    // Plantilla seleccionada
    private val _selectedTemplate = MutableLiveData<LabelTemplate?>()
    val selectedTemplate: LiveData<LabelTemplate?> = _selectedTemplate
    
    // Estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Mensaje de error
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    // Estado de impresión
    private val _printState = MutableLiveData<PrintState>()
    val printState: LiveData<PrintState> = _printState
    
    // Número de copias
    private val _copies = MutableLiveData<Int>(1)
    val copies: LiveData<Int> = _copies
    
    init {
        loadPrinters()
        loadTemplates()
    }
    
    /**
     * Carga la tarea por su ID
     */
    fun loadTask(taskId: Int) {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val loadedTask = taskRepository.getTaskById(taskId)
                
                if (loadedTask != null) {
                    _task.value = loadedTask
                    _isLoading.value = false
                } else {
                    _errorMessage.value = "No se encontró la tarea"
                    _isLoading.value = false
                    
                    // Intentar cargar desde el servidor
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
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al cargar la tarea"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Carga las impresoras guardadas
     */
    fun loadPrinters() {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val savedPrinters = printerRepository.getAllPrinters().value ?: emptyList()
                _printers.value = savedPrinters
                _isLoading.value = false
                
                // Seleccionar la última impresora usada si existe
                val lastPrinterId = getLastUsedPrinterId()
                if (lastPrinterId != -1) {
                    val lastPrinter = savedPrinters.find { it.id == lastPrinterId }
                    if (lastPrinter != null) {
                        _selectedPrinter.value = lastPrinter
                    }
                }
                
                // Si no hay impresoras guardadas, mostrar mensaje
                if (savedPrinters.isEmpty()) {
                    _errorMessage.value = "No hay impresoras guardadas. Añade una en la configuración."
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al cargar impresoras"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Carga las plantillas de etiquetas
     */
    fun loadTemplates() {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val labelTemplates = labelTemplateRepository.getAllTemplates().value ?: emptyList()
                _templates.value = labelTemplates
                _isLoading.value = false
                
                // Seleccionar la última plantilla usada si existe
                val lastTemplateId = getLastUsedTemplateId()
                if (lastTemplateId != -1) {
                    val lastTemplate = labelTemplates.find { it.id == lastTemplateId }
                    if (lastTemplate != null) {
                        _selectedTemplate.value = lastTemplate
                    }
                }
                
                // Si no hay plantillas, mostrar mensaje
                if (labelTemplates.isEmpty()) {
                    _errorMessage.value = "No hay plantillas guardadas. Añade una en la configuración."
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al cargar plantillas"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Selecciona una impresora
     */
    fun selectPrinter(printer: SavedPrinter) {
        _selectedPrinter.value = printer
        
        // Guardar selección
        saveLastUsedPrinterId(printer.id)
    }
    
    /**
     * Selecciona una plantilla
     */
    fun selectTemplate(template: LabelTemplate) {
        _selectedTemplate.value = template
        
        // Guardar selección
        saveLastUsedTemplateId(template.id)
    }
    
    /**
     * Establece el número de copias
     */
    fun setCopies(copies: Int) {
        _copies.value = copies
    }
    
    /**
     * Imprime la etiqueta
     */
    fun printLabel() {
        val currentTask = _task.value
        val printer = _selectedPrinter.value
        val template = _selectedTemplate.value
        val copyCount = _copies.value ?: 1
        
        if (currentTask == null) {
            _errorMessage.value = "No hay una tarea para imprimir"
            return
        }
        
        if (printer == null) {
            _errorMessage.value = "No hay una impresora seleccionada"
            return
        }
        
        if (template == null) {
            _errorMessage.value = "No hay una plantilla seleccionada"
            return
        }
        
        _isLoading.value = true
        _printState.value = PrintState.Printing
        
        viewModelScope.launch {
            try {
                // Procesar la plantilla con los datos de la tarea
                val processedTemplate = processTemplate(template, currentTask)
                
                // Imprimir usando PrinterManager
                val result = printerManager.printLabel(
                    printer = printer,
                    template = processedTemplate,
                    copies = copyCount
                )
                
                if (result) {
                    _printState.value = PrintState.Success
                } else {
                    _printState.value = PrintState.Error("Error al imprimir la etiqueta")
                    _errorMessage.value = "Error al imprimir la etiqueta"
                }
                
                _isLoading.value = false
            } catch (e: Exception) {
                _printState.value = PrintState.Error(e.message ?: "Error desconocido")
                _errorMessage.value = e.message ?: "Error desconocido"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Procesa la plantilla reemplazando variables con datos de la tarea
     */
    private fun processTemplate(template: LabelTemplate, task: Task): String {
        var processedTemplate = template.content
        
        // Reemplazar variables de la tarea
        processedTemplate = processedTemplate.replace("{{task.id}}", task.id.toString())
        processedTemplate = processedTemplate.replace("{{task.title}}", task.title)
        processedTemplate = processedTemplate.replace("{{task.description}}", task.description ?: "")
        processedTemplate = processedTemplate.replace("{{task.status}}", task.status)
        processedTemplate = processedTemplate.replace("{{task.location}}", task.locationName ?: "")
        
        // Formatear y reemplazar fecha
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        processedTemplate = processedTemplate.replace(
            "{{task.date}}",
            task.dueDate?.let { dateFormat.format(it) } ?: ""
        )
        
        // Fecha actual
        processedTemplate = processedTemplate.replace(
            "{{current.date}}",
            dateFormat.format(java.util.Date())
        )
        
        return processedTemplate
    }
    
    /**
     * Obtiene el ID de la última impresora utilizada
     */
    private fun getLastUsedPrinterId(): Int {
        return getApplication<Application>().getSharedPreferences("productiva_prefs", Application.MODE_PRIVATE)
            .getInt("last_printer_id", -1)
    }
    
    /**
     * Guarda el ID de la última impresora utilizada
     */
    private fun saveLastUsedPrinterId(printerId: Int) {
        getApplication<Application>().getSharedPreferences("productiva_prefs", Application.MODE_PRIVATE)
            .edit()
            .putInt("last_printer_id", printerId)
            .apply()
    }
    
    /**
     * Obtiene el ID de la última plantilla utilizada
     */
    private fun getLastUsedTemplateId(): Int {
        return getApplication<Application>().getSharedPreferences("productiva_prefs", Application.MODE_PRIVATE)
            .getInt("last_template_id", -1)
    }
    
    /**
     * Guarda el ID de la última plantilla utilizada
     */
    private fun saveLastUsedTemplateId(templateId: Int) {
        getApplication<Application>().getSharedPreferences("productiva_prefs", Application.MODE_PRIVATE)
            .edit()
            .putInt("last_template_id", templateId)
            .apply()
    }
    
    /**
     * Estados posibles del proceso de impresión
     */
    sealed class PrintState {
        object Initial : PrintState()
        object Printing : PrintState()
        object Success : PrintState()
        data class Error(val message: String) : PrintState()
    }
}