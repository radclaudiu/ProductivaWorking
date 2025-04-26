package com.productiva.android.sync

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.util.Log
import com.productiva.android.ProductivaApplication
import com.productiva.android.session.SessionManager
import com.productiva.android.utils.ConnectivityMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Servicio de trabajos programados para la sincronización en segundo plano.
 * Se ejecuta periódicamente para mantener los datos actualizados, incluso cuando la aplicación
 * no está en primer plano.
 */
class SyncJobService : JobService() {
    
    private val TAG = "SyncJobService"
    
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    
    private lateinit var syncManager: SyncManager
    private lateinit var sessionManager: SessionManager
    private lateinit var connectivityMonitor: ConnectivityMonitor
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Servicio de sincronización creado")
        
        // Inicialización
        syncManager = SyncManager.getInstance(this)
        sessionManager = SessionManager.getInstance()
        connectivityMonitor = ConnectivityMonitor.getInstance(this)
    }
    
    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Iniciando trabajo de sincronización en segundo plano")
        
        // Verificar si hay conexión y usuario activo antes de sincronizar
        if (!connectivityMonitor.isNetworkAvailable()) {
            Log.d(TAG, "Sin conexión a Internet, postergando sincronización")
            jobFinished(params, true) // Pedir reintento
            return true
        }
        
        if (!sessionManager.isLoggedIn()) {
            Log.d(TAG, "Usuario no autenticado, cancelando sincronización")
            jobFinished(params, false) // No reintentar, no hay usuario
            return false
        }
        
        // Iniciar sincronización asíncrona
        serviceScope.launch {
            try {
                // Obtener datos del usuario y empresa para la sincronización
                val currentUser = sessionManager.getCurrentUser()
                val userId = currentUser?.id
                val companyId = currentUser?.companyId
                
                // Realizar sincronización completa
                Log.d(TAG, "Ejecutando sincronización programada para usuario $userId, empresa $companyId")
                syncManager.syncAll(companyId, userId)
                
                // Esperar a que termine la sincronización
                while (syncManager.isSyncing()) {
                    delay(500)
                }
                
                Log.d(TAG, "Sincronización programada completada con éxito")
            } catch (e: Exception) {
                Log.e(TAG, "Error durante la sincronización programada", e)
            } finally {
                // Indicar finalización del trabajo
                jobFinished(params, false)
            }
        }
        
        // Devolver true para indicar que el trabajo sigue en progreso
        return true
    }
    
    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Deteniendo trabajo de sincronización en segundo plano")
        
        // Cancelar trabajos en progreso
        serviceJob.cancel("Servicio de sincronización detenido")
        
        // Devolver true para indicar que el trabajo debería reintentarse
        return true
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Servicio de sincronización destruido")
        
        // Limpiar recursos
        serviceScope.cancel()
    }
    
    companion object {
        const val JOB_ID = 1000
        
        /**
         * Programa un trabajo de sincronización periódica.
         */
        fun scheduleSync(context: Context) {
            SyncWorker.enqueuePeriodic(context)
        }
        
        /**
         * Cancela el trabajo de sincronización programado.
         */
        fun cancelSync(context: Context) {
            SyncWorker.cancel(context)
        }
        
        /**
         * Ejecuta una sincronización inmediata.
         */
        fun syncNow(context: Context) {
            val app = context.applicationContext as ProductivaApplication
            val syncManager = SyncManager.getInstance(context)
            val sessionManager = SessionManager.getInstance()
            
            // Solo sincronizar si hay un usuario autenticado
            if (sessionManager.isLoggedIn()) {
                val currentUser = sessionManager.getCurrentUser()
                val userId = currentUser?.id
                val companyId = currentUser?.companyId
                
                // Ejecutar sincronización completa
                syncManager.syncAll(companyId, userId)
            }
        }
    }
}