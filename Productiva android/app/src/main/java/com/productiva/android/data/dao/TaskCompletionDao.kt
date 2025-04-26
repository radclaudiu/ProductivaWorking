package com.productiva.android.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.productiva.android.data.model.TaskCompletion

/**
 * DAO (Data Access Object) para las operaciones de base de datos relacionadas con completados de tareas.
 */
@Dao
interface TaskCompletionDao {
    
    /**
     * Inserta un registro de completado en la base de datos.
     * Si ya existe un registro con el mismo ID, lo reemplaza.
     *
     * @param completion Registro de completado a insertar.
     * @return ID generado para el registro insertado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(completion: TaskCompletion): Long
    
    /**
     * Inserta varios registros de completado en la base de datos.
     * Si ya existen registros con los mismos IDs, los reemplaza.
     *
     * @param completions Lista de registros de completado a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(completions: List<TaskCompletion>)
    
    /**
     * Obtiene un registro de completado por su ID.
     *
     * @param completionId ID del registro de completado.
     * @return Registro de completado con el ID especificado o null si no existe.
     */
    @Query("SELECT * FROM task_completions WHERE id = :completionId")
    suspend fun getCompletionById(completionId: Int): TaskCompletion?
    
    /**
     * Obtiene un registro de completado por el ID de la tarea asociada.
     *
     * @param taskId ID de la tarea.
     * @return Registro de completado de la tarea especificada o null si no existe.
     */
    @Query("SELECT * FROM task_completions WHERE taskId = :taskId")
    suspend fun getCompletionByTaskId(taskId: Int): TaskCompletion?
    
    /**
     * Obtiene todos los registros de completado con cambios pendientes de sincronización.
     *
     * @return Lista de registros de completado pendientes de sincronizar.
     */
    @Query("SELECT * FROM task_completions WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSyncCompletions(): List<TaskCompletion>
    
    /**
     * Obtiene la cantidad de registros de completado pendientes de sincronización.
     *
     * @return Número de registros de completado pendientes de sincronizar.
     */
    @Query("SELECT COUNT(*) FROM task_completions WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSyncCompletionsCount(): Int
    
    /**
     * Marca varios registros de completado como sincronizados.
     *
     * @param completionIds Lista de IDs de registros de completado a marcar.
     */
    @Query("UPDATE task_completions SET syncStatus = 'SYNCED' WHERE id IN (:completionIds)")
    suspend fun markAsSynced(completionIds: List<Int>)
    
    /**
     * Actualiza el estado de sincronización de un registro de completado.
     *
     * @param completionId ID del registro de completado.
     * @param syncStatus Nuevo estado de sincronización.
     */
    @Query("UPDATE task_completions SET syncStatus = :syncStatus WHERE id = :completionId")
    suspend fun updateSyncStatus(completionId: Int, syncStatus: TaskCompletion.SyncStatus)
}