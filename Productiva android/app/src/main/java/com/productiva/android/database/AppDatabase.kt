package com.productiva.android.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.productiva.android.database.dao.LabelTemplateDao
import com.productiva.android.database.dao.ProductDao
import com.productiva.android.database.dao.TaskDao
import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.Product
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.model.User
import com.productiva.android.utils.DateTimeConverter
import com.productiva.android.utils.ListTypeConverter

/**
 * Base de datos Room para almacenar los datos de la aplicación localmente.
 * Proporciona acceso a los DAOs para cada tipo de entidad.
 */
@Database(
    entities = [
        User::class,
        Task::class,
        TaskCompletion::class,
        LabelTemplate::class,
        Product::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateTimeConverter::class, ListTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * DAO para operaciones con tareas.
     */
    abstract fun taskDao(): TaskDao
    
    /**
     * DAO para operaciones con plantillas de etiquetas.
     */
    abstract fun labelTemplateDao(): LabelTemplateDao
    
    /**
     * DAO para operaciones con productos.
     */
    abstract fun productDao(): ProductDao
    
    companion object {
        // Singleton para evitar múltiples instancias de la base de datos
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Obtiene la instancia de la base de datos, creándola si no existe.
         * 
         * @param context Contexto de la aplicación.
         * @return Instancia de AppDatabase.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "productiva_database"
                )
                .fallbackToDestructiveMigration() // Recrear tablas si la versión cambia
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}