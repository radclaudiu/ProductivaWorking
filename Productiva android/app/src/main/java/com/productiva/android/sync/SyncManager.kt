package com.productiva.android.sync

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.productiva.android.data.repository.CheckpointRepository
import com.productiva.android.data.repository.LabelTemplateRepository
import com.productiva.android.data.repository.ProductRepository
import com.productiva.android.data.repository.TaskRepository
import com.productiva.android.utils.ConnectivityMonitor
import com.productiva.android.utils.PREF_LAST_SYNC_TIME
import com.productiva.android.utils.PREFS_NAME
import com.productiva.android.utils.SYNC_INTERVAL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Administra todas las operaciones de sincronización de la aplicación.
 * Se encarga de coordinar la sincronización de todos los tipos de datos
 * y gestionar la programación de sincronizaciones periódicas.
 */
class SyncManager private constructor(private val context: Context) {
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val workManager = WorkManager.getInstance(context)
    private val notificationHelper = SyncNotificationHelper(context)
    private val connectivityMonitor = ConnectivityMonitor.getInstance(context)
    
    // Repositorios que manejan la sincronización de cada tipo de datos
    private val taskRepository: TaskRepository by lazy { TaskRepository.getInstance(context) }
    private val productRepository: ProductRepository by lazy { ProductRepository.getInstance(context) }
    private val labelTemplateRepository: LabelTemplateRepository by lazy { LabelTemplateRepository.getInstance(context) }
    private val checkpointRepository: CheckpointRepository by lazy { CheckpointRepository.getInstance(context) }
    
    // Estados observables
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()
    
    companion object {
        private const val TAG = "SyncManager"
        private const val SYNC_WORK_NAME = "periodic_sync_work"
        
        @Volatile
        private var instance: SyncManager? = null
        
        /**
         * Obtiene la instancia única del administrador de sincronización.
         *
         * @param context Contexto de la aplicación.
         * @return Instancia del administrador de sincronización.
         */
        fun getInstance(context: Context): SyncManager {
            return instance ?: synchronized(this) {
                instance ?: SyncManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Inicializa el administrador de sincronización.
     */
    init {
        // Observar cambios en la conectividad para sincronizar cuando se recupera la conexión
        coroutineScope.launch {
            connectivityMonitor.isNetworkAvailableFlow.collectLatest { isAvailable ->
                if (isAvailable) {
                    Log.d(TAG, "Conexión recuperada, verificando cambios pendientes...")
                    checkPendingChanges()
                }
            }
        }
        
        // Monitorear los cambios pendientes
        coroutineScope.launch {
            refreshPendingChangesCount()
        }
    }
    
    /**
     * Actualiza el contador de cambios pendientes de sincronización.
     */
    private suspend fun refreshPendingChangesCount() {
        val pendingTasks = taskRepository.getPendingSyncCount()
        val pendingProducts = productRepository.getPendingSyncCount()
        val pendingTemplates = labelTemplateRepository.getPendingSyncCount()
        val pendingCheckpoints = checkpointRepository.getPendingSyncCount()
        
        val total = pendingTasks + pendingProducts + pendingTemplates + pendingCheckpoints
        _pendingSyncCount.value = total
        
        Log.d(TAG, "Cambios pendientes: $total (Tareas: $pendingTasks, Productos: $pendingProducts, Plantillas: $pendingTemplates, Fichajes: $pendingCheckpoints)")
    }
    
    /**
     * Verifica si hay cambios pendientes y los sincroniza automáticamente si corresponde.
     */
    private suspend fun checkPendingChanges() {
        refreshPendingChangesCount()
        
        if (_pendingSyncCount.value > 0) {
            // Si hay cambios pendientes, sincronizar automáticamente
            Log.d(TAG, "Hay ${_pendingSyncCount.value} cambios pendientes, sincronizando automáticamente...")
            syncAll(showNotification = false)
        }
    }
    
    /**
     * Inicia una sincronización completa de todos los tipos de datos.
     *
     * @param forceFullSync Si es true, fuerza una sincronización completa ignorando las marcas de tiempo.
     * @param showNotification Si es true, muestra notificaciones durante la sincronización.
     */
    suspend fun syncAll(forceFullSync: Boolean = false, showNotification: Boolean = true) {
        if (_syncState.value is SyncState.Syncing) {
            Log.d(TAG, "Ya hay una sincronización en curso, ignorando solicitud")
            return
        }
        
        _syncState.value = SyncState.Syncing(0)
        
        if (showNotification) {
            notificationHelper.showSyncProgressNotification("Sincronizando datos...", 0)
        }
        
        try {
            // Obtener la última vez que se sincronizó
            val lastSyncTime = if (forceFullSync) 0L else getLastSyncTime()
            
            // Sincronizar todos los tipos de datos en secuencia
            // Primero los datos de configuración y luego los datos operativos
            
            // 1. Productos
            _syncState.value = SyncState.Syncing(20)
            if (showNotification) {
                notificationHelper.updateSyncProgressNotification("Sincronizando productos...", 20)
            }
            val productResult = productRepository.syncWithServer(lastSyncTime)
            
            // 2. Plantillas de etiquetas
            _syncState.value = SyncState.Syncing(40)
            if (showNotification) {
                notificationHelper.updateSyncProgressNotification("Sincronizando plantillas...", 40)
            }
            val templateResult = labelTemplateRepository.syncWithServer(lastSyncTime)
            
            // 3. Tareas y completados
            _syncState.value = SyncState.Syncing(60)
            if (showNotification) {
                notificationHelper.updateSyncProgressNotification("Sincronizando tareas...", 60)
            }
            val taskResult = taskRepository.syncWithServer(lastSyncTime)
            
            // 4. Fichajes
            _syncState.value = SyncState.Syncing(80)
            if (showNotification) {
                notificationHelper.updateSyncProgressNotification("Sincronizando fichajes...", 80)
            }
            val checkpointResult = checkpointRepository.syncWithServer(lastSyncTime)
            
            // Actualizar la hora de la última sincronización
            val currentTime = System.currentTimeMillis()
            saveLastSyncTime(currentTime)
            
            // Actualizar el contador de cambios pendientes
            refreshPendingChangesCount()
            
            // Determinar si hubo algún error
            val hasErrors = productResult is SyncResult.Error ||
                templateResult is SyncResult.Error ||
                taskResult is SyncResult.Error ||
                checkpointResult is SyncResult.Error
            
            if (hasErrors) {
                _syncState.value = SyncState.Error("Error durante la sincronización")
                if (showNotification) {
                    notificationHelper.showSyncErrorNotification("Error durante la sincronización")
                }
            } else {
                _syncState.value = SyncState.Success(currentTime)
                if (showNotification) {
                    notificationHelper.showSyncCompletedNotification("Sincronización completada")
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la sincronización", e)
            _syncState.value = SyncState.Error(e.message ?: "Error desconocido")
            if (showNotification) {
                notificationHelper.showSyncErrorNotification("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Inicia una sincronización manual a pedido del usuario.
     */
    fun startManualSync() {
        coroutineScope.launch {
            // Forzar sincronización completa en modo manual
            syncAll(forceFullSync = true, showNotification = true)
        }
    }
    
    /**
     * Programa una sincronización periódica en segundo plano.
     */
    fun schedulePeriodicalSync() {
        // Configurar restricciones para la tarea de sincronización
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)  // Requerir conexión a internet
            .build()
        
        // Crear solicitud de trabajo periódico
        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            SYNC_INTERVAL, TimeUnit.MILLISECONDS
        )
            .setConstraints(constraints)
            .build()
        
        // Programar el trabajo, reemplazando cualquier trabajo existente
        workManager.enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            syncWorkRequest
        )
        
        Log.d(TAG, "Sincronización periódica programada cada ${SYNC_INTERVAL/1000/60} minutos")
    }
    
    /**
     * Cancela la sincronización periódica en segundo plano.
     */
    fun cancelPeriodicalSync() {
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
        Log.d(TAG, "Sincronización periódica cancelada")
    }
    
    /**
     * Obtiene la hora de la última sincronización.
     *
     * @return Marca de tiempo de la última sincronización o 0 si nunca se ha sincronizado.
     */
    fun getLastSyncTime(): Long {
        return prefs.getLong(PREF_LAST_SYNC_TIME, 0L)
    }
    
    /**
     * Guarda la hora de la última sincronización.
     *
     * @param timestamp Marca de tiempo a guardar.
     */
    private fun saveLastSyncTime(timestamp: Long) {
        prefs.edit().putLong(PREF_LAST_SYNC_TIME, timestamp).apply()
    }
    
    /**
     * Limpia los recursos cuando el administrador de sincronización ya no se usa.
     */
    fun onDestroy() {
        coroutineScope.cancel()
    }
}

/**
 * Estados posibles durante el proceso de sincronización.
 */
sealed class SyncState {
    /**
     * Estado de inactividad, no hay sincronización en curso.
     */
    object Idle : SyncState()
    
    /**
     * Estado de sincronización en curso.
     *
     * @property progress Porcentaje de progreso de la sincronización (0-100).
     */
    data class Syncing(val progress: Int) : SyncState()
    
    /**
     * Estado de sincronización exitosa.
     *
     * @property timestamp Marca de tiempo de la sincronización exitosa.
     */
    data class Success(val timestamp: Long) : SyncState()
    
    /**
     * Estado de error durante la sincronización.
     *
     * @property message Mensaje de error.
     */
    data class Error(val message: String) : SyncState()
}

/**
 * Resultados posibles de una operación de sincronización.
 */
sealed class SyncResult {
    /**
     * Resultado exitoso de sincronización.
     *
     * @property addedCount Número de elementos añadidos.
     * @property updatedCount Número de elementos actualizados.
     * @property deletedCount Número de elementos eliminados.
     * @property timestamp Marca de tiempo de la sincronización.
     */
    data class Success(
        val addedCount: Int = 0,
        val updatedCount: Int = 0,
        val deletedCount: Int = 0,
        val timestamp: Long = System.currentTimeMillis()
    ) : SyncResult()
    
    /**
     * Resultado de error durante la sincronización.
     *
     * @property message Mensaje de error.
     * @property exception Excepción que causó el error (opcional).
     */
    data class Error(
        val message: String,
        val exception: Exception? = null
    ) : SyncResult()
}