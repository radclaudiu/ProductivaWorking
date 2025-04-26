package com.productiva.android.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.data.model.CheckpointData
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * DAO (Data Access Object) para operaciones relacionadas con registros de fichaje en la base de datos Room.
 */
@Dao
interface CheckpointDao {
    
    /**
     * Inserta un registro de fichaje en la base de datos.
     *
     * @param checkpoint Registro de fichaje a insertar.
     * @return ID del registro insertado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(checkpoint: CheckpointData): Long
    
    /**
     * Inserta varios registros de fichaje en la base de datos.
     *
     * @param checkpoints Lista de registros de fichaje a insertar.
     * @return Lista de IDs de los registros insertados.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(checkpoints: List<CheckpointData>): List<Long>
    
    /**
     * Actualiza un registro de fichaje existente en la base de datos.
     *
     * @param checkpoint Registro de fichaje a actualizar.
     * @return Número de filas actualizadas.
     */
    @Update
    suspend fun update(checkpoint: CheckpointData): Int
    
    /**
     * Actualiza varios registros de fichaje existentes en la base de datos.
     *
     * @param checkpoints Lista de registros de fichaje a actualizar.
     * @return Número de filas actualizadas.
     */
    @Update
    suspend fun updateAll(checkpoints: List<CheckpointData>): Int
    
    /**
     * Elimina un registro de fichaje de la base de datos.
     *
     * @param checkpoint Registro de fichaje a eliminar.
     * @return Número de filas eliminadas.
     */
    @Delete
    suspend fun delete(checkpoint: CheckpointData): Int
    
    /**
     * Obtiene todos los registros de fichaje.
     *
     * @return Lista de todos los registros de fichaje.
     */
    @Query("SELECT * FROM checkpoints ORDER BY checkInTime DESC")
    suspend fun getAllCheckpoints(): List<CheckpointData>
    
    /**
     * Obtiene todos los registros de fichaje como flujo observable.
     *
     * @return Flujo de lista de todos los registros de fichaje.
     */
    @Query("SELECT * FROM checkpoints ORDER BY checkInTime DESC")
    fun getAllCheckpointsFlow(): Flow<List<CheckpointData>>
    
    /**
     * Obtiene un registro de fichaje por su ID.
     *
     * @param checkpointId ID del registro de fichaje.
     * @return Registro de fichaje correspondiente al ID o null si no existe.
     */
    @Query("SELECT * FROM checkpoints WHERE id = :checkpointId")
    suspend fun getCheckpointById(checkpointId: Int): CheckpointData?
    
    /**
     * Obtiene todos los registros de fichaje de un empleado.
     *
     * @param employeeId ID del empleado.
     * @return Lista de registros de fichaje del empleado.
     */
    @Query("SELECT * FROM checkpoints WHERE employeeId = :employeeId ORDER BY checkInTime DESC")
    suspend fun getCheckpointsByEmployee(employeeId: Int): List<CheckpointData>
    
    /**
     * Obtiene todos los registros de fichaje de un empleado como flujo observable.
     *
     * @param employeeId ID del empleado.
     * @return Flujo de lista de registros de fichaje del empleado.
     */
    @Query("SELECT * FROM checkpoints WHERE employeeId = :employeeId ORDER BY checkInTime DESC")
    fun getCheckpointsByEmployeeFlow(employeeId: Int): Flow<List<CheckpointData>>
    
    /**
     * Obtiene todos los registros de fichaje de un punto de fichaje.
     *
     * @param locationId ID del punto de fichaje.
     * @return Lista de registros de fichaje del punto de fichaje.
     */
    @Query("SELECT * FROM checkpoints WHERE locationId = :locationId ORDER BY checkInTime DESC")
    suspend fun getCheckpointsByLocation(locationId: Int): List<CheckpointData>
    
    /**
     * Obtiene todos los registros de fichaje de una empresa.
     *
     * @param companyId ID de la empresa.
     * @return Lista de registros de fichaje de la empresa.
     */
    @Query("SELECT * FROM checkpoints WHERE companyId = :companyId ORDER BY checkInTime DESC")
    suspend fun getCheckpointsByCompany(companyId: Int): List<CheckpointData>
    
    /**
     * Obtiene todos los registros de fichaje de una empresa como flujo observable.
     *
     * @param companyId ID de la empresa.
     * @return Flujo de lista de registros de fichaje de la empresa.
     */
    @Query("SELECT * FROM checkpoints WHERE companyId = :companyId ORDER BY checkInTime DESC")
    fun getCheckpointsByCompanyFlow(companyId: Int): Flow<List<CheckpointData>>
    
    /**
     * Obtiene todos los registros de fichaje con un estado específico.
     *
     * @param status Estado de los registros de fichaje.
     * @return Lista de registros de fichaje con el estado especificado.
     */
    @Query("SELECT * FROM checkpoints WHERE status = :status ORDER BY checkInTime DESC")
    suspend fun getCheckpointsByStatus(status: String): List<CheckpointData>
    
    /**
     * Obtiene todos los registros de fichaje pendientes (sin hora de salida).
     *
     * @return Lista de registros de fichaje pendientes.
     */
    @Query("SELECT * FROM checkpoints WHERE checkOutTime IS NULL AND status = 'pending' ORDER BY checkInTime DESC")
    suspend fun getPendingCheckpoints(): List<CheckpointData>
    
    /**
     * Obtiene todos los registros de fichaje pendientes como flujo observable.
     *
     * @return Flujo de lista de registros de fichaje pendientes.
     */
    @Query("SELECT * FROM checkpoints WHERE checkOutTime IS NULL AND status = 'pending' ORDER BY checkInTime DESC")
    fun getPendingCheckpointsFlow(): Flow<List<CheckpointData>>
    
    /**
     * Obtiene todos los registros de fichaje pendientes de un empleado.
     *
     * @param employeeId ID del empleado.
     * @return Lista de registros de fichaje pendientes del empleado.
     */
    @Query("SELECT * FROM checkpoints WHERE employeeId = :employeeId AND checkOutTime IS NULL AND status = 'pending' ORDER BY checkInTime DESC")
    suspend fun getPendingCheckpointsByEmployee(employeeId: Int): List<CheckpointData>
    
    /**
     * Obtiene el registro de fichaje pendiente más reciente de un empleado.
     *
     * @param employeeId ID del empleado.
     * @return Registro de fichaje pendiente más reciente del empleado o null si no existe.
     */
    @Query("SELECT * FROM checkpoints WHERE employeeId = :employeeId AND checkOutTime IS NULL AND status = 'pending' ORDER BY checkInTime DESC LIMIT 1")
    suspend fun getLastPendingCheckpointByEmployee(employeeId: Int): CheckpointData?
    
    /**
     * Obtiene todos los registros de fichaje de un día específico.
     *
     * @param startOfDay Inicio del día.
     * @param endOfDay Fin del día.
     * @return Lista de registros de fichaje del día especificado.
     */
    @Query("SELECT * FROM checkpoints WHERE checkInTime BETWEEN :startOfDay AND :endOfDay ORDER BY checkInTime ASC")
    suspend fun getCheckpointsByDay(startOfDay: Date, endOfDay: Date): List<CheckpointData>
    
    /**
     * Obtiene todos los registros de fichaje de un empleado en un día específico.
     *
     * @param employeeId ID del empleado.
     * @param startOfDay Inicio del día.
     * @param endOfDay Fin del día.
     * @return Lista de registros de fichaje del empleado en el día especificado.
     */
    @Query("SELECT * FROM checkpoints WHERE employeeId = :employeeId AND checkInTime BETWEEN :startOfDay AND :endOfDay ORDER BY checkInTime ASC")
    suspend fun getCheckpointsByEmployeeAndDay(employeeId: Int, startOfDay: Date, endOfDay: Date): List<CheckpointData>
    
    /**
     * Registra la salida para un fichaje pendiente.
     *
     * @param checkpointId ID del registro de fichaje.
     * @param checkOutTime Hora de salida.
     * @param checkOutLatitude Latitud de la salida (opcional).
     * @param checkOutLongitude Longitud de la salida (opcional).
     * @param hoursWorked Horas trabajadas calculadas.
     * @param status Estado del registro ("completed" o "auto_completed").
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE checkpoints SET checkOutTime = :checkOutTime, checkOutLatitude = :checkOutLatitude, checkOutLongitude = :checkOutLongitude, hoursWorked = :hoursWorked, status = :status, updatedAt = :updatedAt, syncStatus = 'pending_update', pendingChanges = 1 WHERE id = :checkpointId")
    suspend fun registerCheckOut(
        checkpointId: Int,
        checkOutTime: Date,
        checkOutLatitude: Double? = null,
        checkOutLongitude: Double? = null,
        hoursWorked: Double,
        status: String = "completed",
        updatedAt: Date = Date()
    ): Int
    
    /**
     * Cierra automáticamente varios registros de fichaje pendientes.
     *
     * @param checkpointIds Lista de IDs de registros de fichaje.
     * @param checkOutTime Hora de salida.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE checkpoints SET checkOutTime = :checkOutTime, status = 'auto_completed', updatedAt = :updatedAt, syncStatus = 'pending_update', pendingChanges = 1, hoursWorked = (strftime('%s', :checkOutTime) - strftime('%s', checkInTime)) / 3600.0 WHERE id IN (:checkpointIds) AND checkOutTime IS NULL AND status = 'pending'")
    suspend fun autoCloseCheckpoints(
        checkpointIds: List<Int>,
        checkOutTime: Date,
        updatedAt: Date = Date()
    ): Int
    
    /**
     * Obtiene todos los registros de fichaje pendientes de sincronizar con el servidor.
     *
     * @return Lista de registros de fichaje pendientes de sincronizar.
     */
    @Query("SELECT * FROM checkpoints WHERE syncStatus != 'synced' ORDER BY updatedAt DESC")
    suspend fun getPendingSyncCheckpoints(): List<CheckpointData>
    
    /**
     * Obtiene el número de registros de fichaje pendientes de sincronizar con el servidor.
     *
     * @return Número de registros de fichaje pendientes de sincronizar.
     */
    @Query("SELECT COUNT(*) FROM checkpoints WHERE syncStatus != 'synced'")
    suspend fun getPendingSyncCheckpointsCount(): Int
    
    /**
     * Obtiene todos los registros de fichaje pendientes de sincronizar con el servidor como flujo observable.
     *
     * @return Flujo de lista de registros de fichaje pendientes de sincronizar.
     */
    @Query("SELECT * FROM checkpoints WHERE syncStatus != 'synced' ORDER BY updatedAt DESC")
    fun getPendingSyncCheckpointsFlow(): Flow<List<CheckpointData>>
    
    /**
     * Actualiza el estado de sincronización de un registro de fichaje.
     *
     * @param checkpointId ID del registro de fichaje.
     * @param syncStatus Nuevo estado de sincronización.
     * @param lastSyncTime Marca de tiempo de la última sincronización.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE checkpoints SET syncStatus = :syncStatus, lastSyncTime = :lastSyncTime, pendingChanges = :syncStatus != 'synced' WHERE id = :checkpointId")
    suspend fun updateSyncStatus(checkpointId: Int, syncStatus: String, lastSyncTime: Long = System.currentTimeMillis()): Int
    
    /**
     * Marca varios registros de fichaje como sincronizados.
     *
     * @param checkpointIds Lista de IDs de registros de fichaje.
     * @param lastSyncTime Marca de tiempo de la última sincronización.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE checkpoints SET syncStatus = 'synced', lastSyncTime = :lastSyncTime, pendingChanges = 0 WHERE id IN (:checkpointIds)")
    suspend fun markAsSynced(checkpointIds: List<Int>, lastSyncTime: Long = System.currentTimeMillis()): Int
    
    /**
     * Limpia la base de datos de registros de fichaje (elimina todos los registros).
     * Utilizar con precaución.
     */
    @Query("DELETE FROM checkpoints")
    suspend fun clearAll()
}