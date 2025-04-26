package com.productiva.android.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilidades para el manejo de archivos.
 */
object FileUtils {
    private const val TAG = "FileUtils"
    
    /**
     * Crea un archivo a partir de un Uri.
     * Útil para guardar imágenes desde la cámara o la galería.
     */
    fun createFileFromUri(context: Context, uri: Uri, prefijo: String): File? {
        try {
            // Generar nombre de archivo con timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val nombreArchivo = "${prefijo}${timestamp}.jpg"
            
            // Crear archivo en directorio de cache
            val archivoDestino = File(context.cacheDir, nombreArchivo)
            
            // Copiar contenido del Uri al archivo
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(archivoDestino).use { outputStream ->
                    val buffer = ByteArray(4 * 1024) // 4k buffer
                    var bytes: Int
                    while (inputStream.read(buffer).also { bytes = it } != -1) {
                        outputStream.write(buffer, 0, bytes)
                    }
                    outputStream.flush()
                }
            }
            
            return archivoDestino
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear archivo desde Uri: ${e.message}")
            return null
        }
    }
    
    /**
     * Crea un archivo de imagen a partir de un bitmap.
     */
    fun createFileFromBitmap(context: Context, bitmap: Bitmap, prefijo: String): File? {
        try {
            // Generar nombre de archivo con timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val nombreArchivo = "${prefijo}${timestamp}.jpg"
            
            // Crear archivo en directorio de cache
            val archivoDestino = File(context.cacheDir, nombreArchivo)
            
            // Guardar bitmap en archivo
            FileOutputStream(archivoDestino).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.flush()
            }
            
            return archivoDestino
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear archivo desde Bitmap: ${e.message}")
            return null
        }
    }
    
    /**
     * Redimensiona una imagen para reducir su tamaño.
     */
    fun resizeImage(inputStream: InputStream, maxWidth: Int = 1080, maxHeight: Int = 1080): Bitmap? {
        try {
            // Decodificar dimensiones primero
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.reset() // Reiniciar stream para leer de nuevo
            
            // Calcular factor de escala
            var scale = 1
            while (options.outWidth / scale > maxWidth || options.outHeight / scale > maxHeight) {
                scale *= 2
            }
            
            // Decodificar con escala
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = scale
            }
            
            return BitmapFactory.decodeStream(inputStream, null, decodeOptions)
        } catch (e: Exception) {
            Log.e(TAG, "Error al redimensionar imagen: ${e.message}")
            return null
        }
    }
    
    /**
     * Elimina archivos antiguos de caché.
     */
    fun cleanupOldCacheFiles(context: Context, maxAgeHours: Int = 24) {
        try {
            val currentTime = System.currentTimeMillis()
            val maxAgeMs = maxAgeHours * 60 * 60 * 1000L // Convertir horas a ms
            
            val cacheDir = context.cacheDir
            val files = cacheDir.listFiles() ?: return
            
            for (file in files) {
                val fileAge = currentTime - file.lastModified()
                if (fileAge > maxAgeMs) {
                    if (file.delete()) {
                        Log.d(TAG, "Archivo eliminado: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error limpiando archivos antiguos: ${e.message}")
        }
    }
}