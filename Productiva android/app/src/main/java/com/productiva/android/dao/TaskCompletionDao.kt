package com.productiva.android.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.productiva.android.model.TaskCompletion
import kotlinx.coroutines.flow.Flow

/**
 * DAO para acceder y manipular completaciones de tareas en la base de datos.
 */
@Dao
interface TaskCompletionDao {
    
    /**
     * Obtiene todas las completaciones de una tarea específica.
     *
     * @param taskId ID de la tarea.
     * @return Flow con la lista de completaciones de la tarea.
     */
    @Query("SELECT * FROM task_completions WHERE taskId = :taskId ORDER BY completedAt DESC")
    fun getTaskCompletionsByTaskId(taskId: Int): Flow<List<TaskCompletion>>
    
    /**
     * Obtiene la última completación de una tarea.
     *
     * @param taskId ID de la tarea.
     * @return Flow con la última completación de la tarea (o null si no existe).
     */
    @Query("SELECT * FROM task_completions WHERE taskId = :taskId ORDER BY completedAt DESC LIMIT 1")
    fun getLastTaskCompletion(taskId: Int): Flow<TaskCompletion?>
    
    /**
     * Inserta una completación de tarea en la base de datos.
     *
     * @param taskCompletion Completación de tarea a insertar.
     * @return ID generado para la nueva completación.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskCompletion(taskCompletion: TaskCompletion): Long
    
    /**
     * Inserta varias completaciones de tarea en la base de datos.
     *
     * @param completions Lista de completaciones a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskCompletions(completions: List<TaskCompletion>)
    
    /**
     * Obtiene todas las completaciones de tarea pendientes de sincronización.
     *
     * @return Lista de completaciones pendientes de sincronización.
     */
    @Query("SELECT * FROM task_completions WHERE isSynced = 0")
    suspend fun getTaskCompletionsToSync(): List<TaskCompletion>
    
    /**
     * Marca varias completaciones de tarea como sincronizadas.
     *
     * @param completionIds Lista de IDs de completaciones a marcar.
     */
    @Query("UPDATE task_completions SET isSynced = 1, lastSyncAttempt = strftime('%s','now') * 1000, syncError = NULL WHERE id IN (:completionIds)")
    suspend fun markTaskCompletionsAsSynced(completionIds: List<Int>)
    
    /**
     * Marca una completación de tarea como fallida en la sincronización.
     *
     * @param completionId ID de la completación.
     * @param errorMessage Mensaje de error.
     */
    @Query("UPDATE task_completions SET lastSyncAttempt = strftime('%s','now') * 1000, syncError = :errorMessage WHERE id = :completionId")
    suspend fun markTaskCompletionSyncFailed(completionId: Int, errorMessage: String)
    
    /**
     * Elimina una completación de tarea por su ID.
     *
     * @param completionId ID de la completación a eliminar.
     * @return Número de filas eliminadas.
     */
    @Query("DELETE FROM task_completions WHERE id = :completionId")
    suspend fun deleteTaskCompletionById(completionId: Int): Int
    
    /**
     * Elimina todas las completaciones de una tarea.
     *
     * @param taskId ID de la tarea.
     * @return Número de filas eliminadas.
     */
    @Query("DELETE FROM task_completions WHERE taskId = :taskId")
    suspend fun deleteTaskCompletionsByTaskId(taskId: Int): Int
    
    /**
     * Sincroniza las completaciones con los datos del servidor.
     *
     * @param completions Completaciones recibidas del servidor.
     */
    @Transaction
    suspend fun syncTaskCompletionsFromServer(completions: List<TaskCompletion>) {
        insertTaskCompletions(completions)
    }
}