package com.productiva.android.printer

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.brother.ptouch.sdk.LabelInfo
import com.brother.ptouch.sdk.NetPrinter
import com.brother.ptouch.sdk.Printer
import com.brother.ptouch.sdk.PrinterInfo
import com.brother.ptouch.sdk.PrinterStatus
import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.SavedPrinter
import com.productiva.android.repository.PrinterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Clase para gestionar las operaciones de impresión
 */
class PrinterManager(private val context: Context) {
    
    private val printerRepository = PrinterRepository(context)
    private val TAG = "PrinterManager"
    
    /**
     * Busca impresoras Brother en la red
     */
    suspend fun searchBrotherNetworkPrinters(): List<SavedPrinter> = withContext(Dispatchers.IO) {
        val result = mutableListOf<SavedPrinter>()
        
        try {
            val netPrinter = NetPrinter()
            val printerList = netPrinter.getPrinterList()
            
            for (i in 0 until printerList.size) {
                val model = printerList[i].modelName ?: continue
                val address = printerList[i].ipAddress ?: continue
                val name = printerList[i].printerName ?: "Printer $i"
                
                // Crear objeto SavedPrinter con datos de la impresora encontrada
                val savedPrinter = SavedPrinter(
                    name = name,
                    address = address,
                    model = model,
                    printerType = "wifi",
                    paperWidth = getPaperWidthForModel(model),
                    paperHeight = getPaperHeightForModel(model)
                )
                
                result.add(savedPrinter)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al buscar impresoras: ${e.message}")
        }
        
        return@withContext result
    }
    
    /**
     * Imprime una etiqueta en una impresora Brother
     */
    suspend fun printLabel(printer: SavedPrinter, template: LabelTemplate, labelData: Map<String, String>, labelImage: Bitmap?): PrintResult = withContext(Dispatchers.IO) {
        try {
            // Actualizar timestamp de último uso
            printerRepository.updateLastUsed(printer.id)
            
            // Configurar impresora
            val brotherPrinter = Printer()
            val printerInfo = PrinterInfo()
            
            // Establecer información de la impresora
            printerInfo.printerModel = getBrotherModelFromString(printer.model ?: "")
            printerInfo.port = PrinterInfo.Port.NET
            printerInfo.ipAddress = printer.address
            
            // Configurar parámetros
            configurePrinterFromSavedPrinter(printerInfo, printer)
            
            brotherPrinter.setPrinterInfo(printerInfo)
            
            // Imprimir
            val printResult: PrinterStatus
            if (labelImage != null) {
                // Imprimir desde bitmap
                printResult = brotherPrinter.printImage(labelImage)
            } else {
                // TODO: Implementar la creación de imagen a partir de la plantilla y datos
                return@withContext PrintResult(
                    success = false,
                    errorCode = -1,
                    errorMessage = "Impresión desde plantilla no implementada sin imagen"
                )
            }
            
            // Revisar resultado
            if (printResult.errorCode != PrinterInfo.ErrorCode.ERROR_NONE) {
                return@withContext PrintResult(
                    success = false,
                    errorCode = printResult.errorCode,
                    errorMessage = getPrinterErrorMessage(printResult.errorCode)
                )
            }
            
            return@withContext PrintResult(success = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error al imprimir: ${e.message}")
            return@withContext PrintResult(
                success = false,
                errorCode = -1,
                errorMessage = "Error al imprimir: ${e.message}"
            )
        }
    }
    
    /**
     * Obtiene el ancho de papel para un modelo de impresora
     */
    private fun getPaperWidthForModel(model: String): Int {
        return when {
            model.contains("QL-800") || model.contains("QL-810") || model.contains("QL-820") -> 62
            model.contains("QL-700") || model.contains("QL-710") || model.contains("QL-720") -> 62
            model.contains("QL-1100") || model.contains("QL-1110") || model.contains("QL-1115") -> 102
            else -> 62 // Valor por defecto
        }
    }
    
    /**
     * Obtiene el alto de papel para un modelo de impresora
     */
    private fun getPaperHeightForModel(model: String): Int {
        return when {
            model.contains("QL-") -> 29 // Altura típica para etiquetas estándar QL
            else -> 29 // Valor por defecto
        }
    }
    
    /**
     * Obtiene el modelo de impresora Brother a partir de un string
     */
    private fun getBrotherModelFromString(modelString: String): PrinterInfo.Model {
        return when {
            modelString.contains("QL-800") -> PrinterInfo.Model.QL_800
            modelString.contains("QL-810") -> PrinterInfo.Model.QL_810W
            modelString.contains("QL-820") -> PrinterInfo.Model.QL_820NWB
            modelString.contains("QL-1100") -> PrinterInfo.Model.QL_1100
            modelString.contains("QL-1110") -> PrinterInfo.Model.QL_1110NWB
            modelString.contains("QL-1115") -> PrinterInfo.Model.QL_1115NWB
            modelString.contains("QL-700") -> PrinterInfo.Model.QL_700
            modelString.contains("QL-710") -> PrinterInfo.Model.QL_710W
            modelString.contains("QL-720") -> PrinterInfo.Model.QL_720NW
            else -> PrinterInfo.Model.QL_800 // Valor por defecto
        }
    }
    
    /**
     * Configura la información de la impresora a partir de una impresora guardada
     */
    private fun configurePrinterFromSavedPrinter(printerInfo: PrinterInfo, savedPrinter: SavedPrinter) {
        // Configurar el tipo de etiqueta
        printerInfo.labelNameIndex = LabelInfo.QL700.W62.ordinal
        
        // Configurar opciones predeterminadas
        printerInfo.printMode = PrinterInfo.PrintMode.ORIGINAL
        printerInfo.orientation = PrinterInfo.Orientation.LANDSCAPE
        printerInfo.numberOfCopies = 1
        printerInfo.halftone = PrinterInfo.Halftone.PATTERNDITHER
        printerInfo.printQuality = PrinterInfo.PrintQuality.HIGH
        printerInfo.priority = PrinterInfo.Priority.QUALITY
        
        // Parsear y aplicar parámetros adicionales de conexión si existen
        savedPrinter.connectionParams?.let { paramsString ->
            try {
                val params = JSONObject(paramsString)
                
                // Orientación
                if (params.has("orientation")) {
                    val orientation = params.getString("orientation")
                    printerInfo.orientation = if (orientation == "portrait") {
                        PrinterInfo.Orientation.PORTRAIT
                    } else {
                        PrinterInfo.Orientation.LANDSCAPE
                    }
                }
                
                // Número de copias
                if (params.has("copies")) {
                    printerInfo.numberOfCopies = params.getInt("copies")
                }
                
                // Tipo de etiqueta específico
                if (params.has("labelType")) {
                    val labelType = params.getString("labelType")
                    printerInfo.labelNameIndex = getLabelNameIndex(labelType)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al parsear parámetros de conexión: ${e.message}")
            }
        }
    }
    
    /**
     * Obtiene el índice del tipo de etiqueta a partir de un string
     */
    private fun getLabelNameIndex(labelType: String): Int {
        return when (labelType) {
            "W29" -> LabelInfo.QL700.W29.ordinal
            "W62" -> LabelInfo.QL700.W62.ordinal
            "W62RB" -> LabelInfo.QL700.W62RB.ordinal
            "W103" -> LabelInfo.QL700.W103.ordinal
            "W103RB" -> LabelInfo.QL700.W103RB.ordinal
            "DT_W90" -> LabelInfo.QL700.DT_W90.ordinal
            "DT_W102" -> LabelInfo.QL700.DT_W102.ordinal
            "DT_W102H51" -> LabelInfo.QL700.DT_W102H51.ordinal
            "DT_W17H54" -> LabelInfo.QL700.DT_W17H54.ordinal
            "DT_W17H87" -> LabelInfo.QL700.DT_W17H87.ordinal
            else -> LabelInfo.QL700.W62.ordinal // Valor por defecto
        }
    }
    
    /**
     * Obtiene un mensaje de error a partir de un código de error
     */
    private fun getPrinterErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            PrinterInfo.ErrorCode.ERROR_NONE -> "No hay error"
            PrinterInfo.ErrorCode.ERROR_NOT_SAME_MODEL -> "Modelo de impresora no coincide"
            PrinterInfo.ErrorCode.ERROR_BROTHER_PRINTER_NOT_FOUND -> "Impresora Brother no encontrada"
            PrinterInfo.ErrorCode.ERROR_PAPER_EMPTY -> "Sin papel"
            PrinterInfo.ErrorCode.ERROR_BATTERY_EMPTY -> "Batería baja"
            PrinterInfo.ErrorCode.ERROR_COMMUNICATION -> "Error de comunicación"
            PrinterInfo.ErrorCode.ERROR_OVERHEAT -> "Sobrecalentamiento"
            PrinterInfo.ErrorCode.ERROR_PAPER_JAM -> "Atasco de papel"
            PrinterInfo.ErrorCode.ERROR_HIGH_VOLTAGE_ADAPTER -> "Adaptador de alto voltaje"
            PrinterInfo.ErrorCode.ERROR_CHANGE_CASSETTE -> "Cambiar casete"
            PrinterInfo.ErrorCode.ERROR_FEED_OR_CASSETTE_EMPTY -> "Alimentación o casete vacío"
            PrinterInfo.ErrorCode.ERROR_SYSTEM_ERROR -> "Error del sistema"
            PrinterInfo.ErrorCode.ERROR_NO_CASSETTE -> "Sin casete"
            PrinterInfo.ErrorCode.ERROR_WRONG_CASSETTE_DIRECT -> "Casete incorrecto"
            PrinterInfo.ErrorCode.ERROR_CREATE_SOCKET_FAILED -> "Error al crear socket"
            PrinterInfo.ErrorCode.ERROR_CONNECT_SOCKET_FAILED -> "Error al conectar socket"
            PrinterInfo.ErrorCode.ERROR_GET_OUTPUT_STREAM_FAILED -> "Error en stream de salida"
            PrinterInfo.ErrorCode.ERROR_GET_INPUT_STREAM_FAILED -> "Error en stream de entrada"
            PrinterInfo.ErrorCode.ERROR_CLOSE_SOCKET_FAILED -> "Error al cerrar socket"
            PrinterInfo.ErrorCode.ERROR_OUT_OF_MEMORY -> "Sin memoria"
            PrinterInfo.ErrorCode.ERROR_SET_OVER_MARGIN -> "Error en márgenes"
            PrinterInfo.ErrorCode.ERROR_NO_SD_CARD -> "Sin tarjeta SD"
            PrinterInfo.ErrorCode.ERROR_FILE_NOT_SUPPORTED -> "Archivo no soportado"
            PrinterInfo.ErrorCode.ERROR_EVALUATION_TIMEUP -> "Tiempo de evaluación agotado"
            PrinterInfo.ErrorCode.ERROR_WRONG_CUSTOM_INFO -> "Información personalizada incorrecta"
            PrinterInfo.ErrorCode.ERROR_COVER_OPEN -> "Cubierta abierta"
            PrinterInfo.ErrorCode.ERROR_WRONG_LABEL -> "Etiqueta incorrecta"
            PrinterInfo.ErrorCode.ERROR_PORT_NOT_SUPPORTED -> "Puerto no soportado"
            PrinterInfo.ErrorCode.ERROR_WRONG_TEMPLATE_KEY -> "Clave de plantilla incorrecta"
            PrinterInfo.ErrorCode.ERROR_BUSY -> "Impresora ocupada"
            PrinterInfo.ErrorCode.ERROR_TEMPLATE_NOT_PRINT_MODE -> "Plantilla no en modo impresión"
            PrinterInfo.ErrorCode.ERROR_CANCEL -> "Impresión cancelada"
            PrinterInfo.ErrorCode.ERROR_PRINTER_SETTING_NOT_SUPPORTED -> "Configuración no soportada"
            PrinterInfo.ErrorCode.ERROR_INVALID_PARAMETER -> "Parámetro inválido"
            PrinterInfo.ErrorCode.ERROR_INTERNAL_ERROR -> "Error interno"
            PrinterInfo.ErrorCode.ERROR_TEMPLATE_NOT_CONTROL_MODE -> "Plantilla no en modo control"
            PrinterInfo.ErrorCode.ERROR_TEMPLATE_NOT_EXIST -> "Plantilla no existe"
            PrinterInfo.ErrorCode.ERROR_BUFFER_FULL -> "Buffer lleno"
            PrinterInfo.ErrorCode.ERROR_TUBE_EMPTY -> "Tubo vacío"
            PrinterInfo.ErrorCode.ERROR_TUBE_RIBBON_EMPTY -> "Cinta de tubo vacía"
            PrinterInfo.ErrorCode.ERROR_UPDATE_FRIM_NOT_SUPPORTED -> "Actualización de firmware no soportada"
            PrinterInfo.ErrorCode.ERROR_OS_VERSION_NOT_SUPPORTED -> "Versión de SO no soportada"
            PrinterInfo.ErrorCode.ERROR_RESOLUTION_MODE -> "Error de modo de resolución"
            PrinterInfo.ErrorCode.ERROR_POWER_CABLE_UNPLUGGING -> "Cable de alimentación desconectado"
            PrinterInfo.ErrorCode.ERROR_BATTERY_TROUBLE -> "Problema de batería"
            PrinterInfo.ErrorCode.ERROR_UNSUPPORTED_MEDIA -> "Medio no soportado"
            PrinterInfo.ErrorCode.ERROR_TUBE_CUTTER -> "Cortador de tubo"
            PrinterInfo.ErrorCode.ERROR_UNSUPPORTED_TWO_COLOR -> "Dos colores no soportado"
            PrinterInfo.ErrorCode.ERROR_UNSUPPORTED_MONO_COLOR -> "Mono color no soportado"
            PrinterInfo.ErrorCode.ERROR_MINIMUM_LENGTH_LIMIT -> "Límite de longitud mínima"
            else -> "Error desconocido (código: $errorCode)"
        }
    }
    
    /**
     * Clase de datos que representa el resultado de una operación de impresión
     */
    data class PrintResult(
        val success: Boolean,
        val errorCode: Int = 0,
        val errorMessage: String = ""
    )
}