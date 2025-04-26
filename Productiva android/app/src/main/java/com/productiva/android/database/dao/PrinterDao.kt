package com.productiva.android.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.model.SavedPrinter

/**
 * DAO para operaciones con impresoras guardadas en la base de datos local.
 */
@Dao
interface PrinterDao {
    
    /**
     * Inserta una impresora en la base de datos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(printer: SavedPrinter): Long
    
    /**
     * Inserta varias impresoras en la base de datos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(printers: List<SavedPrinter>): List<Long>
    
    /**
     * Actualiza la información de una impresora existente.
     */
    @Update
    suspend fun update(printer: SavedPrinter)
    
    /**
     * Obtiene una impresora por su ID.
     */
    @Query("SELECT * FROM saved_printers WHERE id = :printerId")
    suspend fun getPrinterById(printerId: Int): SavedPrinter?
    
    /**
     * Obtiene una impresora por su dirección MAC.
     */
    @Query("SELECT * FROM saved_printers WHERE mac_address = :macAddress")
    suspend fun getPrinterByMacAddress(macAddress: String): SavedPrinter?
    
    /**
     * Obtiene todas las impresoras guardadas.
     */
    @Query("SELECT * FROM saved_printers ORDER BY name")
    fun getAllPrinters(): LiveData<List<SavedPrinter>>
    
    /**
     * Obtiene la impresora predeterminada, si existe.
     */
    @Query("SELECT * FROM saved_printers WHERE is_default = 1 LIMIT 1")
    suspend fun getDefaultPrinter(): SavedPrinter?
    
    /**
     * Establece una impresora como predeterminada y quita este estado de las demás.
     */
    @Query("UPDATE saved_printers SET is_default = (CASE WHEN id = :printerId THEN 1 ELSE 0 END)")
    suspend fun setDefaultPrinter(printerId: Int): Int
    
    /**
     * Obtiene las impresoras recientemente usadas.
     */
    @Query("SELECT * FROM saved_printers ORDER BY last_used DESC LIMIT :limit")
    fun getRecentlyUsedPrinters(limit: Int = 5): LiveData<List<SavedPrinter>>
    
    /**
     * Actualiza la fecha de último uso de una impresora.
     */
    @Query("UPDATE saved_printers SET last_used = :timestamp WHERE id = :printerId")
    suspend fun updateLastUsed(printerId: Int, timestamp: Long = System.currentTimeMillis()): Int
    
    /**
     * Incrementa el contador de uso de una impresora.
     */
    @Query("UPDATE saved_printers SET use_count = use_count + 1 WHERE id = :printerId")
    suspend fun incrementUseCount(printerId: Int): Int
    
    /**
     * Elimina una impresora por su ID.
     */
    @Query("DELETE FROM saved_printers WHERE id = :printerId")
    suspend fun deletePrinterById(printerId: Int): Int
    
    /**
     * Elimina todas las impresoras.
     */
    @Query("DELETE FROM saved_printers")
    suspend fun deleteAllPrinters()
    
    /**
     * Cuenta el número de impresoras guardadas.
     */
    @Query("SELECT COUNT(*) FROM saved_printers")
    suspend fun getPrintersCount(): Int
}