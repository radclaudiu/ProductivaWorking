package com.productiva.android.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.productiva.android.ProductivaApplication
import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.SavedPrinter
import com.productiva.android.network.RetrofitClient
import com.productiva.android.repository.LabelTemplateRepository
import com.productiva.android.repository.PrinterRepository
import com.productiva.android.repository.ResourceState
import com.productiva.android.services.BrotherPrintService
import com.brother.sdk.Printer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de impresión de etiquetas.
 */
class PrintViewModel(application: Application) : AndroidViewModel(application) {
    
    private val TAG = "PrintViewModel"
    
    // Repositorios
    private val printerRepository: PrinterRepository
    private val labelTemplateRepository: LabelTemplateRepository
    private val printService: BrotherPrintService
    
    // Estado de la operación de impresión
    private val _printState = MutableLiveData<PrintState>()
    val printState: LiveData<PrintState> = _printState
    
    // Impresora seleccionada actualmente
    private val _selectedPrinter = MutableLiveData<SavedPrinter?>()
    val selectedPrinter: LiveData<SavedPrinter?> = _selectedPrinter
    
    // Plantilla seleccionada actualmente
    private val _selectedTemplate = MutableLiveData<LabelTemplate?>()
    val selectedTemplate: LiveData<LabelTemplate?> = _selectedTemplate
    
    // Lista de impresoras disponibles
    private val _availablePrinters = MutableLiveData<List<SavedPrinter>>()
    val availablePrinters: LiveData<List<SavedPrinter>> = _availablePrinters
    
    // Lista de plantillas disponibles
    private val _availableTemplates = MutableLiveData<List<LabelTemplate>>()
    val availableTemplates: LiveData<List<LabelTemplate>> = _availableTemplates
    
    // Lista de impresoras descubiertas (no guardadas)
    private val _discoveredPrinters = MutableLiveData<List<Printer>>()
    val discoveredPrinters: LiveData<List<Printer>> = _discoveredPrinters
    
    // Evento de guardado de impresora
    private val _printerSavedEvent = MutableLiveData<Event<SavedPrinter>>()
    val printerSavedEvent: LiveData<Event<SavedPrinter>> = _printerSavedEvent
    
    /**
     * Estados posibles de las operaciones de impresión.
     */
    sealed class PrintState {
        object Idle : PrintState()
        object Loading : PrintState()
        object Success : PrintState()
        data class Error(val message: String) : PrintState()
        data class PrinterNotReady(val message: String) : PrintState()
        object DiscoveryStarted : PrintState()
        data class DiscoveryComplete(val count: Int) : PrintState()
        data class PrinterSaved(val printer: SavedPrinter) : PrintState()
    }
    
    /**
     * Clase para eventos de un solo uso.
     */
    class Event<out T>(private val content: T) {
        var hasBeenHandled = false
            private set
        
        fun getContentIfNotHandled(): T? {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                content
            }
        }
        
        fun peekContent(): T = content
    }
    
    init {
        // Obtener instancias de la aplicación
        val app = getApplication<ProductivaApplication>()
        val apiService = RetrofitClient.getApiService(app)
        val database = app.database
        
        // Inicializar repositorios
        printerRepository = PrinterRepository(database.printerDao())
        labelTemplateRepository = LabelTemplateRepository(database.labelTemplateDao(), apiService)
        printService = BrotherPrintService(app)
        
        // Iniciar en estado Idle
        _printState.value = PrintState.Idle
        
        // Cargar impresoras guardadas
        loadSavedPrinters()
        
        // Cargar plantillas
        loadTemplates()
    }
    
    /**
     * Carga las impresoras guardadas.
     */
    private fun loadSavedPrinters() {
        viewModelScope.launch {
            try {
                // Verificar primero si hay una impresora predeterminada
                val defaultPrinter = printerRepository.getDefaultPrinter()
                if (defaultPrinter != null) {
                    _selectedPrinter.value = defaultPrinter
                }
                
                // Observar la lista de impresoras guardadas
                printerRepository.getAllPrinters().observeForever { printers ->
                    _availablePrinters.value = printers
                    
                    // Si no hay impresora seleccionada, seleccionar la primera
                    if (_selectedPrinter.value == null && printers.isNotEmpty()) {
                        _selectedPrinter.value = printers.first()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar impresoras guardadas", e)
            }
        }
    }
    
    /**
     * Carga las plantillas de etiquetas.
     */
    private fun loadTemplates() {
        viewModelScope.launch {
            try {
                // Observar la lista de plantillas
                labelTemplateRepository.getAllTemplates().observeForever { templates ->
                    _availableTemplates.value = templates
                    
                    // Si no hay plantilla seleccionada, seleccionar la primera
                    if (_selectedTemplate.value == null && templates.isNotEmpty()) {
                        _selectedTemplate.value = templates.first()
                    }
                }
                
                // Intentar sincronizar plantillas desde el servidor
                labelTemplateRepository.syncLabelTemplates().collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            Log.d(TAG, "Sincronización de plantillas completada: ${state.data?.size ?: 0} plantillas")
                        }
                        is ResourceState.Error -> {
                            Log.e(TAG, "Error en sincronización de plantillas: ${state.message}")
                        }
                        else -> {} // Estado de carga, no hacer nada
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar plantillas", e)
            }
        }
    }
    
    /**
     * Busca impresoras Brother cercanas.
     */
    fun discoverPrinters() {
        _printState.value = PrintState.DiscoveryStarted
        _discoveredPrinters.value = emptyList()
        
        viewModelScope.launch {
            try {
                val printers = printService.discoverPrinters()
                _discoveredPrinters.value = printers
                _printState.value = PrintState.DiscoveryComplete(printers.size)
            } catch (e: Exception) {
                Log.e(TAG, "Error al descubrir impresoras", e)
                _printState.value = PrintState.Error("Error al buscar impresoras: ${e.message}")
            }
        }
    }
    
    /**
     * Guarda una impresora descubierta.
     */
    fun savePrinter(brotherPrinter: Printer) {
        val savedPrinter = printService.convertToSavedPrinter(brotherPrinter)
        
        viewModelScope.launch {
            try {
                printerRepository.saveOrUpdatePrinter(savedPrinter).collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            val printer = state.data!!
                            _printerSavedEvent.value = Event(printer)
                            _printState.value = PrintState.PrinterSaved(printer)
                        }
                        is ResourceState.Error -> {
                            _printState.value = PrintState.Error(
                                state.message ?: "Error desconocido al guardar impresora"
                            )
                        }
                        is ResourceState.Loading -> {
                            _printState.value = PrintState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al guardar impresora", e)
                _printState.value = PrintState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Establece una impresora como predeterminada.
     */
    fun setDefaultPrinter(printerId: Int) {
        viewModelScope.launch {
            try {
                printerRepository.setDefaultPrinter(printerId).collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            loadSavedPrinters() // Recargar impresoras
                        }
                        is ResourceState.Error -> {
                            _printState.value = PrintState.Error(
                                state.message ?: "Error desconocido al establecer impresora predeterminada"
                            )
                        }
                        else -> {} // Estado de carga, no hacer nada
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al establecer impresora predeterminada", e)
                _printState.value = PrintState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Selecciona una impresora.
     */
    fun selectPrinter(printer: SavedPrinter) {
        _selectedPrinter.value = printer
    }
    
    /**
     * Selecciona una plantilla.
     */
    fun selectTemplate(template: LabelTemplate) {
        _selectedTemplate.value = template
    }
    
    /**
     * Imprime una plantilla con los datos proporcionados.
     */
    fun printTemplate(data: Map<String, String>) {
        val printer = _selectedPrinter.value
        val template = _selectedTemplate.value
        
        if (printer == null) {
            _printState.value = PrintState.Error("No hay impresora seleccionada")
            return
        }
        
        if (template == null) {
            _printState.value = PrintState.Error("No hay plantilla seleccionada")
            return
        }
        
        _printState.value = PrintState.Loading
        
        viewModelScope.launch {
            try {
                // Actualizar contador de uso de la plantilla
                labelTemplateRepository.updateUsage(template.id)
                
                // Actualizar último uso de la impresora
                printerRepository.updateLastUsed(printer.id)
                
                // Imprimir plantilla
                val result = printService.printTemplate(printer, template, data)
                
                when (result) {
                    is BrotherPrintService.PrintResult.Success -> {
                        _printState.value = PrintState.Success
                    }
                    is BrotherPrintService.PrintResult.Error -> {
                        _printState.value = PrintState.Error(result.message)
                    }
                    is BrotherPrintService.PrintResult.PrinterNotReady -> {
                        _printState.value = PrintState.PrinterNotReady(result.message)
                    }
                    is BrotherPrintService.PrintResult.RenderError -> {
                        _printState.value = PrintState.Error("Error al renderizar la etiqueta: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al imprimir plantilla", e)
                _printState.value = PrintState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Imprime una imagen desde URI.
     */
    fun printImage(imageUri: Uri) {
        val printer = _selectedPrinter.value
        
        if (printer == null) {
            _printState.value = PrintState.Error("No hay impresora seleccionada")
            return
        }
        
        _printState.value = PrintState.Loading
        
        viewModelScope.launch {
            try {
                // Actualizar último uso de la impresora
                printerRepository.updateLastUsed(printer.id)
                
                // Imprimir imagen
                val result = printService.printImageFromUri(printer, imageUri)
                
                when (result) {
                    is BrotherPrintService.PrintResult.Success -> {
                        _printState.value = PrintState.Success
                    }
                    is BrotherPrintService.PrintResult.Error -> {
                        _printState.value = PrintState.Error(result.message)
                    }
                    is BrotherPrintService.PrintResult.PrinterNotReady -> {
                        _printState.value = PrintState.PrinterNotReady(result.message)
                    }
                    is BrotherPrintService.PrintResult.RenderError -> {
                        _printState.value = PrintState.Error("Error al procesar la imagen: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al imprimir imagen", e)
                _printState.value = PrintState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Elimina una impresora guardada.
     */
    fun deletePrinter(printerId: Int) {
        viewModelScope.launch {
            try {
                printerRepository.deletePrinter(printerId).collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            // Si era la impresora seleccionada, deseleccionar
                            if (_selectedPrinter.value?.id == printerId) {
                                _selectedPrinter.value = null
                            }
                            
                            _printState.value = PrintState.Success
                        }
                        is ResourceState.Error -> {
                            _printState.value = PrintState.Error(
                                state.message ?: "Error desconocido al eliminar impresora"
                            )
                        }
                        is ResourceState.Loading -> {
                            _printState.value = PrintState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar impresora", e)
                _printState.value = PrintState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Marca una plantilla como favorita o no.
     */
    fun setTemplateFavorite(templateId: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                labelTemplateRepository.setFavorite(templateId, isFavorite)
            } catch (e: Exception) {
                Log.e(TAG, "Error al marcar plantilla como favorita", e)
            }
        }
    }
}