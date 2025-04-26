package com.productiva.android.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.productiva.android.session.SessionManager
import java.util.concurrent.TimeUnit

/**
 * Worker para la sincronización periódica de datos usando WorkManager.
 * Proporciona una implementación más moderna y robusta que JobScheduler.
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val TAG = "SyncWorker"
    
    override suspend fun doWork(): Result {
        Log.d(TAG, "Iniciando worker de sincronización")
        
        // Verificar si hay usuario autenticado
        val sessionManager = SessionManager.getInstance()
        if (!sessionManager.isLoggedIn()) {
            Log.d(TAG, "No hay usuario autenticado, cancelando sincronización")
            return Result.failure()
        }
        
        try {
            // Obtener datos del usuario y empresa
            val currentUser = sessionManager.getCurrentUser()
            val userId = currentUser?.id
            val companyId = currentUser?.companyId
            
            // Realizar sincronización
            Log.d(TAG, "Ejecutando sincronización periódica")
            val syncManager = SyncManager.getInstance(applicationContext)
            syncManager.syncAll(companyId, userId)
            
            // Esperar a que termine la sincronización
            while (syncManager.isSyncing()) {
                kotlinx.coroutines.delay(500)
            }
            
            Log.d(TAG, "Sincronización periódica completada")
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la sincronización periódica", e)
            return if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    companion object {
        private const val SYNC_WORK_NAME = "com.productiva.android.PERIODIC_SYNC"
        private const val SYNC_INTERVAL_HOURS = 1L
        private const val MAX_RETRIES = 3
        
        /**
         * Programa una sincronización periódica usando WorkManager.
         */
        fun enqueuePeriodic(context: Context) {
            Log.d("SyncWorker", "Programando sincronización periódica")
            
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                SYNC_INTERVAL_HOURS, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                syncRequest
            )
        }
        
        /**
         * Cancela la sincronización periódica.
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
        }
    }
}