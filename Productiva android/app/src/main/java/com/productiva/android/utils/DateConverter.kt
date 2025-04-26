package com.productiva.android.utils

import androidx.room.TypeConverter
import java.util.Date

/**
 * Convertidor de tipos para Room para fechas
 */
class DateConverter {
    
    /**
     * Convierte un timestamp a Date
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    /**
     * Convierte un Date a timestamp
     */
    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }
}