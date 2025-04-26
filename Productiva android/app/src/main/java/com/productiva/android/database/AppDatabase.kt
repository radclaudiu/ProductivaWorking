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
import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.Product
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion

/**
 * Base de datos principal de la aplicación usando Room.
 * Centraliza el acceso a los datos locales y proporciona DAOs para interactuar con ellos.
 */
@Database(
    entities = [
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
    
    // DAOs
    abstract fun taskDao(): TaskDao
    abstract fun taskCompletionDao(): TaskCompletionDao
    abstract fun productDao(): ProductDao
    abstract fun labelTemplateDao(): LabelTemplateDao
    
    companion object {
        private const val DATABASE_NAME = "productiva.db"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Obtiene la instancia de la base de datos, creándola si es necesario.
         * Implementa el patrón Singleton para garantizar una única instancia de la base de datos.
         *
         * @param context Contexto de la aplicación.
         * @return Instancia de la base de datos.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // En desarrollo, recrear en caso de cambio de esquema
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}