package com.productiva.android.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.productiva.android.dao.LabelTemplateDao
import com.productiva.android.dao.SavedPrinterDao
import com.productiva.android.dao.TaskCompletionDao
import com.productiva.android.dao.TaskDao
import com.productiva.android.dao.UserDao
import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.SavedPrinter
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.model.User
import com.productiva.android.utils.DateConverter

/**
 * Base de datos principal de la aplicación
 * Contiene todas las entidades y DAOs
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

    // DAOs
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun taskCompletionDao(): TaskCompletionDao
    abstract fun savedPrinterDao(): SavedPrinterDao
    abstract fun labelTemplateDao(): LabelTemplateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "productiva_database"
                )
                .fallbackToDestructiveMigration() // En producción deberíamos usar migraciones
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}