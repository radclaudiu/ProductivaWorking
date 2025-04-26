package com.productiva.android.services

import android.content.Context
import android.graphics.Bitmap
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
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.productiva.android.model.LabelElement
import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Servicio para la impresión de etiquetas en impresoras Brother.
 */
class BrotherPrintService(private val context: Context) {
    companion object {
        private const val TAG = "BrotherPrintService"
    }
    
    // Configuración de la impresora
    private val printerInfo = PrinterInfo()
    private val printer = Printer()
    
    /**
     * Inicializa la configuración de la impresora.
     */
    init {
        printer.setPrinterInfo(printerInfo)
        
        // Configuraciones por defecto
        printerInfo.printerModel = PrinterInfo.Model.QL_820NWB
        printerInfo.port = PrinterInfo.Port.NET
        printerInfo.paperSize = PrinterInfo.PaperSize.CUSTOM
        printerInfo.orientation = PrinterInfo.Orientation.LANDSCAPE
        printerInfo.printMode = PrinterInfo.PrintMode.FIT_TO_PAGE
        printerInfo.isAutoCut = true
        printerInfo.isCutAtEnd = true
        printerInfo.numberOfCopies = 1
    }
    
    /**
     * Configura la impresora con los parámetros especificados.
     *
     * @param printerModel Modelo de impresora Brother (por ejemplo, "QL-820NWB").
     * @param ipAddress Dirección IP de la impresora.
     * @param paperWidth Ancho del papel en mm.
     * @param paperHeight Alto del papel en mm.
     * @param orientation Orientación de la impresión ("landscape" o "portrait").
     * @param cutEnabled Si la impresora debe cortar automáticamente.
     * @param copies Número de copias a imprimir.
     */
    fun configureIpPrinter(
        printerModel: String,
        ipAddress: String,
        paperWidth: Int,
        paperHeight: Int,
        orientation: String = "landscape",
        cutEnabled: Boolean = true,
        copies: Int = 1
    ) {
        // Configurar modelo de impresora
        printerInfo.printerModel = getPrinterModel(printerModel)
        
        // Configurar puerto y dirección IP
        printerInfo.port = PrinterInfo.Port.NET
        printerInfo.ipAddress = ipAddress
        
        // Configurar tamaño de papel
        printerInfo.paperSize = PrinterInfo.PaperSize.CUSTOM
        printerInfo.customPaperWidth = paperWidth
        printerInfo.customPaperLength = paperHeight
        
        // Configurar orientación
        printerInfo.orientation = if (orientation.equals("landscape", ignoreCase = true))
            PrinterInfo.Orientation.LANDSCAPE else PrinterInfo.Orientation.PORTRAIT
        
        // Configurar opciones de corte
        printerInfo.isAutoCut = cutEnabled
        printerInfo.isCutAtEnd = cutEnabled
        
        // Configurar número de copias
        printerInfo.numberOfCopies = copies
        
        // Aplicar configuración
        printer.setPrinterInfo(printerInfo)
    }
    
    /**
     * Busca impresoras Brother en la red.
     *
     * @return Lista de impresoras encontradas.
     */
    suspend fun searchNetworkPrinters(): List<PrinterDevice> = withContext(Dispatchers.IO) {
        val netPrinter = NetPrinter()
        val printerList = netPrinter.discover(NetPrinter.SEARCH_TIMEOUT)
        
        return@withContext printerList.map { printer ->
            PrinterDevice(
                ipAddress = printer.ipAddress,
                modelName = printer.modelName,
                macAddress = printer.macAddress,
                nodeName = printer.nodeName,
                location = printer.location
            )
        }
    }
    
    /**
     * Imprime una etiqueta para un producto usando una plantilla específica.
     *
     * @param product Producto para el que se imprimirá la etiqueta.
     * @param template Plantilla de etiqueta a utilizar.
     * @return Resultado de la impresión.
     */
    suspend fun printProductLabel(
        product: Product,
        template: LabelTemplate
    ): PrintResult = withContext(Dispatchers.IO) {
        try {
            // Configurar tamaño de papel según la plantilla
            printerInfo.customPaperWidth = template.width
            printerInfo.customPaperLength = template.height
            
            // Configurar orientación según la plantilla
            printerInfo.orientation = if (template.orientation.equals("landscape", ignoreCase = true))
                PrinterInfo.Orientation.LANDSCAPE else PrinterInfo.Orientation.PORTRAIT
            
            // Aplicar configuración
            printer.setPrinterInfo(printerInfo)
            
            // Generar bitmap de la etiqueta
            val labelBitmap = generateLabelBitmap(product, template)
            
            // Imprimir bitmap
            val result = printer.printImage(labelBitmap)
            val status = printer.printerStatus
            
            return@withContext if (result == PrinterInfo.ErrorCode.ERROR_NONE) {
                PrintResult(true, "Etiqueta impresa correctamente")
            } else {
                PrintResult(false, "Error al imprimir: ${getErrorMessage(result, status)}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al imprimir etiqueta", e)
            return@withContext PrintResult(false, "Error al imprimir: ${e.message}")
        }
    }
    
    /**
     * Genera un bitmap de una etiqueta para un producto específico usando una plantilla.
     *
     * @param product Producto para el que se generará la etiqueta.
     * @param template Plantilla de etiqueta a utilizar.
     * @return Bitmap de la etiqueta generada.
     */
    private fun generateLabelBitmap(product: Product, template: LabelTemplate): Bitmap {
        // Calcular resolución según DPI de la plantilla
        val scale = template.dpi / 72f // Convertir a puntos
        val width = (template.width * scale * 10).toInt() // mm a décimas de pulgada * DPI
        val height = (template.height * scale * 10).toInt() // mm a décimas de pulgada * DPI
        
        // Crear bitmap de la etiqueta
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Fondo blanco
        canvas.drawColor(Color.WHITE)
        
        // Dibujar cada elemento de la plantilla
        for (element in template.elements) {
            drawLabelElement(canvas, element, product, scale)
        }
        
        return bitmap
    }
    
    /**
     * Dibuja un elemento de etiqueta en el canvas.
     *
     * @param canvas Canvas donde se dibujará.
     * @param element Elemento a dibujar.
     * @param product Producto asociado al elemento.
     * @param scale Factor de escala para conversión de unidades.
     */
    private fun drawLabelElement(
        canvas: Canvas,
        element: LabelElement,
        product: Product,
        scale: Float
    ) {
        val x = (element.x * scale).toFloat()
        val y = (element.y * scale).toFloat()
        
        // Evaluar contenido del elemento con los datos del producto
        val content = element.evaluateContent(product)
        
        when (element.type.lowercase()) {
            "text" -> {
                drawTextElement(canvas, element, content, x, y, scale)
            }
            "barcode" -> {
                drawBarcodeElement(canvas, element, content, x, y, scale)
            }
            "qrcode" -> {
                drawQrCodeElement(canvas, element, content, x, y, scale)
            }
            "line" -> {
                drawLineElement(canvas, element, x, y, scale)
            }
            "rectangle" -> {
                drawRectangleElement(canvas, element, x, y, scale)
            }
            // Otros tipos de elementos se podrían implementar aquí
        }
    }
    
    /**
     * Dibuja un elemento de texto en el canvas.
     */
    private fun drawTextElement(
        canvas: Canvas,
        element: LabelElement,
        content: String,
        x: Float,
        y: Float,
        scale: Float
    ) {
        val paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            // Configurar tamaño de fuente
            textSize = element.fontSize?.let { it * scale } ?: (12 * scale)
            
            // Configurar estilo de fuente
            when (element.fontStyle?.lowercase()) {
                "bold" -> typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                "italic" -> typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                "bold_italic" -> typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                else -> typeface = Typeface.DEFAULT
            }
        }
        
        // Calcular alineación del texto
        val textX = when (element.alignment?.lowercase()) {
            "center" -> {
                val textWidth = paint.measureText(content)
                val elementWidth = (element.width ?: 0) * scale
                x + (elementWidth - textWidth) / 2
            }
            "right" -> {
                val textWidth = paint.measureText(content)
                val elementWidth = (element.width ?: 0) * scale
                x + elementWidth - textWidth
            }
            else -> x // Alineación izquierda por defecto
        }
        
        // Dibujar texto
        canvas.drawText(content, textX, y + paint.textSize, paint)
    }
    
    /**
     * Dibuja un elemento de código de barras en el canvas.
     */
    private fun drawBarcodeElement(
        canvas: Canvas,
        element: LabelElement,
        content: String,
        x: Float,
        y: Float,
        scale: Float
    ) {
        try {
            if (content.isBlank()) return
            
            val width = (element.width ?: 150) * scale
            val height = (element.height ?: 50) * scale
            
            // Determinar formato de código de barras
            val format = when (element.barcodeType?.uppercase()) {
                "EAN_13" -> BarcodeFormat.EAN_13
                "EAN_8" -> BarcodeFormat.EAN_8
                "QR_CODE" -> BarcodeFormat.QR_CODE
                "CODE_128" -> BarcodeFormat.CODE_128
                "CODE_39" -> BarcodeFormat.CODE_39
                "UPC_A" -> BarcodeFormat.UPC_A
                else -> BarcodeFormat.CODE_128 // Por defecto
            }
            
            // Generar matriz del código de barras
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                content,
                format,
                width.toInt(),
                height.toInt()
            )
            
            // Convertir matriz a bitmap
            val barcodeBitmap = Bitmap.createBitmap(
                bitMatrix.width,
                bitMatrix.height,
                Bitmap.Config.ARGB_8888
            )
            
            for (i in 0 until bitMatrix.width) {
                for (j in 0 until bitMatrix.height) {
                    barcodeBitmap.setPixel(
                        i, j,
                        if (bitMatrix[i, j]) Color.BLACK else Color.WHITE
                    )
                }
            }
            
            // Dibujar bitmap en el canvas
            canvas.drawBitmap(barcodeBitmap, x, y, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error al generar código de barras", e)
            // Dibujar texto de error
            val paint = Paint().apply {
                color = Color.RED
                isAntiAlias = true
                textSize = 12 * scale
            }
            canvas.drawText("Error: ${e.message}", x, y + paint.textSize, paint)
        }
    }
    
    /**
     * Dibuja un elemento de código QR en el canvas.
     */
    private fun drawQrCodeElement(
        canvas: Canvas,
        element: LabelElement,
        content: String,
        x: Float,
        y: Float,
        scale: Float
    ) {
        try {
            if (content.isBlank()) return
            
            val size = (element.width ?: 100) * scale
            
            // Generar matriz del código QR
            val bitMatrix = MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                size.toInt(),
                size.toInt()
            )
            
            // Convertir matriz a bitmap
            val qrBitmap = Bitmap.createBitmap(
                bitMatrix.width,
                bitMatrix.height,
                Bitmap.Config.ARGB_8888
            )
            
            for (i in 0 until bitMatrix.width) {
                for (j in 0 until bitMatrix.height) {
                    qrBitmap.setPixel(
                        i, j,
                        if (bitMatrix[i, j]) Color.BLACK else Color.WHITE
                    )
                }
            }
            
            // Dibujar bitmap en el canvas
            canvas.drawBitmap(qrBitmap, x, y, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error al generar código QR", e)
            // Dibujar texto de error
            val paint = Paint().apply {
                color = Color.RED
                isAntiAlias = true
                textSize = 12 * scale
            }
            canvas.drawText("Error: ${e.message}", x, y + paint.textSize, paint)
        }
    }
    
    /**
     * Dibuja un elemento de línea en el canvas.
     */
    private fun drawLineElement(
        canvas: Canvas,
        element: LabelElement,
        x: Float,
        y: Float,
        scale: Float
    ) {
        val paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = (element.borderWidth ?: 1) * scale
        }
        
        val lineWidth = (element.width ?: 100) * scale
        
        // Dibujar línea horizontal o vertical según dimensiones
        if (element.height == null || element.height == 0) {
            // Línea horizontal
            canvas.drawLine(x, y, x + lineWidth, y, paint)
        } else {
            // Línea vertical
            val lineHeight = element.height * scale
            canvas.drawLine(x, y, x, y + lineHeight, paint)
        }
    }
    
    /**
     * Dibuja un elemento de rectángulo en el canvas.
     */
    private fun drawRectangleElement(
        canvas: Canvas,
        element: LabelElement,
        x: Float,
        y: Float,
        scale: Float
    ) {
        val paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = (element.borderWidth ?: 1) * scale
        }
        
        val width = (element.width ?: 100) * scale
        val height = (element.height ?: 50) * scale
        
        // Dibujar rectángulo
        val rect = Rect(
            x.toInt(),
            y.toInt(),
            (x + width).toInt(),
            (y + height).toInt()
        )
        canvas.drawRect(rect, paint)
    }
    
    /**
     * Convierte una cadena de modelo de impresora a un objeto Model de la librería Brother.
     */
    private fun getPrinterModel(modelName: String): PrinterInfo.Model {
        return when (modelName.uppercase()) {
            "QL-820NWB" -> PrinterInfo.Model.QL_820NWB
            "QL-810W" -> PrinterInfo.Model.QL_810W
            "QL-800" -> PrinterInfo.Model.QL_800
            "QL-720NW" -> PrinterInfo.Model.QL_720NW
            "QL-710W" -> PrinterInfo.Model.QL_710W
            "QL-700" -> PrinterInfo.Model.QL_700
            "QL-650TD" -> PrinterInfo.Model.QL_650TD
            "QL-580N" -> PrinterInfo.Model.QL_580N
            "QL-550" -> PrinterInfo.Model.QL_550
            "QL-500" -> PrinterInfo.Model.QL_500
            "PT-P900W" -> PrinterInfo.Model.PT_P900W
            "PT-P950NW" -> PrinterInfo.Model.PT_P950NW
            "PT-E550W" -> PrinterInfo.Model.PT_E550W
            "PT-E500" -> PrinterInfo.Model.PT_E500
            "PT-E800W" -> PrinterInfo.Model.PT_E800W
            "PT-P800" -> PrinterInfo.Model.PT_P800
            "PT-P750W" -> PrinterInfo.Model.PT_P750W
            "PT-P700" -> PrinterInfo.Model.PT_P700
            "PT-E850TKW" -> PrinterInfo.Model.PT_E850TKW
            else -> PrinterInfo.Model.QL_820NWB // Modelo predeterminado
        }
    }
    
    /**
     * Obtiene un mensaje de error legible a partir de un código de error y estado de impresora.
     */
    private fun getErrorMessage(errorCode: Int, status: PrinterStatus): String {
        val errorMsg = when (errorCode) {
            PrinterInfo.ErrorCode.ERROR_NONE -> "No hay error"
            PrinterInfo.ErrorCode.ERROR_COMMUNICATION -> "Error de comunicación con la impresora"
            PrinterInfo.ErrorCode.ERROR_PAPER -> "Error de papel (sin papel o atasco)"
            PrinterInfo.ErrorCode.ERROR_BATTERY -> "Batería baja o agotada"
            PrinterInfo.ErrorCode.ERROR_UNSUPPORTED_MEDIA -> "Medio no compatible"
            PrinterInfo.ErrorCode.ERROR_OVERHEAT -> "Sobrecalentamiento de la impresora"
            PrinterInfo.ErrorCode.ERROR_COVER_OPEN -> "Tapa de la impresora abierta"
            PrinterInfo.ErrorCode.ERROR_WRONG_MEDIA -> "Medio incorrecto"
            PrinterInfo.ErrorCode.ERROR_FEED -> "Error de alimentación de papel"
            PrinterInfo.ErrorCode.ERROR_SYSTEM -> "Error del sistema de la impresora"
            PrinterInfo.ErrorCode.ERROR_PORT -> "Error en el puerto especificado"
            PrinterInfo.ErrorCode.ERROR_BUSY -> "Impresora ocupada"
            PrinterInfo.ErrorCode.ERROR_CANCEL -> "Impresión cancelada"
            PrinterInfo.ErrorCode.ERROR_OUT_OF_MEMORY -> "Memoria insuficiente para imprimir"
            PrinterInfo.ErrorCode.ERROR_PRIVACY_PRINT -> "Error de impresión privada"
            PrinterInfo.ErrorCode.ERROR_SETTING_OPEN -> "Error en la configuración"
            PrinterInfo.ErrorCode.ERROR_INVALID_PARAMETER -> "Parámetro inválido"
            PrinterInfo.ErrorCode.ERROR_INTERNAL -> "Error interno de la impresora"
            PrinterInfo.ErrorCode.ERROR_WRONG_CUSTOM_PAPER_SIZE -> "Tamaño de papel personalizado incorrecto"
            PrinterInfo.ErrorCode.ERROR_FIRMWARE_UPDATING -> "Actualización de firmware en progreso"
            else -> "Error desconocido: $errorCode"
        }
        
        // Añadir detalles adicionales del estado si es relevante
        val statusDetails = if (status.errorCode != 0) {
            "Código de error adicional: ${status.errorCode}"
        } else {
            ""
        }
        
        return if (statusDetails.isNotEmpty()) {
            "$errorMsg. $statusDetails"
        } else {
            errorMsg
        }
    }
}

/**
 * Clase de datos para representar una impresora encontrada en la red.
 */
data class PrinterDevice(
    val ipAddress: String,
    val modelName: String,
    val macAddress: String,
    val nodeName: String,
    val location: String
)

/**
 * Clase de datos para representar el resultado de una operación de impresión.
 */
data class PrintResult(
    val success: Boolean,
    val message: String
)