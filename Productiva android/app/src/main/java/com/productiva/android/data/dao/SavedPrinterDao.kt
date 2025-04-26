package com.productiva.android.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.productiva.android.model.SavedPrinter

/**
 * DAO para acceso a impresoras guardadas
 */
@Dao
interface SavedPrinterDao {
    
    /**
     * Obtiene todas las impresoras guardadas
     */
    @Query("SELECT * FROM saved_printers ORDER BY name ASC")
    suspend fun getAllPrinters(): List<SavedPrinter>
    
    /**
     * Obtiene la impresora predeterminada
     */
    @Query("SELECT * FROM saved_printers WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultPrinter(): SavedPrinter?
    
    /**
     * Obtiene una impresora por su dirección MAC
     */
    @Query("SELECT * FROM saved_printers WHERE address = :address LIMIT 1")
    suspend fun getPrinterByAddressSync(address: String): SavedPrinter?
    
    /**
     * Inserta o actualiza una impresora
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrinter(printer: SavedPrinter)
    
    /**
     * Elimina una impresora por su dirección MAC
     */
    @Query("DELETE FROM saved_printers WHERE address = :address")
    suspend fun deletePrinter(address: String)
    
    /**
     * Limpia el estado de impresora predeterminada de todas las impresoras
     */
    @Query("UPDATE saved_printers SET isDefault = 0")
    suspend fun clearDefaultPrinters()
}