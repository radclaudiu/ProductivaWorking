package com.productiva.android.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.brother.sdk.BrotherAndroidLib
import com.brother.sdk.LabelInfo
import com.brother.sdk.Printer
import com.brother.sdk.PrinterDriver
import com.brother.sdk.PrinterInfo
import com.brother.sdk.PrinterStatus
import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.SavedPrinter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Servicio para la gestión de impresoras Brother y el proceso de impresión.
 */
class BrotherPrintService(private val context: Context) {
    
    private val TAG = "BrotherPrintService"
    
    // Inicialización de la biblioteca Brother
    init {
        try {
            BrotherAndroidLib.initialize(context)
            Log.d(TAG, "Biblioteca Brother inicializada correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar biblioteca Brother", e)
        }
    }
    
    /**
     * Busca impresoras Brother cercanas.
     */
    suspend fun discoverPrinters(): List<Printer> = withContext(Dispatchers.IO) {
        try {
            val printers = BrotherAndroidLib.getPrinters(
                PrinterInfo.ConnectionType.BLUETOOTH,
                5000 // Timeout de 5 segundos
            )
            Log.d(TAG, "Impresoras descubiertas: ${printers.size}")
            return@withContext printers
        } catch (e: Exception) {
            Log.e(TAG, "Error al buscar impresoras", e)
            return@withContext emptyList()
        }
    }
    
    /**
     * Convierte una impresora Brother a un objeto SavedPrinter.
     */
    fun convertToSavedPrinter(printer: Printer): SavedPrinter {
        return SavedPrinter(
            id = 0, // ID se asignará al guardar en la base de datos
            name = printer.name,
            model = printer.modelName,
            address = printer.ipAddress ?: printer.macAddress ?: "",
            connectionType = when {
                printer.isBluetoothPrinter -> SavedPrinter.CONNECTION_TYPE_BLUETOOTH
                printer.isNetworkPrinter -> SavedPrinter.CONNECTION_TYPE_WIFI
                else -> SavedPrinter.CONNECTION_TYPE_USB
            },
            lastUsed = System.currentTimeMillis(),
            isDefault = false,
            paperSize = SavedPrinter.PAPER_SIZE_STANDARD,
            printDensity = 100,
            printSpeed = 3,
            cutMode = SavedPrinter.CUT_MODE_FULL
        )
    }
    
    /**
     * Imprime una plantilla de etiqueta con los datos proporcionados.
     */
    suspend fun printTemplate(
        printer: SavedPrinter,
        template: LabelTemplate,
        data: Map<String, String>
    ): PrintResult = withContext(Dispatchers.IO) {
        try {
            // 1. Renderizar plantilla
            val bitmap = renderTemplate(template, data)
            if (bitmap == null) {
                return@withContext PrintResult.RenderError("No se pudo renderizar la plantilla")
            }
            
            // 2. Conectar con la impresora
            val brotherPrinter = connectToPrinter(printer)
            if (brotherPrinter == null) {
                return@withContext PrintResult.PrinterNotReady("No se pudo conectar a la impresora")
            }
            
            // 3. Configurar opciones de impresión
            val printerDriver = PrinterDriver(brotherPrinter)
            val labelInfo = LabelInfo().apply {
                // Configurar según las propiedades de SavedPrinter
                width = when (printer.paperSize) {
                    SavedPrinter.PAPER_SIZE_NARROW -> 29
                    SavedPrinter.PAPER_SIZE_WIDE -> 62
                    else -> 54 // Estándar
                }
                length = 40 // mm
                margin = 3  // mm
                // Otras configuraciones de la etiqueta...
            }
            
            // 4. Imprimir
            val status = printerDriver.printImage(bitmap, labelInfo)
            
            // 5. Verificar resultado
            return@withContext if (status.error == PrinterStatus.ErrorCode.ERROR_NONE) {
                PrintResult.Success
            } else {
                PrintResult.Error("Error al imprimir: ${status.error.name}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en proceso de impresión", e)
            return@withContext PrintResult.Error("Error: ${e.message}")
        }
    }
    
    /**
     * Imprime una imagen desde un URI.
     */
    suspend fun printImageFromUri(
        printer: SavedPrinter,
        imageUri: Uri
    ): PrintResult = withContext(Dispatchers.IO) {
        try {
            // 1. Cargar imagen desde URI
            val bitmap = loadImageFromUri(imageUri)
            if (bitmap == null) {
                return@withContext PrintResult.RenderError("No se pudo cargar la imagen")
            }
            
            // 2. Conectar con la impresora
            val brotherPrinter = connectToPrinter(printer)
            if (brotherPrinter == null) {
                return@withContext PrintResult.PrinterNotReady("No se pudo conectar a la impresora")
            }
            
            // 3. Configurar opciones de impresión
            val printerDriver = PrinterDriver(brotherPrinter)
            val labelInfo = LabelInfo().apply {
                // Configurar según las propiedades de SavedPrinter
                width = when (printer.paperSize) {
                    SavedPrinter.PAPER_SIZE_NARROW -> 29
                    SavedPrinter.PAPER_SIZE_WIDE -> 62
                    else -> 54 // Estándar
                }
                length = 40 // mm
                margin = 3  // mm
                // Otras configuraciones de la etiqueta...
            }
            
            // 4. Imprimir
            val status = printerDriver.printImage(bitmap, labelInfo)
            
            // 5. Verificar resultado
            return@withContext if (status.error == PrinterStatus.ErrorCode.ERROR_NONE) {
                PrintResult.Success
            } else {
                PrintResult.Error("Error al imprimir: ${status.error.name}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al imprimir imagen", e)
            return@withContext PrintResult.Error("Error: ${e.message}")
        }
    }
    
    /**
     * Conecta a una impresora Brother según la configuración guardada.
     */
    private fun connectToPrinter(printer: SavedPrinter): Printer? {
        try {
            // Crear información de conexión según el tipo
            val connectionType = when (printer.connectionType) {
                SavedPrinter.CONNECTION_TYPE_BLUETOOTH -> PrinterInfo.ConnectionType.BLUETOOTH
                SavedPrinter.CONNECTION_TYPE_WIFI -> PrinterInfo.ConnectionType.NETWORK
                SavedPrinter.CONNECTION_TYPE_USB -> PrinterInfo.ConnectionType.USB
                else -> PrinterInfo.ConnectionType.BLUETOOTH
            }
            
            // Crear objeto de información de impresora
            val printerInfo = PrinterInfo().apply {
                this.connectionType = connectionType
                ipAddress = if (connectionType == PrinterInfo.ConnectionType.NETWORK) printer.address else ""
                macAddress = if (connectionType == PrinterInfo.ConnectionType.BLUETOOTH) printer.address else ""
                modelName = printer.model
                // Otras configuraciones...
            }
            
            // Conectar y obtener objeto Printer
            return BrotherAndroidLib.getPrinter(printerInfo, 5000) // Timeout de 5 segundos
        } catch (e: Exception) {
            Log.e(TAG, "Error al conectar a impresora: ${printer.name}", e)
            return null
        }
    }
    
    /**
     * Carga una imagen desde un URI.
     */
    private fun loadImageFromUri(uri: Uri): Bitmap? {
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            return BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar imagen desde URI", e)
            return null
        } finally {
            inputStream?.close()
        }
    }
    
    /**
     * Renderiza una plantilla de etiqueta con los datos proporcionados.
     */
    private fun renderTemplate(template: LabelTemplate, data: Map<String, String>): Bitmap? {
        // Implementación básica. En una versión completa, aquí se renderizaría
        // la plantilla con los datos proporcionados.
        // Por ahora, retornamos un bitmap de ejemplo o null
        return null
    }
    
    /**
     * Verifica el estado de una impresora.
     */
    suspend fun checkPrinterStatus(printer: SavedPrinter): PrinterStatus = withContext(Dispatchers.IO) {
        try {
            val brotherPrinter = connectToPrinter(printer)
            if (brotherPrinter != null) {
                val printerDriver = PrinterDriver(brotherPrinter)
                return@withContext printerDriver.getPrinterStatus()
            }
            return@withContext PrinterStatus().apply {
                error = PrinterStatus.ErrorCode.ERROR_COMMUNICATION
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar estado de impresora", e)
            return@withContext PrinterStatus().apply {
                error = PrinterStatus.ErrorCode.ERROR_INTERNAL
            }
        }
    }
    
    /**
     * Clase sellada para representar el resultado de las operaciones de impresión.
     */
    sealed class PrintResult {
        object Success : PrintResult()
        data class Error(val message: String) : PrintResult()
        data class PrinterNotReady(val message: String) : PrintResult()
        data class RenderError(val message: String) : PrintResult()
    }
}