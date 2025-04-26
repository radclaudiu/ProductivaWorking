package com.productiva.android.utils

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.productiva.android.models.Location
import com.productiva.android.models.Task
import com.productiva.android.models.TaskCompletion
import com.productiva.android.models.User

@Database(
    entities = [User::class, Task::class, TaskCompletion::class, Location::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun taskCompletionDao(): TaskCompletionDao
    abstract fun locationDao(): LocationDao
}