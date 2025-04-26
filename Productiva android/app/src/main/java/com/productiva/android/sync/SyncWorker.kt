package com.productiva.android.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.productiva.android.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker para ejecutar sincronizaciones periódicas en segundo plano.
 * Se encarga de verificar si hay conectividad y si el usuario está autenticado
 * antes de iniciar la sincronización.
 */
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    
    companion object {
        private const val TAG = "SyncWorker"
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando trabajo de sincronización programada")
            
            // Verificar si hay sesión activa
            val sessionManager = SessionManager.getInstance()
            if (!sessionManager.isLoggedIn()) {
                Log.d(TAG, "No hay sesión activa, omitiendo sincronización")
                return@withContext Result.success()
            }
            
            // Obtener SyncManager
            val syncManager = SyncManager.getInstance(applicationContext)
            
            // Verificar si hay datos pendientes de sincronización
            if (syncManager.hasPendingSync()) {
                Log.d(TAG, "Hay datos pendientes de sincronización, iniciando sincronización...")
                
                // Ejecutar sincronización
                val result = syncManager.syncAll(forceRefresh = false)
                
                return@withContext if (result is SyncResult.Success) {
                    Log.d(TAG, "Sincronización completada con éxito: ${result.addedCount} añadidos, ${result.updatedCount} actualizados, ${result.deletedCount} eliminados")
                    Result.success()
                } else {
                    val errorMsg = (result as SyncResult.Error).message
                    Log.e(TAG, "Error en sincronización: $errorMsg")
                    Result.retry()
                }
            } else {
                Log.d(TAG, "No hay datos pendientes de sincronización")
                return@withContext Result.success()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error en trabajo de sincronización", e)
            Result.retry()
        }
    }
}