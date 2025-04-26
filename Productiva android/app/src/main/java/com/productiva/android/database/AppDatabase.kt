package com.productiva.android.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.productiva.android.database.dao.*
import com.productiva.android.model.*
import com.productiva.android.utils.DateConverters

/**
 * Configuración principal de la base de datos Room para la aplicación.
 */
@Database(
    entities = [
        User::class,
        Task::class,
        TaskCompletion::class,
        SavedPrinter::class,
        LabelTemplate::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    
    // DAOs
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun taskCompletionDao(): TaskCompletionDao
    abstract fun printerDao(): PrinterDao
    abstract fun labelTemplateDao(): LabelTemplateDao
    
    companion object {
        private const val DATABASE_NAME = "productiva.db"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Obtiene una instancia de la base de datos.
         * Si la base de datos no existe, se crea una nueva.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration() // En caso de cambio de versión, se reconstruye la BD
                .build()
                
                INSTANCE = instance
                
                instance
            }
        }
    }
}