package com.productiva.android.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.productiva.android.model.TaskCompletion
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para las completaciones de tareas.
 * Proporciona métodos para acceder y manipular la tabla de completaciones de tareas.
 */
@Dao
interface TaskCompletionDao {
    /**
     * Inserta una completación de tarea en la base de datos.
     * Si ya existe una completación con el mismo ID, la reemplaza.
     *
     * @param taskCompletion Completación de tarea a insertar.
     * @return ID de la completación insertada.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskCompletion(taskCompletion: TaskCompletion): Long
    
    /**
     * Inserta múltiples completaciones de tareas en la base de datos.
     * Si ya existe alguna completación con el mismo ID, la reemplaza.
     *
     * @param taskCompletions Lista de completaciones de tareas a insertar.
     * @return Lista de IDs de las completaciones insertadas.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskCompletions(taskCompletions: List<TaskCompletion>): List<Long>
    
    /**
     * Actualiza una completación de tarea existente.
     *
     * @param taskCompletion Completación de tarea a actualizar.
     * @return Número de filas actualizadas.
     */
    @Update
    suspend fun updateTaskCompletion(taskCompletion: TaskCompletion): Int
    
    /**
     * Obtiene una completación de tarea por su ID.
     *
     * @param completionId ID de la completación.
     * @return Flow con la completación, o null si no existe.
     */
    @Query("SELECT * FROM task_completions WHERE id = :completionId")
    fun getTaskCompletionById(completionId: Int): Flow<TaskCompletion?>
    
    /**
     * Obtiene todas las completaciones de una tarea específica.
     *
     * @param taskId ID de la tarea.
     * @return Flow con la lista de completaciones de la tarea.
     */
    @Query("SELECT * FROM task_completions WHERE taskId = :taskId ORDER BY completedAt DESC")
    fun getTaskCompletionsByTaskId(taskId: Int): Flow<List<TaskCompletion>>
    
    /**
     * Obtiene todas las completaciones que necesitan sincronizarse.
     *
     * @return Lista de completaciones que requieren sincronización.
     */
    @Query("SELECT * FROM task_completions WHERE isLocalOnly = 1 AND isSynced = 0")
    suspend fun getTaskCompletionsToSync(): List<TaskCompletion>
    
    /**
     * Marca completaciones de tareas como sincronizadas.
     *
     * @param completionIds Lista de IDs de completaciones.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE task_completions SET isSynced = 1 WHERE id IN (:completionIds)")
    suspend fun markTaskCompletionsAsSynced(completionIds: List<Int>): Int
    
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
     * Obtiene todas las completaciones realizadas por un usuario específico.
     *
     * @param userId ID del usuario.
     * @return Flow con la lista de completaciones realizadas por el usuario.
     */
    @Query("SELECT * FROM task_completions WHERE completedBy = :userId ORDER BY completedAt DESC")
    fun getTaskCompletionsByUserId(userId: Int): Flow<List<TaskCompletion>>
    
    /**
     * Elimina todas las completaciones.
     */
    @Query("DELETE FROM task_completions")
    suspend fun deleteAllTaskCompletions()
    
    /**
     * Obtiene una completación de tarea por su ID de forma síncrona.
     *
     * @param completionId ID de la completación.
     * @return La completación, o null si no existe.
     */
    @Query("SELECT * FROM task_completions WHERE id = :completionId")
    suspend fun getTaskCompletionByIdSync(completionId: Int): TaskCompletion?
    
    /**
     * Transacción para sincronizar completaciones desde el servidor.
     * Actualiza las completaciones locales con los datos del servidor.
     *
     * @param completions Lista de completaciones del servidor.
     */
    @Transaction
    suspend fun syncTaskCompletionsFromServer(completions: List<TaskCompletion>) {
        if (completions.isNotEmpty()) {
            // Transformar las completaciones para indicar que están sincronizadas
            val syncedCompletions = completions.map { completion ->
                completion.copy(isLocalOnly = false, isSynced = true)
            }
            
            // Insertar las completaciones sincronizadas
            insertTaskCompletions(syncedCompletions)
        }
    }
    
    /**
     * Incrementa el contador de reintentos y actualiza el timestamp del último intento de sincronización.
     *
     * @param completionId ID de la completación.
     * @param timestamp Timestamp del intento de sincronización.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE task_completions SET retryCount = retryCount + 1, lastSyncAttempt = :timestamp WHERE id = :completionId")
    suspend fun incrementRetryCount(completionId: Int, timestamp: Long): Int
}