package com.productiva.android.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.model.SavedPrinter

/**
 * Interfaz de acceso a datos para la entidad SavedPrinter
 */
@Dao
interface SavedPrinterDao {
    
    /**
     * Obtiene todas las impresoras guardadas
     */
    @Query("SELECT * FROM saved_printers ORDER BY name ASC")
    fun getAllPrinters(): LiveData<List<SavedPrinter>>
    
    /**
     * Obtiene una impresora por su ID
     */
    @Query("SELECT * FROM saved_printers WHERE id = :printerId LIMIT 1")
    suspend fun getPrinterById(printerId: Int): SavedPrinter?
    
    /**
     * Obtiene una impresora por su dirección
     */
    @Query("SELECT * FROM saved_printers WHERE address = :address LIMIT 1")
    suspend fun getPrinterByAddress(address: String): SavedPrinter?
    
    /**
     * Obtiene la impresora predeterminada
     */
    @Query("SELECT * FROM saved_printers WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultPrinter(): SavedPrinter?
    
    /**
     * Obtiene impresoras por tipo
     */
    @Query("SELECT * FROM saved_printers WHERE printerType = :printerType ORDER BY name ASC")
    fun getPrintersByType(printerType: String): LiveData<List<SavedPrinter>>
    
    /**
     * Busca impresoras por nombre
     */
    @Query("SELECT * FROM saved_printers WHERE name LIKE '%' || :query || '%'")
    fun searchPrinters(query: String): LiveData<List<SavedPrinter>>
    
    /**
     * Inserta una nueva impresora
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(printer: SavedPrinter): Long
    
    /**
     * Inserta múltiples impresoras
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(printers: List<SavedPrinter>): List<Long>
    
    /**
     * Actualiza una impresora existente
     */
    @Update
    suspend fun update(printer: SavedPrinter): Int
    
    /**
     * Elimina una impresora por su ID
     */
    @Query("DELETE FROM saved_printers WHERE id = :printerId")
    suspend fun deletePrinterById(printerId: Int): Int
    
    /**
     * Elimina todas las impresoras
     */
    @Query("DELETE FROM saved_printers")
    suspend fun deleteAll(): Int
    
    /**
     * Actualiza el timestamp de último uso de una impresora
     */
    @Query("UPDATE saved_printers SET lastUsed = :timestamp WHERE id = :printerId")
    suspend fun updateLastUsed(printerId: Int, timestamp: Long): Int
    
    /**
     * Establece una impresora como predeterminada y quita el estado de predeterminada de las demás
     */
    @Query("UPDATE saved_printers SET isDefault = CASE WHEN id = :printerId THEN 1 ELSE 0 END")
    suspend fun setAsDefault(printerId: Int): Int
}