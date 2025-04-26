package com.productiva.android.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker para ejecutar sincronizaciones periódicas en segundo plano.
 * Utiliza WorkManager para programar y ejecutar las sincronizaciones.
 */
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    
    companion object {
        private const val TAG = "SyncWorker"
    }
    
    /**
     * Ejecuta la tarea de sincronización en segundo plano.
     * Este método es llamado por WorkManager cuando es momento de ejecutar la tarea.
     *
     * @return Resultado de la ejecución.
     */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando sincronización periódica en segundo plano...")
            
            // Obtener la instancia del administrador de sincronización
            val syncManager = SyncManager.getInstance(applicationContext)
            
            // Configurar el helper de notificaciones para el proceso de sincronización
            val notificationHelper = SyncNotificationHelper(applicationContext)
            notificationHelper.showSyncProgressNotification("Sincronizando datos...", 0)
            
            // Ejecutar la sincronización
            // No forzamos una sincronización completa, solo sincronizamos los cambios desde la última vez
            syncManager.syncAll(forceFullSync = false, showNotification = true)
            
            Log.d(TAG, "Sincronización periódica completada con éxito")
            return@withContext Result.success()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la sincronización periódica", e)
            
            // Si hay un error mostrar notificación
            val notificationHelper = SyncNotificationHelper(applicationContext)
            notificationHelper.showSyncErrorNotification("Error: ${e.message}")
            
            // Si es un error de red o temporal, solicitar reintentar más tarde
            return@withContext if (e is java.net.UnknownHostException ||
                                   e is java.net.SocketTimeoutException ||
                                   e is java.io.IOException) {
                Result.retry()
            } else {
                // Para otros errores, reportar fallo
                Result.failure()
            }
        }
    }
}