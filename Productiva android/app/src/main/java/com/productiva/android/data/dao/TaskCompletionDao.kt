package com.productiva.android.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.data.model.TaskCompletion
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * DAO (Data Access Object) para operaciones relacionadas con los completados de tareas en la base de datos Room.
 */
@Dao
interface TaskCompletionDao {
    
    /**
     * Inserta un registro de completado de tarea en la base de datos.
     *
     * @param completion Registro de completado a insertar.
     * @return ID del registro insertado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(completion: TaskCompletion): Long
    
    /**
     * Inserta varios registros de completado de tarea en la base de datos.
     *
     * @param completions Lista de registros de completado a insertar.
     * @return Lista de IDs de los registros insertados.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(completions: List<TaskCompletion>): List<Long>
    
    /**
     * Actualiza un registro de completado de tarea existente en la base de datos.
     *
     * @param completion Registro de completado a actualizar.
     * @return Número de filas actualizadas.
     */
    @Update
    suspend fun update(completion: TaskCompletion): Int
    
    /**
     * Actualiza varios registros de completado de tarea existentes en la base de datos.
     *
     * @param completions Lista de registros de completado a actualizar.
     * @return Número de filas actualizadas.
     */
    @Update
    suspend fun updateAll(completions: List<TaskCompletion>): Int
    
    /**
     * Elimina un registro de completado de tarea de la base de datos.
     *
     * @param completion Registro de completado a eliminar.
     * @return Número de filas eliminadas.
     */
    @Delete
    suspend fun delete(completion: TaskCompletion): Int
    
    /**
     * Obtiene todos los registros de completado de tarea.
     *
     * @return Lista de todos los registros de completado.
     */
    @Query("SELECT * FROM task_completions ORDER BY completedAt DESC")
    suspend fun getAllCompletions(): List<TaskCompletion>
    
    /**
     * Obtiene todos los registros de completado de tarea como flujo observable.
     *
     * @return Flujo de lista de todos los registros de completado.
     */
    @Query("SELECT * FROM task_completions ORDER BY completedAt DESC")
    fun getAllCompletionsFlow(): Flow<List<TaskCompletion>>
    
    /**
     * Obtiene un registro de completado de tarea por su ID.
     *
     * @param completionId ID del registro de completado.
     * @return Registro de completado correspondiente al ID o null si no existe.
     */
    @Query("SELECT * FROM task_completions WHERE id = :completionId")
    suspend fun getCompletionById(completionId: Int): TaskCompletion?
    
    /**
     * Obtiene todos los registros de completado de tarea para una tarea específica.
     *
     * @param taskId ID de la tarea.
     * @return Lista de registros de completado para la tarea.
     */
    @Query("SELECT * FROM task_completions WHERE taskId = :taskId ORDER BY completedAt DESC")
    suspend fun getCompletionsForTask(taskId: Int): List<TaskCompletion>
    
    /**
     * Obtiene todos los registros de completado de tarea para una tarea específica como flujo observable.
     *
     * @param taskId ID de la tarea.
     * @return Flujo de lista de registros de completado para la tarea.
     */
    @Query("SELECT * FROM task_completions WHERE taskId = :taskId ORDER BY completedAt DESC")
    fun getCompletionsForTaskFlow(taskId: Int): Flow<List<TaskCompletion>>
    
    /**
     * Obtiene el último registro de completado para una tarea específica.
     *
     * @param taskId ID de la tarea.
     * @return Último registro de completado para la tarea o null si no existe.
     */
    @Query("SELECT * FROM task_completions WHERE taskId = :taskId ORDER BY completedAt DESC LIMIT 1")
    suspend fun getLastCompletionForTask(taskId: Int): TaskCompletion?
    
    /**
     * Obtiene todos los registros de completado realizados por un usuario específico.
     *
     * @param userId ID del usuario.
     * @return Lista de registros de completado realizados por el usuario.
     */
    @Query("SELECT * FROM task_completions WHERE completedBy = :userId ORDER BY completedAt DESC")
    suspend fun getCompletionsByUser(userId: Int): List<TaskCompletion>
    
    /**
     * Obtiene todos los registros de completado pendientes de sincronizar con el servidor.
     *
     * @return Lista de registros de completado pendientes de sincronizar.
     */
    @Query("SELECT * FROM task_completions WHERE syncStatus != 'synced' ORDER BY completedAt DESC")
    suspend fun getPendingSyncCompletions(): List<TaskCompletion>
    
    /**
     * Obtiene el número de registros de completado pendientes de sincronizar con el servidor.
     *
     * @return Número de registros de completado pendientes de sincronizar.
     */
    @Query("SELECT COUNT(*) FROM task_completions WHERE syncStatus != 'synced'")
    suspend fun getPendingSyncCompletionsCount(): Int
    
    /**
     * Obtiene todos los registros de completado pendientes de sincronizar con el servidor como flujo observable.
     *
     * @return Flujo de lista de registros de completado pendientes de sincronizar.
     */
    @Query("SELECT * FROM task_completions WHERE syncStatus != 'synced' ORDER BY completedAt DESC")
    fun getPendingSyncCompletionsFlow(): Flow<List<TaskCompletion>>
    
    /**
     * Actualiza el estado de sincronización de un registro de completado.
     *
     * @param completionId ID del registro de completado.
     * @param syncStatus Nuevo estado de sincronización.
     * @param lastSyncTime Marca de tiempo de la última sincronización.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE task_completions SET syncStatus = :syncStatus, lastSyncTime = :lastSyncTime, pendingChanges = :syncStatus != 'synced' WHERE id = :completionId")
    suspend fun updateSyncStatus(completionId: Int, syncStatus: String, lastSyncTime: Long = System.currentTimeMillis()): Int
    
    /**
     * Marca varios registros de completado como sincronizados.
     *
     * @param completionIds Lista de IDs de registros de completado.
     * @param lastSyncTime Marca de tiempo de la última sincronización.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE task_completions SET syncStatus = 'synced', lastSyncTime = :lastSyncTime, pendingChanges = 0 WHERE id IN (:completionIds)")
    suspend fun markAsSynced(completionIds: List<Int>, lastSyncTime: Long = System.currentTimeMillis()): Int
    
    /**
     * Actualiza el estado de subida de archivos de un registro de completado.
     *
     * @param completionId ID del registro de completado.
     * @param photoUploaded true si la foto ha sido subida, false en caso contrario.
     * @param signatureUploaded true si la firma ha sido subida, false en caso contrario.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE task_completions SET photoUploaded = :photoUploaded, signatureUploaded = :signatureUploaded, pendingChanges = 1 WHERE id = :completionId")
    suspend fun updateUploadStatus(completionId: Int, photoUploaded: Boolean, signatureUploaded: Boolean): Int
    
    /**
     * Obtiene los registros de completado realizados entre dos fechas.
     *
     * @param startDate Fecha de inicio.
     * @param endDate Fecha de fin.
     * @return Lista de registros de completado realizados entre las fechas especificadas.
     */
    @Query("SELECT * FROM task_completions WHERE completedAt BETWEEN :startDate AND :endDate ORDER BY completedAt DESC")
    suspend fun getCompletionsBetweenDates(startDate: Date, endDate: Date): List<TaskCompletion>
    
    /**
     * Limpia la base de datos de registros de completado (elimina todos los registros).
     * Utilizar con precaución.
     */
    @Query("DELETE FROM task_completions")
    suspend fun clearAll()
}