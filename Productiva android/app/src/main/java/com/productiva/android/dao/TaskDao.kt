package com.productiva.android.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.productiva.android.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * DAO para acceder y manipular tareas en la base de datos.
 */
@Dao
interface TaskDao {
    
    /**
     * Obtiene todas las tareas.
     *
     * @return Flow con la lista de tareas.
     */
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC, priority DESC")
    fun getAllTasks(): Flow<List<Task>>
    
    /**
     * Obtiene todas las tareas asignadas a un usuario específico.
     *
     * @param userId ID del usuario.
     * @return Flow con la lista de tareas del usuario.
     */
    @Query("SELECT * FROM tasks WHERE assignedToUserId = :userId ORDER BY dueDate ASC, priority DESC")
    fun getTasksByUserId(userId: Int): Flow<List<Task>>
    
    /**
     * Obtiene todas las tareas pendientes asignadas a un usuario específico.
     *
     * @param userId ID del usuario.
     * @return Flow con la lista de tareas pendientes del usuario.
     */
    @Query("SELECT * FROM tasks WHERE assignedToUserId = :userId AND status IN ('pending', 'in_progress') ORDER BY dueDate ASC, priority DESC")
    fun getPendingTasksByUserId(userId: Int): Flow<List<Task>>
    
    /**
     * Obtiene una tarea por su ID.
     *
     * @param taskId ID de la tarea.
     * @return Flow con la tarea (o null si no existe).
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskById(taskId: Int): Flow<Task?>
    
    /**
     * Obtiene una tarea por su ID de forma síncrona.
     *
     * @param taskId ID de la tarea.
     * @return La tarea (o null si no existe).
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskByIdSync(taskId: Int): Task?
    
    /**
     * Inserta una tarea en la base de datos.
     *
     * @param task Tarea a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)
    
    /**
     * Inserta varias tareas en la base de datos.
     *
     * @param tasks Lista de tareas a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)
    
    /**
     * Actualiza una tarea existente.
     *
     * @param task Tarea con los datos actualizados.
     * @return Número de filas actualizadas.
     */
    @Update
    suspend fun updateTask(task: Task): Int
    
    /**
     * Actualiza el estado de una tarea.
     *
     * @param taskId ID de la tarea.
     * @param status Nuevo estado.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE tasks SET status = :status, updatedAt = strftime('%s','now') * 1000 WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Int, status: String): Int
    
    /**
     * Elimina una tarea por su ID.
     *
     * @param taskId ID de la tarea a eliminar.
     * @return Número de filas eliminadas.
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int): Int
    
    /**
     * Sincroniza las tareas con los datos del servidor.
     * Inserta o actualiza las tareas recibidas, y elimina las que ya no existen en el servidor.
     *
     * @param tasks Tareas recibidas del servidor.
     * @param tasksToDelete IDs de tareas a eliminar (opcional).
     * @param syncTime Timestamp de la sincronización.
     */
    @Transaction
    suspend fun syncTasksFromServer(tasks: List<Task>, tasksToDelete: List<Int>, syncTime: Long) {
        // Insertar o actualizar tareas recibidas
        for (task in tasks) {
            insertTask(task.copy(lastSyncTime = syncTime, isLocalOnly = false))
        }
        
        // Eliminar tareas que ya no existen en el servidor
        if (tasksToDelete.isNotEmpty()) {
            deleteTasksByIds(tasksToDelete)
        }
    }
    
    /**
     * Elimina varias tareas por sus IDs.
     *
     * @param taskIds Lista de IDs de tareas a eliminar.
     */
    @Query("DELETE FROM tasks WHERE id IN (:taskIds)")
    suspend fun deleteTasksByIds(taskIds: List<Int>)
}