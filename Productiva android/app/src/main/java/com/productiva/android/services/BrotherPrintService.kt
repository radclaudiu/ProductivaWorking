package com.productiva.android.services

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import com.brother.sdk.LabelInfo
import com.brother.sdk.PaperSize
import com.brother.sdk.Printer
import com.brother.sdk.PrinterDriver
import com.brother.sdk.PrinterInfo
import com.brother.sdk.PrinterStatus
import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.SavedPrinter
import com.productiva.android.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

/**
 * Servicio para la impresión de etiquetas utilizando el SDK de Brother.
 */
class BrotherPrintService(
    private val context: Context
) {
    private val TAG = "BrotherPrintService"
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Estado de la operación de impresión.
     */
    sealed class PrintResult {
        /**
         * La impresión fue exitosa.
         */
        object Success : PrintResult()
        
        /**
         * Error durante la impresión con mensaje descriptivo.
         */
        data class Error(val message: String, val errorCode: Int = -1) : PrintResult()
        
        /**
         * La impresora no está lista (desconectada, sin papel, etc.).
         */
        data class PrinterNotReady(val message: String) : PrintResult()
        
        /**
         * Error al renderizar la etiqueta.
         */
        data class RenderError(val message: String) : PrintResult()
    }
    
    /**
     * Descubre impresoras Brother disponibles mediante Bluetooth.
     */
    suspend fun discoverPrinters(): List<Printer> = withContext(Dispatchers.IO) {
        try {
            // Buscar impresoras Brother por Bluetooth
            val printers = PrinterDriver.discoverPrinters().get().await()
            Log.d(TAG, "Impresoras descubiertas: ${printers.size}")
            return@withContext printers
        } catch (e: Exception) {
            Log.e(TAG, "Error al descubrir impresoras", e)
            return@withContext emptyList()
        }
    }
    
    /**
     * Convierte un objeto Printer de Brother a nuestro modelo SavedPrinter.
     */
    fun convertToSavedPrinter(brotherPrinter: Printer): SavedPrinter {
        // Determinar tamaño de papel basado en modelo de impresora
        val (paperWidth, paperHeight) = getPaperSizeForModel(brotherPrinter.model)
        
        return SavedPrinter(
            name = brotherPrinter.name,
            model = brotherPrinter.model,
            macAddress = brotherPrinter.macAddress,
            ipAddress = brotherPrinter.ipAddress,
            connectionType = getBestConnectionType(brotherPrinter),
            paperWidth = paperWidth,
            paperHeight = paperHeight,
            dpi = 203 // DPI estándar para la mayoría de impresoras Brother
        )
    }
    
    /**
     * Determina el tamaño de papel basado en el modelo de impresora.
     */
    private fun getPaperSizeForModel(model: String): Pair<Int, Int> {
        return when {
            model.contains("QL-800") || model.contains("QL-810") || model.contains("QL-820") -> 
                Pair(62, 100) // Ancho x Alto en mm
            model.contains("QL-700") || model.contains("QL-710") || model.contains("QL-720") -> 
                Pair(62, 100)
            model.contains("QL-1100") || model.contains("QL-1110") -> 
                Pair(103, 150)
            model.contains("PT-") -> 
                Pair(24, 80) // Etiquetas tipo cinta
            else -> 
                Pair(62, 100) // Tamaño predeterminado
        }
    }
    
    /**
     * Obtiene el mejor tipo de conexión disponible para una impresora.
     */
    private fun getBestConnectionType(printer: Printer): String {
        return when {
            printer.bluetoothAddress != null -> "bluetooth"
            printer.ipAddress != null -> "wifi"
            printer.usbSerialNumber != null -> "usb"
            else -> "unknown"
        }
    }
    
    /**
     * Imprime una plantilla de etiqueta en una impresora Brother.
     */
    suspend fun printTemplate(
        printer: SavedPrinter,
        template: LabelTemplate,
        data: Map<String, String>
    ): PrintResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Preparando impresión de etiqueta: ${template.name}")
            
            // Renderizar la plantilla con los datos
            val html = prepareHtmlWithData(template, data)
            
            // Convertir HTML a imagen
            val bitmap = renderHtmlToBitmap(html, template.width, template.height)
                ?: return@withContext PrintResult.RenderError("Error al renderizar plantilla HTML")
            
            // Configurar la impresora
            val printerInfo = configurePrinter(printer)
            
            // Conectar con la impresora
            val printerDriver = getPrinterDriver(printer, printerInfo)
                ?: return@withContext PrintResult.Error("No se pudo conectar con la impresora")
            
            // Verificar estado de la impresora
            val status = printerDriver.printerStatus.get().await()
            if (!status.isReady) {
                val errorMessage = getPrinterErrorMessage(status)
                return@withContext PrintResult.PrinterNotReady(errorMessage)
            }
            
            // Crear archivo temporal para la imagen
            val imageFile = FileUtils.createFileFromBitmap(
                context, 
                bitmap, 
                "label_${template.id}_"
            ) ?: return@withContext PrintResult.Error("Error al crear archivo temporal")
            
            // Imprimir la imagen
            Log.d(TAG, "Enviando trabajo de impresión")
            val printResult = printerDriver.printImage(imageFile).get().await()
            
            // Limpiar archivo temporal
            imageFile.delete()
            
            // Verificar resultado
            return@withContext if (printResult) {
                Log.d(TAG, "Impresión exitosa")
                PrintResult.Success
            } else {
                val errorStatus = printerDriver.printerStatus.get().await()
                val errorMessage = getPrinterErrorMessage(errorStatus)
                Log.e(TAG, "Error en impresión: $errorMessage")
                PrintResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al imprimir plantilla", e)
            return@withContext PrintResult.Error("Error al imprimir: ${e.message}")
        }
    }
    
    /**
     * Prepara el HTML con los datos para la plantilla.
     */
    private fun prepareHtmlWithData(template: LabelTemplate, data: Map<String, String>): String {
        var html = template.htmlContent
        
        // Reemplazar variables en el HTML
        data.forEach { (key, value) ->
            html = html.replace("{{$key}}", value)
        }
        
        // Agregar CSS
        val css = template.cssContent ?: ""
        html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { 
                        margin: 0; 
                        padding: 0; 
                        width: ${template.width}mm; 
                        height: ${template.height}mm; 
                        overflow: hidden;
                    }
                    $css
                </style>
            </head>
            <body>
                $html
            </body>
            </html>
        """.trimIndent()
        
        return html
    }
    
    /**
     * Renderiza HTML a un bitmap usando WebView.
     */
    private suspend fun renderHtmlToBitmap(html: String, width: Int, height: Int): Bitmap? {
        return suspendCancellableCoroutine { continuation ->
            coroutineScope.launch(Dispatchers.Main) {
                try {
                    val webView = WebView(context)
                    webView.settings.apply {
                        javaScriptEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                    }
                    
                    // Calcular pixeles basados en densidad de pantalla
                    val density = context.resources.displayMetrics.density
                    val pixelWidth = (width * density * 3.779528).toInt() // Convertir mm a píxeles
                    val pixelHeight = (height * density * 3.779528).toInt()
                    
                    webView.layout(0, 0, pixelWidth, pixelHeight)
                    
                    webView.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String) {
                            try {
                                // Crear bitmap del tamaño de la etiqueta
                                val bitmap = Bitmap.createBitmap(
                                    pixelWidth, 
                                    pixelHeight, 
                                    Bitmap.Config.ARGB_8888
                                )
                                
                                // Renderizar WebView en el bitmap
                                view.draw(android.graphics.Canvas(bitmap))
                                
                                // Devolver resultado
                                if (!continuation.isCompleted) {
                                    continuation.resume(bitmap)
                                }
                                
                                // Limpiar WebView
                                webView.destroy()
                            } catch (e: Exception) {
                                Log.e(TAG, "Error al renderizar HTML", e)
                                if (!continuation.isCompleted) {
                                    continuation.resume(null)
                                }
                            }
                        }
                    }
                    
                    // Cargar HTML
                    webView.loadDataWithBaseURL(
                        "file:///android_asset/",
                        html,
                        "text/html",
                        "UTF-8",
                        null
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error al configurar WebView", e)
                    if (!continuation.isCompleted) {
                        continuation.resume(null)
                    }
                }
            }
        }
    }
    
    /**
     * Configura la información de la impresora para Brother SDK.
     */
    private fun configurePrinter(printer: SavedPrinter): PrinterInfo {
        val printerInfo = PrinterInfo()
        
        // Configurar conexión
        when (printer.connectionType.lowercase()) {
            "bluetooth" -> {
                printerInfo.printerModel = printer.model
                printerInfo.port = PrinterInfo.Port.BLUETOOTH
                printerInfo.macAddress = printer.macAddress
            }
            "wifi" -> {
                printerInfo.printerModel = printer.model
                printerInfo.port = PrinterInfo.Port.NET
                printerInfo.ipAddress = printer.ipAddress ?: ""
            }
            "usb" -> {
                printerInfo.printerModel = printer.model
                printerInfo.port = PrinterInfo.Port.USB
            }
            else -> {
                // Bluetooth por defecto
                printerInfo.printerModel = printer.model
                printerInfo.port = PrinterInfo.Port.BLUETOOTH
                printerInfo.macAddress = printer.macAddress
            }
        }
        
        // Configurar tamaño de papel
        printerInfo.paperSize = getPaperSizeForModel(printer)
        printerInfo.orientation = PrinterInfo.Orientation.PORTRAIT
        printerInfo.printMode = PrinterInfo.PrintMode.FIT_TO_PAGE
        printerInfo.isAutoCut = true
        
        return printerInfo
    }
    
    /**
     * Obtiene el tamaño de papel correcto para el SDK de Brother.
     */
    private fun getPaperSizeForModel(printer: SavedPrinter): PaperSize {
        // Mapear dimensiones a tamaños de Brother
        return when {
            printer.model.contains("QL-") -> {
                when {
                    printer.paperWidth <= 17 -> PaperSize.ROLL_W17H54
                    printer.paperWidth <= 29 -> PaperSize.ROLL_W29H90
                    printer.paperWidth <= 38 -> PaperSize.ROLL_W38H90
                    printer.paperWidth <= 50 -> PaperSize.ROLL_W50H30
                    printer.paperWidth <= 54 -> PaperSize.ROLL_W54H29
                    printer.paperWidth <= 62 -> PaperSize.ROLL_W62
                    printer.paperWidth <= 102 -> PaperSize.ROLL_W102
                    else -> PaperSize.ROLL_W62
                }
            }
            printer.model.contains("PT-") -> {
                when {
                    printer.paperWidth <= 3.5 -> PaperSize.PT_3_5
                    printer.paperWidth <= 6 -> PaperSize.PT_6
                    printer.paperWidth <= 9 -> PaperSize.PT_9
                    printer.paperWidth <= 12 -> PaperSize.PT_12
                    printer.paperWidth <= 18 -> PaperSize.PT_18
                    printer.paperWidth <= 24 -> PaperSize.PT_24
                    else -> PaperSize.PT_24
                }
            }
            else -> PaperSize.ROLL_W62 // Valor predeterminado
        }
    }
    
    /**
     * Obtiene un driver de impresora para Brother SDK.
     */
    private suspend fun getPrinterDriver(
        printer: SavedPrinter,
        printerInfo: PrinterInfo
    ): PrinterDriver? = withContext(Dispatchers.IO) {
        try {
            val driver = PrinterDriver(printerInfo)
            val isConnected = driver.connect().get().await()
            
            if (isConnected) {
                return@withContext driver
            } else {
                Log.e(TAG, "No se pudo conectar con la impresora")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener driver de impresora", e)
            return@withContext null
        }
    }
    
    /**
     * Obtiene un mensaje de error descriptivo basado en el estado de la impresora.
     */
    private fun getPrinterErrorMessage(status: PrinterStatus): String {
        return when {
            !status.isOnline -> "La impresora no está conectada"
            status.isPaperEmpty -> "No hay papel en la impresora"
            status.isCoverOpen -> "La tapa de la impresora está abierta"
            status.isBatteryLow -> "Batería baja en la impresora"
            status.isError -> "Error en la impresora: ${status.errorCode}"
            status.isProcessing -> "La impresora está procesando otro trabajo"
            !status.isReady -> "La impresora no está lista"
            else -> "Error desconocido"
        }
    }
    
    /**
     * Crea un archivo PDF a partir de una plantilla para previsualización.
     */
    suspend fun createPdfPreview(
        template: LabelTemplate,
        data: Map<String, String>
    ): File? = withContext(Dispatchers.IO) {
        try {
            // Renderizar la plantilla con los datos
            val html = prepareHtmlWithData(template, data)
            
            // Convertir HTML a imagen
            val bitmap = renderHtmlToBitmap(html, template.width, template.height)
                ?: return@withContext null
            
            // Crear archivo PDF para previsualización (usando PdfRenderer)
            // Implementación simplificada: guardamos como imagen por ahora
            val previewFile = FileUtils.createFileFromBitmap(
                context, 
                bitmap, 
                "preview_${template.id}_"
            )
            
            return@withContext previewFile
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear vista previa", e)
            return@withContext null
        }
    }
    
    /**
     * Imprime una imagen desde Uri a una impresora Brother.
     */
    suspend fun printImageFromUri(
        printer: SavedPrinter,
        imageUri: Uri
    ): PrintResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Preparando impresión de imagen desde Uri")
            
            // Convertir Uri a File
            val imageFile = FileUtils.createFileFromUri(
                context, 
                imageUri, 
                "print_image_"
            ) ?: return@withContext PrintResult.Error("Error al preparar imagen para impresión")
            
            // Configurar la impresora
            val printerInfo = configurePrinter(printer)
            
            // Conectar con la impresora
            val printerDriver = getPrinterDriver(printer, printerInfo)
                ?: return@withContext PrintResult.Error("No se pudo conectar con la impresora")
            
            // Verificar estado de la impresora
            val status = printerDriver.printerStatus.get().await()
            if (!status.isReady) {
                val errorMessage = getPrinterErrorMessage(status)
                return@withContext PrintResult.PrinterNotReady(errorMessage)
            }
            
            // Imprimir la imagen
            Log.d(TAG, "Enviando trabajo de impresión")
            val printResult = printerDriver.printImage(imageFile).get().await()
            
            // Limpiar archivo temporal
            imageFile.delete()
            
            // Verificar resultado
            return@withContext if (printResult) {
                Log.d(TAG, "Impresión exitosa")
                PrintResult.Success
            } else {
                val errorStatus = printerDriver.printerStatus.get().await()
                val errorMessage = getPrinterErrorMessage(errorStatus)
                Log.e(TAG, "Error en impresión: $errorMessage")
                PrintResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al imprimir imagen", e)
            return@withContext PrintResult.Error("Error al imprimir: ${e.message}")
        }
    }
}