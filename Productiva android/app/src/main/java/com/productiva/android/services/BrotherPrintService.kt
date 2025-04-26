package com.productiva.android.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.brother.ptouch.sdk.LabelInfo
import com.brother.ptouch.sdk.NetPrinter
import com.brother.ptouch.sdk.Printer
import com.brother.ptouch.sdk.PrinterInfo
import com.brother.ptouch.sdk.PrinterStatus
import com.productiva.android.R
import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.concurrent.CountDownLatch
import kotlin.math.roundToInt

/**
 * Servicio de impresión para impresoras Brother.
 * Maneja la comunicación con la impresora, búsqueda de dispositivos y tareas de impresión.
 */
class BrotherPrintService(private val context: Context) {
    
    private val TAG = "BrotherPrintService"
    
    // Handler para ejecutar callbacks en el hilo principal
    private val mainHandler = Handler(Looper.getMainLooper())
    
    // Instancia de la clase Printer de Brother SDK
    private val printer = Printer()
    
    // Información de la impresora
    private val printerInfo = PrinterInfo()
    
    /**
     * Inicializa el servicio de impresión.
     */
    init {
        // Configurar el contexto para el SDK
        printerInfo.printerModel = PrinterInfo.Model.QL_820NWB
        printerInfo.port = PrinterInfo.Port.NET
        printerInfo.printMode = PrinterInfo.PrintMode.ORIGINAL
        printerInfo.isAutoCut = true
        
        // Asignar la información de impresora al objeto printer
        printer.setPrinterInfo(printerInfo)
    }
    
    /**
     * Busca impresoras Brother en la red.
     * 
     * @return Lista de impresoras encontradas.
     */
    suspend fun searchNetworkPrinters(): List<PrinterDevice> = withContext(Dispatchers.IO) {
        val printers = mutableListOf<PrinterDevice>()
        
        try {
            val netPrinter = NetPrinter()
            val brotherPrinters = netPrinter.discoverNetPrinters()
            
            if (brotherPrinters != null) {
                for (i in 0 until brotherPrinters.size) {
                    val model = brotherPrinters.model[i]
                    val name = brotherPrinters.modelName[i]
                    val ip = brotherPrinters.ipAddress[i]
                    val macAddress = brotherPrinters.macAddress[i]
                    
                    printers.add(
                        PrinterDevice(
                            name = name ?: "Impresora Brother",
                            model = model,
                            ipAddress = ip,
                            macAddress = macAddress,
                            location = "Red",
                            status = "Disponible"
                        )
                    )
                    
                    Log.d(TAG, "Impresora encontrada: $name ($model) - IP: $ip - MAC: $macAddress")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al buscar impresoras: ${e.message}", e)
        }
        
        return@withContext printers
    }
    
    /**
     * Conecta a una impresora específica por IP.
     * 
     * @param ipAddress Dirección IP de la impresora.
     * @return true si la conexión fue exitosa, false en caso contrario.
     */
    suspend fun connectToPrinter(ipAddress: String): Boolean = withContext(Dispatchers.IO) {
        try {
            printerInfo.ipAddress = ipAddress
            printer.setPrinterInfo(printerInfo)
            
            val latch = CountDownLatch(1)
            var isConnected = false
            
            val printerThread = Thread {
                val status = printer.printerStatus
                isConnected = status.errorCode == PrinterInfo.ErrorCode.ERROR_NONE
                latch.countDown()
            }
            
            printerThread.start()
            latch.await()
            
            return@withContext isConnected
        } catch (e: Exception) {
            Log.e(TAG, "Error al conectar con la impresora: ${e.message}", e)
            return@withContext false
        }
    }
    
    /**
     * Imprime una etiqueta basada en una plantilla y un producto.
     * 
     * @param template Plantilla de etiqueta a utilizar.
     * @param product Producto a imprimir en la etiqueta.
     * @param printerIP Dirección IP de la impresora.
     * @param onResult Callback con el resultado de la impresión.
     */
    suspend fun printLabel(
        template: LabelTemplate,
        product: Product,
        printerIP: String,
        onResult: (Boolean, String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            // Conectar a la impresora
            if (!connectToPrinter(printerIP)) {
                mainHandler.post {
                    onResult(false, "No se pudo conectar con la impresora. Verifique la conexión.")
                }
                return@withContext
            }
            
            // Configurar tipo de papel según la plantilla
            configurePaperType(template)
            
            // Generar la imagen de la etiqueta
            val labelBitmap = generateLabelBitmap(template, product)
            
            // Imprimir la imagen
            val result = printImage(labelBitmap)
            
            mainHandler.post {
                if (result) {
                    onResult(true, "Etiqueta impresa correctamente")
                } else {
                    onResult(false, "Error al imprimir la etiqueta")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al imprimir etiqueta: ${e.message}", e)
            mainHandler.post {
                onResult(false, "Error: ${e.message}")
            }
        }
    }
    
    /**
     * Configura el tipo de papel según la plantilla.
     * 
     * @param template Plantilla de etiqueta a utilizar.
     */
    private fun configurePaperType(template: LabelTemplate) {
        // Configurar el tipo de papel según el valor de paperType en la plantilla
        template.paperType?.let { paperType ->
            when (paperType) {
                "W29H90" -> {
                    printerInfo.labelNameIndex = LabelInfo.QL700.W29H90.ordinal
                    printerInfo.isAutoCut = true
                    printerInfo.isCutAtEnd = true
                }
                "W62H100" -> {
                    printerInfo.labelNameIndex = LabelInfo.QL700.W62H100.ordinal
                    printerInfo.isAutoCut = true
                    printerInfo.isCutAtEnd = true
                }
                "W62" -> {
                    printerInfo.labelNameIndex = LabelInfo.QL700.W62.ordinal
                    printerInfo.isAutoCut = true
                    printerInfo.isCutAtEnd = true
                }
                "W29" -> {
                    printerInfo.labelNameIndex = LabelInfo.QL700.W29.ordinal
                    printerInfo.isAutoCut = true
                    printerInfo.isCutAtEnd = true
                }
                else -> {
                    // Tipo de papel predeterminado si no se reconoce
                    printerInfo.labelNameIndex = LabelInfo.QL700.W62.ordinal
                }
            }
        } ?: run {
            // Valor predeterminado si no se especifica paperType
            printerInfo.labelNameIndex = LabelInfo.QL700.W62.ordinal
        }
        
        // Aplicar configuración
        printer.setPrinterInfo(printerInfo)
    }
    
    /**
     * Genera una imagen de bitmap para la etiqueta basada en la plantilla y el producto.
     * 
     * @param template Plantilla de etiqueta a utilizar.
     * @param product Producto a imprimir en la etiqueta.
     * @return Bitmap con la imagen de la etiqueta.
     */
    private fun generateLabelBitmap(template: LabelTemplate, product: Product): Bitmap {
        // Crear un bitmap con las dimensiones de la plantilla
        val bitmap = Bitmap.createBitmap(
            template.width,
            template.height,
            Bitmap.Config.ARGB_8888
        )
        
        val canvas = Canvas(bitmap)
        
        // Pintar el fondo blanco
        canvas.drawColor(Color.WHITE)
        
        // Parsear la definición de la plantilla
        val templateData = JSONObject(template.templateData)
        
        // Procesar cada campo de la plantilla
        template.fields?.forEach { field ->
            // Obtener el valor del campo según el producto
            val fieldValue = getFieldValueForProduct(field.id, product)
            
            // Dibujar el campo según su tipo
            when (field.type) {
                "TEXT" -> {
                    val paint = Paint().apply {
                        color = Color.BLACK
                        textSize = (field.fontSize ?: 12).toFloat()
                        isAntiAlias = true
                        
                        // Aplicar el estilo de fuente si está especificado
                        field.fontName?.let { fontName ->
                            when {
                                fontName.contains("bold", ignoreCase = true) -> 
                                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                                fontName.contains("italic", ignoreCase = true) -> 
                                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                                else -> 
                                    typeface = Typeface.DEFAULT
                            }
                        }
                    }
                    
                    // Alineación del texto
                    val x = field.x.toFloat()
                    val y = field.y.toFloat() + paint.textSize // Ajustar para la línea base
                    
                    canvas.drawText(fieldValue, x, y, paint)
                }
                "BARCODE" -> {
                    // Implementar código de barras con una biblioteca externa o usar
                    // una representación simple para esta versión
                    val paint = Paint().apply {
                        color = Color.BLACK
                        textSize = 10f
                        isAntiAlias = true
                    }
                    canvas.drawText("BARCODE: $fieldValue", field.x.toFloat(), field.y.toFloat(), paint)
                }
                "QR" -> {
                    // Implementar código QR con una biblioteca externa o usar
                    // una representación simple para esta versión
                    val paint = Paint().apply {
                        color = Color.BLACK
                        textSize = 10f
                        isAntiAlias = true
                    }
                    canvas.drawText("QR: $fieldValue", field.x.toFloat(), field.y.toFloat(), paint)
                }
            }
        }
        
        return bitmap
    }
    
    /**
     * Obtiene el valor de un campo para un producto específico.
     * 
     * @param fieldId ID del campo de la plantilla.
     * @param product Producto del que obtener la información.
     * @return Valor del campo como String.
     */
    private fun getFieldValueForProduct(fieldId: String, product: Product): String {
        return when (fieldId) {
            "name" -> product.name
            "price" -> product.getFormattedPrice()
            "sku" -> product.sku ?: ""
            "barcode" -> product.barcode ?: ""
            "category" -> "" // No tenemos el nombre de la categoría en el modelo Product
            "stock" -> product.stock.toString()
            "description" -> product.description ?: ""
            "location" -> product.location ?: ""
            else -> ""
        }
    }
    
    /**
     * Imprime una imagen bitmap.
     * 
     * @param bitmap Imagen a imprimir.
     * @return true si la impresión fue exitosa, false en caso contrario.
     */
    private fun printImage(bitmap: Bitmap): Boolean {
        try {
            // Establecer la imagen a imprimir
            printer.setBitmap(bitmap)
            
            // Imprimir
            val status = printer.printImage()
            
            // Verificar el resultado
            return status.errorCode == PrinterInfo.ErrorCode.ERROR_NONE
        } catch (e: Exception) {
            Log.e(TAG, "Error al imprimir imagen: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Obtiene el estado actual de la impresora.
     * 
     * @return Estado de la impresora.
     */
    suspend fun getPrinterStatus(ipAddress: String): PrinterStatus = withContext(Dispatchers.IO) {
        try {
            // Configurar la dirección IP
            printerInfo.ipAddress = ipAddress
            printer.setPrinterInfo(printerInfo)
            
            // Obtener el estado
            return@withContext printer.printerStatus
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener estado de la impresora: ${e.message}", e)
            return@withContext PrinterStatus()
        }
    }
    
    /**
     * Clase que representa una impresora encontrada.
     */
    data class PrinterDevice(
        val name: String,
        val model: String,
        val ipAddress: String,
        val macAddress: String,
        val location: String,
        val status: String
    )
}