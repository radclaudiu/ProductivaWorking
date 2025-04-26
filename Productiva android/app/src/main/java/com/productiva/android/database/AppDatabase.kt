package com.productiva.android.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.productiva.android.database.converters.DateConverter
import com.productiva.android.database.converters.StringListConverter
import com.productiva.android.database.dao.LabelTemplateDao
import com.productiva.android.database.dao.PrinterDao
import com.productiva.android.database.dao.TaskCompletionDao
import com.productiva.android.database.dao.TaskDao
import com.productiva.android.database.dao.UserDao
import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.SavedPrinter
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.model.User

/**
 * Configuración principal de la base de datos Room
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
    exportSchema = true
)
@TypeConverters(
    DateConverter::class,
    StringListConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * DAO para operaciones de usuarios
     */
    abstract fun userDao(): UserDao
    
    /**
     * DAO para operaciones de tareas
     */
    abstract fun taskDao(): TaskDao
    
    /**
     * DAO para operaciones de completaciones de tareas
     */
    abstract fun taskCompletionDao(): TaskCompletionDao
    
    /**
     * DAO para operaciones de impresoras guardadas
     */
    abstract fun printerDao(): PrinterDao
    
    /**
     * DAO para operaciones de plantillas de etiquetas
     */
    abstract fun labelTemplateDao(): LabelTemplateDao
}