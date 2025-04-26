package com.productiva.android.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.productiva.android.database.dao.PrinterDao
import com.productiva.android.model.SavedPrinter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Repositorio para gestionar impresoras guardadas en la aplicación.
 */
class PrinterRepository(
    private val printerDao: PrinterDao
) {
    private val TAG = "PrinterRepository"
    
    /**
     * Obtiene todas las impresoras guardadas.
     */
    fun getAllPrinters(): LiveData<List<SavedPrinter>> {
        return printerDao.getAllPrinters()
    }
    
    /**
     * Obtiene la impresora predeterminada, si existe.
     */
    suspend fun getDefaultPrinter(): SavedPrinter? {
        return withContext(Dispatchers.IO) {
            printerDao.getDefaultPrinter()
        }
    }
    
    /**
     * Obtiene una impresora por su ID.
     */
    suspend fun getPrinterById(printerId: Int): SavedPrinter? {
        return withContext(Dispatchers.IO) {
            printerDao.getPrinterById(printerId)
        }
    }
    
    /**
     * Obtiene una impresora por su dirección MAC.
     */
    suspend fun getPrinterByMacAddress(macAddress: String): SavedPrinter? {
        return withContext(Dispatchers.IO) {
            printerDao.getPrinterByMacAddress(macAddress)
        }
    }
    
    /**
     * Obtiene las impresoras recientemente usadas.
     */
    fun getRecentlyUsedPrinters(limit: Int = 5): LiveData<List<SavedPrinter>> {
        return printerDao.getRecentlyUsedPrinters(limit)
    }
    
    /**
     * Guarda una nueva impresora o actualiza una existente.
     */
    suspend fun saveOrUpdatePrinter(printer: SavedPrinter): Flow<ResourceState<SavedPrinter>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Comprobar si ya existe una impresora con la misma dirección MAC
            val existingPrinter = printerDao.getPrinterByMacAddress(printer.macAddress)
            
            val result = if (existingPrinter != null) {
                // Actualizar la impresora existente
                val updatedPrinter = existingPrinter.copy(
                    name = printer.name,
                    model = printer.model,
                    ipAddress = printer.ipAddress,
                    connectionType = printer.connectionType,
                    paperWidth = printer.paperWidth,
                    paperHeight = printer.paperHeight,
                    dpi = printer.dpi,
                    companyId = printer.companyId,
                    locationId = printer.locationId,
                    printSettings = printer.printSettings
                )
                
                withContext(Dispatchers.IO) {
                    printerDao.update(updatedPrinter)
                }
                
                updatedPrinter
            } else {
                // Insertar nueva impresora
                val id = withContext(Dispatchers.IO) {
                    printerDao.insert(printer)
                }
                
                // Recuperar la impresora guardada
                val savedPrinter = printerDao.getPrinterById(id.toInt())
                savedPrinter ?: printer
            }
            
            emit(ResourceState.Success(result))
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar impresora", e)
            emit(ResourceState.Error("Error al guardar impresora: ${e.message}"))
        }
    }
    
    /**
     * Establece una impresora como predeterminada.
     */
    suspend fun setDefaultPrinter(printerId: Int): Flow<ResourceState<Boolean>> = flow {
        emit(ResourceState.Loading())
        
        try {
            val affectedRows = withContext(Dispatchers.IO) {
                printerDao.setDefaultPrinter(printerId)
            }
            
            emit(ResourceState.Success(affectedRows > 0))
        } catch (e: Exception) {
            Log.e(TAG, "Error al establecer impresora predeterminada", e)
            emit(ResourceState.Error("Error al establecer impresora predeterminada: ${e.message}"))
        }
    }
    
    /**
     * Actualiza la fecha de último uso de una impresora.
     */
    suspend fun updateLastUsed(printerId: Int) {
        withContext(Dispatchers.IO) {
            printerDao.updateLastUsed(printerId)
            printerDao.incrementUseCount(printerId)
        }
    }
    
    /**
     * Elimina una impresora por su ID.
     */
    suspend fun deletePrinter(printerId: Int): Flow<ResourceState<Boolean>> = flow {
        emit(ResourceState.Loading())
        
        try {
            val affectedRows = withContext(Dispatchers.IO) {
                printerDao.deletePrinterById(printerId)
            }
            
            if (affectedRows > 0) {
                emit(ResourceState.Success(true))
            } else {
                emit(ResourceState.Error("No se encontró la impresora a eliminar"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar impresora", e)
            emit(ResourceState.Error("Error al eliminar impresora: ${e.message}"))
        }
    }
    
    /**
     * Cuenta el número de impresoras guardadas.
     */
    suspend fun getPrintersCount(): Int {
        return withContext(Dispatchers.IO) {
            printerDao.getPrintersCount()
        }
    }
}