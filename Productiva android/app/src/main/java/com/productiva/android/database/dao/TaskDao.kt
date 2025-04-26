package com.productiva.android.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.model.Task

/**
 * DAO para operaciones con tareas en la base de datos local.
 */
@Dao
interface TaskDao {
    
    /**
     * Inserta una tarea en la base de datos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long
    
    /**
     * Inserta varias tareas en la base de datos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<Task>): List<Long>
    
    /**
     * Actualiza la información de una tarea existente.
     */
    @Update
    suspend fun update(task: Task)
    
    /**
     * Obtiene una tarea por su ID.
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId AND is_deleted = 0")
    suspend fun getTaskById(taskId: Int): Task?
    
    /**
     * Obtiene todas las tareas.
     */
    @Query("SELECT * FROM tasks WHERE is_deleted = 0 ORDER BY priority DESC, due_date ASC")
    fun getAllTasks(): LiveData<List<Task>>
    
    /**
     * Obtiene tareas por estado.
     */
    @Query("SELECT * FROM tasks WHERE status = :status AND is_deleted = 0 ORDER BY priority DESC, due_date ASC")
    fun getTasksByStatus(status: String): LiveData<List<Task>>
    
    /**
     * Obtiene tareas por compañía.
     */
    @Query("SELECT * FROM tasks WHERE company_id = :companyId AND is_deleted = 0 ORDER BY priority DESC, due_date ASC")
    fun getTasksByCompany(companyId: Int): LiveData<List<Task>>
    
    /**
     * Obtiene tareas por ubicación.
     */
    @Query("SELECT * FROM tasks WHERE location_id = :locationId AND is_deleted = 0 ORDER BY priority DESC, due_date ASC")
    fun getTasksByLocation(locationId: Int): LiveData<List<Task>>
    
    /**
     * Obtiene tareas asignadas a un usuario.
     */
    @Query("SELECT * FROM tasks WHERE assigned_to = :userId AND is_deleted = 0 ORDER BY priority DESC, due_date ASC")
    fun getTasksAssignedToUser(userId: Int): LiveData<List<Task>>
    
    /**
     * Obtiene tareas asignadas a un usuario con un estado específico.
     */
    @Query("SELECT * FROM tasks WHERE assigned_to = :userId AND status = :status AND is_deleted = 0 ORDER BY priority DESC, due_date ASC")
    fun getTasksAssignedToUserByStatus(userId: Int, status: String): LiveData<List<Task>>
    
    /**
     * Busca tareas por título o descripción.
     */
    @Query("SELECT * FROM tasks WHERE (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') AND is_deleted = 0 ORDER BY priority DESC, due_date ASC")
    fun searchTasks(query: String): LiveData<List<Task>>
    
    /**
     * Marca una tarea como eliminada.
     */
    @Query("UPDATE tasks SET is_deleted = 1 WHERE id = :taskId")
    suspend fun markTaskAsDeleted(taskId: Int): Int
    
    /**
     * Actualiza el estado de una tarea.
     */
    @Query("UPDATE tasks SET status = :status, updated_at = :updatedAt WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Int, status: String, updatedAt: String): Int
    
    /**
     * Elimina una tarea por su ID.
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int): Int
    
    /**
     * Elimina todas las tareas.
     */
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
    
    /**
     * Cuenta el número de tareas por estado.
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE status = :status AND is_deleted = 0")
    suspend fun getTasksCountByStatus(status: String): Int
}