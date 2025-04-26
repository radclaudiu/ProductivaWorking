package com.productiva.android.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
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
     * Crea un archivo temporal para una imagen.
     */
    fun createTempImageFile(context: Context, prefix: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${prefix}_${timeStamp}.jpg"
        return File(context.cacheDir, fileName)
    }
    
    /**
     * Crea un archivo a partir de un Bitmap.
     */
    fun createFileFromBitmap(context: Context, bitmap: Bitmap, prefix: String): File? {
        try {
            val file = createTempImageFile(context, prefix)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            return file
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear archivo desde bitmap", e)
            return null
        }
    }
    
    /**
     * Convierte un URI a un archivo.
     */
    fun uriToFile(context: Context, uri: Uri, prefix: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val file = createTempImageFile(context, prefix)
                copyInputStreamToFile(inputStream, file)
                file
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al convertir URI a archivo", e)
            null
        }
    }
    
    /**
     * Copia un InputStream a un archivo.
     */
    private fun copyInputStreamToFile(inputStream: InputStream, file: File) {
        try {
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(4 * 1024) // 4KB buffer
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }
        } finally {
            inputStream.close()
        }
    }
    
    /**
     * Obtiene un URI para un archivo usando FileProvider.
     */
    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
    
    /**
     * Elimina archivos temporales antiguos.
     */
    fun cleanupTempFiles(context: Context, maxAgeHours: Int = 24) {
        try {
            val cacheDir = context.cacheDir
            val now = System.currentTimeMillis()
            val maxAge = maxAgeHours * 60 * 60 * 1000L // Convertir horas a milisegundos
            
            cacheDir.listFiles()?.forEach { file ->
                val age = now - file.lastModified()
                if (age > maxAge) {
                    if (file.delete()) {
                        Log.d(TAG, "Archivo temporal eliminado: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al limpiar archivos temporales", e)
        }
    }
    
    /**
     * Guarda un archivo para su uso permanente (no temporal).
     */
    fun saveFilePermanently(context: Context, sourceFile: File, destinationFileName: String): File? {
        try {
            // Crear directorio de archivos si no existe
            val filesDir = File(context.filesDir, "uploads")
            if (!filesDir.exists()) {
                filesDir.mkdirs()
            }
            
            // Crear archivo de destino
            val destFile = File(filesDir, destinationFileName)
            
            // Copiar contenido
            sourceFile.inputStream().use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            return destFile
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar archivo permanentemente", e)
            return null
        }
    }
    
    /**
     * Prepara el directorio para guardar archivos de firma.
     */
    fun prepareSignatureDirectory(context: Context): File {
        val signatureDir = File(context.filesDir, "signatures")
        if (!signatureDir.exists()) {
            signatureDir.mkdirs()
        }
        return signatureDir
    }
    
    /**
     * Prepara el directorio para guardar archivos de fotos de tareas.
     */
    fun prepareTaskPhotosDirectory(context: Context): File {
        val photosDir = File(context.filesDir, "task_photos")
        if (!photosDir.exists()) {
            photosDir.mkdirs()
        }
        return photosDir
    }
}