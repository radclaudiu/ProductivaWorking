package com.productiva.android.data.converter

import androidx.room.TypeConverter
import java.util.Date

/**
 * Conversor para almacenar y recuperar objetos Date en la base de datos Room.
 * Convierte entre objetos Date y valores Long (timestamp en milisegundos).
 */
class DateConverter {
    
    /**
     * Convierte un timestamp (Long) a un objeto Date.
     *
     * @param value Timestamp en milisegundos desde epoch.
     * @return Objeto Date correspondiente al timestamp o null si el valor es null.
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    /**
     * Convierte un objeto Date a un timestamp (Long).
     *
     * @param date Objeto Date a convertir.
     * @return Timestamp en milisegundos desde epoch o null si el objeto Date es null.
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}