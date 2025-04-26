package com.productiva.android.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.model.Task
import java.util.Date

/**
 * DAO para la entidad Tarea
 */
@Dao
interface TaskDao {
    
    /**
     * Inserta una tarea en la base de datos
     * @param task Tarea a insertar
     * @return ID insertado
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long
    
    /**
     * Inserta múltiples tareas en la base de datos
     * @param tasks Lista de tareas a insertar
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)
    
    /**
     * Actualiza una tarea existente
     * @param task Tarea con los datos actualizados
     */
    @Update
    suspend fun updateTask(task: Task)
    
    /**
     * Obtiene una tarea por su ID
     * @param id ID de la tarea
     * @return Tarea encontrada o null
     */
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): Task?
    
    /**
     * Obtiene todas las tareas
     * @return LiveData con la lista de todas las tareas
     */
    @Query("SELECT * FROM tasks ORDER BY due_date ASC")
    fun getAllTasks(): LiveData<List<Task>>
    
    /**
     * Obtiene tareas asignadas a un usuario
     * @param userId ID del usuario asignado
     * @return LiveData con la lista de tareas asignadas
     */
    @Query("SELECT * FROM tasks WHERE assigned_to = :userId ORDER BY due_date ASC")
    fun getTasksByAssignee(userId: Int): LiveData<List<Task>>
    
    /**
     * Obtiene tareas por estado
     * @param status Estado de las tareas a buscar
     * @return LiveData con la lista de tareas con el estado especificado
     */
    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY due_date ASC")
    fun getTasksByStatus(status: String): LiveData<List<Task>>
    
    /**
     * Obtiene tareas por estado y usuario asignado
     * @param status Estado de las tareas a buscar
     * @param userId ID del usuario asignado
     * @return LiveData con la lista de tareas filtradas
     */
    @Query("SELECT * FROM tasks WHERE status = :status AND assigned_to = :userId ORDER BY due_date ASC")
    fun getTasksByStatusAndAssignee(status: String, userId: Int): LiveData<List<Task>>
    
    /**
     * Obtiene tareas por ubicación
     * @param locationId ID de la ubicación
     * @return LiveData con la lista de tareas de la ubicación
     */
    @Query("SELECT * FROM tasks WHERE location_id = :locationId ORDER BY due_date ASC")
    fun getTasksByLocation(locationId: Int): LiveData<List<Task>>
    
    /**
     * Obtiene tareas por fecha de vencimiento
     * @param date Fecha de vencimiento
     * @return LiveData con la lista de tareas con esa fecha de vencimiento
     */
    @Query("SELECT * FROM tasks WHERE due_date = :date ORDER BY due_date ASC")
    fun getTasksByDueDate(date: Date): LiveData<List<Task>>
    
    /**
     * Obtiene tareas pendientes de sincronización
     * @return Lista de tareas no sincronizadas
     */
    @Query("SELECT * FROM tasks WHERE synced = 0 OR is_local_only = 1")
    suspend fun getUnsyncedTasks(): List<Task>
    
    /**
     * Actualiza el estado de sincronización de una tarea
     * @param taskId ID de la tarea
     * @param synced Estado de sincronización (true/false)
     * @param lastSynced Fecha de última sincronización
     */
    @Query("UPDATE tasks SET synced = :synced, last_synced = :lastSynced, is_local_only = 0 WHERE id = :taskId")
    suspend fun updateSyncStatus(taskId: Int, synced: Boolean, lastSynced: Date)
    
    /**
     * Elimina todas las tareas (usado para sincronización completa)
     */
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}