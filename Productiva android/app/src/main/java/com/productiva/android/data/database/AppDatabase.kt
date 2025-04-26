package com.productiva.android.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.productiva.android.data.converters.DateConverter
import com.productiva.android.data.converters.ListConverter
import com.productiva.android.data.converters.MapConverter
import com.productiva.android.data.dao.*
import com.productiva.android.data.model.*

/**
 * Base de datos Room para la aplicación Productiva.
 * Contiene todas las entidades y DAOs para el almacenamiento local.
 */
@Database(
    entities = [
        User::class,
        Company::class,
        Employee::class,
        Task::class,
        TaskAssignment::class,
        Product::class,
        Checkpoint::class,
        CheckpointRecord::class,
        LabelTemplate::class,
        // Nuevas entidades para arqueo de caja
        CashRegister::class,
        CashRegisterSummary::class,
        CashRegisterToken::class,
        // Entidades para sincronización pendiente
        SyncPendingOperation::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class, ListConverter::class, MapConverter::class)
abstract class AppDatabase : RoomDatabase() {

    // Definir todos los DAOs
    abstract fun userDao(): UserDao
    abstract fun companyDao(): CompanyDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun taskDao(): TaskDao
    abstract fun taskAssignmentDao(): TaskAssignmentDao
    abstract fun productDao(): ProductDao
    abstract fun checkpointDao(): CheckpointDao
    abstract fun checkpointRecordDao(): CheckpointRecordDao
    abstract fun labelTemplateDao(): LabelTemplateDao
    abstract fun cashRegisterDao(): CashRegisterDao
    abstract fun cashRegisterSummaryDao(): CashRegisterSummaryDao
    abstract fun cashRegisterTokenDao(): CashRegisterTokenDao
    abstract fun syncPendingOperationDao(): SyncPendingOperationDao

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
                    .fallbackToDestructiveMigration() // Solo en desarrollo
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}