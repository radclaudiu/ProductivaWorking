package com.productiva.android.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para operaciones con tareas en la base de datos local.
 */
@Dao
interface TaskDao {
    /**
     * Obtiene todas las tareas.
     */
    @Query("SELECT * FROM tasks ORDER BY priority DESC, dueDate ASC")
    fun getAllTasks(): Flow<List<Task>>
    
    /**
     * Obtiene una tarea por su ID.
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskById(taskId: Int): Flow<Task?>
    
    /**
     * Obtiene una tarea por su ID de forma síncrona.
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskByIdSync(taskId: Int): Task?
    
    /**
     * Obtiene tareas por estado.
     */
    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY priority DESC, dueDate ASC")
    fun getTasksByStatus(status: String): Flow<List<Task>>
    
    /**
     * Obtiene tareas asignadas a un usuario.
     */
    @Query("SELECT * FROM tasks WHERE assignedTo = :userId ORDER BY priority DESC, dueDate ASC")
    fun getTasksAssignedToUser(userId: Int): Flow<List<Task>>
    
    /**
     * Obtiene tareas pendientes asignadas a un usuario.
     */
    @Query("SELECT * FROM tasks WHERE assignedTo = :userId AND status IN ('PENDING', 'IN_PROGRESS') ORDER BY priority DESC, dueDate ASC")
    fun getPendingTasksForUser(userId: Int): Flow<List<Task>>
    
    /**
     * Obtiene tareas creadas por un usuario.
     */
    @Query("SELECT * FROM tasks WHERE createdBy = :userId ORDER BY createdAt DESC")
    fun getTasksCreatedByUser(userId: Int): Flow<List<Task>>
    
    /**
     * Busca tareas por título o descripción.
     */
    @Query("SELECT * FROM tasks WHERE title LIKE :query OR description LIKE :query ORDER BY priority DESC")
    fun searchTasks(query: String): Flow<List<Task>>
    
    /**
     * Inserta una tarea.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)
    
    /**
     * Inserta múltiples tareas.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)
    
    /**
     * Actualiza una tarea.
     */
    @Update
    suspend fun updateTask(task: Task)
    
    /**
     * Elimina todas las tareas.
     */
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
    
    /**
     * Marca una tarea como completada.
     */
    @Query("UPDATE tasks SET status = 'COMPLETED', completedAt = :completedAt, needsSync = 1, lastSyncTimestamp = :timestamp WHERE id = :taskId")
    suspend fun markTaskAsCompleted(taskId: Int, completedAt: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Marca una tarea como cancelada.
     */
    @Query("UPDATE tasks SET status = 'CANCELLED', completedAt = :completedAt, needsSync = 1, lastSyncTimestamp = :timestamp WHERE id = :taskId")
    suspend fun markTaskAsCancelled(taskId: Int, completedAt: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Actualiza la ruta de la firma.
     */
    @Query("UPDATE tasks SET signaturePath = :path, needsSync = 1, lastSyncTimestamp = :timestamp WHERE id = :taskId")
    suspend fun updateSignaturePath(taskId: Int, path: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Actualiza la ruta de la foto.
     */
    @Query("UPDATE tasks SET photoPath = :path, needsSync = 1, lastSyncTimestamp = :timestamp WHERE id = :taskId")
    suspend fun updatePhotoPath(taskId: Int, path: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Actualiza las notas de completado.
     */
    @Query("UPDATE tasks SET completionNotes = :notes, needsSync = 1, lastSyncTimestamp = :timestamp WHERE id = :taskId")
    suspend fun updateCompletionNotes(taskId: Int, notes: String?, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Marca una tarea para sincronización.
     */
    @Query("UPDATE tasks SET needsSync = :needsSync, lastSyncTimestamp = :timestamp WHERE id = :taskId")
    suspend fun markForSync(taskId: Int, needsSync: Boolean, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Obtiene tareas que necesitan sincronización.
     */
    @Query("SELECT * FROM tasks WHERE needsSync = 1 ORDER BY lastSyncTimestamp ASC")
    suspend fun getTasksForSync(): List<Task>
    
    /**
     * Cuenta tareas que necesitan sincronización.
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE needsSync = 1")
    fun countTasksForSync(): Flow<Int>
    
    /**
     * Inserta un completado de tarea.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskCompletion(taskCompletion: TaskCompletion)
    
    /**
     * Obtiene un completado de tarea por ID de tarea.
     */
    @Query("SELECT * FROM task_completions WHERE taskId = :taskId")
    fun getTaskCompletionByTaskId(taskId: Int): Flow<TaskCompletion?>
    
    /**
     * Obtiene un completado de tarea por ID de tarea de forma síncrona.
     */
    @Query("SELECT * FROM task_completions WHERE taskId = :taskId")
    suspend fun getTaskCompletionByTaskIdSync(taskId: Int): TaskCompletion?
    
    /**
     * Obtiene todos los completados de tareas que necesitan sincronización.
     */
    @Query("SELECT * FROM task_completions WHERE needsSync = 1 AND syncAttempts < 5 ORDER BY lastSyncAttempt ASC")
    suspend fun getTaskCompletionsForSync(): List<TaskCompletion>
    
    /**
     * Actualiza el estado de sincronización de un completado de tarea.
     */
    @Query("UPDATE task_completions SET needsSync = :needsSync, syncError = :error, lastSyncAttempt = :timestamp, syncAttempts = syncAttempts + 1 WHERE taskId = :taskId")
    suspend fun updateTaskCompletionSyncStatus(taskId: Int, needsSync: Boolean, error: String?, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Cuenta completados de tareas que necesitan sincronización.
     */
    @Query("SELECT COUNT(*) FROM task_completions WHERE needsSync = 1")
    fun countTaskCompletionsForSync(): Flow<Int>
}