package com.productiva.android.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilidades para manejo de archivos
 */
object FileUtils {
    private const val TAG = "FileUtils"
    
    /**
     * Crea un archivo temporal para una imagen
     */
    fun createTempImageFile(context: Context, prefix: String = "IMG_"): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${prefix}${timeStamp}_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        
        return try {
            File.createTempFile(fileName, ".jpg", storageDir)
        } catch (e: IOException) {
            Log.e(TAG, "Error al crear archivo temporal: ${e.message}")
            null
        }
    }
    
    /**
     * Guarda un bitmap como imagen JPG
     */
    fun saveBitmapToFile(bitmap: Bitmap, file: File): Boolean {
        return try {
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                fos.flush()
            }
            true
        } catch (e: IOException) {
            Log.e(TAG, "Error al guardar bitmap: ${e.message}")
            false
        }
    }
    
    /**
     * Carga un bitmap desde un archivo
     */
    fun loadBitmapFromFile(file: File): Bitmap? {
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar bitmap: ${e.message}")
            null
        }
    }
    
    /**
     * Copia un archivo desde un Uri a un archivo destino
     */
    fun copyUriToFile(context: Context, uri: Uri, destFile: File): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    copyStream(input, output)
                }
            }
            true
        } catch (e: IOException) {
            Log.e(TAG, "Error al copiar uri a archivo: ${e.message}")
            false
        }
    }
    
    /**
     * Copia datos desde un InputStream a un FileOutputStream
     */
    private fun copyStream(input: InputStream, output: FileOutputStream) {
        val buffer = ByteArray(4 * 1024) // 4KB buffer
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
        }
        output.flush()
    }
    
    /**
     * Elimina un archivo si existe
     */
    fun deleteFile(file: File?): Boolean {
        return file?.exists() == true && file.delete()
    }
    
    /**
     * Elimina archivos antiguos de un directorio
     */
    fun deleteOldFiles(directory: File, maxAgeMillis: Long): Int {
        if (!directory.exists() || !directory.isDirectory) return 0
        
        val currentTime = System.currentTimeMillis()
        var deletedCount = 0
        
        directory.listFiles()?.forEach { file ->
            if (file.isFile && (currentTime - file.lastModified() > maxAgeMillis)) {
                if (file.delete()) {
                    deletedCount++
                }
            }
        }
        
        return deletedCount
    }
    
    /**
     * Obtiene el tamaño de un archivo en bytes
     */
    fun getFileSize(file: File?): Long {
        return file?.length() ?: 0
    }
    
    /**
     * Obtiene la extensión de un archivo
     */
    fun getFileExtension(file: File): String {
        val name = file.name
        val lastDotIndex = name.lastIndexOf('.')
        return if (lastDotIndex > 0) {
            name.substring(lastDotIndex + 1).lowercase(Locale.getDefault())
        } else {
            ""
        }
    }
}