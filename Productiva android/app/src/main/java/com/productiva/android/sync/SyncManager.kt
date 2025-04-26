package com.productiva.android.sync

import android.content.Context
import android.util.Log
import com.productiva.android.ProductivaApplication
import com.productiva.android.network.RetrofitClient
import com.productiva.android.repository.LabelTemplateRepository
import com.productiva.android.repository.ProductRepository
import com.productiva.android.repository.ResourceState
import com.productiva.android.repository.TaskRepository
import com.productiva.android.utils.ConnectivityMonitor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Gestor centralizado de sincronización entre la base de datos local y el servidor.
 * Coordina la sincronización de diferentes tipos de datos y gestiona los reintentos
 * cuando se recupera la conectividad.
 */
class SyncManager private constructor(private val context: Context) {
    
    private val TAG = "SyncManager"
    
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    
    private val isSyncing = AtomicBoolean(false)
    private val connectivityMonitor = ConnectivityMonitor.getInstance(context)
    
    private val app = context.applicationContext as ProductivaApplication
    private val apiService = RetrofitClient.getApiService(app)
    
    // Repositorios
    private val taskRepository = TaskRepository(app.database.taskDao(), apiService)
    private val productRepository = ProductRepository(app.database.productDao(), apiService)
    private val labelTemplateRepository = LabelTemplateRepository(app.database.labelTemplateDao(), apiService)
    
    /**
     * Inicia una sincronización completa de todos los datos.
     */
    fun syncAll(companyId: Int? = null, userId: Int? = null) {
        if (isSyncing.getAndSet(true)) {
            Log.d(TAG, "Ya hay una sincronización en progreso, ignorando solicitud")
            return
        }
        
        Log.d(TAG, "Iniciando sincronización completa...")
        
        scope.launch {
            try {
                // Sincronizar tareas primero (incluye completados pendientes)
                taskRepository.syncTasks(userId).collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            Log.d(TAG, "Sincronización de tareas completada: ${state.data?.size} tareas")
                        }
                        is ResourceState.Error -> {
                            Log.e(TAG, "Error en sincronización de tareas: ${state.message}")
                        }
                        else -> {}
                    }
                }
                
                // Sincronizar productos
                productRepository.syncProducts(companyId = companyId).collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            Log.d(TAG, "Sincronización de productos completada: ${state.data?.size} productos")
                        }
                        is ResourceState.Error -> {
                            Log.e(TAG, "Error en sincronización de productos: ${state.message}")
                        }
                        else -> {}
                    }
                }
                
                // Sincronizar plantillas de etiquetas
                labelTemplateRepository.syncLabelTemplates().collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            Log.d(TAG, "Sincronización de plantillas completada: ${state.data?.size} plantillas")
                        }
                        is ResourceState.Error -> {
                            Log.e(TAG, "Error en sincronización de plantillas: ${state.message}")
                        }
                        else -> {}
                    }
                }
                
                Log.d(TAG, "Sincronización completa finalizada con éxito")
            } catch (e: CancellationException) {
                Log.w(TAG, "Sincronización cancelada")
            } catch (e: Exception) {
                Log.e(TAG, "Error durante la sincronización completa", e)
            } finally {
                isSyncing.set(false)
            }
        }
    }
    
    /**
     * Sincroniza sólo las tareas.
     */
    fun syncTasks(userId: Int? = null) {
        if (isSyncing.get()) {
            Log.d(TAG, "Ya hay una sincronización en progreso, programando tareas para después")
            return
        }
        
        Log.d(TAG, "Iniciando sincronización de tareas...")
        
        scope.launch {
            try {
                isSyncing.set(true)
                
                taskRepository.syncTasks(userId).collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            Log.d(TAG, "Sincronización de tareas completada: ${state.data?.size} tareas")
                        }
                        is ResourceState.Error -> {
                            Log.e(TAG, "Error en sincronización de tareas: ${state.message}")
                        }
                        else -> {}
                    }
                }
            } catch (e: CancellationException) {
                Log.w(TAG, "Sincronización de tareas cancelada")
            } catch (e: Exception) {
                Log.e(TAG, "Error durante la sincronización de tareas", e)
            } finally {
                isSyncing.set(false)
            }
        }
    }
    
    /**
     * Sincroniza sólo los productos.
     */
    fun syncProducts(companyId: Int? = null, locationId: Int? = null) {
        if (isSyncing.get()) {
            Log.d(TAG, "Ya hay una sincronización en progreso, programando productos para después")
            return
        }
        
        Log.d(TAG, "Iniciando sincronización de productos...")
        
        scope.launch {
            try {
                isSyncing.set(true)
                
                productRepository.syncProducts(locationId, companyId).collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            Log.d(TAG, "Sincronización de productos completada: ${state.data?.size} productos")
                        }
                        is ResourceState.Error -> {
                            Log.e(TAG, "Error en sincronización de productos: ${state.message}")
                        }
                        else -> {}
                    }
                }
            } catch (e: CancellationException) {
                Log.w(TAG, "Sincronización de productos cancelada")
            } catch (e: Exception) {
                Log.e(TAG, "Error durante la sincronización de productos", e)
            } finally {
                isSyncing.set(false)
            }
        }
    }
    
    /**
     * Sincroniza sólo las plantillas de etiquetas.
     */
    fun syncLabelTemplates() {
        if (isSyncing.get()) {
            Log.d(TAG, "Ya hay una sincronización en progreso, programando plantillas para después")
            return
        }
        
        Log.d(TAG, "Iniciando sincronización de plantillas...")
        
        scope.launch {
            try {
                isSyncing.set(true)
                
                labelTemplateRepository.syncLabelTemplates().collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            Log.d(TAG, "Sincronización de plantillas completada: ${state.data?.size} plantillas")
                        }
                        is ResourceState.Error -> {
                            Log.e(TAG, "Error en sincronización de plantillas: ${state.message}")
                        }
                        else -> {}
                    }
                }
            } catch (e: CancellationException) {
                Log.w(TAG, "Sincronización de plantillas cancelada")
            } catch (e: Exception) {
                Log.e(TAG, "Error durante la sincronización de plantillas", e)
            } finally {
                isSyncing.set(false)
            }
        }
    }
    
    /**
     * Verifica si actualmente hay una sincronización en progreso.
     */
    fun isSyncing(): Boolean {
        return isSyncing.get()
    }
    
    /**
     * Configura un listener para la conectividad y sincroniza automáticamente
     * cuando se recupera la conexión.
     */
    fun setupConnectivityListener() {
        scope.launch {
            connectivityMonitor.networkStatus.collect { isConnected ->
                if (isConnected && !isSyncing.get()) {
                    Log.d(TAG, "Conectividad recuperada, iniciando sincronización automática")
                    syncAll()
                }
            }
        }
    }
    
    /**
     * Limpia los recursos al destruir el manager.
     */
    fun destroy() {
        job.cancel()
    }
    
    companion object {
        @Volatile
        private var instance: SyncManager? = null
        
        fun getInstance(context: Context): SyncManager {
            return instance ?: synchronized(this) {
                instance ?: SyncManager(context.applicationContext).also { instance = it }
            }
        }
    }
}