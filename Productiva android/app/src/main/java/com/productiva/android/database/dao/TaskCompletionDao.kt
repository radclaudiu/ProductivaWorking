package com.productiva.android.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.model.TaskCompletion

/**
 * DAO para operaciones con finalizaciones de tareas en la base de datos local.
 */
@Dao
interface TaskCompletionDao {
    
    /**
     * Inserta una finalización de tarea en la base de datos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(taskCompletion: TaskCompletion): Long
    
    /**
     * Inserta varias finalizaciones de tareas en la base de datos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(taskCompletions: List<TaskCompletion>): List<Long>
    
    /**
     * Actualiza la información de una finalización de tarea existente.
     */
    @Update
    suspend fun update(taskCompletion: TaskCompletion)
    
    /**
     * Obtiene una finalización de tarea por su ID.
     */
    @Query("SELECT * FROM task_completions WHERE id = :completionId")
    suspend fun getTaskCompletionById(completionId: Int): TaskCompletion?
    
    /**
     * Obtiene todas las finalizaciones de tareas para una tarea específica.
     */
    @Query("SELECT * FROM task_completions WHERE task_id = :taskId ORDER BY completion_date DESC")
    fun getCompletionsForTask(taskId: Int): LiveData<List<TaskCompletion>>
    
    /**
     * Obtiene todas las finalizaciones de tareas para un usuario específico.
     */
    @Query("SELECT * FROM task_completions WHERE completed_by = :userId ORDER BY completion_date DESC")
    fun getCompletionsByUser(userId: Int): LiveData<List<TaskCompletion>>
    
    /**
     * Obtiene todas las finalizaciones de tareas pendientes de sincronizar.
     */
    @Query("SELECT * FROM task_completions WHERE is_synced = 0")
    suspend fun getUnsyncedCompletions(): List<TaskCompletion>
    
    /**
     * Marca una finalización de tarea como sincronizada.
     */
    @Query("UPDATE task_completions SET is_synced = 1, server_id = :serverId WHERE id = :localId")
    suspend fun markAsSynced(localId: Int, serverId: Int): Int
    
    /**
     * Obtiene la última finalización para una tarea específica.
     */
    @Query("SELECT * FROM task_completions WHERE task_id = :taskId ORDER BY completion_date DESC LIMIT 1")
    suspend fun getLastCompletionForTask(taskId: Int): TaskCompletion?
    
    /**
     * Elimina una finalización de tarea por su ID.
     */
    @Query("DELETE FROM task_completions WHERE id = :completionId")
    suspend fun deleteTaskCompletionById(completionId: Int): Int
    
    /**
     * Elimina todas las finalizaciones de tareas para una tarea específica.
     */
    @Query("DELETE FROM task_completions WHERE task_id = :taskId")
    suspend fun deleteCompletionsForTask(taskId: Int): Int
    
    /**
     * Elimina todas las finalizaciones de tareas.
     */
    @Query("DELETE FROM task_completions")
    suspend fun deleteAllTaskCompletions()
    
    /**
     * Cuenta el número de finalizaciones para una tarea específica.
     */
    @Query("SELECT COUNT(*) FROM task_completions WHERE task_id = :taskId")
    suspend fun getCompletionsCountForTask(taskId: Int): Int
}