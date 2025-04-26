package com.productiva.android.sync

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Servicio de sincronización en segundo plano que se ejecuta periódicamente.
 * Utiliza JobScheduler para planificar sincronizaciones periódicas con el servidor.
 */
class SyncJobService : JobService() {
    
    private val TAG = "SyncJobService"
    
    // Ámbito de corrutina para la sincronización
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Gestor de sincronización
    private lateinit var syncManager: SyncManager
    
    // Tarea actual
    private var currentJob: JobParameters? = null
    
    companion object {
        // ID del trabajo de sincronización
        private const val JOB_ID = 1000
        
        // Intervalo mínimo entre sincronizaciones (30 minutos)
        private val SYNC_INTERVAL = TimeUnit.MINUTES.toMillis(30)
        
        // Flexibilidad del intervalo (5 minutos)
        private val SYNC_FLEX_TIME = TimeUnit.MINUTES.toMillis(5)
        
        /**
         * Programa la sincronización periódica.
         * 
         * @param context Contexto de la aplicación.
         */
        fun scheduleSyncJob(context: Context) {
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            
            // Verificar si el trabajo ya está programado
            val pendingJobs = jobScheduler.allPendingJobs
            if (pendingJobs.any { it.id == JOB_ID }) {
                Log.d("SyncJobService", "Trabajo de sincronización ya programado")
                return
            }
            
            val componentName = ComponentName(context, SyncJobService::class.java)
            
            val jobInfo = JobInfo.Builder(JOB_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // Cualquier tipo de red
                .setPersisted(true) // Persistir después de reinicios
                .setPeriodic(SYNC_INTERVAL, SYNC_FLEX_TIME) // Sincronización periódica
                .build()
            
            val result = jobScheduler.schedule(jobInfo)
            if (result == JobScheduler.RESULT_SUCCESS) {
                Log.d("SyncJobService", "Sincronización programada correctamente")
            } else {
                Log.e("SyncJobService", "Error al programar sincronización")
            }
        }
        
        /**
         * Programa una sincronización inmediata.
         * 
         * @param context Contexto de la aplicación.
         */
        fun scheduleImmediateSync(context: Context) {
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            
            val componentName = ComponentName(context, SyncJobService::class.java)
            
            val jobInfo = JobInfo.Builder(JOB_ID + 1, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // Cualquier tipo de red
                .setMinimumLatency(0) // Ejecutar lo antes posible
                .setOverrideDeadline(TimeUnit.SECONDS.toMillis(5)) // Plazo máximo
                .setBackoffCriteria(
                    TimeUnit.MINUTES.toMillis(5),
                    JobInfo.BACKOFF_POLICY_LINEAR
                ) // Criterio de reintento
                .build()
            
            val result = jobScheduler.schedule(jobInfo)
            if (result == JobScheduler.RESULT_SUCCESS) {
                Log.d("SyncJobService", "Sincronización inmediata programada correctamente")
            } else {
                Log.e("SyncJobService", "Error al programar sincronización inmediata")
            }
        }
        
        /**
         * Cancela todas las sincronizaciones programadas.
         * 
         * @param context Contexto de la aplicación.
         */
        fun cancelAllSyncJobs(context: Context) {
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.cancel(JOB_ID)
            jobScheduler.cancel(JOB_ID + 1)
            Log.d("SyncJobService", "Sincronizaciones canceladas")
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        syncManager = SyncManager(applicationContext)
        Log.d(TAG, "Servicio de sincronización creado")
    }
    
    /**
     * Se llama cuando el sistema determina que es momento de ejecutar el trabajo.
     * 
     * @param params Parámetros del trabajo.
     * @return true si el trabajo debe continuar en segundo plano, false si ha terminado.
     */
    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Iniciando trabajo de sincronización")
        
        currentJob = params
        
        // Iniciar sincronización en una corrutina
        serviceScope.launch {
            try {
                // Realizar sincronización
                syncManager.syncAll()
                
                // Notificar al sistema que el trabajo ha terminado
                jobFinished(params, false)
                Log.d(TAG, "Trabajo de sincronización completado")
            } catch (e: Exception) {
                Log.e(TAG, "Error en trabajo de sincronización", e)
                
                // Notificar al sistema que el trabajo debe volver a intentarse
                jobFinished(params, true)
            }
        }
        
        // Devolver true para indicar que el trabajo continuará en segundo plano
        return true
    }
    
    /**
     * Se llama cuando el sistema necesita detener el trabajo antes de que termine.
     * 
     * @param params Parámetros del trabajo.
     * @return true si el trabajo debe volver a intentarse, false en caso contrario.
     */
    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Trabajo de sincronización detenido")
        
        // Devolver true para indicar que el trabajo debe volver a intentarse
        return true
    }
}