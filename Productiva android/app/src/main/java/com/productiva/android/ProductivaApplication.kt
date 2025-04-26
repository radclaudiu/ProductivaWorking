package com.productiva.android

import android.app.Application
import android.content.Context
import androidx.work.*
import com.productiva.android.data.database.AppDatabase
import com.productiva.android.network.NetworkStatusManager
import com.productiva.android.sync.SyncManager
import com.productiva.android.sync.SyncWorker
import com.productiva.android.utils.NotificationChannelHelper
import com.productiva.android.utils.SessionManager
import java.util.concurrent.TimeUnit

/**
 * Clase Application personalizada para Productiva
 * Inicializa componentes clave de la aplicación como la base de datos,
 * el administrador de sesiones y el trabajador de sincronización.
 */
class ProductivaApplication : Application() {

    // Acceso lazy a dependencias principales
    val database by lazy { AppDatabase.getDatabase(this) }
    val sessionManager by lazy { SessionManager(this) }
    val networkStatusManager by lazy { NetworkStatusManager(this) }
    val syncManager by lazy { SyncManager(this, database, sessionManager, networkStatusManager) }

    override fun onCreate() {
        super.onCreate()

        // Inicializar canales de notificación para Android 8.0+
        NotificationChannelHelper.createNotificationChannels(this)

        // Configurar la sincronización periódica en segundo plano
        setupPeriodicSync()
    }

    /**
     * Configura el trabajo periódico de sincronización usando WorkManager
     */
    private fun setupPeriodicSync() {
        // Restricciones para ejecutar el trabajo
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Configuración del trabajo periódico
        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES,  // Frecuencia mínima de 15 minutos
            5, TimeUnit.MINUTES    // Flexibilidad de 5 minutos
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES)
            .build()

        // Programar el trabajo con reemplazo si ya existe
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,  // Mantener el trabajo existente si ya está programado
            syncWorkRequest
        )
    }

    companion object {
        private const val SYNC_WORK_NAME = "productiva_periodic_sync"

        /**
         * Obtiene la instancia de ProductivaApplication desde el contexto
         */
        fun getInstance(context: Context): ProductivaApplication {
            return context.applicationContext as ProductivaApplication
        }
    }
}