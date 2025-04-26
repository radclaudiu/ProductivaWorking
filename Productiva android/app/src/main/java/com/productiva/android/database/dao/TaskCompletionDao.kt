package com.productiva.android.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.model.TaskCompletion

/**
 * DAO para operaciones con completaciones de tareas en la base de datos local.
 */
@Dao
interface TaskCompletionDao {
    
    /**
     * Inserta una completación de tarea en la base de datos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(completion: TaskCompletion): Long
    
    /**
     * Inserta varias completaciones de tareas en la base de datos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(completions: List<TaskCompletion>): List<Long>
    
    /**
     * Actualiza la información de una completación existente.
     */
    @Update
    suspend fun update(completion: TaskCompletion)
    
    /**
     * Obtiene una completación por su ID.
     */
    @Query("SELECT * FROM task_completions WHERE id = :completionId")
    suspend fun getCompletionById(completionId: Int): TaskCompletion?
    
    /**
     * Obtiene todas las completaciones para una tarea específica.
     */
    @Query("SELECT * FROM task_completions WHERE task_id = :taskId ORDER BY completion_date DESC")
    fun getCompletionsByTaskId(taskId: Int): LiveData<List<TaskCompletion>>
    
    /**
     * Obtiene completaciones por usuario.
     */
    @Query("SELECT * FROM task_completions WHERE user_id = :userId ORDER BY completion_date DESC")
    fun getCompletionsByUser(userId: Int): LiveData<List<TaskCompletion>>
    
    /**
     * Obtiene completaciones por ubicación.
     */
    @Query("SELECT * FROM task_completions WHERE location_id = :locationId ORDER BY completion_date DESC")
    fun getCompletionsByLocation(locationId: Int): LiveData<List<TaskCompletion>>
    
    /**
     * Elimina una completación por su ID.
     */
    @Query("DELETE FROM task_completions WHERE id = :completionId")
    suspend fun deleteCompletionById(completionId: Int): Int
    
    /**
     * Elimina todas las completaciones para una tarea específica.
     */
    @Query("DELETE FROM task_completions WHERE task_id = :taskId")
    suspend fun deleteCompletionsByTaskId(taskId: Int): Int
    
    /**
     * Elimina todas las completaciones.
     */
    @Query("DELETE FROM task_completions")
    suspend fun deleteAllCompletions()
    
    /**
     * Obtiene completaciones que tienen sincronización pendiente.
     */
    @Query("SELECT * FROM task_completions WHERE syncPending = 1")
    suspend fun getCompletionsWithPendingSync(): List<TaskCompletion>
    
    /**
     * Marca una completación como sincronizada y actualiza su ID remoto.
     */
    @Query("UPDATE task_completions SET syncPending = 0, remoteId = :remoteId WHERE id = :localId")
    suspend fun markCompletionSynced(localId: Int, remoteId: Int): Int
    
    /**
     * Obtiene la última completación para una tarea.
     */
    @Query("SELECT * FROM task_completions WHERE task_id = :taskId ORDER BY completion_date DESC LIMIT 1")
    suspend fun getLastCompletionForTask(taskId: Int): TaskCompletion?
    
    /**
     * Cuenta el número de completaciones para una tarea.
     */
    @Query("SELECT COUNT(*) FROM task_completions WHERE task_id = :taskId")
    suspend fun getCompletionsCountForTask(taskId: Int): Int
    
    /**
     * Verifica si una tarea tiene completaciones.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM task_completions WHERE task_id = :taskId LIMIT 1)")
    suspend fun hasCompletionsForTask(taskId: Int): Boolean
}