package com.productiva.android.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import kotlinx.coroutines.flow.Flow

/**
 * DAO para acceder y manipular tareas en la base de datos.
 */
@Dao
interface TaskDao {
    // Consultas para Tareas
    
    /**
     * Obtiene todas las tareas.
     */
    @Query("SELECT * FROM tasks ORDER BY priority DESC, due_date ASC")
    fun getAllTasks(): Flow<List<Task>>
    
    /**
     * Obtiene todas las tareas (versión sincrónica).
     */
    @Query("SELECT * FROM tasks ORDER BY priority DESC, due_date ASC")
    suspend fun getAllTasksSync(): List<Task>
    
    /**
     * Obtiene una tarea por su ID.
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskById(taskId: Int): Flow<Task?>
    
    /**
     * Obtiene una tarea por su ID (versión sincrónica).
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskByIdSync(taskId: Int): Task?
    
    /**
     * Obtiene tareas por estado.
     */
    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY priority DESC, due_date ASC")
    fun getTasksByStatus(status: String): Flow<List<Task>>
    
    /**
     * Obtiene tareas asignadas a un usuario específico.
     */
    @Query("SELECT * FROM tasks WHERE assigned_to = :userId ORDER BY priority DESC, due_date ASC")
    fun getTasksByAssignedUser(userId: Int): Flow<List<Task>>
    
    /**
     * Obtiene tareas por estado y usuario asignado.
     */
    @Query("SELECT * FROM tasks WHERE status = :status AND assigned_to = :userId ORDER BY priority DESC, due_date ASC")
    fun getTasksByStatusAndUser(status: String, userId: Int): Flow<List<Task>>
    
    /**
     * Obtiene tareas que necesitan sincronización.
     */
    @Query("SELECT * FROM tasks WHERE needs_sync = 1")
    fun getTasksNeedingSync(): Flow<List<Task>>
    
    /**
     * Obtiene tareas que necesitan sincronización (versión sincrónica).
     */
    @Query("SELECT * FROM tasks WHERE needs_sync = 1")
    suspend fun getTasksNeedingSyncSync(): List<Task>
    
    /**
     * Inserta una nueva tarea.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(task: Task)
    
    /**
     * Inserta múltiples tareas.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTasks(tasks: List<Task>)
    
    /**
     * Actualiza una tarea existente.
     */
    @Update
    suspend fun updateTask(task: Task)
    
    /**
     * Actualiza múltiples tareas.
     */
    @Update
    suspend fun updateTasks(tasks: List<Task>)
    
    /**
     * Inserta o actualiza tareas (upsert).
     */
    @Transaction
    suspend fun upsertTasks(tasks: List<Task>) {
        for (task in tasks) {
            val existingTask = getTaskByIdSync(task.id)
            if (existingTask == null) {
                insertTask(task)
            } else if (!existingTask.needsSync) {
                // Solo actualizar si la tarea local no necesita sincronización
                updateTask(task)
            }
        }
    }
    
    // Consultas para Completados de Tareas
    
    /**
     * Obtiene todos los completados de tareas pendientes de sincronizar.
     */
    @Query("SELECT * FROM task_completions WHERE synced = 0")
    suspend fun getPendingTaskCompletionsSync(): List<TaskCompletion>
    
    /**
     * Inserta un nuevo completado de tarea.
     */
    @Insert
    suspend fun insertTaskCompletion(completion: TaskCompletion)
    
    /**
     * Actualiza un completado de tarea existente.
     */
    @Update
    suspend fun updateTaskCompletion(completion: TaskCompletion)
    
    /**
     * Elimina completados de tareas que ya se han sincronizado.
     */
    @Query("DELETE FROM task_completions WHERE synced = 1")
    suspend fun deleteSyncedTaskCompletions()
}