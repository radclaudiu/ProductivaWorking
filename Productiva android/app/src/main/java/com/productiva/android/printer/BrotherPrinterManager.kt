package com.productiva.android.printer

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import com.brother.ptouch.sdk.PrinterDriver
import com.brother.ptouch.sdk.PrinterInfo
import com.brother.ptouch.sdk.PrinterStatus
import org.json.JSONObject
import java.util.concurrent.Executors

/**
 * Gestor de impresión para impresoras Brother en la aplicación Android.
 * Proporciona una interfaz JavaScript para la WebView que permite imprimir etiquetas
 * directamente desde el navegador web interno usando la biblioteca SDK oficial de Brother.
 */
class BrotherPrinterManager(private val context: Context) {
    
    private val TAG = "BrotherPrinterManager"
    private val executor = Executors.newSingleThreadExecutor()
    
    // Almacena la última impresora utilizada
    private var lastPrinter: String? = null
    
    /**
     * Clase que expone métodos a JavaScript mediante JavascriptInterface
     */
    inner class BrotherPrinterJSInterface {
        
        /**
         * Método llamado desde JavaScript para imprimir una etiqueta
         * @param printDataJson Datos de la etiqueta en formato JSON
         * @return Resultado de la operación ("SUCCESS" o mensaje de error)
         */
        @JavascriptInterface
        fun printLabel(printDataJson: String): String {
            Log.d(TAG, "Solicitud de impresión recibida: $printDataJson")
            
            return try {
                // Procesar los datos recibidos desde JavaScript
                val printData = JSONObject(printDataJson)
                
                // Extraer información de la etiqueta
                val productName = printData.optString("productName", "")
                val conservationType = printData.optString("conservationType", "")
                val preparedBy = printData.optString("preparedBy", "")
                val startDate = printData.optString("startDate", "")
                val expiryDate = printData.optString("expiryDate", "")
                val secondaryExpiryDate = printData.optString("secondaryExpiryDate", "")
                val quantity = printData.optInt("quantity", 1)
                
                // Imprimir en un hilo separado
                executor.submit {
                    try {
                        val result = printLabelWithBrother(
                            productName,
                            conservationType,
                            preparedBy,
                            startDate,
                            expiryDate,
                            secondaryExpiryDate,
                            quantity
                        )
                        
                        if (result) {
                            Log.d(TAG, "Impresión completada con éxito")
                            // No necesitamos retornar nada aquí, ya que estamos en un hilo diferente
                        } else {
                            Log.e(TAG, "Error en la impresión")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Excepción durante la impresión", e)
                    }
                }
                
                // Retornar éxito inmediatamente, la impresión continuará en segundo plano
                "SUCCESS"
            } catch (e: Exception) {
                Log.e(TAG, "Error al procesar datos de impresión", e)
                "ERROR: ${e.message}"
            }
        }
        
        /**
         * Método llamado desde JavaScript para verificar que la interfaz está disponible
         */
        @JavascriptInterface
        fun onWebViewReady() {
            Log.d(TAG, "WebView notifica que está lista para interactuar")
        }
        
        /**
         * Método llamado desde JavaScript para obtener las impresoras disponibles
         * @return Lista de impresoras en formato JSON
         */
        @JavascriptInterface
        fun getAvailablePrinters(): String {
            // Este método podría implementarse para escanear impresoras Bluetooth o USB
            // Para simplificar, solo devolvemos un JSON con la última impresora utilizada
            val jsonObject = JSONObject()
            jsonObject.put("printers", JSONObject().apply {
                put("lastPrinter", lastPrinter ?: "")
            })
            return jsonObject.toString()
        }
    }
    
    /**
     * Método principal para imprimir una etiqueta usando la biblioteca Brother
     */
    private fun printLabelWithBrother(
        productName: String,
        conservationType: String,
        preparedBy: String,
        startDate: String,
        expiryDate: String,
        secondaryExpiryDate: String,
        quantity: Int
    ): Boolean {
        Log.d(TAG, "Iniciando impresión Brother: $productName, cantidad: $quantity")
        
        try {
            // Inicializar el driver de la impresora Brother
            val printerDriver = PrinterDriver()
            val printerInfo = PrinterInfo()
            
            // Configurar la información de la impresora
            // Nota: Estos valores deben ajustarse según el modelo específico
            printerInfo.printerModel = PrinterInfo.Model.TD_4550DNWB
            printerInfo.port = PrinterInfo.Port.BLUETOOTH
            
            // Si tenemos una impresora almacenada, usarla
            if (lastPrinter != null) {
                printerInfo.macAddress = lastPrinter
            }
            
            // Inicializar el driver con la información de la impresora
            printerDriver.setPrinterInfo(printerInfo)
            
            // Preparar el comando de impresión ESC/P
            val escpCommand = generateESCPCommand(
                productName,
                conservationType,
                preparedBy,
                startDate,
                expiryDate,
                secondaryExpiryDate
            )
            
            // Abrir la conexión con la impresora
            val openResult = printerDriver.open()
            if (openResult.errorCode != PrinterInfo.ErrorCode.ERROR_NONE) {
                Log.e(TAG, "Error al abrir conexión con impresora: ${openResult.errorCode}")
                return false
            }
            
            // Imprimir la cantidad especificada de etiquetas
            var success = true
            for (i in 1..quantity) {
                // Enviar los datos a la impresora
                val printResult = printerDriver.sendESCPCommand(escpCommand)
                
                if (printResult.errorCode != PrinterInfo.ErrorCode.ERROR_NONE) {
                    Log.e(TAG, "Error al imprimir etiqueta ${i}: ${printResult.errorCode}")
                    success = false
                    break
                }
                
                // Pequeña pausa entre etiquetas
                if (i < quantity) {
                    Thread.sleep(500)
                }
            }
            
            // Cerrar la conexión
            printerDriver.close()
            
            return success
        } catch (e: Exception) {
            Log.e(TAG, "Excepción durante la impresión Brother", e)
            return false
        }
    }
    
    /**
     * Genera el comando ESC/P para la impresora Brother
     */
    private fun generateESCPCommand(
        productName: String,
        conservationType: String,
        preparedBy: String,
        startDate: String,
        expiryDate: String,
        secondaryExpiryDate: String
    ): ByteArray {
        // Comandos ESC/P para impresoras Brother
        val ESC = 0x1B.toByte()
        val GS = 0x1D.toByte()
        
        // Inicializar comando
        val command = mutableListOf<Byte>()
        
        // Inicializar impresora
        command.add(ESC)
        command.add(0x40.toByte()) // @
        
        // Título del producto (centrado, grande y negrita)
        command.add(ESC)
        command.add(0x61.toByte()) // a
        command.add(0x01.toByte()) // Centrado
        
        command.add(GS)
        command.add(0x21.toByte()) // !
        command.add(0x11.toByte()) // Doble alto y ancho
        
        command.add(ESC)
        command.add(0x45.toByte()) // E
        command.add(0x01.toByte()) // Negrita activada
        
        // Añadir texto del producto
        productName.toByteArray().forEach { command.add(it) }
        
        // Salto de línea
        command.add(0x0A.toByte()) // LF
        command.add(0x0A.toByte()) // LF
        
        // Tipo de conservación (centrado y negrita)
        command.add(GS)
        command.add(0x21.toByte()) // !
        command.add(0x00.toByte()) // Tamaño normal
        
        // Añadir texto de conservación
        conservationType.toByteArray().forEach { command.add(it) }
        
        // Salto de línea
        command.add(0x0A.toByte()) // LF
        command.add(0x0A.toByte()) // LF
        
        // Alinear a la izquierda y quitar negrita para el resto
        command.add(ESC)
        command.add(0x61.toByte()) // a
        command.add(0x00.toByte()) // Izquierda
        
        command.add(ESC)
        command.add(0x45.toByte()) // E
        command.add(0x00.toByte()) // Negrita desactivada
        
        // Información del empleado
        preparedBy.toByteArray().forEach { command.add(it) }
        command.add(0x0A.toByte()) // LF
        
        // Fecha de inicio
        startDate.toByteArray().forEach { command.add(it) }
        command.add(0x0A.toByte()) // LF
        
        // Fecha de caducidad (en negrita)
        command.add(ESC)
        command.add(0x45.toByte()) // E
        command.add(0x01.toByte()) // Negrita activada
        
        expiryDate.toByteArray().forEach { command.add(it) }
        command.add(0x0A.toByte()) // LF
        
        // Fecha de caducidad secundaria si existe
        if (secondaryExpiryDate.isNotEmpty()) {
            command.add(ESC)
            command.add(0x45.toByte()) // E
            command.add(0x00.toByte()) // Negrita desactivada
            
            secondaryExpiryDate.toByteArray().forEach { command.add(it) }
            command.add(0x0A.toByte()) // LF
        }
        
        // Espacio final y cortar papel
        command.add(0x0A.toByte()) // LF
        command.add(0x0A.toByte()) // LF
        command.add(0x0A.toByte()) // LF
        
        // Comando para cortar papel
        command.add(GS)
        command.add(0x56.toByte()) // V
        command.add(0x41.toByte()) // A
        command.add(0x10.toByte()) // 16 puntos
        
        return command.toByteArray()
    }
    
    /**
     * Establece la última impresora utilizada
     */
    fun setLastPrinter(macAddress: String) {
        lastPrinter = macAddress
    }
    
    /**
     * Devuelve la interfaz JavaScript para usar en la WebView
     */
    fun getJavaScriptInterface(): BrotherPrinterJSInterface {
        return BrotherPrinterJSInterface()
    }
}