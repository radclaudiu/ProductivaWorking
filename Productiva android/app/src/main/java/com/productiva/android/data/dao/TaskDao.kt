package com.productiva.android.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.productiva.android.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * DAO (Data Access Object) para operaciones relacionadas con tareas en la base de datos Room.
 */
@Dao
interface TaskDao {
    
    /**
     * Inserta una tarea en la base de datos.
     *
     * @param task Tarea a insertar.
     * @return ID de la tarea insertada.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long
    
    /**
     * Inserta varias tareas en la base de datos.
     *
     * @param tasks Lista de tareas a insertar.
     * @return Lista de IDs de las tareas insertadas.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<Task>): List<Long>
    
    /**
     * Actualiza una tarea existente en la base de datos.
     *
     * @param task Tarea a actualizar.
     * @return Número de filas actualizadas.
     */
    @Update
    suspend fun update(task: Task): Int
    
    /**
     * Actualiza varias tareas existentes en la base de datos.
     *
     * @param tasks Lista de tareas a actualizar.
     * @return Número de filas actualizadas.
     */
    @Update
    suspend fun updateAll(tasks: List<Task>): Int
    
    /**
     * Elimina una tarea de la base de datos.
     *
     * @param task Tarea a eliminar.
     * @return Número de filas eliminadas.
     */
    @Delete
    suspend fun delete(task: Task): Int
    
    /**
     * Obtiene todas las tareas como flujo observable.
     *
     * @return Flujo de lista de todas las tareas.
     */
    @Query("SELECT * FROM tasks WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getAllTasksFlow(): Flow<List<Task>>
    
    /**
     * Obtiene todas las tareas.
     *
     * @return Lista de todas las tareas.
     */
    @Query("SELECT * FROM tasks WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    suspend fun getAllTasks(): List<Task>
    
    /**
     * Obtiene una tarea por su ID.
     *
     * @param taskId ID de la tarea.
     * @return Tarea correspondiente al ID o null si no existe.
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId AND isDeleted = 0")
    suspend fun getTaskById(taskId: Int): Task?
    
    /**
     * Obtiene todas las tareas para una empresa.
     *
     * @param companyId ID de la empresa.
     * @return Lista de tareas de la empresa.
     */
    @Query("SELECT * FROM tasks WHERE companyId = :companyId AND isDeleted = 0 ORDER BY updatedAt DESC")
    suspend fun getTasksByCompany(companyId: Int): List<Task>
    
    /**
     * Obtiene todas las tareas para una empresa como flujo observable.
     *
     * @param companyId ID de la empresa.
     * @return Flujo de lista de tareas de la empresa.
     */
    @Query("SELECT * FROM tasks WHERE companyId = :companyId AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getTasksByCompanyFlow(companyId: Int): Flow<List<Task>>
    
    /**
     * Obtiene todas las tareas asignadas a un empleado.
     *
     * @param employeeId ID del empleado.
     * @return Lista de tareas asignadas al empleado.
     */
    @Query("SELECT * FROM tasks WHERE assignedTo = :employeeId AND isDeleted = 0 ORDER BY updatedAt DESC")
    suspend fun getTasksByAssignee(employeeId: Int): List<Task>
    
    /**
     * Obtiene todas las tareas asignadas a un empleado como flujo observable.
     *
     * @param employeeId ID del empleado.
     * @return Flujo de lista de tareas asignadas al empleado.
     */
    @Query("SELECT * FROM tasks WHERE assignedTo = :employeeId AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getTasksByAssigneeFlow(employeeId: Int): Flow<List<Task>>
    
    /**
     * Obtiene todas las tareas con un estado específico.
     *
     * @param status Estado de las tareas.
     * @return Lista de tareas con el estado especificado.
     */
    @Query("SELECT * FROM tasks WHERE status = :status AND isDeleted = 0 ORDER BY updatedAt DESC")
    suspend fun getTasksByStatus(status: String): List<Task>
    
    /**
     * Obtiene todas las tareas con un estado específico como flujo observable.
     *
     * @param status Estado de las tareas.
     * @return Flujo de lista de tareas con el estado especificado.
     */
    @Query("SELECT * FROM tasks WHERE status = :status AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getTasksByStatusFlow(status: String): Flow<List<Task>>
    
    /**
     * Obtiene todas las tareas pendientes de sincronizar con el servidor.
     *
     * @return Lista de tareas pendientes de sincronizar.
     */
    @Query("SELECT * FROM tasks WHERE syncStatus != 'synced' ORDER BY updatedAt DESC")
    suspend fun getPendingSyncTasks(): List<Task>
    
    /**
     * Obtiene el número de tareas pendientes de sincronizar con el servidor.
     *
     * @return Número de tareas pendientes de sincronizar.
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE syncStatus != 'synced'")
    suspend fun getPendingSyncTasksCount(): Int
    
    /**
     * Obtiene todas las tareas pendientes de sincronizar con el servidor como flujo observable.
     *
     * @return Flujo de lista de tareas pendientes de sincronizar.
     */
    @Query("SELECT * FROM tasks WHERE syncStatus != 'synced' ORDER BY updatedAt DESC")
    fun getPendingSyncTasksFlow(): Flow<List<Task>>
    
    /**
     * Actualiza el estado de sincronización de una tarea.
     *
     * @param taskId ID de la tarea.
     * @param syncStatus Nuevo estado de sincronización.
     * @param lastSyncTime Marca de tiempo de la última sincronización.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE tasks SET syncStatus = :syncStatus, lastSyncTime = :lastSyncTime, pendingChanges = :syncStatus != 'synced' WHERE id = :taskId")
    suspend fun updateSyncStatus(taskId: Int, syncStatus: String, lastSyncTime: Long = System.currentTimeMillis()): Int
    
    /**
     * Marca varias tareas como sincronizadas.
     *
     * @param taskIds Lista de IDs de tareas.
     * @param lastSyncTime Marca de tiempo de la última sincronización.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE tasks SET syncStatus = 'synced', lastSyncTime = :lastSyncTime, pendingChanges = 0 WHERE id IN (:taskIds)")
    suspend fun markAsSynced(taskIds: List<Int>, lastSyncTime: Long = System.currentTimeMillis()): Int
    
    /**
     * Marca una tarea como eliminada (borrado lógico).
     *
     * @param taskId ID de la tarea.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE tasks SET isDeleted = 1, syncStatus = 'pending_delete', pendingChanges = 1, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun markAsDeleted(taskId: Int, updatedAt: Date = Date()): Int
    
    /**
     * Elimina físicamente las tareas marcadas como eliminadas y ya sincronizadas.
     *
     * @return Número de filas eliminadas.
     */
    @Query("DELETE FROM tasks WHERE isDeleted = 1 AND syncStatus = 'synced'")
    suspend fun deleteMarkedTasks(): Int
    
    /**
     * Obtiene las tareas actualizadas después de una fecha específica.
     *
     * @param timestamp Marca de tiempo a partir de la cual buscar actualizaciones.
     * @return Lista de tareas actualizadas después de la fecha especificada.
     */
    @Query("SELECT * FROM tasks WHERE updatedAt >= :timestamp AND isDeleted = 0")
    suspend fun getTasksUpdatedAfter(timestamp: Date): List<Task>
    
    /**
     * Actualiza el estado de una tarea a completado.
     *
     * @param taskId ID de la tarea.
     * @param completedAt Fecha de completado.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE tasks SET status = 'completed', completedAt = :completedAt, updatedAt = :completedAt, syncStatus = 'pending_update', pendingChanges = 1 WHERE id = :taskId")
    suspend fun completeTask(taskId: Int, completedAt: Date = Date()): Int
    
    /**
     * Obtiene las tareas con fecha de vencimiento próxima.
     *
     * @param currentDate Fecha actual.
     * @return Lista de tareas con fecha de vencimiento próxima.
     */
    @Query("SELECT * FROM tasks WHERE dueDate IS NOT NULL AND dueDate >= :currentDate AND dueDate <= datetime(:currentDate, '+3 days') AND status != 'completed' AND isDeleted = 0 ORDER BY dueDate ASC")
    suspend fun getUpcomingTasks(currentDate: Date = Date()): List<Task>
    
    /**
     * Obtiene las tareas con fecha de vencimiento próxima como flujo observable.
     *
     * @param currentDate Fecha actual.
     * @return Flujo de lista de tareas con fecha de vencimiento próxima.
     */
    @Query("SELECT * FROM tasks WHERE dueDate IS NOT NULL AND dueDate >= :currentDate AND dueDate <= datetime(:currentDate, '+3 days') AND status != 'completed' AND isDeleted = 0 ORDER BY dueDate ASC")
    fun getUpcomingTasksFlow(currentDate: Date = Date()): Flow<List<Task>>
    
    /**
     * Limpia la base de datos de tareas (elimina todas las tareas).
     * Utilizar con precaución.
     */
    @Query("DELETE FROM tasks")
    suspend fun clearAll()
}