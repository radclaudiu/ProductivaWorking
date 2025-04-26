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
     * Obtiene todas las impresoras guardadas.
     */
    @Query("SELECT * FROM saved_printers ORDER BY name")
    fun getAllPrinters(): LiveData<List<SavedPrinter>>
    
    /**
     * Obtiene la impresora predeterminada.
     */
    @Query("SELECT * FROM saved_printers WHERE is_default = 1 LIMIT 1")
    suspend fun getDefaultPrinter(): SavedPrinter?
    
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
     * Establece una impresora como predeterminada y quita el estado predeterminado de las demás.
     */
    @Query("UPDATE saved_printers SET is_default = (id = :printerId)")
    suspend fun setDefaultPrinter(printerId: Int): Int
    
    /**
     * Actualiza la fecha de último uso de una impresora.
     */
    @Query("UPDATE saved_printers SET last_used = :timestamp WHERE id = :printerId")
    suspend fun updateLastUsed(printerId: Int, timestamp: Long): Int
    
    /**
     * Obtiene impresoras por tipo de conexión.
     */
    @Query("SELECT * FROM saved_printers WHERE connection_type = :connectionType ORDER BY name")
    fun getPrintersByConnectionType(connectionType: String): LiveData<List<SavedPrinter>>
    
    /**
     * Busca impresoras por nombre o dirección.
     */
    @Query("SELECT * FROM saved_printers WHERE name LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%' ORDER BY name")
    fun searchPrinters(query: String): LiveData<List<SavedPrinter>>
    
    /**
     * Obtiene la impresora utilizada más recientemente.
     */
    @Query("SELECT * FROM saved_printers ORDER BY last_used DESC LIMIT 1")
    suspend fun getMostRecentlyUsedPrinter(): SavedPrinter?
    
    /**
     * Verifica si existe alguna impresora guardada.
     */
    @Query("SELECT COUNT(*) FROM saved_printers")
    suspend fun getPrintersCount(): Int
}