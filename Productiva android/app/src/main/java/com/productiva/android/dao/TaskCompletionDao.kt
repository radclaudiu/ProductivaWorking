package com.productiva.android.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.model.TaskCompletion
import java.util.Date

/**
 * DAO para la entidad de Completado de Tarea
 */
@Dao
interface TaskCompletionDao {
    
    /**
     * Inserta un registro de completado de tarea
     * @param taskCompletion Registro a insertar
     * @return ID generado para el registro insertado
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskCompletion(taskCompletion: TaskCompletion): Long
    
    /**
     * Actualiza un registro de completado de tarea
     * @param taskCompletion Registro con los datos actualizados
     */
    @Update
    suspend fun updateTaskCompletion(taskCompletion: TaskCompletion)
    
    /**
     * Obtiene un registro de completado por su ID
     * @param id ID del registro
     * @return Registro encontrado o null
     */
    @Query("SELECT * FROM task_completions WHERE id = :id")
    suspend fun getTaskCompletionById(id: Int): TaskCompletion?
    
    /**
     * Obtiene el último registro de completado para una tarea
     * @param taskId ID de la tarea
     * @return Último registro de completado o null
     */
    @Query("SELECT * FROM task_completions WHERE taskId = :taskId ORDER BY completed_at DESC LIMIT 1")
    suspend fun getLastCompletionForTask(taskId: Int): TaskCompletion?
    
    /**
     * Obtiene todos los registros de completado para una tarea
     * @param taskId ID de la tarea
     * @return LiveData con la lista de registros de completado
     */
    @Query("SELECT * FROM task_completions WHERE taskId = :taskId ORDER BY completed_at DESC")
    fun getAllCompletionsForTask(taskId: Int): LiveData<List<TaskCompletion>>
    
    /**
     * Obtiene registros de completado por usuario
     * @param userId ID del usuario que completó las tareas
     * @return LiveData con la lista de registros de completado
     */
    @Query("SELECT * FROM task_completions WHERE completed_by = :userId ORDER BY completed_at DESC")
    fun getCompletionsByUser(userId: Int): LiveData<List<TaskCompletion>>
    
    /**
     * Obtiene registros de completado pendientes de sincronización
     * @return Lista de registros no sincronizados
     */
    @Query("SELECT * FROM task_completions WHERE synced = 0 OR is_local_only = 1")
    suspend fun getUnsyncedCompletions(): List<TaskCompletion>
    
    /**
     * Actualiza el estado de sincronización de un registro
     * @param id ID del registro
     * @param synced Estado de sincronización (true/false)
     * @param lastSynced Fecha de última sincronización
     */
    @Query("UPDATE task_completions SET synced = :synced, last_synced = :lastSynced, is_local_only = 0 WHERE id = :id")
    suspend fun updateSyncStatus(id: Int, synced: Boolean, lastSynced: Date)
    
    /**
     * Elimina todos los registros de completado (usado para sincronización completa)
     */
    @Query("DELETE FROM task_completions")
    suspend fun deleteAllCompletions()
}