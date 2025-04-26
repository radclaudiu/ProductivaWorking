package com.productiva.android.utils

import android.content.Context
import android.util.Log
import com.productiva.android.BuildConfig
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilidad para gestionar logs de la aplicación.
 * Guarda logs tanto en LogCat como en archivos locales.
 */
object AppLogger {
    
    private const val MAX_LOG_FILE_SIZE = 5 * 1024 * 1024 // 5MB
    private const val LOG_TAG = "ProductivaApp"
    private const val FILE_DATE_FORMAT = "yyyy-MM-dd"
    
    private var logFile: File? = null
    private var isInitialized = false
    private var isDebugMode = BuildConfig.DEBUG
    
    /**
     * Inicializa el logger
     */
    fun init(context: Context) {
        try {
            val logsDir = File(context.getExternalFilesDir(null), "logs")
            if (!logsDir.exists()) {
                logsDir.mkdirs()
            }
            
            val dateStr = SimpleDateFormat(FILE_DATE_FORMAT, Locale.getDefault()).format(Date())
            logFile = File(logsDir, "productiva_log_$dateStr.txt")
            
            // Verificar tamaño del archivo y rotarlo si necesario
            if (logFile!!.exists() && logFile!!.length() > MAX_LOG_FILE_SIZE) {
                rotateLogFile(logsDir, dateStr)
            }
            
            isInitialized = true
            
            d(LOG_TAG, "Logger inicializado correctamente")
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error al inicializar logger: ${e.message}")
        }
    }
    
    /**
     * Log de nivel Debug
     */
    fun d(tag: String, message: String) {
        log(Log.DEBUG, tag, message)
    }
    
    /**
     * Log de nivel Info
     */
    fun i(tag: String, message: String) {
        log(Log.INFO, tag, message)
    }
    
    /**
     * Log de nivel Warning
     */
    fun w(tag: String, message: String) {
        log(Log.WARN, tag, message)
    }
    
    /**
     * Log de nivel Error
     */
    fun e(tag: String, message: String) {
        log(Log.ERROR, tag, message)
    }
    
    /**
     * Log de una excepción
     */
    fun e(tag: String, message: String, throwable: Throwable) {
        log(Log.ERROR, tag, "$message: ${throwable.message}")
        Log.e(tag, message, throwable)
    }
    
    /**
     * Método interno para registrar logs
     */
    private fun log(priority: Int, tag: String, message: String) {
        // LogCat
        when (priority) {
            Log.DEBUG -> if (isDebugMode) Log.d(tag, message)
            Log.INFO -> Log.i(tag, message)
            Log.WARN -> Log.w(tag, message)
            Log.ERROR -> Log.e(tag, message)
        }
        
        // Archivo de log
        if (isInitialized && logFile != null) {
            try {
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
                    .format(Date())
                val priorityStr = when (priority) {
                    Log.DEBUG -> "DEBUG"
                    Log.INFO -> "INFO"
                    Log.WARN -> "WARN"
                    Log.ERROR -> "ERROR"
                    else -> "UNKNOWN"
                }
                
                val logLine = "$timestamp [$priorityStr] $tag: $message\n"
                
                FileWriter(logFile, true).use { writer ->
                    writer.append(logLine)
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Error escribiendo en archivo de log: ${e.message}")
            }
        }
    }
    
    /**
     * Rota el archivo de log cuando supera el tamaño máximo
     */
    private fun rotateLogFile(logsDir: File, dateStr: String) {
        try {
            val timestamp = SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())
            val oldFile = logFile
            val newFileName = "productiva_log_${dateStr}_$timestamp.txt"
            val newFile = File(logsDir, newFileName)
            
            if (oldFile!!.renameTo(newFile)) {
                logFile = File(logsDir, "productiva_log_$dateStr.txt")
                d(LOG_TAG, "Archivo de log rotado a: $newFileName")
            } else {
                Log.e(LOG_TAG, "Error al rotar archivo de log")
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error en rotación de logs: ${e.message}")
        }
    }
}