package com.productiva.android.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.productiva.android.ProductivaApplication
import com.productiva.android.repository.ResourceState
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect

/**
 * Worker para ejecutar la sincronización en segundo plano.
 * Este worker es programado por WorkManager para ejecutarse periódicamente.
 */
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    
    companion object {
        private const val TAG = "SyncWorker"
    }
    
    /**
     * Ejecuta la sincronización en segundo plano.
     */
    override suspend fun doWork(): Result {
        Log.d(TAG, "Iniciando sincronización programada")
        
        // Notificar inicio de sincronización
        val notificationHelper = SyncNotificationHelper(applicationContext)
        notificationHelper.showSyncInProgressNotification()
        
        try {
            // Obtener instancia del SyncManager
            val app = applicationContext as ProductivaApplication
            val syncManager = app.getSyncManager()
            
            // Ejecutar sincronización
            var success = true
            var errorMessage = ""
            
            syncManager.syncNow()
                .catch { exception ->
                    Log.e(TAG, "Error durante la sincronización", exception)
                    success = false
                    errorMessage = exception.message ?: "Error desconocido"
                }
                .collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            val result = state.data
                            Log.d(TAG, "Sincronización completada: ${result.totalItemsSynced} elementos sincronizados, ${result.totalErrors} errores")
                            
                            if (result.totalErrors > 0) {
                                // Hay algunos errores, pero se completó parcialmente
                                success = false
                                errorMessage = "Sincronización parcial: ${result.totalErrors} errores"
                            }
                        }
                        is ResourceState.Error -> {
                            Log.e(TAG, "Error en la sincronización: ${state.message}")
                            success = false
                            errorMessage = state.message
                        }
                        else -> {
                            // Ignorar otros estados
                        }
                    }
                }
            
            // Mostrar notificación de resultado
            if (success) {
                notificationHelper.showSyncCompletedNotification()
                return Result.success()
            } else {
                notificationHelper.showSyncErrorNotification(errorMessage)
                // Se considera un éxito parcial para que el WorkManager no lo reprograme inmediatamente
                return Result.success()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error no manejado durante la sincronización", e)
            notificationHelper.showSyncErrorNotification(e.message ?: "Error desconocido")
            return Result.failure()
        }
    }
}