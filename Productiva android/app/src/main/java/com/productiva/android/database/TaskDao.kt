package com.productiva.android.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.model.Task
import java.util.Date

/**
 * Interfaz de acceso a datos para la entidad Task
 */
@Dao
interface TaskDao {
    
    /**
     * Obtiene todas las tareas
     */
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasks(): LiveData<List<Task>>
    
    /**
     * Obtiene una tarea por su ID
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Int): Task?
    
    /**
     * Obtiene tareas por usuario
     */
    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY dueDate ASC")
    fun getTasksByUser(userId: Int): LiveData<List<Task>>
    
    /**
     * Obtiene tareas por estado
     */
    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY dueDate ASC")
    fun getTasksByStatus(status: String): LiveData<List<Task>>
    
    /**
     * Obtiene tareas por usuario y estado
     */
    @Query("SELECT * FROM tasks WHERE userId = :userId AND status = :status ORDER BY dueDate ASC")
    fun getTasksByUserAndStatus(userId: Int, status: String): LiveData<List<Task>>
    
    /**
     * Obtiene tareas por empresa
     */
    @Query("SELECT * FROM tasks WHERE companyId = :companyId ORDER BY dueDate ASC")
    fun getTasksByCompany(companyId: Int): LiveData<List<Task>>
    
    /**
     * Obtiene tareas por ubicación
     */
    @Query("SELECT * FROM tasks WHERE locationId = :locationId ORDER BY dueDate ASC")
    fun getTasksByLocation(locationId: Int): LiveData<List<Task>>
    
    /**
     * Obtiene tareas por rango de fechas
     */
    @Query("SELECT * FROM tasks WHERE dueDate BETWEEN :startDate AND :endDate ORDER BY dueDate ASC")
    fun getTasksByDateRange(startDate: Date, endDate: Date): LiveData<List<Task>>
    
    /**
     * Busca tareas por título o descripción
     */
    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchTasks(query: String): LiveData<List<Task>>
    
    /**
     * Inserta una nueva tarea
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long
    
    /**
     * Inserta múltiples tareas
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<Task>): List<Long>
    
    /**
     * Actualiza una tarea existente
     */
    @Update
    suspend fun update(task: Task): Int
    
    /**
     * Actualiza el estado de una tarea
     */
    @Query("UPDATE tasks SET status = :status, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Int, status: String, updatedAt: Date): Int
    
    /**
     * Elimina una tarea por su ID
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int): Int
    
    /**
     * Elimina todas las tareas
     */
    @Query("DELETE FROM tasks")
    suspend fun deleteAll(): Int
    
    /**
     * Obtiene el número total de tareas
     */
    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTaskCount(): Int
    
    /**
     * Obtiene tareas pendientes de sincronización
     */
    @Query("SELECT * FROM tasks WHERE locallyModified = 1")
    suspend fun getUnsynced(): List<Task>
}