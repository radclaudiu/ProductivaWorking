package com.productiva.android.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.productiva.android.database.AppDatabase
import com.productiva.android.database.SavedPrinterDao
import com.productiva.android.model.SavedPrinter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio para gestionar impresoras guardadas
 */
class PrinterRepository(context: Context) {
    
    private val printerDao: SavedPrinterDao
    
    init {
        val database = AppDatabase.getInstance(context)
        printerDao = database.savedPrinterDao()
    }
    
    /**
     * Obtiene todas las impresoras guardadas
     */
    fun getAllPrinters(): LiveData<List<SavedPrinter>> {
        return printerDao.getAllPrinters()
    }
    
    /**
     * Obtiene una impresora por su ID
     */
    suspend fun getPrinterById(printerId: Int): SavedPrinter? {
        return withContext(Dispatchers.IO) {
            printerDao.getPrinterById(printerId)
        }
    }
    
    /**
     * Obtiene una impresora por su dirección
     */
    suspend fun getPrinterByAddress(address: String): SavedPrinter? {
        return withContext(Dispatchers.IO) {
            printerDao.getPrinterByAddress(address)
        }
    }
    
    /**
     * Obtiene la impresora predeterminada
     */
    suspend fun getDefaultPrinter(): SavedPrinter? {
        return withContext(Dispatchers.IO) {
            printerDao.getDefaultPrinter()
        }
    }
    
    /**
     * Obtiene impresoras por tipo
     */
    fun getPrintersByType(printerType: String): LiveData<List<SavedPrinter>> {
        return printerDao.getPrintersByType(printerType)
    }
    
    /**
     * Busca impresoras por nombre
     */
    fun searchPrinters(query: String): LiveData<List<SavedPrinter>> {
        return printerDao.searchPrinters(query)
    }
    
    /**
     * Inserta una nueva impresora
     */
    suspend fun insertPrinter(printer: SavedPrinter): Long {
        return withContext(Dispatchers.IO) {
            printerDao.insert(printer)
        }
    }
    
    /**
     * Inserta múltiples impresoras
     */
    suspend fun insertAllPrinters(printers: List<SavedPrinter>): List<Long> {
        return withContext(Dispatchers.IO) {
            printerDao.insertAll(printers)
        }
    }
    
    /**
     * Actualiza una impresora existente
     */
    suspend fun updatePrinter(printer: SavedPrinter): Int {
        return withContext(Dispatchers.IO) {
            printerDao.update(printer)
        }
    }
    
    /**
     * Elimina una impresora por su ID
     */
    suspend fun deletePrinter(printerId: Int): Int {
        return withContext(Dispatchers.IO) {
            printerDao.deletePrinterById(printerId)
        }
    }
    
    /**
     * Actualiza el timestamp de último uso de una impresora
     */
    suspend fun updateLastUsed(printerId: Int): Int {
        return withContext(Dispatchers.IO) {
            printerDao.updateLastUsed(printerId, System.currentTimeMillis())
        }
    }
    
    /**
     * Establece una impresora como predeterminada
     */
    suspend fun setAsDefault(printerId: Int): Int {
        return withContext(Dispatchers.IO) {
            printerDao.setAsDefault(printerId)
        }
    }
    
    /**
     * Guarda una impresora, actualizándola si ya existe o insertándola si es nueva
     */
    suspend fun savePrinter(printer: SavedPrinter): Long {
        return withContext(Dispatchers.IO) {
            val existingPrinter = printerDao.getPrinterByAddress(printer.address)
            
            if (existingPrinter != null) {
                printerDao.update(printer)
                existingPrinter.id.toLong()
            } else {
                printerDao.insert(printer)
            }
        }
    }
    
    /**
     * Guarda una impresora y la establece como predeterminada
     */
    suspend fun saveAndSetAsDefault(printer: SavedPrinter): Long {
        return withContext(Dispatchers.IO) {
            val id = savePrinter(printer)
            
            // Si es una nueva impresora, necesitamos recuperar su ID
            val printerId = if (printer.id != 0) printer.id else id.toInt()
            printerDao.setAsDefault(printerId)
            
            id
        }
    }
}