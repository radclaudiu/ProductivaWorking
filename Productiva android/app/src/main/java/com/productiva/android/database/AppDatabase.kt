package com.productiva.android.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.SavedPrinter
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.model.User

/**
 * Base de datos principal de la aplicación
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
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * DAO para usuarios
     */
    abstract fun userDao(): UserDao
    
    /**
     * DAO para tareas
     */
    abstract fun taskDao(): TaskDao
    
    /**
     * DAO para completaciones de tareas
     */
    abstract fun taskCompletionDao(): TaskCompletionDao
    
    /**
     * DAO para impresoras guardadas
     */
    abstract fun savedPrinterDao(): SavedPrinterDao
    
    /**
     * DAO para plantillas de etiquetas
     */
    abstract fun labelTemplateDao(): LabelTemplateDao
    
    companion object {
        private const val DATABASE_NAME = "productiva_database"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Obtiene una instancia de la base de datos
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}