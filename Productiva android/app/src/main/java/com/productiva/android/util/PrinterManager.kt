package com.productiva.android.util

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.productiva.android.model.SavedPrinter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

/**
 * Clase que gestiona la comunicación con impresoras Bluetooth, especialmente Brother
 */
class PrinterManager(private val application: Application) {
    
    private val TAG = "PrinterManager"
    
    // UUID estándar para comunicación Bluetooth SPP (Serial Port Profile)
    private val BLUETOOTH_SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    
    // Adaptador Bluetooth
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        bluetoothManager.adapter
    }
    
    /**
     * Comprueba si el Bluetooth está disponible y activado
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    
    /**
     * Obtiene una lista de dispositivos Bluetooth emparejados
     */
    fun getPairedDevices(): List<BluetoothDevice> {
        if (!isBluetoothEnabled()) {
            return emptyList()
        }
        
        return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }
    
    /**
     * Imprime una etiqueta usando la impresora y plantilla especificadas
     */
    suspend fun printLabel(printer: SavedPrinter, template: String, copies: Int = 1): Boolean = withContext(Dispatchers.IO) {
        if (!isBluetoothEnabled()) {
            Log.e(TAG, "Bluetooth no está activado")
            return@withContext false
        }
        
        val device = findDeviceByAddress(printer.address)
        if (device == null) {
            Log.e(TAG, "No se encontró el dispositivo con dirección ${printer.address}")
            return@withContext false
        }
        
        var socket: android.bluetooth.BluetoothSocket? = null
        var outputStream: OutputStream? = null
        
        try {
            // Crear socket y conectar
            socket = device.createRfcommSocketToServiceRecord(BLUETOOTH_SPP_UUID)
            socket.connect()
            
            // Obtener stream de salida
            outputStream = socket.outputStream
            
            // Preparar datos según el tipo de impresora
            val printData = when (printer.printerType) {
                "BROTHER_CPCL" -> prepareBrotherCpclData(template, printer)
                "BROTHER_ESC_POS" -> prepareEscPosData(template, printer)
                "GENERIC_ESC_POS" -> prepareEscPosData(template, printer)
                else -> prepareGenericData(template)
            }
            
            // Imprimir el número de copias solicitado
            for (i in 1..copies) {
                outputStream.write(printData)
                outputStream.flush()
                
                // Pequeña pausa entre copias
                if (i < copies) {
                    Thread.sleep(500)
                }
            }
            
            Log.d(TAG, "Etiqueta impresa correctamente")
            return@withContext true
            
        } catch (e: IOException) {
            Log.e(TAG, "Error al imprimir: ${e.message}", e)
            return@withContext false
        } finally {
            // Cerrar recursos
            try {
                outputStream?.close()
                socket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error al cerrar recursos: ${e.message}", e)
            }
        }
    }
    
    /**
     * Busca un dispositivo Bluetooth por su dirección MAC
     */
    private fun findDeviceByAddress(address: String): BluetoothDevice? {
        if (!isBluetoothEnabled()) {
            return null
        }
        
        val pairedDevices = bluetoothAdapter?.bondedDevices ?: return null
        
        return pairedDevices.find { it.address == address }
    }
    
    /**
     * Prepara los datos para impresoras Brother en formato CPCL
     */
    private fun prepareBrotherCpclData(template: String, printer: SavedPrinter): ByteArray {
        // Configurar opciones específicas de Brother
        val width = printer.width ?: 384
        val height = printer.height ?: 576
        val density = printer.density ?: 6
        
        // Encabezado CPCL
        val cpclHeader = """
            ! 0 200 200 $height $width
            PW $width
            TONE $density
            SPEED 2
            PAGE-WIDTH $width
            
        """.trimIndent()
        
        // Formatear el template como comandos CPCL
        val cpclCommands = formatTemplateToCPCL(template, width)
        
        // Finalizar el documento
        val cpclFooter = """
            
            FORM
            PRINT
            
        """.trimIndent()
        
        // Combinar todo
        val fullCpclCommand = cpclHeader + cpclCommands + cpclFooter
        
        return fullCpclCommand.toByteArray()
    }
    
    /**
     * Prepara los datos para impresoras que usan ESC/POS
     */
    private fun prepareEscPosData(template: String, printer: SavedPrinter): ByteArray {
        // Inicialización de la impresora
        val initBytes = byteArrayOf(0x1B, 0x40) // ESC @
        
        // Ajustar ancho de impresión
        val width = printer.width ?: 384
        val widthBytes = when {
            width <= 384 -> byteArrayOf(0x1B, 0x57, 0x00, 0x00, 0x80, 0x01) // ESC W para 384 puntos
            else -> byteArrayOf(0x1B, 0x57, 0x00, 0x00, 0x00, 0x02) // ESC W para 576 puntos
        }
        
        // Formatear el template
        val formattedText = formatTemplateToText(template)
        val textBytes = formattedText.toByteArray()
        
        // Cortar el papel
        val cutBytes = byteArrayOf(0x1D, 0x56, 0x42, 0x00) // GS V B
        
        // Combinar todos los bytes
        return initBytes + widthBytes + textBytes + cutBytes
    }
    
    /**
     * Prepara datos genéricos para otras impresoras
     */
    private fun prepareGenericData(template: String): ByteArray {
        // Simplemente enviar el texto como bytes
        return formatTemplateToText(template).toByteArray()
    }
    
    /**
     * Formatea una plantilla para impresión en formato CPCL
     */
    private fun formatTemplateToCPCL(template: String, width: Int): String {
        // Dividir el template en líneas
        val lines = template.split("\n")
        val sb = StringBuilder()
        
        var yPos = 20
        val lineHeight = 30
        
        for (line in lines) {
            if (line.isNotEmpty()) {
                // Texto normal
                sb.append("TEXT 0 0 20 $yPos $line\n")
                yPos += lineHeight
            } else {
                // Línea vacía - solo incrementar posición Y
                yPos += lineHeight / 2
            }
        }
        
        return sb.toString()
    }
    
    /**
     * Formatea una plantilla para impresión en formato de texto plano
     */
    private fun formatTemplateToText(template: String): String {
        // Ajustar a formato para impresoras térmicas
        return template + "\n\n\n\n" // Añadir líneas en blanco para avance del papel
    }
}