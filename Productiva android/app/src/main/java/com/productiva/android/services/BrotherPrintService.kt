package com.productiva.android.services

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.Toast
import com.brother.ptouch.sdk.LabelInfo
import com.brother.ptouch.sdk.NetPrinter
import com.brother.ptouch.sdk.Printer
import com.brother.ptouch.sdk.PrinterInfo
import com.brother.sdk.BrotherPrintLibrary
import com.brother.sdk.printing.PrinterDriverGenerateResult
import com.brother.sdk.printing.PrinterModel
import com.productiva.android.ProductivaApplication
import com.productiva.android.model.SavedPrinter
import com.productiva.android.utils.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Servicio para gestionar la impresión en impresoras Brother.
 * Maneja la impresión en segundo plano para evitar bloquear la UI.
 */
class BrotherPrintService : IntentService("BrotherPrintService") {
    
    companion object {
        private const val TAG = "BrotherPrintService"
        
        // Acciones
        const val ACTION_PRINT_PDF = "com.productiva.android.action.PRINT_PDF"
        const val ACTION_PRINT_IMAGE = "com.productiva.android.action.PRINT_IMAGE"
        const val ACTION_FIND_PRINTERS = "com.productiva.android.action.FIND_PRINTERS"
        
        // Parámetros
        const val EXTRA_PRINTER_ID = "com.productiva.android.extra.PRINTER_ID"
        const val EXTRA_FILE_PATH = "com.productiva.android.extra.FILE_PATH"
        const val EXTRA_PRINTER_ADDRESS = "com.productiva.android.extra.PRINTER_ADDRESS"
        const val EXTRA_PRINTER_MODEL = "com.productiva.android.extra.PRINTER_MODEL"
        const val EXTRA_CONNECTION_TYPE = "com.productiva.android.extra.CONNECTION_TYPE"
        const val EXTRA_PAPER_WIDTH = "com.productiva.android.extra.PAPER_WIDTH"
        const val EXTRA_PAPER_HEIGHT = "com.productiva.android.extra.PAPER_HEIGHT"
        const val EXTRA_QUALITY = "com.productiva.android.extra.QUALITY"
        const val EXTRA_ORIENTATION = "com.productiva.android.extra.ORIENTATION"
        const val EXTRA_PRINT_COPIES = "com.productiva.android.extra.PRINT_COPIES"
        
        // Broadcasts de respuesta
        const val ACTION_PRINT_COMPLETE = "com.productiva.android.action.PRINT_COMPLETE"
        const val ACTION_PRINT_ERROR = "com.productiva.android.action.PRINT_ERROR"
        const val ACTION_PRINTERS_FOUND = "com.productiva.android.action.PRINTERS_FOUND"
        
        // Resultados adicionales
        const val EXTRA_ERROR_MESSAGE = "com.productiva.android.extra.ERROR_MESSAGE"
        const val EXTRA_PRINTERS_FOUND = "com.productiva.android.extra.PRINTERS_FOUND"
        
        /**
         * Inicia el servicio para imprimir un archivo PDF
         */
        fun printPdf(context: Context, printerId: Int, filePath: String, copies: Int = 1) {
            val intent = Intent(context, BrotherPrintService::class.java).apply {
                action = ACTION_PRINT_PDF
                putExtra(EXTRA_PRINTER_ID, printerId)
                putExtra(EXTRA_FILE_PATH, filePath)
                putExtra(EXTRA_PRINT_COPIES, copies)
            }
            context.startService(intent)
        }
        
        /**
         * Inicia el servicio para imprimir una imagen
         */
        fun printImage(context: Context, printerId: Int, filePath: String, copies: Int = 1) {
            val intent = Intent(context, BrotherPrintService::class.java).apply {
                action = ACTION_PRINT_IMAGE
                putExtra(EXTRA_PRINTER_ID, printerId)
                putExtra(EXTRA_FILE_PATH, filePath)
                putExtra(EXTRA_PRINT_COPIES, copies)
            }
            context.startService(intent)
        }
        
        /**
         * Inicia el servicio para buscar impresoras disponibles
         */
        fun findPrinters(context: Context) {
            val intent = Intent(context, BrotherPrintService::class.java).apply {
                action = ACTION_FIND_PRINTERS
            }
            context.startService(intent)
        }
    }
    
    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return
        
        when (intent.action) {
            ACTION_PRINT_PDF -> {
                val printerId = intent.getIntExtra(EXTRA_PRINTER_ID, -1)
                val filePath = intent.getStringExtra(EXTRA_FILE_PATH)
                val copies = intent.getIntExtra(EXTRA_PRINT_COPIES, 1)
                
                if (printerId == -1 || filePath.isNullOrEmpty()) {
                    sendErrorBroadcast("Parámetros de impresión inválidos")
                    return
                }
                
                handlePrintPdf(printerId, filePath, copies)
            }
            
            ACTION_PRINT_IMAGE -> {
                val printerId = intent.getIntExtra(EXTRA_PRINTER_ID, -1)
                val filePath = intent.getStringExtra(EXTRA_FILE_PATH)
                val copies = intent.getIntExtra(EXTRA_PRINT_COPIES, 1)
                
                if (printerId == -1 || filePath.isNullOrEmpty()) {
                    sendErrorBroadcast("Parámetros de impresión inválidos")
                    return
                }
                
                handlePrintImage(printerId, filePath, copies)
            }
            
            ACTION_FIND_PRINTERS -> {
                handleFindPrinters()
            }
        }
    }
    
    /**
     * Maneja la impresión de un archivo PDF
     */
    private fun handlePrintPdf(printerId: Int, filePath: String, copies: Int) {
        try {
            AppLogger.d(TAG, "Iniciando impresión de PDF: $filePath para impresora ID: $printerId")
            
            // Obtener configuración de la impresora desde la base de datos
            val printerDao = ProductivaApplication.instance.database.printerDao()
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val printer = printerDao.getPrinterById(printerId)
                    
                    if (printer == null) {
                        withContext(Dispatchers.Main) {
                            sendErrorBroadcast("Impresora no encontrada")
                        }
                        return@launch
                    }
                    
                    // Convertir PDF a bitmap
                    val bitmap = convertPdfToBitmap(filePath)
                    
                    if (bitmap == null) {
                        withContext(Dispatchers.Main) {
                            sendErrorBroadcast("Error al convertir PDF a imagen")
                        }
                        return@launch
                    }
                    
                    // Imprimir la imagen
                    val result = printBitmap(printer, bitmap, copies)
                    
                    withContext(Dispatchers.Main) {
                        if (result) {
                            sendCompleteBroadcast()
                        } else {
                            sendErrorBroadcast("Error al imprimir PDF")
                        }
                    }
                } catch (e: Exception) {
                    AppLogger.e(TAG, "Error en handlePrintPdf", e)
                    withContext(Dispatchers.Main) {
                        sendErrorBroadcast("Error: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error general en handlePrintPdf", e)
            sendErrorBroadcast("Error: ${e.message}")
        }
    }
    
    /**
     * Maneja la impresión de una imagen
     */
    private fun handlePrintImage(printerId: Int, filePath: String, copies: Int) {
        try {
            AppLogger.d(TAG, "Iniciando impresión de imagen: $filePath para impresora ID: $printerId")
            
            // Obtener configuración de la impresora desde la base de datos
            val printerDao = ProductivaApplication.instance.database.printerDao()
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val printer = printerDao.getPrinterById(printerId)
                    
                    if (printer == null) {
                        withContext(Dispatchers.Main) {
                            sendErrorBroadcast("Impresora no encontrada")
                        }
                        return@launch
                    }
                    
                    // Cargar la imagen
                    val bitmap = BitmapFactory.decodeFile(filePath)
                    
                    if (bitmap == null) {
                        withContext(Dispatchers.Main) {
                            sendErrorBroadcast("Error al cargar imagen")
                        }
                        return@launch
                    }
                    
                    // Imprimir la imagen
                    val result = printBitmap(printer, bitmap, copies)
                    
                    withContext(Dispatchers.Main) {
                        if (result) {
                            sendCompleteBroadcast()
                        } else {
                            sendErrorBroadcast("Error al imprimir imagen")
                        }
                    }
                } catch (e: Exception) {
                    AppLogger.e(TAG, "Error en handlePrintImage", e)
                    withContext(Dispatchers.Main) {
                        sendErrorBroadcast("Error: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error general en handlePrintImage", e)
            sendErrorBroadcast("Error: ${e.message}")
        }
    }
    
    /**
     * Busca impresoras Brother disponibles en la red
     */
    private fun handleFindPrinters() {
        try {
            AppLogger.d(TAG, "Buscando impresoras Brother disponibles")
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Buscar impresoras en la red
                    val netPrinter = NetPrinter()
                    val printers = netPrinter.getNetPrinters()
                    
                    val foundPrinters = mutableListOf<Map<String, String>>()
                    
                    if (printers != null && printers.isNotEmpty()) {
                        for (i in printers.indices) {
                            val printer = printers[i]
                            val printerInfo = mapOf(
                                "model" to printer.modelName,
                                "address" to printer.ipAddress,
                                "name" to (printer.nodeName ?: printer.modelName),
                                "location" to (printer.location ?: "")
                            )
                            foundPrinters.add(printerInfo)
                        }
                    }
                    
                    withContext(Dispatchers.Main) {
                        sendPrintersFoundBroadcast(foundPrinters)
                    }
                } catch (e: Exception) {
                    AppLogger.e(TAG, "Error en handleFindPrinters", e)
                    withContext(Dispatchers.Main) {
                        sendErrorBroadcast("Error al buscar impresoras: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error general en handleFindPrinters", e)
            sendErrorBroadcast("Error: ${e.message}")
        }
    }
    
    /**
     * Convierte un archivo PDF a un bitmap
     */
    private fun convertPdfToBitmap(pdfFilePath: String): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val file = File(pdfFilePath)
            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(fileDescriptor)
            
            // Solo tomamos la primera página
            val page = pdfRenderer.openPage(0)
            
            // Crear un bitmap del tamaño de la página
            bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
            
            page.close()
            pdfRenderer.close()
            fileDescriptor.close()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error en convertPdfToBitmap", e)
        }
        
        return bitmap
    }
    
    /**
     * Imprime un bitmap en una impresora Brother
     */
    private fun printBitmap(savedPrinter: SavedPrinter, bitmap: Bitmap, copies: Int): Boolean {
        try {
            // Configurar la impresora
            val printer = Printer()
            val printerInfo = printer.printerInfo
            
            // Configurar el modelo de impresora
            when {
                savedPrinter.model.startsWith("QL-") -> {
                    printerInfo.printerModel = PrinterInfo.Model.QL_720NW
                }
                savedPrinter.model.startsWith("PT-") -> {
                    printerInfo.printerModel = PrinterInfo.Model.PT_P950NW
                }
                else -> {
                    printerInfo.printerModel = PrinterInfo.Model.QL_720NW
                }
            }
            
            // Configurar tipo de conexión
            when (savedPrinter.connectionType) {
                SavedPrinter.TYPE_BLUETOOTH -> {
                    printerInfo.port = PrinterInfo.Port.BLUETOOTH
                    printerInfo.macAddress = savedPrinter.address
                }
                SavedPrinter.TYPE_WIFI -> {
                    printerInfo.port = PrinterInfo.Port.NET
                    printerInfo.ipAddress = savedPrinter.address
                }
                SavedPrinter.TYPE_USB -> {
                    printerInfo.port = PrinterInfo.Port.USB
                }
            }
            
            // Configurar etiqueta
            printerInfo.paperSize = getLabelPaperSize(savedPrinter.paperWidth, savedPrinter.paperHeight)
            printerInfo.orientation = when (savedPrinter.orientation) {
                SavedPrinter.ORIENTATION_LANDSCAPE -> PrinterInfo.Orientation.LANDSCAPE
                else -> PrinterInfo.Orientation.PORTRAIT
            }
            
            // Configurar calidad
            printerInfo.printQuality = when (savedPrinter.printQuality) {
                SavedPrinter.QUALITY_HIGH -> PrinterInfo.PrintQuality.HIGH_RESOLUTION
                SavedPrinter.QUALITY_NORMAL -> PrinterInfo.PrintQuality.NORMAL
                else -> PrinterInfo.PrintQuality.NORMAL
            }
            
            // Número de copias
            printerInfo.numberOfCopies = copies
            
            // Imprimir
            printer.printerInfo = printerInfo
            val result = printer.print(bitmap)
            
            return result == PrinterInfo.Result.SUCCESS
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error en printBitmap", e)
            return false
        }
    }
    
    /**
     * Obtiene el tamaño de papel adecuado para Brother
     */
    private fun getLabelPaperSize(width: Int, height: Int): LabelInfo.QL700.Media {
        // Por defecto usamos tamaño de etiqueta de dirección
        var labelSize = LabelInfo.QL700.Media.W62RB
        
        // Determinar el tamaño basado en dimensiones
        when {
            width == 62 && height == 29 -> labelSize = LabelInfo.QL700.Media.W62H29
            width == 62 -> labelSize = LabelInfo.QL700.Media.W62RB
            width == 29 -> labelSize = LabelInfo.QL700.Media.W29H90
            width == 17 -> labelSize = LabelInfo.QL700.Media.W17H54
            width == 12 -> labelSize = LabelInfo.QL700.Media.W12
        }
        
        return labelSize
    }
    
    /**
     * Envía un broadcast de impresión completada
     */
    private fun sendCompleteBroadcast() {
        val intent = Intent(ACTION_PRINT_COMPLETE)
        sendBroadcast(intent)
    }
    
    /**
     * Envía un broadcast de error de impresión
     */
    private fun sendErrorBroadcast(errorMessage: String) {
        val intent = Intent(ACTION_PRINT_ERROR).apply {
            putExtra(EXTRA_ERROR_MESSAGE, errorMessage)
        }
        sendBroadcast(intent)
        
        // También mostramos un Toast para notificar al usuario
        Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Envía un broadcast con las impresoras encontradas
     */
    private fun sendPrintersFoundBroadcast(printers: List<Map<String, String>>) {
        val intent = Intent(ACTION_PRINTERS_FOUND).apply {
            putExtra(EXTRA_PRINTERS_FOUND, ArrayList(printers))
        }
        sendBroadcast(intent)
    }
}