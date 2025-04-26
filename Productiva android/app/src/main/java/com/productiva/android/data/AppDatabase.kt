package com.productiva.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.productiva.android.data.dao.LabelTemplateDao
import com.productiva.android.data.dao.SavedPrinterDao
import com.productiva.android.data.dao.TaskCompletionDao
import com.productiva.android.data.dao.TaskDao
import com.productiva.android.data.dao.UserDao
import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.SavedPrinter
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.model.User
import com.productiva.android.utils.DateConverter

/**
 * Base de datos Room para la aplicación
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
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * DAO para operaciones con usuarios
     */
    abstract fun userDao(): UserDao
    
    /**
     * DAO para operaciones con tareas
     */
    abstract fun taskDao(): TaskDao
    
    /**
     * DAO para operaciones con completados de tareas
     */
    abstract fun taskCompletionDao(): TaskCompletionDao
    
    /**
     * DAO para operaciones con impresoras guardadas
     */
    abstract fun savedPrinterDao(): SavedPrinterDao
    
    /**
     * DAO para operaciones con plantillas de etiquetas
     */
    abstract fun labelTemplateDao(): LabelTemplateDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Obtiene la instancia de la base de datos (singleton)
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "productiva_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                
                instance
            }
        }
    }
}