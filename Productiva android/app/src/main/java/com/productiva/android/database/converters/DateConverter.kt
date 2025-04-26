package com.productiva.android.database.converters

import androidx.room.TypeConverter
import java.util.Date

/**
 * Conversor para tipos Date en Room
 */
class DateConverter {
    
    /**
     * Convierte un timestamp (Long) a un objeto Date
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    /**
     * Convierte un objeto Date a un timestamp (Long)
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}