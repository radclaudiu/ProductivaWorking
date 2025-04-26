package com.productiva.android.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.SavedPrinter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.EnumMap
import java.util.Locale
import java.util.UUID

/**
 * Gestor de impresoras Bluetooth para imprimir etiquetas Brother
 */
class BluetoothPrinterManager(private val context: Context) {
    
    // UUID estándar para la comunicación por Bluetooth SPP (Serial Port Profile)
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    
    // Bluetooth Adapter
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    
    /**
     * Descubre dispositivos Bluetooth disponibles
     */
    suspend fun discoverDevices(): List<BluetoothDevice> = withContext(Dispatchers.IO) {
        val adapter = bluetoothAdapter ?: return@withContext emptyList()
        
        // Obtener dispositivos ya emparejados
        val bondedDevices = adapter.bondedDevices.toList()
        
        // TODO: Agregar lógica para descubrir dispositivos no emparejados si se necesita
        
        // Devolver dispositivos emparejados
        bondedDevices
    }
    
    /**
     * Obtiene dispositivos guardados previamente
     */
    fun getSavedDevices(savedPrinters: List<SavedPrinter>): List<BluetoothDevice> {
        val adapter = bluetoothAdapter ?: return emptyList()
        val devices = mutableListOf<BluetoothDevice>()
        
        for (printer in savedPrinters) {
            try {
                val device = adapter.getRemoteDevice(printer.address)
                devices.add(device)
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener dispositivo guardado: ${printer.address}", e)
            }
        }
        
        return devices
    }
    
    /**
     * Imprime una etiqueta en la impresora Brother seleccionada
     * 
     * @param device Impresora Bluetooth
     * @param title Título de la etiqueta
     * @param extraText Texto adicional para la etiqueta
     * @param date Fecha de impresión
     * @param printer Configuración de la impresora guardada
     * @param template Plantilla de etiqueta
     * @return true si la impresión fue exitosa, false en caso contrario
     */
    suspend fun printLabel(
        device: BluetoothDevice,
        title: String,
        extraText: String,
        date: Long,
        printer: SavedPrinter? = null,
        template: LabelTemplate? = null
    ): Boolean = withContext(Dispatchers.IO) {
        var socket: BluetoothSocket? = null
        var outputStream: OutputStream? = null
        
        try {
            // Conectar con la impresora
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket.connect()
            
            // Obtener el flujo de salida
            outputStream = socket.outputStream
            
            // Formatear la fecha según la plantilla o formato predeterminado
            val dateFormat = template?.dateFormat ?: "dd/MM/yyyy HH:mm"
            val dateString = SimpleDateFormat(dateFormat, Locale.getDefault())
                .format(Date(date))
            
            // Preparar los datos a imprimir para Brother
            val data = generateBrotherLabelData(title, extraText, dateString, printer, template)
            
            // Enviar datos a la impresora
            outputStream.write(data)
            outputStream.flush()
            
            // Éxito
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error al imprimir etiqueta Brother", e)
            return@withContext false
        } finally {
            // Cerrar recursos
            try {
                outputStream?.close()
                socket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error al cerrar conexión Bluetooth", e)
            }
        }
    }
    
    /**
     * Genera los datos de la etiqueta para una impresora Brother
     * Implementa los comandos específicos de Brother para etiquetas
     */
    private fun generateBrotherLabelData(
        title: String,
        extraText: String,
        dateString: String,
        printer: SavedPrinter?,
        template: LabelTemplate?
    ): ByteArray {
        // Obtener configuración o usar valores predeterminados
        val paperWidth = printer?.paperWidth ?: 62
        val paperLength = printer?.paperLength ?: 100
        val printDensity = printer?.printDensity ?: 0
        val printSpeed = printer?.printSpeed ?: 1
        
        // Valores de la plantilla o predeterminados
        val showTitle = template?.showTitle ?: true
        val showDate = template?.showDate ?: true
        val showExtraText = template?.showExtraText ?: true
        val showQrCode = template?.showQrCode ?: false
        val showBarcode = template?.showBarcode ?: false
        
        val titleFontSize = template?.titleFontSize ?: 4
        val dateFontSize = template?.dateFontSize ?: 2
        val extraTextFontSize = template?.extraTextFontSize ?: 3
        
        val marginTop = template?.marginTop ?: 3
        val marginLeft = template?.marginLeft ?: 3
        
        // Generar comandos ESC/P para la impresora Brother
        val commandStream = mutableListOf<Byte>()
        
        // Inicializar impresora y configurar
        commandStream.addAll(BROTHER_INITIALIZE.toList())
        
        // Configurar tamaño de la etiqueta
        commandStream.addAll(setLabelSize(paperWidth, paperLength).toList())
        
        // Configurar densidad de impresión
        commandStream.addAll(setPrintDensity(printDensity).toList())
        
        // Configurar velocidad de impresión
        commandStream.addAll(setPrintSpeed(printSpeed).toList())
        
        // Posicionar cursor al inicio con márgenes
        commandStream.addAll(setCursorPosition(marginLeft, marginTop).toList())
        
        // Título
        if (showTitle && title.isNotEmpty()) {
            commandStream.addAll(setFontSize(titleFontSize).toList())
            commandStream.addAll("$title\n".toByteArray().toList())
        }
        
        // Texto adicional
        if (showExtraText && extraText.isNotEmpty()) {
            commandStream.addAll(setFontSize(extraTextFontSize).toList())
            commandStream.addAll("$extraText\n".toByteArray().toList())
        }
        
        // Fecha
        if (showDate) {
            commandStream.addAll(setFontSize(dateFontSize).toList())
            commandStream.addAll("Fecha: $dateString\n".toByteArray().toList())
        }
        
        // Generar QR si está habilitado
        if (showQrCode) {
            val qrContent = "$title - $dateString"
            val qrCodeData = generateQRCodeForBrother(qrContent, 200)
            commandStream.addAll(qrCodeData.toList())
        }
        
        // Código de barras si está habilitado
        if (showBarcode) {
            val barcodeContent = "${System.currentTimeMillis()}"
            val barcodeData = generateBarcodeForBrother(barcodeContent)
            commandStream.addAll(barcodeData.toList())
        }
        
        // Finalizar impresión y cortar
        commandStream.addAll(BROTHER_PRINT_AND_CUT.toList())
        
        return commandStream.toByteArray()
    }
    
    /**
     * Configura el tamaño de la etiqueta
     */
    private fun setLabelSize(widthMm: Int, heightMm: Int): ByteArray {
        // Convertir mm a puntos (1 mm = 8 puntos aproximadamente en Brother)
        val width = widthMm * 8
        val height = heightMm * 8
        
        // Comando ESC/P para tamaño de etiqueta
        return byteArrayOf(
            0x1B, 0x69, 0x7A, // ESC i z - Configurar tamaño
            width.toByte(), (width shr 8).toByte(),
            height.toByte(), (height shr 8).toByte()
        )
    }
    
    /**
     * Configura la densidad de impresión
     */
    private fun setPrintDensity(density: Int): ByteArray {
        // Densidad de -5 a 5 (0 es normal)
        val adjustedDensity = when {
            density < -5 -> -5
            density > 5 -> 5
            else -> density
        }
        
        // Convertir a formato Brother (-5 a 5 -> 0 a 10)
        val brotherDensity = adjustedDensity + 5
        
        return byteArrayOf(
            0x1B, 0x69, 0x44, // ESC i D - Configurar densidad
            brotherDensity.toByte()
        )
    }
    
    /**
     * Configura la velocidad de impresión
     */
    private fun setPrintSpeed(speed: Int): ByteArray {
        // Velocidad 0-2 (0=lento, 1=normal, 2=rápido)
        val adjustedSpeed = when {
            speed < 0 -> 0
            speed > 2 -> 2
            else -> speed
        }
        
        return byteArrayOf(
            0x1B, 0x69, 0x53, // ESC i S - Configurar velocidad
            adjustedSpeed.toByte()
        )
    }
    
    /**
     * Configura la posición del cursor
     */
    private fun setCursorPosition(x: Int, y: Int): ByteArray {
        // Convertir mm a puntos
        val xPos = x * 8
        val yPos = y * 8
        
        return byteArrayOf(
            0x1B, 0x24, // ESC $ - Posición horizontal absoluta
            xPos.toByte(), (xPos shr 8).toByte(),
            0x1B, 0x69, 0x56, // ESC i V - Posición vertical
            yPos.toByte(), (yPos shr 8).toByte()
        )
    }
    
    /**
     * Configura el tamaño de fuente
     */
    private fun setFontSize(size: Int): ByteArray {
        // Tamaño 1-5 (1=muy pequeño, 3=normal, 5=muy grande)
        val adjustedSize = when {
            size < 1 -> 1
            size > 5 -> 5
            else -> size
        }
        
        // Convertir a comando brother (1-5 -> 24,32,48,64,96)
        val brotherSize = when (adjustedSize) {
            1 -> 24
            2 -> 32
            3 -> 48
            4 -> 64
            5 -> 96
            else -> 48
        }
        
        return byteArrayOf(
            0x1B, 0x58, brotherSize.toByte(), // ESC X - Tamaño de fuente
            0x00 // Atributos adicionales
        )
    }
    
    /**
     * Genera un código QR para impresora Brother
     */
    private fun generateQRCodeForBrother(content: String, size: Int = 200): ByteArray {
        // Crear hint para la generación del código QR
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        hints[EncodeHintType.MARGIN] = 1
        
        try {
            // Generar código QR usando ZXing
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            
            // Convertir a mapa de bits para Brother
            val commandStream = mutableListOf<Byte>()
            
            // Comandos de inicio para gráfico Brother
            val width = bitMatrix.width
            val height = bitMatrix.height
            
            // Comando de raster graphics
            commandStream.add(0x1B.toByte()) // ESC
            commandStream.add(0x2A.toByte()) // *
            commandStream.add(0x72.toByte()) // r - modo raster
            commandStream.add(0x01.toByte()) // calidad estándar
            
            // Ancho del gráfico en bytes (cada byte = 8 pixeles)
            val widthBytes = (width + 7) / 8
            commandStream.add(widthBytes.toByte())
            commandStream.add((widthBytes shr 8).toByte())
            
            // Altura en dots
            commandStream.add(height.toByte())
            commandStream.add((height shr 8).toByte())
            
            // Datos del código QR
            for (y in 0 until height) {
                for (x in 0 until widthBytes) {
                    var b = 0
                    for (i in 0 until 8) {
                        if (x * 8 + i < width && bitMatrix.get(x * 8 + i, y)) {
                            b = b or (1 shl (7 - i))
                        }
                    }
                    commandStream.add(b.toByte())
                }
            }
            
            return commandStream.toByteArray()
        } catch (e: Exception) {
            Log.e(TAG, "Error al generar código QR", e)
            return byteArrayOf()
        }
    }
    
    /**
     * Genera un código de barras para impresora Brother
     */
    private fun generateBarcodeForBrother(content: String): ByteArray {
        val commandStream = mutableListOf<Byte>()
        
        // Comando para código de barras CODE39
        commandStream.add(0x1B.toByte()) // ESC
        commandStream.add(0x69.toByte()) // i
        commandStream.add(0x62.toByte()) // b - barcode
        commandStream.add(0x06.toByte()) // CODE128
        
        // Altura del código 30-300 dots
        commandStream.add(100.toByte())
        
        // No imprimir texto bajo el código de barras
        commandStream.add(0x00.toByte())
        
        // Contenido del código de barras
        commandStream.addAll(content.toByteArray().toList())
        
        // Terminador
        commandStream.add(0x00.toByte())
        
        return commandStream.toByteArray()
    }
    
    companion object {
        private const val TAG = "BluetoothPrinterManager"
        
        // Comandos estándar para impresoras Brother
        private val BROTHER_INITIALIZE = byteArrayOf(
            0x1B, 0x40,  // ESC @ - Inicializar
            0x1B, 0x69, 0x61, 0x01  // ESC i a - Habilitar modo Brother ESC/P
        )
        
        private val BROTHER_PRINT_AND_CUT = byteArrayOf(
            0x0C,  // FF - Form feed
            0x1B, 0x69, 0x4D, 0x40,  // ESC i M - Auto corte
            0x1A  // SUB - End of data
        )
    }
}