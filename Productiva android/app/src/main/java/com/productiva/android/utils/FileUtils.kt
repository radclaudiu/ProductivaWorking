package com.productiva.android.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.productiva.android.ProductivaApplication
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilidades para el manejo de archivos
 */
class FileUtils(private val app: ProductivaApplication) {
    
    private val context: Context = app.applicationContext
    
    /**
     * Obtiene un archivo a partir de una URI
     * @param uri URI del archivo
     * @return File o null si hay error
     */
    fun getFileFromUri(uri: Uri): File? {
        return try {
            when (uri.scheme) {
                ContentResolver.SCHEME_CONTENT -> copyContentUriToFile(uri)
                ContentResolver.SCHEME_FILE -> File(uri.path ?: return null)
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Copia un archivo de una URI de contenido a un archivo temporal
     * @param uri URI del contenido
     * @return File o null si hay error
     */
    private fun copyContentUriToFile(uri: Uri): File? {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: ""
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: ""
        
        val cacheDir = context.cacheDir
        val file = File.createTempFile(
            "tmp_${System.currentTimeMillis()}_",
            ".$extension",
            cacheDir
        )
        
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(4 * 1024) // 4k buffer
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                    outputStream.flush()
                }
            } ?: return null
            
            return file
        } catch (e: IOException) {
            return null
        }
    }
    
    /**
     * Crea un archivo para una imagen basado en el tipo (firma o foto)
     * @param type Tipo de archivo ('signature' o 'photo')
     * @return File creado
     */
    fun createImageFile(type: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(null)
        
        return File.createTempFile(
            "${type}_${timeStamp}_",
            if (type == "signature") ".png" else ".jpg",
            storageDir
        )
    }
    
    /**
     * Elimina archivos más antiguos que cierto tiempo
     * @param directory Directorio donde buscar
     * @param daysOld Días de antigüedad para considerar un archivo viejo
     * @return Número de archivos eliminados
     */
    fun deleteOldFiles(directory: File, daysOld: Int): Int {
        if (!directory.exists() || !directory.isDirectory) return 0
        
        val cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000)
        var deletedCount = 0
        
        directory.listFiles()?.forEach { file ->
            if (file.isFile && file.lastModified() < cutoffTime) {
                if (file.delete()) {
                    deletedCount++
                }
            }
        }
        
        return deletedCount
    }
}