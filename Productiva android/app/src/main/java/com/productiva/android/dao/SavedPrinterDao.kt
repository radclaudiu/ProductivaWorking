package com.productiva.android.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.model.SavedPrinter

/**
 * DAO para interactuar con la tabla de impresoras guardadas
 */
@Dao
interface SavedPrinterDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(printer: SavedPrinter): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(printers: List<SavedPrinter>)
    
    @Update
    suspend fun update(printer: SavedPrinter)
    
    @Query("SELECT * FROM saved_printers WHERE id = :printerId")
    suspend fun getPrinterById(printerId: Int): SavedPrinter?
    
    @Query("SELECT * FROM saved_printers ORDER BY name ASC")
    fun getAllPrinters(): LiveData<List<SavedPrinter>>
    
    @Query("SELECT * FROM saved_printers WHERE address = :address LIMIT 1")
    suspend fun getPrinterByAddress(address: String): SavedPrinter?
    
    @Query("SELECT * FROM saved_printers WHERE is_default = 1 LIMIT 1")
    suspend fun getDefaultPrinter(): SavedPrinter?
    
    @Query("UPDATE saved_printers SET is_default = 0")
    suspend fun clearDefaultPrinters()
    
    @Query("UPDATE saved_printers SET is_default = 1 WHERE id = :printerId")
    suspend fun setDefaultPrinter(printerId: Int)
    
    @Query("DELETE FROM saved_printers")
    suspend fun deleteAll()
    
    @Query("DELETE FROM saved_printers WHERE id = :printerId")
    suspend fun deletePrinterById(printerId: Int)
    
    @Query("SELECT COUNT(*) FROM saved_printers")
    suspend fun getPrinterCount(): Int
}