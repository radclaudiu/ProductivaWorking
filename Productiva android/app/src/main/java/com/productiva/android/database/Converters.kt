package com.productiva.android.database

import androidx.room.TypeConverter
import java.util.Date

/**
 * Convertidores para tipos de datos complejos en Room
 */
class Converters {
    
    /**
     * Convierte un timestamp Long a Date
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    /**
     * Convierte un Date a timestamp Long
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    /**
     * Convierte una lista de strings a un string separado por comas
     */
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(",")
    }
    
    /**
     * Convierte un string separado por comas a una lista de strings
     */
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split(",")?.filter { it.isNotEmpty() }
    }
}