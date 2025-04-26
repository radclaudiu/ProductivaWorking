package com.productiva.android.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.data.model.Task

/**
 * DAO (Data Access Object) para las operaciones de base de datos relacionadas con tareas.
 */
@Dao
interface TaskDao {
    
    /**
     * Inserta una tarea en la base de datos.
     * Si ya existe un registro con el mismo ID, lo reemplaza.
     *
     * @param task Tarea a insertar.
     * @return ID generado para la tarea insertada.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long
    
    /**
     * Inserta varias tareas en la base de datos.
     * Si ya existen registros con los mismos IDs, los reemplaza.
     *
     * @param tasks Lista de tareas a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<Task>)
    
    /**
     * Actualiza una tarea existente en la base de datos.
     *
     * @param task Tarea a actualizar.
     */
    @Update
    suspend fun update(task: Task)
    
    /**
     * Obtiene todas las tareas de la base de datos.
     *
     * @return Lista de todas las tareas.
     */
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    suspend fun getAllTasks(): List<Task>
    
    /**
     * Obtiene una tarea por su ID.
     *
     * @param taskId ID de la tarea.
     * @return Tarea con el ID especificado o null si no existe.
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?
    
    /**
     * Obtiene todas las tareas pendientes (no completadas).
     *
     * @return Lista de tareas pendientes.
     */
    @Query("SELECT * FROM tasks WHERE status != 'COMPLETED' AND status != 'CANCELLED' ORDER BY dueDate, priority DESC")
    suspend fun getPendingTasks(): List<Task>
    
    /**
     * Obtiene todas las tareas completadas.
     *
     * @return Lista de tareas completadas.
     */
    @Query("SELECT * FROM tasks WHERE status = 'COMPLETED' ORDER BY updatedAt DESC")
    suspend fun getCompletedTasks(): List<Task>
    
    /**
     * Busca tareas por título o descripción.
     *
     * @param query Texto a buscar.
     * @return Lista de tareas que coinciden con la búsqueda.
     */
    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    suspend fun searchTasks(query: String): List<Task>
    
    /**
     * Marca una tarea como eliminada (para sincronización posterior).
     * No elimina físicamente la tarea, solo actualiza su estado.
     *
     * @param taskId ID de la tarea a marcar.
     */
    @Query("UPDATE tasks SET syncStatus = 'PENDING_DELETE', pendingChanges = 1, updatedAt = CURRENT_TIMESTAMP WHERE id = :taskId")
    suspend fun markAsDeleted(taskId: Int)
    
    /**
     * Obtiene todas las tareas con cambios pendientes de sincronización.
     *
     * @return Lista de tareas pendientes de sincronizar.
     */
    @Query("SELECT * FROM tasks WHERE syncStatus != 'SYNCED' OR pendingChanges = 1")
    suspend fun getPendingSyncTasks(): List<Task>
    
    /**
     * Obtiene la cantidad de tareas pendientes de sincronización.
     *
     * @return Número de tareas pendientes de sincronizar.
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE syncStatus != 'SYNCED' OR pendingChanges = 1")
    suspend fun getPendingSyncTasksCount(): Int
    
    /**
     * Marca varias tareas como sincronizadas.
     *
     * @param taskIds Lista de IDs de tareas a marcar.
     */
    @Query("UPDATE tasks SET syncStatus = 'SYNCED', pendingChanges = 0 WHERE id IN (:taskIds)")
    suspend fun markAsSynced(taskIds: List<Int>)
    
    /**
     * Actualiza el estado de sincronización de una tarea.
     *
     * @param taskId ID de la tarea.
     * @param syncStatus Nuevo estado de sincronización.
     */
    @Query("UPDATE tasks SET syncStatus = :syncStatus, pendingChanges = 0 WHERE id = :taskId")
    suspend fun updateSyncStatus(taskId: Int, syncStatus: Task.SyncStatus)
    
    /**
     * Elimina físicamente todas las tareas marcadas para eliminación y ya sincronizadas.
     */
    @Query("DELETE FROM tasks WHERE syncStatus = 'PENDING_DELETE' OR (syncStatus = 'SYNCED' AND pendingChanges = 1)")
    suspend fun deleteMarkedTasks()
}