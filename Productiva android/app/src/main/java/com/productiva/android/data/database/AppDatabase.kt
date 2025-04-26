package com.productiva.android.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de datos principal de la aplicación utilizando Room.
 * Contiene todas las entidades y DAOs necesarios para el funcionamiento offline.
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
     * DAO para operaciones con completado de tareas.
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
     * DAO para operaciones con registros de fichaje.
     */
    abstract fun checkpointDao(): CheckpointDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Obtiene una instancia de la base de datos, creándola si no existe.
         * Utiliza el patrón Singleton para garantizar una única instancia.
         *
         * @param context Contexto de la aplicación.
         * @param scope Scope de corrutina para operaciones de inicialización.
         * @return Instancia de la base de datos.
         */
        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "productiva_database"
                )
                .fallbackToDestructiveMigration() // En caso de cambio de versión, recrear la base de datos
                .addCallback(AppDatabaseCallback(scope))
                .build()
                
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Callback para operaciones a realizar en la creación o apertura de la base de datos.
         */
        private class AppDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
            
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        // Aquí podríamos prepopular la base de datos si fuera necesario
                    }
                }
            }
            
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                
                // Activar foreign keys en SQLite
                db.execSQL("PRAGMA foreign_keys = ON")
            }
        }
    }
}