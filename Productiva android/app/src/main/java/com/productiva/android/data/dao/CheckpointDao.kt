package com.productiva.android.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.productiva.android.data.model.CheckpointData
import java.util.Date

/**
 * DAO (Data Access Object) para las operaciones de base de datos relacionadas con fichajes.
 */
@Dao
interface CheckpointDao {
    
    /**
     * Inserta un fichaje en la base de datos.
     * Si ya existe un registro con el mismo ID, lo reemplaza.
     *
     * @param checkpoint Fichaje a insertar.
     * @return ID generado para el fichaje insertado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(checkpoint: CheckpointData): Long
    
    /**
     * Inserta varios fichajes en la base de datos.
     * Si ya existen registros con los mismos IDs, los reemplaza.
     *
     * @param checkpoints Lista de fichajes a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(checkpoints: List<CheckpointData>)
    
    /**
     * Obtiene todos los fichajes de la base de datos.
     *
     * @return Lista de todos los fichajes.
     */
    @Query("SELECT * FROM checkpoints ORDER BY checkInTime DESC")
    suspend fun getAllCheckpoints(): List<CheckpointData>
    
    /**
     * Obtiene un fichaje por su ID.
     *
     * @param checkpointId ID del fichaje.
     * @return Fichaje con el ID especificado o null si no existe.
     */
    @Query("SELECT * FROM checkpoints WHERE id = :checkpointId")
    suspend fun getCheckpointById(checkpointId: Int): CheckpointData?
    
    /**
     * Obtiene los fichajes de un empleado específico.
     *
     * @param employeeId ID del empleado.
     * @return Lista de fichajes del empleado.
     */
    @Query("SELECT * FROM checkpoints WHERE employeeId = :employeeId ORDER BY checkInTime DESC")
    suspend fun getCheckpointsByEmployee(employeeId: Int): List<CheckpointData>
    
    /**
     * Obtiene los fichajes de un día específico.
     *
     * @param startOfDay Inicio del día.
     * @param endOfDay Fin del día.
     * @return Lista de fichajes del día.
     */
    @Query("SELECT * FROM checkpoints WHERE checkInTime >= :startOfDay AND checkInTime <= :endOfDay ORDER BY checkInTime")
    suspend fun getCheckpointsByDay(startOfDay: Date, endOfDay: Date): List<CheckpointData>
    
    /**
     * Obtiene el último fichaje pendiente (sin hora de salida) de un empleado.
     *
     * @param employeeId ID del empleado.
     * @return Último fichaje pendiente del empleado o null si no hay ninguno.
     */
    @Query("SELECT * FROM checkpoints WHERE employeeId = :employeeId AND status = 'PENDING' ORDER BY checkInTime DESC LIMIT 1")
    suspend fun getLastPendingCheckpointByEmployee(employeeId: Int): CheckpointData?
    
    /**
     * Obtiene todos los fichajes pendientes (sin hora de salida).
     *
     * @return Lista de fichajes pendientes.
     */
    @Query("SELECT * FROM checkpoints WHERE status = 'PENDING' ORDER BY checkInTime")
    suspend fun getPendingCheckpoints(): List<CheckpointData>
    
    /**
     * Registra la salida para un fichaje.
     *
     * @param checkpointId ID del fichaje.
     * @param checkOutTime Fecha y hora de salida.
     * @param checkOutLatitude Latitud de la ubicación de salida (opcional).
     * @param checkOutLongitude Longitud de la ubicación de salida (opcional).
     * @param hoursWorked Horas trabajadas.
     * @param isAutomatic Indica si el cierre es automático.
     * @param notes Notas adicionales (opcional).
     */
    @Query("UPDATE checkpoints SET checkOutTime = :checkOutTime, checkOutLatitude = :checkOutLatitude, checkOutLongitude = :checkOutLongitude, hoursWorked = :hoursWorked, status = :status, notes = CASE WHEN :notes IS NULL THEN notes ELSE :notes END, syncStatus = 'PENDING_UPDATE', pendingChanges = 1, updatedAt = CURRENT_TIMESTAMP WHERE id = :checkpointId")
    suspend fun registerCheckOut(
        checkpointId: Int,
        checkOutTime: Date,
        checkOutLatitude: Double? = null,
        checkOutLongitude: Double? = null,
        hoursWorked: Double,
        status: CheckpointData.Status = CheckpointData.Status.COMPLETED,
        notes: String? = null
    )
    
    /**
     * Obtiene todos los fichajes con cambios pendientes de sincronización.
     *
     * @return Lista de fichajes pendientes de sincronizar.
     */
    @Query("SELECT * FROM checkpoints WHERE syncStatus != 'SYNCED' OR pendingChanges = 1")
    suspend fun getPendingSyncCheckpoints(): List<CheckpointData>
    
    /**
     * Obtiene la cantidad de fichajes pendientes de sincronización.
     *
     * @return Número de fichajes pendientes de sincronizar.
     */
    @Query("SELECT COUNT(*) FROM checkpoints WHERE syncStatus != 'SYNCED' OR pendingChanges = 1")
    suspend fun getPendingSyncCheckpointsCount(): Int
    
    /**
     * Marca varios fichajes como sincronizados.
     *
     * @param checkpointIds Lista de IDs de fichajes a marcar.
     */
    @Query("UPDATE checkpoints SET syncStatus = 'SYNCED', pendingChanges = 0 WHERE id IN (:checkpointIds)")
    suspend fun markAsSynced(checkpointIds: List<Int>)
    
    /**
     * Actualiza el estado de sincronización de un fichaje.
     *
     * @param checkpointId ID del fichaje.
     * @param syncStatus Nuevo estado de sincronización.
     */
    @Query("UPDATE checkpoints SET syncStatus = :syncStatus, pendingChanges = 0 WHERE id = :checkpointId")
    suspend fun updateSyncStatus(checkpointId: Int, syncStatus: CheckpointData.SyncStatus)
}