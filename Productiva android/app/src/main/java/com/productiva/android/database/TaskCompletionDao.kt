package com.productiva.android.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.model.TaskCompletion
import java.util.Date

/**
 * Interfaz de acceso a datos para la entidad TaskCompletion
 */
@Dao
interface TaskCompletionDao {
    
    /**
     * Obtiene todas las completaciones
     */
    @Query("SELECT * FROM task_completions ORDER BY completionDate DESC")
    fun getAllCompletions(): LiveData<List<TaskCompletion>>
    
    /**
     * Obtiene una completación por su ID
     */
    @Query("SELECT * FROM task_completions WHERE id = :completionId LIMIT 1")
    suspend fun getCompletionById(completionId: Int): TaskCompletion?
    
    /**
     * Obtiene completaciones por tarea
     */
    @Query("SELECT * FROM task_completions WHERE taskId = :taskId ORDER BY completionDate DESC")
    fun getCompletionsByTaskId(taskId: Int): LiveData<List<TaskCompletion>>
    
    /**
     * Obtiene completaciones por usuario
     */
    @Query("SELECT * FROM task_completions WHERE userId = :userId ORDER BY completionDate DESC")
    fun getCompletionsByUserId(userId: Int): LiveData<List<TaskCompletion>>
    
    /**
     * Obtiene completaciones por rango de fechas
     */
    @Query("SELECT * FROM task_completions WHERE completionDate BETWEEN :startDate AND :endDate ORDER BY completionDate DESC")
    fun getCompletionsByDateRange(startDate: Date, endDate: Date): LiveData<List<TaskCompletion>>
    
    /**
     * Inserta una nueva completación
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(completion: TaskCompletion): Long
    
    /**
     * Inserta múltiples completaciones
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(completions: List<TaskCompletion>): List<Long>
    
    /**
     * Actualiza una completación existente
     */
    @Update
    suspend fun update(completion: TaskCompletion): Int
    
    /**
     * Elimina una completación por su ID
     */
    @Query("DELETE FROM task_completions WHERE id = :completionId")
    suspend fun deleteCompletionById(completionId: Int): Int
    
    /**
     * Elimina todas las completaciones
     */
    @Query("DELETE FROM task_completions")
    suspend fun deleteAll(): Int
    
    /**
     * Obtiene completaciones pendientes de sincronización
     */
    @Query("SELECT * FROM task_completions WHERE pendingUpload = 1")
    suspend fun getPendingUpload(): List<TaskCompletion>
    
    /**
     * Marca una completación como sincronizada
     */
    @Query("UPDATE task_completions SET synced = 1, pendingUpload = 0, lastSyncedAt = :syncDate WHERE id = :completionId")
    suspend fun markAsSynced(completionId: Int, syncDate: Date): Int
    
    /**
     * Actualiza la ruta de firma de una completación
     */
    @Query("UPDATE task_completions SET signaturePath = :path, hasSignature = 1 WHERE id = :completionId")
    suspend fun updateSignaturePath(completionId: Int, path: String): Int
    
    /**
     * Actualiza la ruta de foto de una completación
     */
    @Query("UPDATE task_completions SET photoPath = :path, hasPhoto = 1 WHERE id = :completionId")
    suspend fun updatePhotoPath(completionId: Int, path: String): Int
}