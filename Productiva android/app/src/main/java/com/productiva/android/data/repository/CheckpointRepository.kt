package com.productiva.android.data.repository

import android.content.Context
import android.util.Log
import com.productiva.android.data.database.AppDatabase
import com.productiva.android.data.model.CheckpointData
import com.productiva.android.network.NetworkResult
import com.productiva.android.network.model.SyncResponse
import com.productiva.android.network.safeApiCall
import com.productiva.android.repository.ResourceState
import com.productiva.android.session.SessionManager
import com.productiva.android.sync.SyncResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.HashMap

/**
 * Repositorio que gestiona el acceso a datos de fichajes,
 * tanto desde la base de datos local como desde el servidor remoto.
 */
class CheckpointRepository private constructor(context: Context) : BaseRepository(context) {
    
    private val checkpointDao = AppDatabase.getDatabase(context, kotlinx.coroutines.MainScope()).checkpointDao()
    private val sessionManager = SessionManager.getInstance()
    
    companion object {
        private const val TAG = "CheckpointRepository"
        
        @Volatile
        private var instance: CheckpointRepository? = null
        
        /**
         * Obtiene la instancia única del repositorio.
         *
         * @param context Contexto de la aplicación.
         * @return Instancia del repositorio.
         */
        fun getInstance(context: Context): CheckpointRepository {
            return instance ?: synchronized(this) {
                instance ?: CheckpointRepository(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Obtiene todos los fichajes del día actual como flujo de ResourceState.
     *
     * @param forceRefresh Si es true, fuerza una actualización desde el servidor.
     * @return Flujo de ResourceState con los fichajes.
     */
    fun getTodayCheckpoints(forceRefresh: Boolean = false): Flow<ResourceState<List<CheckpointData>>> {
        // Formato para la fecha en la API
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        
        return networkBoundResource(
            shouldFetch = { checkpoints -> forceRefresh || checkpoints.isNullOrEmpty() },
            remoteFetch = {
                val companyId = sessionManager.getCurrentCompanyId()
                safeApiCall {
                    apiService.getCheckpoints(companyId = companyId, date = today)
                }
            },
            localFetch = {
                // Obtener el inicio y fin del día actual
                val calendar = java.util.Calendar.getInstance()
                calendar.time = Date()
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startOfDay = calendar.time
                
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
                calendar.set(java.util.Calendar.MINUTE, 59)
                calendar.set(java.util.Calendar.SECOND, 59)
                calendar.set(java.util.Calendar.MILLISECOND, 999)
                val endOfDay = calendar.time
                
                checkpointDao.getCheckpointsByDay(startOfDay, endOfDay)
            },
            saveFetchResult = { checkpoints ->
                withContext(Dispatchers.IO) {
                    // Guardar fichajes en la base de datos local
                    val checkpointsToSave = checkpoints.map { 
                        it.withSyncStatus(CheckpointData.SyncStatus.SYNCED) 
                    }
                    checkpointDao.insertAll(checkpointsToSave)
                }
            }
        )
    }
    
    /**
     * Obtiene todos los fichajes pendientes (sin hora de salida) como flujo de ResourceState.
     *
     * @param forceRefresh Si es true, fuerza una actualización desde el servidor.
     * @return Flujo de ResourceState con los fichajes pendientes.
     */
    fun getPendingCheckpoints(forceRefresh: Boolean = false): Flow<ResourceState<List<CheckpointData>>> {
        return networkBoundResource(
            shouldFetch = { checkpoints -> forceRefresh },
            remoteFetch = {
                val companyId = sessionManager.getCurrentCompanyId()
                // No hay endpoint específico para fichajes pendientes, obtener todos los de hoy y filtrar
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = dateFormat.format(Date())
                
                val result = safeApiCall {
                    apiService.getCheckpoints(companyId = companyId, date = today)
                }
                
                when (result) {
                    is NetworkResult.Success -> {
                        val pendingCheckpoints = result.data.filter { it.isPending() }
                        NetworkResult.Success(pendingCheckpoints)
                    }
                    is NetworkResult.Error -> result
                    NetworkResult.Loading -> NetworkResult.Loading
                }
            },
            localFetch = {
                checkpointDao.getPendingCheckpoints()
            },
            saveFetchResult = { checkpoints ->
                withContext(Dispatchers.IO) {
                    // Guardar fichajes en la base de datos local
                    val checkpointsToSave = checkpoints.map { 
                        it.withSyncStatus(CheckpointData.SyncStatus.SYNCED) 
                    }
                    checkpointDao.insertAll(checkpointsToSave)
                }
            }
        )
    }
    
    /**
     * Obtiene el fichaje pendiente más reciente de un empleado.
     *
     * @param employeeId ID del empleado.
     * @return Fichaje pendiente o null si no existe.
     */
    suspend fun getLastPendingCheckpointForEmployee(employeeId: Int): CheckpointData? = withContext(Dispatchers.IO) {
        checkpointDao.getLastPendingCheckpointByEmployee(employeeId)
    }
    
    /**
     * Registra un nuevo fichaje de entrada.
     *
     * @param employeeId ID del empleado.
     * @param locationId ID del punto de fichaje.
     * @param latitude Latitud de la ubicación (opcional).
     * @param longitude Longitud de la ubicación (opcional).
     * @return Flujo de ResourceState con el fichaje creado.
     */
    suspend fun checkIn(
        employeeId: Int,
        locationId: Int,
        latitude: Double? = null,
        longitude: Double? = null
    ): ResourceState<CheckpointData> = withContext(Dispatchers.IO) {
        try {
            // Verificar si ya hay un fichaje pendiente
            val pendingCheckpoint = checkpointDao.getLastPendingCheckpointByEmployee(employeeId)
            if (pendingCheckpoint != null) {
                return@withContext ResourceState.Error("Ya hay un fichaje pendiente para este empleado")
            }
            
            // Crear nuevo fichaje
            val companyId = sessionManager.getCurrentCompanyId()
            val now = Date()
            
            val checkpoint = CheckpointData(
                employeeId = employeeId,
                locationId = locationId,
                companyId = companyId,
                checkInTime = now,
                checkInLatitude = latitude,
                checkInLongitude = longitude,
                status = CheckpointData.Status.PENDING,
                createdAt = now,
                updatedAt = now,
                syncStatus = CheckpointData.SyncStatus.PENDING_UPLOAD,
                pendingChanges = true
            )
            
            // Guardar en la base de datos local
            val checkpointId = checkpointDao.insert(checkpoint).toInt()
            val savedCheckpoint = checkpointDao.getCheckpointById(checkpointId)
                ?: return@withContext ResourceState.Error("Error al guardar el fichaje")
            
            // Intentar sincronizar si hay conexión
            if (connectivityMonitor.isNetworkAvailable()) {
                when (val result = safeApiCall { apiService.registerCheckpoint(savedCheckpoint) }) {
                    is NetworkResult.Success -> {
                        // Actualizar con los datos del servidor
                        val serverCheckpoint = result.data
                        checkpointDao.insert(serverCheckpoint.withSyncStatus(CheckpointData.SyncStatus.SYNCED))
                        
                        return@withContext ResourceState.Success(serverCheckpoint)
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Error al sincronizar fichaje: ${result.message}")
                        return@withContext ResourceState.Success(savedCheckpoint, isFromCache = true)
                    }
                    is NetworkResult.Loading -> {
                        return@withContext ResourceState.Success(savedCheckpoint, isFromCache = true)
                    }
                }
            } else {
                // Sin conexión, devolver éxito pero con datos locales
                return@withContext ResourceState.Success(savedCheckpoint, isFromCache = true)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar entrada", e)
            ResourceState.Error(e.message ?: "Error desconocido")
        }
    }
    
    /**
     * Registra la salida para un fichaje pendiente.
     *
     * @param checkpointId ID del fichaje.
     * @param latitude Latitud de la ubicación (opcional).
     * @param longitude Longitud de la ubicación (opcional).
     * @return Flujo de ResourceState con el fichaje actualizado.
     */
    suspend fun checkOut(
        checkpointId: Int,
        latitude: Double? = null,
        longitude: Double? = null
    ): ResourceState<CheckpointData> = withContext(Dispatchers.IO) {
        try {
            // Obtener el fichaje
            val checkpoint = checkpointDao.getCheckpointById(checkpointId)
                ?: return@withContext ResourceState.Error("Fichaje no encontrado")
            
            // Verificar si ya está completado
            if (!checkpoint.isPending()) {
                return@withContext ResourceState.Error("El fichaje ya está completado")
            }
            
            // Registrar salida
            val now = Date()
            val hoursWorked = calculateHoursWorked(checkpoint.checkInTime, now)
            
            checkpointDao.registerCheckOut(
                checkpointId = checkpointId,
                checkOutTime = now,
                checkOutLatitude = latitude,
                checkOutLongitude = longitude,
                hoursWorked = hoursWorked
            )
            
            // Obtener el fichaje actualizado
            val updatedCheckpoint = checkpointDao.getCheckpointById(checkpointId)
                ?: return@withContext ResourceState.Error("Error al actualizar el fichaje")
            
            // Intentar sincronizar si hay conexión
            if (connectivityMonitor.isNetworkAvailable()) {
                when (val result = safeApiCall { apiService.registerCheckpoint(updatedCheckpoint) }) {
                    is NetworkResult.Success -> {
                        // Actualizar con los datos del servidor
                        val serverCheckpoint = result.data
                        checkpointDao.insert(serverCheckpoint.withSyncStatus(CheckpointData.SyncStatus.SYNCED))
                        
                        return@withContext ResourceState.Success(serverCheckpoint)
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Error al sincronizar fichaje completado: ${result.message}")
                        return@withContext ResourceState.Success(updatedCheckpoint, isFromCache = true)
                    }
                    is NetworkResult.Loading -> {
                        return@withContext ResourceState.Success(updatedCheckpoint, isFromCache = true)
                    }
                }
            } else {
                // Sin conexión, devolver éxito pero con datos locales
                return@withContext ResourceState.Success(updatedCheckpoint, isFromCache = true)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar salida", e)
            ResourceState.Error(e.message ?: "Error desconocido")
        }
    }
    
    /**
     * Calcula las horas trabajadas entre dos fechas.
     *
     * @param checkIn Fecha y hora de entrada.
     * @param checkOut Fecha y hora de salida.
     * @return Número de horas trabajadas.
     */
    private fun calculateHoursWorked(checkIn: Date, checkOut: Date): Double {
        val diffMillis = checkOut.time - checkIn.time
        return (diffMillis / (1000.0 * 60 * 60)) // Convertir milisegundos a horas
    }
    
    /**
     * Sincroniza todos los fichajes pendientes con el servidor.
     *
     * @param lastSyncTime Marca de tiempo de la última sincronización.
     * @return Resultado de la sincronización.
     */
    suspend fun syncWithServer(lastSyncTime: Long): SyncResult = executeSyncOperation {
        val companyId = sessionManager.getCurrentCompanyId()
        
        // 1. Obtener todos los fichajes pendientes de sincronización
        val pendingCheckpoints = checkpointDao.getPendingSyncCheckpoints()
        
        if (pendingCheckpoints.isEmpty() && lastSyncTime == 0L) {
            // No hay nada que sincronizar y no es una sincronización inicial
            return@executeSyncOperation SyncResult.Success()
        }
        
        // 2. Preparar los datos para la sincronización
        val syncData = HashMap<String, Any>()
        
        // 2.1. Añadir fichajes a sincronizar
        syncData["checkpoints"] = pendingCheckpoints
        
        // 2.2. Añadir última vez sincronizado para recibir actualizaciones del servidor
        syncData["last_sync"] = lastSyncTime
        
        // 2.3. Añadir ID de empresa
        syncData["company_id"] = companyId
        
        // 3. Realizar la sincronización con el servidor
        val result = safeApiCall {
            apiService.syncCheckpoints(syncData)
        }
        
        when (result) {
            is NetworkResult.Success -> {
                // 4. Procesar la respuesta del servidor
                val response = result.data.data ?: throw Exception("Respuesta vacía del servidor")
                
                // 4.1. Guardar fichajes añadidos y actualizados
                val checkpointsToSave = mutableListOf<CheckpointData>()
                
                response.added.forEach { checkpoint ->
                    checkpointsToSave.add(checkpoint.withSyncStatus(CheckpointData.SyncStatus.SYNCED))
                }
                
                response.updated.forEach { checkpoint ->
                    checkpointsToSave.add(checkpoint.withSyncStatus(CheckpointData.SyncStatus.SYNCED))
                }
                
                if (checkpointsToSave.isNotEmpty()) {
                    checkpointDao.insertAll(checkpointsToSave)
                }
                
                // 4.2. Marcar como sincronizados los fichajes que enviamos
                val syncedIds = pendingCheckpoints.map { it.id }
                if (syncedIds.isNotEmpty()) {
                    checkpointDao.markAsSynced(syncedIds)
                }
                
                SyncResult.Success(
                    addedCount = response.added.size,
                    updatedCount = response.updated.size,
                    deletedCount = 0 // Los fichajes no se eliminan
                )
            }
            is NetworkResult.Error -> {
                throw Exception(result.message)
            }
            is NetworkResult.Loading -> {
                throw Exception("Estado de carga inesperado")
            }
        }
    }
    
    /**
     * Obtiene el número de fichajes pendientes de sincronización.
     *
     * @return Número de fichajes pendientes de sincronización.
     */
    suspend fun getPendingSyncCount(): Int = withContext(Dispatchers.IO) {
        checkpointDao.getPendingSyncCheckpointsCount()
    }
}