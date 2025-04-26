package com.productiva.android.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.productiva.android.data.converter.DateConverter
import com.productiva.android.data.converter.ListConverter
import com.productiva.android.data.converter.MapConverter
import com.productiva.android.data.dao.CheckpointDao
import com.productiva.android.data.dao.LabelTemplateDao
import com.productiva.android.data.dao.ProductDao
import com.productiva.android.data.dao.TaskCompletionDao
import com.productiva.android.data.dao.TaskDao
import com.productiva.android.data.model.CheckpointData
import com.productiva.android.data.model.LabelTemplate
import com.productiva.android.data.model.Product
import com.productiva.android.data.model.Task
import com.productiva.android.data.model.TaskCompletion
import kotlinx.coroutines.CoroutineScope

/**
 * Clase de base de datos para Room que define las entidades y DAOs.
 */
@Database(
    entities = [
        Task::class,
        TaskCompletion::class,
        Product::class,
        LabelTemplate::class,
        CheckpointData::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class, ListConverter::class, MapConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * DAO para operaciones con tareas.
     */
    abstract fun taskDao(): TaskDao
    
    /**
     * DAO para operaciones con completados de tareas.
     */
    abstract fun taskCompletionDao(): TaskCompletionDao
    
    /**
     * DAO para operaciones con productos.
     */
    abstract fun productDao(): ProductDao
    
    /**
     * DAO para operaciones con plantillas de etiquetas.
     */
    abstract fun labelTemplateDao(): LabelTemplateDao
    
    /**
     * DAO para operaciones con fichajes.
     */
    abstract fun checkpointDao(): CheckpointDao
    
    companion object {
        private const val DATABASE_NAME = "productiva_database"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Obtiene la instancia única de la base de datos.
         *
         * @param context Contexto de la aplicación.
         * @param scope Ámbito de corrutina para operaciones asíncronas.
         * @return Instancia de la base de datos.
         */
        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // Para desarrollo, en producción usar migraciones
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}