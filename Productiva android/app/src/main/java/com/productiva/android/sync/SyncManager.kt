package com.productiva.android.sync

import android.content.Context
import android.util.Log
import com.productiva.android.database.AppDatabase
import com.productiva.android.network.ApiService
import com.productiva.android.network.RetrofitClient
import com.productiva.android.repository.LabelTemplateRepository
import com.productiva.android.repository.ProductRepository
import com.productiva.android.repository.ResourceState
import com.productiva.android.repository.TaskRepository
import com.productiva.android.utils.ConnectivityMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Gestor de sincronización para mantener los datos locales actualizados con el servidor.
 * Maneja la lógica de sincronización de tareas, productos y plantillas de etiquetas.
 */
class SyncManager(private val context: Context) {
    private val TAG = "SyncManager"
    
    // Dependencias
    private val database = AppDatabase.getDatabase(context)
    private val apiService = RetrofitClient.getApiService(context)
    private val connectivityMonitor = ConnectivityMonitor(context)
    
    // Repositorios
    private val taskRepository = TaskRepository(database.taskDao(), apiService)
    private val productRepository = ProductRepository(database.productDao(), apiService)
    private val labelTemplateRepository = LabelTemplateRepository(database.labelTemplateDao(), apiService)
    
    // Ámbito de corrutina para operaciones de sincronización
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Estado de sincronización
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState
    
    // Banderas para evitar sincronizaciones simultáneas
    private val isSyncing = AtomicBoolean(false)
    private val hasPendingSync = AtomicBoolean(false)
    
    // Último tiempo de sincronización
    private var lastSyncTimestamp: Long = 0
    
    init {
        // Monitorizar conectividad para iniciar sincronización cuando hay conexión
        connectivityMonitor.observe { isConnected ->
            if (isConnected && hasPendingSync.get()) {
                syncAll()
            }
        }
    }
    
    /**
     * Sincroniza todos los datos con el servidor.
     * 
     * @param userId ID del usuario actual para filtrar tareas asignadas.
     * @return true si la sincronización fue iniciada, false si ya hay una sincronización en curso.
     */
    fun syncAll(userId: Int? = null): Boolean {
        if (isSyncing.getAndSet(true)) {
            // Ya hay una sincronización en curso
            hasPendingSync.set(true)
            return false
        }
        
        if (!connectivityMonitor.isConnected()) {
            _syncState.value = SyncState.Error("No hay conexión a Internet")
            isSyncing.set(false)
            hasPendingSync.set(true)
            return false
        }
        
        _syncState.value = SyncState.Syncing(SyncStep.STARTED)
        
        syncScope.launch {
            try {
                // Sincronizar datos en orden: primero se envían cambios locales, luego se obtienen datos del servidor
                
                // 1. Sincronizar cambios locales pendientes
                _syncState.value = SyncState.Syncing(SyncStep.UPLOADING_CHANGES)
                val completionsResult = syncPendingTaskCompletions()
                val productsResult = syncPendingProductChanges()
                val templatesResult = syncPendingTemplateUsages()
                
                // 2. Descargar datos actualizados del servidor
                _syncState.value = SyncState.Syncing(SyncStep.DOWNLOADING_TASKS)
                val tasksResult = syncTasks(userId)
                
                _syncState.value = SyncState.Syncing(SyncStep.DOWNLOADING_PRODUCTS)
                val productsDownloadResult = syncProducts()
                
                _syncState.value = SyncState.Syncing(SyncStep.DOWNLOADING_TEMPLATES)
                val templatesDownloadResult = syncLabelTemplates()
                
                // 3. Actualizar timestamp y estado
                lastSyncTimestamp = System.currentTimeMillis()
                
                // 4. Verificar si hubo errores
                if (tasksResult && productsDownloadResult && templatesDownloadResult) {
                    _syncState.value = SyncState.Success(lastSyncTimestamp)
                } else {
                    // Hubo errores en alguna sincronización
                    val errorMessage = buildErrorMessage(tasksResult, productsDownloadResult, templatesDownloadResult)
                    _syncState.value = SyncState.PartialSuccess(lastSyncTimestamp, errorMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en sincronización", e)
                _syncState.value = SyncState.Error(e.message ?: "Error desconocido")
            } finally {
                isSyncing.set(false)
                
                // Si hay sincronización pendiente y hay conexión, iniciar otra sincronización
                if (hasPendingSync.getAndSet(false) && connectivityMonitor.isConnected()) {
                    syncAll(userId)
                }
            }
        }
        
        return true
    }
    
    /**
     * Sincroniza solo las tareas con el servidor.
     * 
     * @param userId ID del usuario actual para filtrar tareas asignadas.
     * @return true si la sincronización fue exitosa, false en caso contrario.
     */
    private suspend fun syncTasks(userId: Int? = null): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                var success = true
                
                // Sincronizar tareas
                taskRepository.syncTasks(userId).collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            Log.d(TAG, "Tareas sincronizadas correctamente: ${state.data?.size ?: 0}")
                        }
                        is ResourceState.Error -> {
                            Log.e(TAG, "Error al sincronizar tareas: ${state.message}")
                            success = false
                        }
                        else -> {}
                    }
                }
                
                return@withContext success
            } catch (e: Exception) {
                Log.e(TAG, "Error al sincronizar tareas", e)
                return@withContext false
            }
        }
    }
    
    /**
     * Sincroniza solo los productos con el servidor.
     * 
     * @return true si la sincronización fue exitosa, false en caso contrario.
     */
    private suspend fun syncProducts(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                var success = true
                
                // Sincronizar productos
                productRepository.syncProducts().collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            Log.d(TAG, "Productos sincronizados correctamente: ${state.data?.size ?: 0}")
                        }
                        is ResourceState.Error -> {
                            Log.e(TAG, "Error al sincronizar productos: ${state.message}")
                            success = false
                        }
                        else -> {}
                    }
                }
                
                return@withContext success
            } catch (e: Exception) {
                Log.e(TAG, "Error al sincronizar productos", e)
                return@withContext false
            }
        }
    }
    
    /**
     * Sincroniza solo las plantillas de etiquetas con el servidor.
     * 
     * @return true si la sincronización fue exitosa, false en caso contrario.
     */
    private suspend fun syncLabelTemplates(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                var success = true
                
                // Sincronizar plantillas
                labelTemplateRepository.syncLabelTemplates().collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            Log.d(TAG, "Plantillas sincronizadas correctamente: ${state.data?.size ?: 0}")
                        }
                        is ResourceState.Error -> {
                            Log.e(TAG, "Error al sincronizar plantillas: ${state.message}")
                            success = false
                        }
                        else -> {}
                    }
                }
                
                return@withContext success
            } catch (e: Exception) {
                Log.e(TAG, "Error al sincronizar plantillas", e)
                return@withContext false
            }
        }
    }
    
    /**
     * Sincroniza los completados de tareas pendientes con el servidor.
     * 
     * @return true si la sincronización fue exitosa, false en caso contrario.
     */
    private suspend fun syncPendingTaskCompletions(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                var success = true
                
                // Sincronizar completados pendientes
                taskRepository.syncPendingTaskCompletions().collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            Log.d(TAG, "Completados sincronizados correctamente: ${state.data ?: 0}")
                        }
                        is ResourceState.Error -> {
                            Log.e(TAG, "Error al sincronizar completados: ${state.message}")
                            success = false
                        }
                        else -> {}
                    }
                }
                
                return@withContext success
            } catch (e: Exception) {
                Log.e(TAG, "Error al sincronizar completados", e)
                return@withContext false
            }
        }
    }
    
    /**
     * Sincroniza los cambios de productos pendientes con el servidor.
     * 
     * @return true si la sincronización fue exitosa, false en caso contrario.
     */
    private suspend fun syncPendingProductChanges(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                var success = true
                
                // Sincronizar cambios de productos pendientes
                productRepository.syncPendingProductChanges().collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            Log.d(TAG, "Cambios de productos sincronizados correctamente: ${state.data ?: 0}")
                        }
                        is ResourceState.Error -> {
                            Log.e(TAG, "Error al sincronizar cambios de productos: ${state.message}")
                            success = false
                        }
                        else -> {}
                    }
                }
                
                return@withContext success
            } catch (e: Exception) {
                Log.e(TAG, "Error al sincronizar cambios de productos", e)
                return@withContext false
            }
        }
    }
    
    /**
     * Sincroniza los usos de plantillas pendientes con el servidor.
     * 
     * @return true si la sincronización fue exitosa, false en caso contrario.
     */
    private suspend fun syncPendingTemplateUsages(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                var success = true
                
                // Sincronizar usos de plantillas pendientes
                labelTemplateRepository.syncPendingUsageCounts().collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            Log.d(TAG, "Usos de plantillas sincronizados correctamente: ${state.data ?: 0}")
                        }
                        is ResourceState.Error -> {
                            Log.e(TAG, "Error al sincronizar usos de plantillas: ${state.message}")
                            success = false
                        }
                        else -> {}
                    }
                }
                
                return@withContext success
            } catch (e: Exception) {
                Log.e(TAG, "Error al sincronizar usos de plantillas", e)
                return@withContext false
            }
        }
    }
    
    /**
     * Construye un mensaje de error basado en los resultados de sincronización.
     * 
     * @param tasksResult Resultado de sincronización de tareas.
     * @param productsResult Resultado de sincronización de productos.
     * @param templatesResult Resultado de sincronización de plantillas.
     * @return Mensaje de error.
     */
    private fun buildErrorMessage(
        tasksResult: Boolean,
        productsResult: Boolean,
        templatesResult: Boolean
    ): String {
        val errors = mutableListOf<String>()
        
        if (!tasksResult) errors.add("tareas")
        if (!productsResult) errors.add("productos")
        if (!templatesResult) errors.add("plantillas")
        
        return if (errors.isNotEmpty()) {
            "Error al sincronizar ${errors.joinToString(", ")}"
        } else {
            "Sincronización parcial"
        }
    }
    
    /**
     * Obtiene el timestamp de la última sincronización exitosa.
     * 
     * @return Timestamp en milisegundos.
     */
    fun getLastSyncTimestamp(): Long {
        return lastSyncTimestamp
    }
    
    /**
     * Verifica si hay datos pendientes de sincronización.
     * 
     * @return true si hay datos pendientes, false en caso contrario.
     */
    suspend fun hasPendingChanges(): Boolean {
        return withContext(Dispatchers.IO) {
            val pendingTasks = database.taskDao().countTasksForSync().value ?: 0
            val pendingCompletions = database.taskDao().countTaskCompletionsForSync().value ?: 0
            val pendingProducts = database.productDao().countProductsForSync().value ?: 0
            val pendingTemplates = database.labelTemplateDao().countTemplatesForSync().value ?: 0
            
            return@withContext pendingTasks > 0 || pendingCompletions > 0 || 
                    pendingProducts > 0 || pendingTemplates > 0
        }
    }
}

/**
 * Estados posibles de sincronización.
 */
sealed class SyncState {
    /**
     * Sin actividad de sincronización.
     */
    object Idle : SyncState()
    
    /**
     * Sincronización en curso.
     * 
     * @param step Paso actual de la sincronización.
     */
    data class Syncing(val step: SyncStep) : SyncState()
    
    /**
     * Sincronización exitosa.
     * 
     * @param timestamp Timestamp de la sincronización.
     */
    data class Success(val timestamp: Long) : SyncState()
    
    /**
     * Sincronización parcialmente exitosa.
     * 
     * @param timestamp Timestamp de la sincronización.
     * @param errorMessage Mensaje de error.
     */
    data class PartialSuccess(val timestamp: Long, val errorMessage: String) : SyncState()
    
    /**
     * Error en la sincronización.
     * 
     * @param message Mensaje de error.
     */
    data class Error(val message: String) : SyncState()
}

/**
 * Pasos de la sincronización.
 */
enum class SyncStep {
    STARTED,
    UPLOADING_CHANGES,
    DOWNLOADING_TASKS,
    DOWNLOADING_PRODUCTS,
    DOWNLOADING_TEMPLATES,
    COMPLETED
}