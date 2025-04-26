package com.productiva.android.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.Log
import com.brother.ptouch.sdk.NetPrinter
import com.brother.ptouch.sdk.Printer
import com.brother.ptouch.sdk.PrinterInfo
import com.brother.ptouch.sdk.PrinterStatus
import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.Product
import com.productiva.android.repository.LabelTemplateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.Date

/**
 * Servicio para la impresión de etiquetas en impresoras Brother.
 * Proporciona métodos para configurar la impresora, generar etiquetas y enviarlas a imprimir.
 */
class BrotherPrintService(
    private val context: Context,
    private val labelTemplateRepository: LabelTemplateRepository
) {
    private val TAG = "BrotherPrintService"
    
    private var printer: Printer? = null
    private var printerInfo: PrinterInfo? = null
    
    // Impresora actualmente conectada
    private var currentPrinter: NetPrinter? = null
    
    /**
     * Inicializa el servicio de impresión.
     */
    init {
        initializePrinter()
    }
    
    /**
     * Inicializa la impresora con la configuración por defecto.
     */
    private fun initializePrinter() {
        printer = Printer()
        printerInfo = printer?.printerInfo
        
        printerInfo?.apply {
            // Configuración por defecto para impresoras Brother
            printerModel = PrinterInfo.Model.QL_810W
            port = PrinterInfo.Port.NET
            paperSize = PrinterInfo.PaperSize.CUSTOM
            orientation = PrinterInfo.Orientation.LANDSCAPE
            printMode = PrinterInfo.PrintMode.FIT_TO_PAGE
            isAutoCut = true
            numberOfCopies = 1
            halftone = PrinterInfo.Halftone.PATTERNDITHER
            printQuality = PrinterInfo.PrintQuality.HIGH_RESOLUTION
        }
        
        printer?.printerInfo = printerInfo
    }
    
    /**
     * Busca impresoras Brother en la red.
     *
     * @return Lista de impresoras encontradas.
     */
    suspend fun searchNetworkPrinters(): List<NetPrinter> = withContext(Dispatchers.IO) {
        val netPrinters = printer?.getNetPrinters("QL-810W") ?: emptyArray()
        Log.d(TAG, "Found ${netPrinters.size} network printers")
        return@withContext netPrinters.toList()
    }
    
    /**
     * Configura la impresora a utilizar.
     *
     * @param netPrinter Impresora a configurar.
     * @return True si la configuración fue exitosa.
     */
    fun setNetworkPrinter(netPrinter: NetPrinter): Boolean {
        printerInfo?.apply {
            ipAddress = netPrinter.ipAddress
            macAddress = netPrinter.macAddress
            nodeName = netPrinter.modelName
            port = PrinterInfo.Port.NET
        }
        
        printer?.printerInfo = printerInfo
        currentPrinter = netPrinter
        
        Log.d(TAG, "Printer configured: ${netPrinter.modelName} at ${netPrinter.ipAddress}")
        return true
    }
    
    /**
     * Comprueba el estado de la impresora.
     *
     * @return Estado de la impresora.
     */
    suspend fun checkPrinterStatus(): PrinterStatus = withContext(Dispatchers.IO) {
        return@withContext printer?.getPrinterStatus() ?: PrinterStatus()
    }
    
    /**
     * Imprime una etiqueta para un producto usando una plantilla.
     *
     * @param product Producto para el que generar la etiqueta.
     * @param templateId ID de la plantilla a utilizar (opcional).
     * @return Estado de la impresión.
     */
    suspend fun printProductLabel(product: Product, templateId: Int? = null): PrintResult = withContext(Dispatchers.IO) {
        try {
            // 1. Obtener plantilla de etiqueta
            val template = if (templateId != null) {
                labelTemplateRepository.getTemplateById(templateId).first()
            } else {
                labelTemplateRepository.getDefaultTemplateForType("product").first()
            }
            
            if (template == null) {
                Log.e(TAG, "No label template found")
                return@withContext PrintResult(false, "No se encontró una plantilla para la etiqueta")
            }
            
            // 2. Incrementar contador de uso
            labelTemplateRepository.incrementTemplateUseCount(template.id)
            
            // 3. Configurar la impresora según la plantilla
            configureWithTemplate(template)
            
            // 4. Generar la imagen para la etiqueta
            val labelImage = generateProductLabelImage(product, template)
            
            // 5. Guardar la imagen temporalmente
            val tempFile = File(context.cacheDir, "label_${Date().time}.png")
            FileOutputStream(tempFile).use { output ->
                labelImage.compress(Bitmap.CompressFormat.PNG, 100, output)
            }
            
            // 6. Enviar a imprimir
            val result = printer?.printFile(tempFile.absolutePath) ?: PrinterInfo.ErrorCode.ERROR_INTERNAL
            
            // 7. Limpiar archivos temporales
            tempFile.delete()
            
            // 8. Verificar resultado
            val success = result == PrinterInfo.ErrorCode.ERROR_NONE
            val message = if (success) {
                "Etiqueta impresa correctamente"
            } else {
                "Error al imprimir etiqueta: ${getPrinterErrorMessage(result)}"
            }
            
            Log.d(TAG, message)
            return@withContext PrintResult(success, message)
        } catch (e: Exception) {
            Log.e(TAG, "Error printing label", e)
            return@withContext PrintResult(false, "Error al imprimir etiqueta: ${e.message}")
        }
    }
    
    /**
     * Configura la impresora según una plantilla.
     *
     * @param template Plantilla de etiqueta.
     */
    private fun configureWithTemplate(template: LabelTemplate) {
        printerInfo?.apply {
            // Configurar tamaño de papel
            when (template.paperSize) {
                "62mm" -> paperSize = PrinterInfo.PaperSize.CUSTOM
                "29mm" -> paperSize = PrinterInfo.PaperSize.CUSTOM
                else -> paperSize = PrinterInfo.PaperSize.CUSTOM
            }
            
            // Configurar orientación
            orientation = if (template.orientation == "portrait") {
                PrinterInfo.Orientation.PORTRAIT
            } else {
                PrinterInfo.Orientation.LANDSCAPE
            }
            
            // Configurar otras opciones
            isAutoCut = true
            numberOfCopies = 1
            
            // Establecer dimensiones personalizadas si es necesario
            val width = when (template.paperSize) {
                "62mm" -> 696
                "29mm" -> 306
                else -> 696
            }
            
            val length = if (template.orientation == "portrait") 999 else 394
            customPaperWidth = width
            customPaperLength = length
        }
        
        printer?.printerInfo = printerInfo
    }
    
    /**
     * Genera una imagen de la etiqueta para un producto.
     *
     * @param product Producto para el que generar la etiqueta.
     * @param template Plantilla de etiqueta.
     * @return Imagen de la etiqueta.
     */
    private suspend fun generateProductLabelImage(product: Product, template: LabelTemplate): Bitmap = withContext(Dispatchers.IO) {
        // Obtener datos para la etiqueta
        val labelData = template.generateProductLabelData(product)
        
        // Definir dimensiones según la plantilla
        val width = when (template.paperSize) {
            "62mm" -> 696
            "29mm" -> 306
            else -> 696
        }
        
        val height = if (template.orientation == "portrait") 999 else 394
        
        // Crear bitmap y canvas
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Fondo blanco
        canvas.drawColor(Color.WHITE)
        
        // Configurar paint para texto
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = template.fontSize * 3.5f // Escalar para mejor legibilidad
            isAntiAlias = true
            typeface = if (template.boldTitle) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
        
        // Margen y posición actual
        val margin = 20
        var currentY = margin + textPaint.textSize
        
        // Dibujar título (nombre del producto)
        textPaint.typeface = if (template.boldTitle) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        textPaint.textSize = template.fontSize * 4f
        val title = labelData["title"] as String
        drawWrappedText(canvas, title, margin, currentY, width - 2 * margin, textPaint)
        
        currentY += textPaint.textSize * 1.5f
        
        // Dibujar código
        textPaint.typeface = Typeface.DEFAULT
        textPaint.textSize = template.fontSize * 3f
        val code = labelData["code"] as String
        if (code.isNotEmpty()) {
            canvas.drawText("Cód: $code", margin.toFloat(), currentY, textPaint)
            currentY += textPaint.textSize * 1.2f
        }
        
        // Dibujar precio si está incluido
        if (template.includePrice && labelData.containsKey("price")) {
            textPaint.typeface = Typeface.DEFAULT_BOLD
            textPaint.textSize = template.fontSize * 5f
            val price = labelData["price"] as String
            canvas.drawText(price, margin.toFloat(), currentY, textPaint)
            currentY += textPaint.textSize * 1.5f
        }
        
        // Dibujar campos personalizados
        textPaint.typeface = Typeface.DEFAULT
        textPaint.textSize = template.fontSize * 2.5f
        
        for (field in template.customFields) {
            if (labelData.containsKey(field)) {
                val value = labelData[field] as String
                if (value.isNotEmpty()) {
                    canvas.drawText("$field: $value", margin.toFloat(), currentY, textPaint)
                    currentY += textPaint.textSize * 1.2f
                }
            }
        }
        
        // Dibujar fecha si está incluida
        if (template.includeDate && labelData.containsKey("date")) {
            val date = labelData["date"] as String
            canvas.drawText(date, margin.toFloat(), currentY, textPaint)
        }
        
        // Cargar y dibujar logo si está incluido
        if (template.includeLogo && labelData.containsKey("logoUrl")) {
            try {
                val logoUrl = labelData["logoUrl"] as String
                val logoBitmap = loadLogoBitmap(logoUrl)
                
                if (logoBitmap != null) {
                    // Ubicar el logo en la esquina superior derecha
                    val logoSize = width / 4
                    val srcRect = Rect(0, 0, logoBitmap.width, logoBitmap.height)
                    val dstRect = Rect(width - logoSize - margin, margin, width - margin, margin + logoSize)
                    canvas.drawBitmap(logoBitmap, srcRect, dstRect, null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading logo", e)
            }
        }
        
        // Dibujar código de barras si está incluido
        if (template.includeBarcode && labelData.containsKey("barcode")) {
            // Aquí se implementaría la generación del código de barras
            // Para simplificar, solo escribimos el código
            textPaint.typeface = Typeface.MONOSPACE
            textPaint.textSize = template.fontSize * 3f
            val barcode = labelData["barcode"] as String
            
            val barcodeY = height - margin - textPaint.textSize
            canvas.drawText(barcode, margin.toFloat(), barcodeY, textPaint)
        }
        
        return@withContext bitmap
    }
    
    /**
     * Dibuja texto con saltos de línea si es necesario.
     */
    private fun drawWrappedText(canvas: Canvas, text: String, x: Int, y: Float, maxWidth: Int, paint: Paint) {
        val lines = ArrayList<String>()
        val words = text.split(" ")
        
        var currentLine = ""
        
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val textWidth = paint.measureText(testLine)
            
            if (textWidth <= maxWidth) {
                currentLine = testLine
            } else {
                lines.add(currentLine)
                currentLine = word
            }
        }
        
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        
        var currentY = y
        for (line in lines) {
            canvas.drawText(line, x.toFloat(), currentY, paint)
            currentY += paint.textSize * 1.2f
        }
    }
    
    /**
     * Carga una imagen de logo desde una URL.
     */
    private suspend fun loadLogoBitmap(urlString: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlString)
            val connection = url.openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()
            
            val input = connection.getInputStream()
            return@withContext BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading logo from URL", e)
            return@withContext null
        }
    }
    
    /**
     * Obtiene un mensaje descriptivo para un código de error de impresión.
     */
    private fun getPrinterErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            PrinterInfo.ErrorCode.ERROR_NONE -> "No error"
            PrinterInfo.ErrorCode.ERROR_NOT_SAME_MODEL -> "Modelo de impresora incompatible"
            PrinterInfo.ErrorCode.ERROR_BROTHER_PRINTER_NOT_FOUND -> "Impresora Brother no encontrada"
            PrinterInfo.ErrorCode.ERROR_PAPER_EMPTY -> "Sin papel"
            PrinterInfo.ErrorCode.ERROR_BATTERY_EMPTY -> "Batería baja"
            PrinterInfo.ErrorCode.ERROR_COMMUNICATION -> "Error de comunicación"
            PrinterInfo.ErrorCode.ERROR_OVERHEAT -> "Impresora sobrecalentada"
            PrinterInfo.ErrorCode.ERROR_PAPER_JAM -> "Papel atascado"
            PrinterInfo.ErrorCode.ERROR_HIGH_VOLTAGE_ADAPTER -> "Problema con el adaptador"
            PrinterInfo.ErrorCode.ERROR_COVER_OPEN -> "Cubierta abierta"
            PrinterInfo.ErrorCode.ERROR_WRONG_MEDIA -> "Medio incorrecto"
            PrinterInfo.ErrorCode.ERROR_FEED_OR_CASSETTE_EMPTY -> "Alimentador o casete vacío"
            PrinterInfo.ErrorCode.ERROR_FILE_NOT_SUPPORTED -> "Archivo no soportado"
            PrinterInfo.ErrorCode.ERROR_NOT_AVAILABLE -> "No disponible"
            PrinterInfo.ErrorCode.ERROR_PRINTER_SETTING_NOT_SUPPORTED -> "Configuración no soportada"
            PrinterInfo.ErrorCode.ERROR_UNEXPECTED_TRANSMISSION_ERROR -> "Error de transmisión"
            PrinterInfo.ErrorCode.ERROR_INTERNAL -> "Error interno"
            else -> "Error desconocido: $errorCode"
        }
    }
    
    /**
     * Clase para encapsular el resultado de una impresión.
     */
    data class PrintResult(
        val success: Boolean,
        val message: String
    )
}