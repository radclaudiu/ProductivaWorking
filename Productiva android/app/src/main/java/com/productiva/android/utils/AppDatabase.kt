package com.productiva.android.utils

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.productiva.android.data.dao.TaskDao
import com.productiva.android.data.dao.UserDao
import com.productiva.android.model.Company
import com.productiva.android.model.Location
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.model.User

/**
 * Base de datos Room para la aplicación
 * Define todas las entidades y DAOs
 */
@Database(
    entities = [
        User::class,
        Task::class,
        TaskCompletion::class,
        Location::class,
        Company::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Obtiene una instancia de la base de datos, creándola si no existe
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "productiva_database"
                )
                .fallbackToDestructiveMigration() // En una app de producción, usar migrations adecuadas
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}