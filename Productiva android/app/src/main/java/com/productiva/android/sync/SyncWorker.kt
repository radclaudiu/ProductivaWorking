package com.productiva.android.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.productiva.android.ProductivaApplication
import com.productiva.android.data.model.SyncState
import com.productiva.android.repository.ResourceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

/**
 * Trabajador de sincronización en segundo plano implementado con WorkManager.
 * Se ejecuta periódicamente para sincronizar datos entre la aplicación local y el servidor.
 */
class SyncWorker(
    context: Context, 
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    private val TAG = "SyncWorker"
    
    // Estado observable para monitorear el progreso de la sincronización
    companion object {
        val syncProgress = MutableStateFlow<ResourceState<SyncState>>(ResourceState.Idle())
    }
    
    /**
     * Método principal que ejecuta el trabajo de sincronización.
     */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Iniciando trabajo de sincronización en segundo plano")
        syncProgress.value = ResourceState.Loading()
        
        try {
            // Obtener instancia de la aplicación para acceder a las dependencias
            val application = applicationContext as ProductivaApplication
            val syncManager = application.syncManager
            val networkStatusManager = application.networkStatusManager
            
            // Verificar conectividad
            if (!networkStatusManager.isNetworkAvailable()) {
                Log.d(TAG, "No hay conexión a Internet, programando reintento")
                syncProgress.value = ResourceState.Error("No hay conexión a Internet")
                return@withContext Result.retry()
            }
            
            // Ejecutar sincronización
            syncManager.syncAll()
            
            // Monitorear el resultado de la sincronización
            val result = monitorSyncResult(syncManager)
            
            // Actualizar estado final
            return@withContext if (result) {
                Log.d(TAG, "Sincronización en segundo plano completada con éxito")
                syncProgress.value = ResourceState.Success(SyncState(true, System.currentTimeMillis()))
                Result.success()
            } else {
                Log.w(TAG, "Sincronización en segundo plano completada con errores")
                syncProgress.value = ResourceState.Error("Sincronización completada con errores")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la sincronización en segundo plano: ${e.message}", e)
            syncProgress.value = ResourceState.Error("Error: ${e.message}")
            return@withContext Result.failure()
        }
    }
    
    /**
     * Monitorea el resultado de la sincronización del SyncManager.
     */
    private suspend fun monitorSyncResult(syncManager: SyncManager): Boolean {
        // En una implementación completa, este método monitorearía el LiveData del SyncManager
        // y esperaría hasta que la sincronización se complete
        // Por simplicidad, simplemente esperamos 5 segundos para simular el monitoreo
        kotlinx.coroutines.delay(5000)
        
        // En una implementación real, verificaríamos el estado final de la sincronización
        // y retornaríamos true/false según el resultado
        return true
    }
}