package com.productiva.android.printer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.brother.ptouch.sdk.PrinterInfo
import com.brother.ptouch.sdk.PrinterInfo.ErrorCode
import com.brother.ptouch.sdk.PrinterInfo.Model
import com.brother.ptouch.sdk.PrinterStatus
import com.brother.ptouch.sdk.Printer
import com.brother.ptouch.sdk.connection.BluetoothConnection
import com.brother.ptouch.sdk.connection.NetPrinter
import com.brother.ptouch.sdk.connection.NetworkConnection
import com.brother.ptouch.sdk.connection.USBConnection
import com.productiva.android.data.model.FieldPosition
import com.productiva.android.data.model.LabelTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.Executors

/**
 * Servicio para la impresión de etiquetas en impresoras Brother.
 * Proporciona métodos para imprimir etiquetas a partir de plantillas y datos.
 */
class BrotherPrintService private constructor(private val context: Context) {
    
    private val printer = Printer()
    private val printerInfo = printer.printerInfo
    private val executor = Executors.newFixedThreadPool(2)
    private val handler = Handler(Looper.getMainLooper())
    
    companion object {
        private const val TAG = "BrotherPrintService"
        
        // Constantes para tipos de conexión
        const val CONNECTION_USB = "usb"
        const val CONNECTION_BLUETOOTH = "bluetooth"
        const val CONNECTION_NETWORK = "network"
        
        // Tipos comunes de etiquetas
        const val LABEL_TYPE_STANDARD = "standard"
        const val LABEL_TYPE_DIE_CUT = "die_cut"
        const val LABEL_TYPE_CONTINUOUS = "continuous"
        
        @Volatile
        private var instance: BrotherPrintService? = null
        
        /**
         * Obtiene la instancia única del servicio de impresión.
         *
         * @param context Contexto de la aplicación.
         * @return Instancia del servicio de impresión.
         */
        fun getInstance(context: Context): BrotherPrintService {
            return instance ?: synchronized(this) {
                instance ?: BrotherPrintService(context.applicationContext).also { instance = it }
            }
        }
    }
    
    init {
        // Configuración inicial
        printerInfo.numberOfCopies = 1
        printerInfo.orientation = PrinterInfo.Orientation.PORTRAIT
        printerInfo.printMode = PrinterInfo.PrintMode.FIT_TO_PAGE
        printerInfo.isAutoCut = true
        
        // Por defecto, usar ancho máximo para la etiqueta
        printerInfo.paperSize = PrinterInfo.PaperSize.CUSTOM
        printerInfo.customPaperWidth = 62f  // Ancho máximo para QL-800 (62mm)
        printerInfo.customLength = 90f  // Largo de 90mm
        printerInfo.labelNameIndex = 0  // Label predefinido (ignorado si usamos custom)
        printerInfo.isHalfCut = false
        printerInfo.isSpecialTape = false
    }
    
    /**
     * Configura la impresora para conexión USB.
     */
    fun configureUsbConnection() {
        printerInfo.port = PrinterInfo.Port.USB
        printerInfo.connectionType = USBConnection.CONN_TYPE
    }
    
    /**
     * Configura la impresora para conexión Bluetooth.
     *
     * @param macAddress Dirección MAC de la impresora Bluetooth.
     */
    fun configureBluetoothConnection(macAddress: String) {
        printerInfo.port = PrinterInfo.Port.BLUETOOTH
        printerInfo.connectionType = BluetoothConnection.CONN_TYPE
        printerInfo.macAddress = macAddress
    }
    
    /**
     * Configura la impresora para conexión de red.
     *
     * @param ipAddress Dirección IP de la impresora.
     * @param port Puerto de la impresora (opcional, por defecto 9100).
     */
    fun configureNetworkConnection(ipAddress: String, port: Int = 9100) {
        printerInfo.port = PrinterInfo.Port.NET
        printerInfo.connectionType = NetworkConnection.CONN_TYPE
        printerInfo.ipAddress = ipAddress
        printerInfo.tcpIpPort = port
    }
    
    /**
     * Configura el modelo de impresora Brother.
     *
     * @param model Modelo de impresora como cadena de texto.
     */
    fun configureModel(model: String) {
        try {
            // Convertir el nombre del modelo a la constante de la SDK
            when (model.uppercase()) {
                "QL-800" -> printerInfo.printerModel = Model.QL_800
                "QL-810W" -> printerInfo.printerModel = Model.QL_810W
                "QL-820NWB" -> printerInfo.printerModel = Model.QL_820NWB
                "QL-700" -> printerInfo.printerModel = Model.QL_700
                "QL-710W" -> printerInfo.printerModel = Model.QL_710W
                "QL-720NW" -> printerInfo.printerModel = Model.QL_720NW
                "QL-1100" -> printerInfo.printerModel = Model.QL_1100
                "QL-1110NWB" -> printerInfo.printerModel = Model.QL_1110NWB
                "QL-1115NWB" -> printerInfo.printerModel = Model.QL_1115NWB
                "PT-P900W" -> printerInfo.printerModel = Model.PT_P900W
                "PT-P950NW" -> printerInfo.printerModel = Model.PT_P950NW
                else -> {
                    // Por defecto usar QL-800
                    Log.w(TAG, "Modelo de impresora desconocido: $model. Usando QL-800 por defecto.")
                    printerInfo.printerModel = Model.QL_800
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al configurar modelo de impresora", e)
        }
    }
    
    /**
     * Configura el tamaño y tipo de papel/etiqueta.
     *
     * @param width Ancho en milímetros.
     * @param height Alto en milímetros (opcional para etiquetas continuas).
     * @param labelType Tipo de etiqueta (standard, die_cut, continuous).
     */
    fun configurePaperSize(width: Int, height: Int? = null, labelType: String = LABEL_TYPE_DIE_CUT) {
        printerInfo.paperSize = PrinterInfo.PaperSize.CUSTOM
        printerInfo.customPaperWidth = width.toFloat()
        
        // Si es una etiqueta continua, el alto es controlado por el texto
        if (height != null) {
            printerInfo.customLength = height.toFloat()
        } else {
            // Alto auto-ajustable para etiquetas continuas
            printerInfo.customLength = 0f
        }
        
        // Configurar tipo de etiqueta
        printerInfo.isAutoCut = true
        
        when (labelType) {
            LABEL_TYPE_DIE_CUT -> {
                // Etiquetas pre-cortadas
                printerInfo.printMode = PrinterInfo.PrintMode.FIT_TO_PAGE
                printerInfo.isAutoCut = false
                printerInfo.isHalfCut = false
            }
            LABEL_TYPE_CONTINUOUS -> {
                // Rollo continuo
                printerInfo.printMode = PrinterInfo.PrintMode.SCALE_TO_FIT
                printerInfo.isAutoCut = true
                printerInfo.isHalfCut = false
            }
            else -> {
                // Etiquetas estándar
                printerInfo.printMode = PrinterInfo.PrintMode.FIT_TO_PAGE
                printerInfo.isAutoCut = true
                printerInfo.isHalfCut = false
            }
        }
    }
    
    /**
     * Imprime una etiqueta a partir de una plantilla y un mapa de datos.
     *
     * @param template Plantilla de etiqueta.
     * @param data Mapa con los datos a imprimir (clave = nombre del campo, valor = texto).
     * @return Resultado de la impresión.
     */
    suspend fun printLabel(
        template: LabelTemplate,
        data: Map<String, String>
    ): PrintResult = withContext(Dispatchers.IO) {
        try {
            // Configurar tamaño de la etiqueta según la plantilla
            configurePaperSize(
                width = template.width,
                height = template.height,
                labelType = if (template.height == null) LABEL_TYPE_CONTINUOUS else LABEL_TYPE_DIE_CUT
            )
            
            // Generar bitmap de la etiqueta a partir de la plantilla y los datos
            val labelBitmap = generateLabelBitmap(template, data)
            
            // Guardar el bitmap en un archivo temporal
            val tempFile = createTempFile(labelBitmap)
            
            // Imprimir el archivo
            val result = printImage(tempFile.absolutePath)
            
            // Eliminar el archivo temporal
            try {
                if (tempFile.exists()) {
                    tempFile.delete()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error al eliminar archivo temporal", e)
            }
            
            result
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al imprimir etiqueta", e)
            PrintResult(false, "Error al imprimir etiqueta: ${e.message}")
        }
    }
    
    /**
     * Genera un bitmap a partir de una plantilla y datos.
     *
     * @param template Plantilla de etiqueta.
     * @param data Mapa con los datos a imprimir.
     * @return Bitmap generado.
     */
    private fun generateLabelBitmap(
        template: LabelTemplate,
        data: Map<String, String>
    ): Bitmap {
        // Calcular dimensiones del bitmap (en píxeles)
        val ppi = template.dpi.toFloat() // puntos por pulgada
        val mmToInch = 0.0393701f // factor de conversión de mm a pulgadas
        
        val widthPx = (template.width * mmToInch * ppi).toInt()
        val heightPx = template.height?.let { (it * mmToInch * ppi).toInt() } ?: 400 // altura por defecto para etiquetas continuas
        
        // Crear bitmap y canvas
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Pintar fondo blanco
        canvas.drawColor(Color.WHITE)
        
        // Configurar pincel para texto
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textAlign = Paint.Align.LEFT
        }
        
        // Dibujar cada campo según su posición en la plantilla
        for ((fieldName, fieldPos) in template.fields) {
            val text = data[fieldName] ?: continue
            
            // Convertir posición de mm a píxeles
            val xPx = (fieldPos.x * mmToInch * ppi).toInt()
            val yPx = (fieldPos.y * mmToInch * ppi).toInt()
            
            // Configurar fuente según campo
            val fontSize = fieldPos.fontSize ?: 10
            val fontSizePx = (fontSize * mmToInch * ppi).toInt()
            
            paint.textSize = fontSizePx.toFloat()
            paint.isFakeBoldText = fieldPos.isBold
            
            // Configurar alineación
            val textAlign = when (fieldPos.alignment) {
                "center" -> Paint.Align.CENTER
                "right" -> Paint.Align.RIGHT
                else -> Paint.Align.LEFT
            }
            paint.textAlign = textAlign
            
            // Configurar estilo
            val typeface = when {
                fieldPos.isBold && fieldPos.isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                fieldPos.isBold -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                fieldPos.isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                else -> Typeface.DEFAULT
            }
            paint.typeface = typeface
            
            // Dibujar texto
            canvas.save()
            
            // Aplicar rotación si es necesario
            if (fieldPos.rotation != 0) {
                canvas.rotate(fieldPos.rotation.toFloat(), xPx.toFloat(), yPx.toFloat())
            }
            
            // Dibujar texto con ajuste a ancho máximo si está definido
            if (fieldPos.width != null) {
                val widthPxField = (fieldPos.width * mmToInch * ppi).toInt()
                drawTextWithWidth(canvas, text, xPx.toFloat(), yPx.toFloat(), paint, widthPxField)
            } else {
                canvas.drawText(text, xPx.toFloat(), yPx.toFloat(), paint)
            }
            
            canvas.restore()
        }
        
        return bitmap
    }
    
    /**
     * Dibuja texto con ajuste de ancho máximo.
     *
     * @param canvas Canvas donde dibujar.
     * @param text Texto a dibujar.
     * @param x Posición X.
     * @param y Posición Y.
     * @param paint Pincel para dibujar.
     * @param maxWidth Ancho máximo en píxeles.
     */
    private fun drawTextWithWidth(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        paint: Paint,
        maxWidth: Int
    ) {
        val textWidth = paint.measureText(text)
        
        if (textWidth <= maxWidth) {
            // El texto cabe completo
            canvas.drawText(text, x, y, paint)
        } else {
            // Hay que truncar o dividir el texto
            val textBounds = Rect()
            paint.getTextBounds(text, 0, text.length, textBounds)
            val charWidth = textWidth / text.length
            val maxChars = (maxWidth / charWidth).toInt()
            
            if (maxChars <= 3) {
                // Muy poco espacio, truncar con '...'
                canvas.drawText("...", x, y, paint)
            } else if (maxChars < text.length) {
                // Truncar y añadir '...'
                val truncated = text.take(maxChars - 3) + "..."
                canvas.drawText(truncated, x, y, paint)
            } else {
                // No debería ocurrir
                canvas.drawText(text, x, y, paint)
            }
        }
    }
    
    /**
     * Crea un archivo temporal con el bitmap de la etiqueta.
     *
     * @param bitmap Bitmap a guardar.
     * @return Archivo temporal creado.
     */
    private fun createTempFile(bitmap: Bitmap): File {
        val file = File(context.cacheDir, "label_${System.currentTimeMillis()}.png")
        
        try {
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error al guardar bitmap en archivo temporal", e)
            throw e
        }
        
        return file
    }
    
    /**
     * Imprime una imagen desde un archivo.
     *
     * @param filePath Ruta del archivo a imprimir.
     * @return Resultado de la impresión.
     */
    private fun printImage(filePath: String): PrintResult {
        try {
            // Verificar si el archivo existe
            val file = File(filePath)
            if (!file.exists()) {
                return PrintResult(false, "Archivo no encontrado: $filePath")
            }
            
            // Conectar con la impresora
            val isConnected = printer.startCommunication()
            if (!isConnected) {
                val error = getErrorMessage(printerInfo.errorCode)
                return PrintResult(false, "Error de conexión: $error")
            }
            
            // Imprimir imagen
            val isPrinted = printer.printFile(filePath)
            
            // Verificar resultado
            if (isPrinted) {
                // Obtener estado de la impresora
                val status = printer.printerStatus
                
                if (status.errorCode == PrinterInfo.ErrorCode.ERROR_NONE) {
                    return PrintResult(true, "Impresión exitosa")
                } else {
                    val error = getErrorMessage(status.errorCode)
                    return PrintResult(false, "Error durante la impresión: $error")
                }
            } else {
                val error = getErrorMessage(printerInfo.errorCode)
                return PrintResult(false, "Error al enviar a imprimir: $error")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al imprimir imagen", e)
            return PrintResult(false, "Error: ${e.message}")
        } finally {
            // Cerrar comunicación con la impresora
            try {
                printer.endCommunication()
            } catch (e: Exception) {
                Log.w(TAG, "Error al cerrar comunicación con impresora", e)
            }
        }
    }
    
    /**
     * Busca impresoras disponibles en la red.
     *
     * @return Lista de impresoras encontradas (IP, nombre, modelo).
     */
    suspend fun findNetworkPrinters(): List<PrinterDevice> = withContext(Dispatchers.IO) {
        val result = mutableListOf<PrinterDevice>()
        
        try {
            val printers = Printer.getNetPrinters(context)
            
            for (printer in printers) {
                result.add(
                    PrinterDevice(
                        id = "${printer.ipAddress}:${printer.port}",
                        name = printer.modelName,
                        model = printer.modelName,
                        ipAddress = printer.ipAddress,
                        macAddress = null,
                        connectionType = CONNECTION_NETWORK,
                        isDefault = false
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al buscar impresoras en red", e)
        }
        
        result
    }
    
    /**
     * Busca impresoras Brother conectadas por USB.
     *
     * @return Lista de impresoras encontradas.
     */
    suspend fun findUsbPrinters(): List<PrinterDevice> = withContext(Dispatchers.IO) {
        val result = mutableListOf<PrinterDevice>()
        
        try {
            // Configurar conexión USB
            configureUsbConnection()
            
            // Intentar conectar
            val isConnected = printer.startCommunication()
            
            if (isConnected) {
                // Obtener información de la impresora
                val status = printer.printerStatus
                
                if (status.errorCode == ErrorCode.ERROR_NONE) {
                    val modelName = printerInfo.printerModel.toString()
                    
                    result.add(
                        PrinterDevice(
                            id = "usb_printer",
                            name = modelName,
                            model = modelName,
                            ipAddress = null,
                            macAddress = null,
                            connectionType = CONNECTION_USB,
                            isDefault = false
                        )
                    )
                }
                
                // Cerrar conexión
                printer.endCommunication()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al buscar impresoras USB", e)
        }
        
        result
    }
    
    /**
     * Busca impresoras Brother conectadas por Bluetooth.
     *
     * @return Lista de impresoras encontradas.
     */
    suspend fun findBluetoothPrinters(): List<PrinterDevice> = withContext(Dispatchers.IO) {
        val result = mutableListOf<PrinterDevice>()
        
        try {
            // En el caso de Bluetooth, necesitaríamos usar APIs de Android para encontrar dispositivos
            // y luego intentar conectar con ellos para verificar si son impresoras Brother.
            // Este método es más complejo y requiere permisos adicionales de Bluetooth.
            
            // Aquí implementaríamos la búsqueda de dispositivos Bluetooth y
            // luego verificar si son impresoras Brother intentando conectar con ellos.
            
            // Por simplicidad, dejamos esta implementación pendiente para futuras versiones.
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al buscar impresoras Bluetooth", e)
        }
        
        result
    }
    
    /**
     * Obtiene mensaje de error a partir del código de error.
     *
     * @param errorCode Código de error.
     * @return Mensaje de error descriptivo.
     */
    private fun getErrorMessage(errorCode: ErrorCode): String {
        return when (errorCode) {
            ErrorCode.ERROR_NONE -> "No hay error"
            ErrorCode.ERROR_NOT_SUPPORTED -> "Operación no soportada"
            ErrorCode.ERROR_COMMUNICATION -> "Error de comunicación"
            ErrorCode.ERROR_PAPER_EMPTY -> "Sin papel"
            ErrorCode.ERROR_BATTERY_EMPTY -> "Batería baja"
            ErrorCode.ERROR_OVERHEAT -> "Sobrecalentamiento"
            ErrorCode.ERROR_PAPER_JAM -> "Atasco de papel"
            ErrorCode.ERROR_HIGH_VOLTAGE_ADAPTER -> "Adaptador de alto voltaje"
            ErrorCode.ERROR_CHANGE_CASSETTE -> "Cambiar casete"
            ErrorCode.ERROR_FEED_OR_CASSETTE_EMPTY -> "Alimentador o casete vacío"
            ErrorCode.ERROR_SYSTEM -> "Error de sistema"
            ErrorCode.ERROR_NO_CASSETTE -> "Sin casete"
            ErrorCode.ERROR_WRONG_CASSETTE_DIRECT -> "Casete incorrecto (directo)"
            ErrorCode.ERROR_CREATE_SOCKET_FAILED -> "Error al crear socket"
            ErrorCode.ERROR_CONNECT_SOCKET_FAILED -> "Error al conectar socket"
            ErrorCode.ERROR_GET_OUTPUT_STREAM_FAILED -> "Error al obtener stream de salida"
            ErrorCode.ERROR_GET_INPUT_STREAM_FAILED -> "Error al obtener stream de entrada"
            ErrorCode.ERROR_CLOSE_SOCKET_FAILED -> "Error al cerrar socket"
            ErrorCode.ERROR_OUT_OF_MEMORY -> "Sin memoria"
            ErrorCode.ERROR_SET_OVER_MARGIN -> "Margen excedido"
            ErrorCode.ERROR_NO_SD_CARD -> "Sin tarjeta SD"
            ErrorCode.ERROR_FILE_NOT_SUPPORTED -> "Archivo no soportado"
            ErrorCode.ERROR_EVALUATION_TIMEUP -> "Tiempo de evaluación agotado"
            ErrorCode.ERROR_WRONG_CUSTOM_INFO -> "Información personalizada incorrecta"
            ErrorCode.ERROR_NO_ADDRESS -> "Sin dirección"
            ErrorCode.ERROR_NOT_MATCH_ADDRESS -> "Dirección no coincide"
            ErrorCode.ERROR_FILE_NOT_FOUND -> "Archivo no encontrado"
            ErrorCode.ERROR_TEMPLATE_FILE_NOT_MATCH_MODEL -> "Plantilla no coincide con modelo"
            ErrorCode.ERROR_TEMPLATE_NOT_TRANS_MODEL -> "Plantilla no transferible a modelo"
            ErrorCode.ERROR_COVER_OPEN -> "Cubierta abierta"
            ErrorCode.ERROR_WRONG_TEMPLATE_KEY -> "Clave de plantilla incorrecta"
            ErrorCode.ERROR_TEMPLATE_NOT_PRINT_MODEL -> "Plantilla no imprimible en modelo"
            ErrorCode.ERROR_TEMPLATE_NOT_EXIST -> "Plantilla no existe"
            ErrorCode.ERROR_BUFFER_FULL -> "Buffer lleno"
            ErrorCode.ERROR_TUBE_EMPTY -> "Tubo vacío"
            ErrorCode.ERROR_TUBE_RIBBON_EMPTY -> "Cinta de tubo vacía"
            ErrorCode.ERROR_UPDATE_FIRMWARE_NOT_ON_AC -> "Actualización de firmware requiere conexión AC"
            ErrorCode.ERROR_WRONG_TEMPLATE -> "Plantilla incorrecta"
            ErrorCode.ERROR_BUSY -> "Impresora ocupada"
            ErrorCode.ERROR_TEMPLATE_NOT_SUPPORTED -> "Plantilla no soportada"
            ErrorCode.ERROR_WRONG_CASSETTE -> "Casete incorrecto"
            ErrorCode.ERROR_WRONG_CASSETTE_2 -> "Casete incorrecto (2)"
            ErrorCode.ERROR_TURN_OFF_PPOWER -> "Error de energía"
            ErrorCode.ERROR_UNSUPPORT_MEDIA -> "Medio no soportado"
            ErrorCode.ERROR_UNSUPPORT_MEDIA_SIZE -> "Tamaño de medio no soportado"
            ErrorCode.ERROR_UNSUPPORT_MEDIA_WIDTH -> "Ancho de medio no soportado"
            ErrorCode.ERROR_WRONG_MEDIA -> "Medio incorrecto"
            ErrorCode.ERROR_UNSUPPORT_SETTING_ITEM -> "Configuración no soportada"
            ErrorCode.ERROR_SETTING_ITEM_LIMITATION -> "Limitación de configuración"
            ErrorCode.ERROR_PRINTER_SETTING_NOT_SUPPORTED -> "Configuración de impresora no soportada"
            ErrorCode.ERROR_WRONG_LABEL -> "Etiqueta incorrecta"
            ErrorCode.ERROR_PORT_NOT_SUPPORTED -> "Puerto no soportado"
            ErrorCode.ERROR_WRONG_TEMPLATE_DATA -> "Datos de plantilla incorrectos"
            ErrorCode.ERROR_TIMEOUT -> "Tiempo de espera agotado"
            ErrorCode.ERROR_LABELS_REMAINING_AMOUNT -> "Error en cantidad de etiquetas restantes"
            ErrorCode.ERROR_AUTHENTICATION_FAIL -> "Error de autenticación"
            ErrorCode.ERROR_WRONG_DOT_BY_MM_400 -> "Error en puntos por mm (400)"
            ErrorCode.ERROR_WRONG_DOT_BY_MM -> "Error en puntos por mm"
            ErrorCode.ERROR_WRONG_NO_OF_COLORS -> "Número incorrecto de colores"
            ErrorCode.ERROR_MAIN_BOARD -> "Error en placa principal"
            ErrorCode.ERROR_NOTIFICATION -> "Error en notificación"
            ErrorCode.ERROR_UNSUPPORTED_MEDIA_SIZE_PRINT -> "Tamaño de medio no soportado para impresión"
            ErrorCode.ERROR_CANCEL_FAILED -> "Error al cancelar"
            ErrorCode.ERROR_PRINTER_MODEL_NOT_SUPPORTED -> "Modelo de impresora no soportado"
            ErrorCode.ERROR_UNSUPPORTED_TEMPLATE -> "Plantilla no soportada"
            ErrorCode.ERROR_INVALID_PARAMETER -> "Parámetro inválido"
            ErrorCode.ERROR_COOLING -> "Error de enfriamiento"
            ErrorCode.ERROR_UNSUPPORTED_MEDIAM_13 -> "Medio no soportado (13)"
            ErrorCode.ERROR_UNSUPPORTED_MEDIAM_14 -> "Medio no soportado (14)"
            ErrorCode.ERROR_UNSUPPORTED_MEDIAM_15 -> "Medio no soportado (15)"
            ErrorCode.ERROR_NO_ROLLS -> "Sin rollos"
            ErrorCode.ERROR_WRONG_ROLL_INSTALLED -> "Rollo incorrecto instalado"
            ErrorCode.ERROR_USB_DEVICE_NOT_SUPPORTED -> "Dispositivo USB no soportado"
            ErrorCode.ERROR_INSERTED_LABEL_NOT_SUPPORTED -> "Etiqueta insertada no soportada"
            ErrorCode.ERROR_HEAD_DEGRADATION -> "Degradación del cabezal de impresión"
            ErrorCode.ERROR_WAITING_INSERT_LABEL -> "Esperando inserción de etiqueta"
            ErrorCode.ERROR_INSERT_LABEL_WRONG -> "Etiqueta insertada incorrecta"
            ErrorCode.ERROR_BAD_HEAD_ELEMENT -> "Elemento de cabezal defectuoso"
            ErrorCode.ERROR_RESOLUTION_MODE -> "Error en modo de resolución"
            ErrorCode.ERROR_TUBE_CUTTER -> "Error en cortador de tubos"
            ErrorCode.ERROR_TUBE_CUTTER_JAM -> "Atasco en cortador de tubos"
            ErrorCode.ERROR_LABEL_CUTTER -> "Error en cortador de etiquetas"
            ErrorCode.ERROR_LABEL_CUTTER_JAM -> "Atasco en cortador de etiquetas"
            ErrorCode.ERROR_TUBE_JAM -> "Atasco de tubos"
            ErrorCode.ERROR_TAPE_EMPTY -> "Cinta vacía"
            ErrorCode.ERROR_UNSUPPORTED_MEDIA_TYPE -> "Tipo de medio no soportado"
            else -> "Error desconocido: $errorCode"
        }
    }
}

/**
 * Representa el resultado de una operación de impresión.
 *
 * @property success Indica si la operación fue exitosa.
 * @property message Mensaje descriptivo del resultado.
 */
data class PrintResult(
    val success: Boolean,
    val message: String
)

/**
 * Representa una impresora encontrada durante la búsqueda.
 *
 * @property id Identificador único de la impresora.
 * @property name Nombre amigable de la impresora.
 * @property model Modelo de la impresora.
 * @property ipAddress Dirección IP de la impresora (para conexiones de red).
 * @property macAddress Dirección MAC de la impresora (para conexiones Bluetooth).
 * @property connectionType Tipo de conexión (usb, bluetooth, network).
 * @property isDefault Indica si es la impresora predeterminada.
 */
data class PrinterDevice(
    val id: String,
    val name: String,
    val model: String,
    val ipAddress: String? = null,
    val macAddress: String? = null,
    val connectionType: String,
    val isDefault: Boolean = false
)