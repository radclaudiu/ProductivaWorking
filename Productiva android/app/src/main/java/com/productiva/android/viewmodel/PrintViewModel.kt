package com.productiva.android.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.productiva.android.ProductivaApplication
import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.Product
import com.productiva.android.repository.LabelTemplateRepository
import com.productiva.android.repository.ProductRepository
import com.productiva.android.repository.ResourceState
import com.productiva.android.services.BrotherPrintService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de impresión de etiquetas.
 */
class PrintViewModel(application: Application) : AndroidViewModel(application) {
    
    private val TAG = "PrintViewModel"
    
    // Repositorios
    private val labelTemplateRepository: LabelTemplateRepository
    private val productRepository: ProductRepository
    
    // Servicio de impresión
    private val printerService = BrotherPrintService(application)
    
    // Estado de impresión
    private val _printState = MutableStateFlow<PrintState>(PrintState.Idle)
    val printState: StateFlow<PrintState> = _printState
    
    // Impresoras encontradas
    private val _printers = MutableLiveData<List<BrotherPrintService.PrinterDevice>>(emptyList())
    val printers: LiveData<List<BrotherPrintService.PrinterDevice>> = _printers
    
    // Impresora seleccionada
    private val _selectedPrinter = MutableLiveData<BrotherPrintService.PrinterDevice?>(null)
    val selectedPrinter: LiveData<BrotherPrintService.PrinterDevice?> = _selectedPrinter
    
    // Plantillas disponibles
    val labelTemplates: LiveData<List<LabelTemplate>>
    
    // Plantilla seleccionada
    private val _selectedTemplate = MutableLiveData<LabelTemplate?>(null)
    val selectedTemplate: LiveData<LabelTemplate?> = _selectedTemplate
    
    init {
        val app = getApplication<ProductivaApplication>()
        val database = app.database
        val apiService = app.apiService
        
        // Inicializar repositorios
        labelTemplateRepository = LabelTemplateRepository(database.labelTemplateDao(), apiService)
        productRepository = ProductRepository(database.productDao(), apiService)
        
        // Inicializar observadores
        labelTemplates = labelTemplateRepository.getAllLabelTemplates().asLiveData()
    }
    
    /**
     * Busca impresoras Brother disponibles en la red.
     */
    fun searchPrinters() {
        _printState.value = PrintState.Searching
        
        viewModelScope.launch {
            try {
                val foundPrinters = printerService.searchNetworkPrinters()
                _printers.value = foundPrinters
                
                if (foundPrinters.isNotEmpty()) {
                    _printState.value = PrintState.PrintersFound(foundPrinters.size)
                } else {
                    _printState.value = PrintState.Error("No se encontraron impresoras Brother")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al buscar impresoras", e)
                _printState.value = PrintState.Error("Error al buscar impresoras: ${e.message}")
            }
        }
    }
    
    /**
     * Selecciona una impresora para usar.
     */
    fun selectPrinter(printer: BrotherPrintService.PrinterDevice) {
        _selectedPrinter.value = printer
        Log.d(TAG, "Impresora seleccionada: ${printer.name} (${printer.ipAddress})")
    }
    
    /**
     * Selecciona una plantilla para usar.
     */
    fun selectTemplate(template: LabelTemplate) {
        _selectedTemplate.value = template
        Log.d(TAG, "Plantilla seleccionada: ${template.name}")
        
        // Incrementar contador de uso
        viewModelScope.launch {
            try {
                labelTemplateRepository.updateLabelTemplateUsage(template.id).collect { state ->
                    if (state is ResourceState.Error) {
                        Log.e(TAG, "Error al actualizar contador de uso: ${state.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al actualizar contador de uso", e)
            }
        }
    }
    
    /**
     * Sincroniza plantillas de etiquetas con el servidor.
     */
    fun syncTemplates() {
        _printState.value = PrintState.Syncing
        
        viewModelScope.launch {
            try {
                labelTemplateRepository.syncLabelTemplates().collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            Log.d(TAG, "Plantillas sincronizadas: ${state.data?.size}")
                            _printState.value = PrintState.Idle
                        }
                        is ResourceState.Error -> {
                            Log.e(TAG, "Error al sincronizar plantillas: ${state.message}")
                            _printState.value = PrintState.Error(state.message ?: "Error al sincronizar plantillas")
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al sincronizar plantillas", e)
                _printState.value = PrintState.Error("Error al sincronizar plantillas: ${e.message}")
            }
        }
    }
    
    /**
     * Imprime una etiqueta para un producto.
     */
    fun printLabel(product: Product) {
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
        
        _printState.value = PrintState.Printing
        
        viewModelScope.launch {
            try {
                printerService.printLabel(
                    template = template,
                    product = product,
                    printerIP = printer.ipAddress
                ) { success, message ->
                    if (success) {
                        _printState.value = PrintState.Success(message)
                    } else {
                        _printState.value = PrintState.Error(message)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al imprimir etiqueta", e)
                _printState.value = PrintState.Error("Error al imprimir etiqueta: ${e.message}")
            }
        }
    }
    
    /**
     * Obtiene el estado de la impresora.
     */
    fun checkPrinterStatus() {
        val printer = _selectedPrinter.value ?: return
        
        viewModelScope.launch {
            try {
                val status = printerService.getPrinterStatus(printer.ipAddress)
                Log.d(TAG, "Estado de impresora: ${status.errorCode}")
                
                if (status.errorCode == 0) {
                    _printState.value = PrintState.Ready
                } else {
                    _printState.value = PrintState.Error("Error en impresora: ${status.errorCode}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener estado de impresora", e)
                _printState.value = PrintState.Error("Error al comprobar impresora: ${e.message}")
            }
        }
    }
}

/**
 * Estados posibles de impresión.
 */
sealed class PrintState {
    object Idle : PrintState()
    object Searching : PrintState()
    data class PrintersFound(val count: Int) : PrintState()
    object Syncing : PrintState()
    object Printing : PrintState()
    object Ready : PrintState()
    data class Success(val message: String) : PrintState()
    data class Error(val message: String) : PrintState()
}