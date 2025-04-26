package com.productiva.android.data.converter

import androidx.room.TypeConverter
import java.util.Date

/**
 * Conversor para el tipo Date de Java, que permite almacenar fechas en Room.
 * Convierte entre Date y Long para persistencia en la base de datos.
 */
class DateConverter {
    /**
     * Convierte una fecha (Date) a un valor Long para almacenamiento.
     *
     * @param date Fecha a convertir.
     * @return Valor en milisegundos (epoch time) o null si la fecha es null.
     */
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
    
    /**
     * Convierte un valor Long a una fecha (Date).
     *
     * @param value Valor en milisegundos (epoch time).
     * @return Objeto Date o null si el valor es null.
     */
    @TypeConverter
    fun toDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }
}