package com.productiva.android.utils

import androidx.room.TypeConverter
import java.util.Date

/**
 * Clase conversor para tipos de datos complejos en Room
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}