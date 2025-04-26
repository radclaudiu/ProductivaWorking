package com.productiva.android.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.productiva.android.dao.SavedPrinterDao
import com.productiva.android.database.AppDatabase
import com.productiva.android.model.SavedPrinter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio para manejar operaciones relacionadas con impresoras guardadas
 */
class PrinterRepository(private val context: Context) {
    
    private val savedPrinterDao: SavedPrinterDao = AppDatabase.getDatabase(context).savedPrinterDao()
    
    /**
     * Obtiene todas las impresoras guardadas como LiveData desde la base de datos local
     */
    fun getAllPrinters(): LiveData<List<SavedPrinter>> {
        return savedPrinterDao.getAllPrinters()
    }
    
    /**
     * Obtiene una impresora por su ID desde la base de datos local
     */
    suspend fun getPrinterById(printerId: Int): SavedPrinter? = withContext(Dispatchers.IO) {
        return@withContext savedPrinterDao.getPrinterById(printerId)
    }
    
    /**
     * Obtiene una impresora por su dirección desde la base de datos local
     */
    suspend fun getPrinterByAddress(address: String): SavedPrinter? = withContext(Dispatchers.IO) {
        return@withContext savedPrinterDao.getPrinterByAddress(address)
    }
    
    /**
     * Obtiene la impresora predeterminada desde la base de datos local
     */
    suspend fun getDefaultPrinter(): SavedPrinter? = withContext(Dispatchers.IO) {
        return@withContext savedPrinterDao.getDefaultPrinter()
    }
    
    /**
     * Guarda una nueva impresora en la base de datos local
     */
    suspend fun savePrinter(printer: SavedPrinter): Long = withContext(Dispatchers.IO) {
        if (printer.isDefault) {
            savedPrinterDao.clearDefaultPrinters()
        }
        return@withContext savedPrinterDao.insert(printer)
    }
    
    /**
     * Actualiza una impresora existente en la base de datos local
     */
    suspend fun updatePrinter(printer: SavedPrinter) = withContext(Dispatchers.IO) {
        if (printer.isDefault) {
            savedPrinterDao.clearDefaultPrinters()
        }
        savedPrinterDao.update(printer)
    }
    
    /**
     * Elimina una impresora de la base de datos local
     */
    suspend fun deletePrinter(printerId: Int) = withContext(Dispatchers.IO) {
        savedPrinterDao.deletePrinterById(printerId)
    }
    
    /**
     * Establece una impresora como predeterminada
     */
    suspend fun setDefaultPrinter(printerId: Int) = withContext(Dispatchers.IO) {
        savedPrinterDao.clearDefaultPrinters()
        savedPrinterDao.setDefaultPrinter(printerId)
    }
    
    /**
     * Obtiene la cantidad de impresoras guardadas
     */
    suspend fun getPrinterCount(): Int = withContext(Dispatchers.IO) {
        return@withContext savedPrinterDao.getPrinterCount()
    }
    
    /**
     * Actualiza el último uso de una impresora
     */
    suspend fun updateLastUsed(printerId: Int) = withContext(Dispatchers.IO) {
        val printer = savedPrinterDao.getPrinterById(printerId)
        printer?.let {
            val updatedPrinter = it.copy(lastUsed = System.currentTimeMillis())
            savedPrinterDao.update(updatedPrinter)
        }
    }
}