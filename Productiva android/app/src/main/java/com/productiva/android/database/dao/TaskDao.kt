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
     * Si ya existe una tarea con el mismo ID, la reemplaza.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long
    
    /**
     * Inserta varias tareas en la base de datos.
     * Si ya existen tareas con los mismos IDs, las reemplaza.
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
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?
    
    /**
     * Obtiene todas las tareas.
     */
    @Query("SELECT * FROM tasks ORDER BY due_date ASC")
    fun getAllTasks(): LiveData<List<Task>>
    
    /**
     * Obtiene tareas por estado.
     */
    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY due_date ASC")
    fun getTasksByStatus(status: String): LiveData<List<Task>>
    
    /**
     * Obtiene tareas por usuario asignado.
     */
    @Query("SELECT * FROM tasks WHERE user_id = :userId ORDER BY due_date ASC")
    fun getTasksByUser(userId: Int): LiveData<List<Task>>
    
    /**
     * Obtiene tareas por ubicación.
     */
    @Query("SELECT * FROM tasks WHERE location_id = :locationId ORDER BY due_date ASC")
    fun getTasksByLocation(locationId: Int): LiveData<List<Task>>
    
    /**
     * Obtiene tareas por compañía.
     */
    @Query("SELECT * FROM tasks WHERE company_id = :companyId ORDER BY due_date ASC")
    fun getTasksByCompany(companyId: Int): LiveData<List<Task>>
    
    /**
     * Busca tareas por título o descripción.
     */
    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY due_date ASC")
    fun searchTasks(query: String): LiveData<List<Task>>
    
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
     * Actualiza el estado de una tarea.
     */
    @Query("UPDATE tasks SET status = :newStatus WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Int, newStatus: String): Int
    
    /**
     * Obtiene tareas pendientes o en progreso.
     */
    @Query("SELECT * FROM tasks WHERE status IN ('pending', 'in_progress') ORDER BY due_date ASC")
    fun getActiveTasks(): LiveData<List<Task>>
    
    /**
     * Obtiene tareas que tienen sincronización pendiente.
     */
    @Query("SELECT * FROM tasks WHERE syncPending = 1")
    suspend fun getTasksWithPendingSync(): List<Task>
    
    /**
     * Marca una tarea como sincronizada.
     */
    @Query("UPDATE tasks SET syncPending = 0 WHERE id = :taskId")
    suspend fun markTaskSynced(taskId: Int): Int
}