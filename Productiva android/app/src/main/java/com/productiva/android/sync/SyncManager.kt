package com.productiva.android.sync

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.PersistableBundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit

/**
 * Administrador de sincronización que coordina las operaciones de sincronización.
 */
class SyncManager(private val context: Context) {
    
    private val TAG = "SyncManager"
    
    // Estado actual de la sincronización
    private val _syncState = MutableLiveData<SyncState>()
    val syncState: LiveData<SyncState> = _syncState
    
    // Preferencias de sincronización
    private val syncPrefs = context.getSharedPreferences(SYNC_PREFERENCES, Context.MODE_PRIVATE)
    
    companion object {
        private const val SYNC_PREFERENCES = "sync_preferences"
        private const val LAST_SYNC_TIME = "last_sync_time"
        private const val SYNC_INTERVAL = "sync_interval_hours"
        private const val SYNC_JOB_ID = 1000
        
        private const val DEFAULT_SYNC_INTERVAL_HOURS = 2
        private const val MIN_SYNC_INTERVAL_HOURS = 1
        private const val MAX_SYNC_INTERVAL_HOURS = 24
    }
    
    init {
        _syncState.value = SyncState.Idle
    }
    
    /**
     * Estados posibles de sincronización.
     */
    sealed class SyncState {
        object Idle : SyncState()
        object Syncing : SyncState()
        data class Success(val timestamp: Long) : SyncState()
        data class Error(val message: String) : SyncState()
    }
    
    /**
     * Verifica si hay conexión a Internet disponible.
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        
        return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }
    
    /**
     * Programa sincronizaciones periódicas.
     */
    fun schedulePeriodicalSync() {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        
        // Obtener intervalo de sincronización
        val intervalHours = getSyncInterval()
        val intervalMillis = TimeUnit.HOURS.toMillis(intervalHours.toLong())
        
        // Configurar trabajo
        val componentName = ComponentName(context, SyncJobService::class.java)
        val jobInfo = JobInfo.Builder(SYNC_JOB_ID, componentName)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPersisted(true)
            .setPeriodic(intervalMillis)
            .build()
        
        // Programar trabajo
        val result = jobScheduler.schedule(jobInfo)
        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Sincronización periódica programada cada $intervalHours horas")
        } else {
            Log.e(TAG, "Error al programar sincronización periódica")
        }
    }
    
    /**
     * Cancela las sincronizaciones programadas.
     */
    fun cancelScheduledSync() {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.cancel(SYNC_JOB_ID)
        Log.d(TAG, "Sincronización periódica cancelada")
    }
    
    /**
     * Ejecuta una sincronización inmediata.
     */
    fun syncNow(): Flow<SyncState> = flow {
        if (!isNetworkAvailable()) {
            emit(SyncState.Error("No hay conexión a Internet disponible"))
            return@flow
        }
        
        emit(SyncState.Syncing)
        _syncState.postValue(SyncState.Syncing)
        
        try {
            // Crear trabajo de sincronización inmediata
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            val componentName = ComponentName(context, SyncJobService::class.java)
            
            val extras = PersistableBundle()
            extras.putBoolean("immediate", true)
            
            val jobInfo = JobInfo.Builder(SYNC_JOB_ID + 1, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setExtras(extras)
                .setOverrideDeadline(0) // Ejecutar inmediatamente
                .build()
            
            val result = jobScheduler.schedule(jobInfo)
            
            if (result == JobScheduler.RESULT_SUCCESS) {
                // Actualizar tiempo de última sincronización
                val timestamp = System.currentTimeMillis()
                updateLastSyncTime(timestamp)
                
                val state = SyncState.Success(timestamp)
                emit(state)
                _syncState.postValue(state)
            } else {
                val state = SyncState.Error("No se pudo iniciar la sincronización")
                emit(state)
                _syncState.postValue(state)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la sincronización", e)
            val state = SyncState.Error("Error: ${e.message}")
            emit(state)
            _syncState.postValue(state)
        }
    }
    
    /**
     * Obtiene la última fecha de sincronización.
     */
    fun getLastSyncTime(): Long {
        return syncPrefs.getLong(LAST_SYNC_TIME, 0)
    }
    
    /**
     * Actualiza la fecha de última sincronización.
     */
    fun updateLastSyncTime(timestamp: Long) {
        syncPrefs.edit().putLong(LAST_SYNC_TIME, timestamp).apply()
    }
    
    /**
     * Obtiene el intervalo de sincronización en horas.
     */
    fun getSyncInterval(): Int {
        return syncPrefs.getInt(SYNC_INTERVAL, DEFAULT_SYNC_INTERVAL_HOURS)
    }
    
    /**
     * Establece el intervalo de sincronización en horas.
     */
    fun setSyncInterval(hours: Int) {
        val validHours = hours.coerceIn(MIN_SYNC_INTERVAL_HOURS, MAX_SYNC_INTERVAL_HOURS)
        syncPrefs.edit().putInt(SYNC_INTERVAL, validHours).apply()
        
        // Reprogramar con el nuevo intervalo
        cancelScheduledSync()
        schedulePeriodicalSync()
    }
}