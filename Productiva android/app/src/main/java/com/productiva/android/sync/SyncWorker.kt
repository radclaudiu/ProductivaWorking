package com.productiva.android.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Trabajador de sincronización para WorkManager.
 * Ejecuta la sincronización en segundo plano periódicamente.
 */
class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        private const val TAG = "SyncWorker"
    }
    
    /**
     * Método principal que ejecuta el trabajo de sincronización.
     * Se llama automáticamente por WorkManager según la programación.
     *
     * @return Resultado de la ejecución.
     */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Iniciando trabajo de sincronización programado")
        
        try {
            // Obtener instancia del gestor de sincronización
            val syncManager = SyncManager.getInstance(applicationContext)
            
            // Ejecutar sincronización
            val result = syncManager.performManualSync()
            
            // Evaluar resultado
            return@withContext when (result) {
                is SyncResult.Success -> {
                    Log.d(TAG, "Sincronización programada completada con éxito")
                    Log.d(TAG, "Tareas: ${result.taskChanges}")
                    Log.d(TAG, "Productos: ${result.productChanges}")
                    Log.d(TAG, "Plantillas: ${result.labelTemplateChanges}")
                    Log.d(TAG, "Fichajes: ${result.checkpointChanges}")
                    Result.success()
                }
                is SyncResult.Error -> {
                    Log.e(TAG, "Error en sincronización programada: ${result.message}")
                    
                    // Evaluar si debe reintentarse
                    if (result.message.contains("conexión") || 
                        result.message.contains("timeout") ||
                        result.message.contains("network") ||
                        result.message.contains("internet")) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en trabajo de sincronización", e)
            
            // Reintento solo para errores de red
            if (e.message?.contains("network") == true ||
                e.message?.contains("internet") == true ||
                e.message?.contains("conexión") == true ||
                e.message?.contains("timeout") == true) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}