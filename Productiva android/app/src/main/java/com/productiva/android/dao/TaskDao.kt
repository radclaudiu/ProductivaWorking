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
 * Data Access Object para las tareas.
 * Proporciona métodos para acceder y manipular la tabla de tareas.
 */
@Dao
interface TaskDao {
    /**
     * Inserta una tarea en la base de datos.
     * Si ya existe una tarea con el mismo ID, la reemplaza.
     *
     * @param task Tarea a insertar.
     * @return ID de la tarea insertada.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long
    
    /**
     * Inserta múltiples tareas en la base de datos.
     * Si ya existe alguna tarea con el mismo ID, la reemplaza.
     *
     * @param tasks Lista de tareas a insertar.
     * @return Lista de IDs de las tareas insertadas.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>): List<Long>
    
    /**
     * Actualiza una tarea existente.
     *
     * @param task Tarea a actualizar.
     * @return Número de filas actualizadas.
     */
    @Update
    suspend fun updateTask(task: Task): Int
    
    /**
     * Obtiene todas las tareas.
     *
     * @return Flow con la lista de todas las tareas.
     */
    @Query("SELECT * FROM tasks ORDER BY dueDate, dueTime")
    fun getAllTasks(): Flow<List<Task>>
    
    /**
     * Obtiene una tarea por su ID.
     *
     * @param taskId ID de la tarea.
     * @return Flow con la tarea, o null si no existe.
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskById(taskId: Int): Flow<Task?>
    
    /**
     * Obtiene todas las tareas asignadas a un usuario específico.
     *
     * @param userId ID del usuario.
     * @return Flow con la lista de tareas asignadas al usuario.
     */
    @Query("SELECT * FROM tasks WHERE assignedTo = :userId ORDER BY dueDate, dueTime")
    fun getTasksByUserId(userId: Int): Flow<List<Task>>
    
    /**
     * Obtiene todas las tareas pendientes.
     *
     * @return Flow con la lista de tareas pendientes.
     */
    @Query("SELECT * FROM tasks WHERE status = 'pending' OR status = 'in_progress' ORDER BY dueDate, dueTime")
    fun getPendingTasks(): Flow<List<Task>>
    
    /**
     * Obtiene todas las tareas pendientes asignadas a un usuario específico.
     *
     * @param userId ID del usuario.
     * @return Flow con la lista de tareas pendientes asignadas al usuario.
     */
    @Query("SELECT * FROM tasks WHERE (status = 'pending' OR status = 'in_progress') AND assignedTo = :userId ORDER BY dueDate, dueTime")
    fun getPendingTasksByUserId(userId: Int): Flow<List<Task>>
    
    /**
     * Obtiene todas las tareas para una ubicación específica.
     *
     * @param locationId ID de la ubicación.
     * @return Flow con la lista de tareas para la ubicación.
     */
    @Query("SELECT * FROM tasks WHERE locationId = :locationId ORDER BY dueDate, dueTime")
    fun getTasksByLocationId(locationId: Int): Flow<List<Task>>
    
    /**
     * Obtiene todas las tareas pendientes para una ubicación específica.
     *
     * @param locationId ID de la ubicación.
     * @return Flow con la lista de tareas pendientes para la ubicación.
     */
    @Query("SELECT * FROM tasks WHERE (status = 'pending' OR status = 'in_progress') AND locationId = :locationId ORDER BY dueDate, dueTime")
    fun getPendingTasksByLocationId(locationId: Int): Flow<List<Task>>
    
    /**
     * Obtiene todas las tareas que requieren sincronización.
     *
     * @return Lista de tareas que necesitan sincronizarse.
     */
    @Query("SELECT * FROM tasks WHERE isLocallyModified = 1")
    suspend fun getTasksToSync(): List<Task>
    
    /**
     * Marca todas las tareas como sincronizadas.
     *
     * @param taskIds Lista de IDs de tareas.
     * @param syncTime Timestamp de la sincronización.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE tasks SET isLocallyModified = 0, lastSyncTime = :syncTime WHERE id IN (:taskIds)")
    suspend fun markTasksAsSynced(taskIds: List<Int>, syncTime: Long): Int
    
    /**
     * Actualiza el estado de una tarea.
     *
     * @param taskId ID de la tarea.
     * @param status Nuevo estado.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE tasks SET status = :status, isLocallyModified = 1 WHERE id = :taskId")
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
     * Elimina todas las tareas.
     */
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
    
    /**
     * Obtiene una tarea por su ID de forma síncrona.
     *
     * @param taskId ID de la tarea.
     * @return La tarea, o null si no existe.
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskByIdSync(taskId: Int): Task?
    
    /**
     * Elimina las tareas por IDs.
     *
     * @param taskIds Lista de IDs de tareas a eliminar.
     * @return Número de filas eliminadas.
     */
    @Query("DELETE FROM tasks WHERE id IN (:taskIds)")
    suspend fun deleteTasksByIds(taskIds: List<Int>): Int
    
    /**
     * Transacción para sincronizar tareas desde el servidor.
     * Inserta nuevas tareas, actualiza existentes y elimina las que ya no existen.
     *
     * @param tasks Lista de tareas del servidor.
     * @param deletedIds Lista de IDs de tareas eliminadas en el servidor.
     * @param syncTime Timestamp de la sincronización.
     */
    @Transaction
    suspend fun syncTasksFromServer(tasks: List<Task>, deletedIds: List<Int>, syncTime: Long) {
        // Eliminar tareas marcadas como eliminadas
        if (deletedIds.isNotEmpty()) {
            deleteTasksByIds(deletedIds)
        }
        
        // Insertar o actualizar tareas
        val tasksWithSyncTime = tasks.map { task ->
            task.copy(lastSyncTime = syncTime, isLocallyModified = false)
        }
        
        if (tasksWithSyncTime.isNotEmpty()) {
            insertTasks(tasksWithSyncTime)
        }
    }
}