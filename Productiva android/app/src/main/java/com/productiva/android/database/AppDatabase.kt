package com.productiva.android.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.productiva.android.dao.LabelTemplateDao
import com.productiva.android.dao.ProductDao
import com.productiva.android.dao.TaskCompletionDao
import com.productiva.android.dao.TaskDao
import com.productiva.android.dao.UserDao
import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.Product
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.model.User
import java.util.concurrent.Executors

/**
 * Base de datos Room para la aplicación.
 * Contiene todas las entidades y DAOs.
 */
@Database(
    entities = [
        User::class,
        Task::class,
        TaskCompletion::class,
        Product::class,
        LabelTemplate::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    /**
     * DAO para usuarios.
     */
    abstract fun userDao(): UserDao
    
    /**
     * DAO para tareas.
     */
    abstract fun taskDao(): TaskDao
    
    /**
     * DAO para completados de tareas.
     */
    abstract fun taskCompletionDao(): TaskCompletionDao
    
    /**
     * DAO para productos.
     */
    abstract fun productDao(): ProductDao
    
    /**
     * DAO para plantillas de etiquetas.
     */
    abstract fun labelTemplateDao(): LabelTemplateDao
    
    companion object {
        private const val DATABASE_NAME = "productiva_db"
        
        @Volatile
        private var instance: AppDatabase? = null
        
        /**
         * Obtiene la instancia única de la base de datos.
         */
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }
        
        /**
         * Construye la base de datos con las opciones necesarias.
         */
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration() // Solo en desarrollo, reemplazar con migraciones reales en producción
                .setQueryExecutor(Executors.newFixedThreadPool(4)) // Ejecutor de consultas personalizado
                .build()
        }
    }
}