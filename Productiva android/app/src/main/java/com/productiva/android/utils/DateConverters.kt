package com.productiva.android.utils

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Clase auxiliar para convertir entre tipos de fecha y valores que Room puede almacenar en la base de datos.
 */
class DateConverters {
    
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    
    /**
     * Convierte una fecha a una cadena de texto.
     */
    @TypeConverter
    fun dateToString(date: Date?): String? {
        return date?.let { dateFormat.format(it) }
    }
    
    /**
     * Convierte una cadena de texto a una fecha.
     */
    @TypeConverter
    fun stringToDate(value: String?): Date? {
        return value?.let {
            try {
                dateFormat.parse(it)
            } catch (e: Exception) {
                try {
                    // Intenta con formato solo de fecha sin hora
                    dateOnlyFormat.parse(it)
                } catch (e2: Exception) {
                    null
                }
            }
        }
    }
    
    /**
     * Convierte un timestamp a una fecha.
     */
    @TypeConverter
    fun timestampToDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    /**
     * Convierte una fecha a un timestamp.
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    companion object {
        /**
         * Formatea una fecha con el formato estándar de la aplicación (dd-MM-yyyy).
         */
        fun formatDate(date: Date?): String {
            return date?.let { 
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it) 
            } ?: ""
        }
        
        /**
         * Formatea una fecha con hora con el formato estándar de la aplicación (dd-MM-yyyy HH:mm).
         */
        fun formatDateTime(date: Date?): String {
            return date?.let { 
                SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(it) 
            } ?: ""
        }
        
        /**
         * Obtiene una fecha a partir de un string en formato dd-MM-yyyy.
         */
        fun parseDate(dateString: String?): Date? {
            if (dateString.isNullOrEmpty()) return null
            
            return try {
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateString)
            } catch (e: Exception) {
                null
            }
        }
        
        /**
         * Formatea la fecha actual con el formato estándar de la aplicación (dd-MM-yyyy).
         */
        fun getCurrentDateFormatted(): String {
            return formatDate(Date())
        }
        
        /**
         * Formatea la fecha y hora actual con el formato estándar de la aplicación (dd-MM-yyyy HH:mm:ss).
         */
        fun getCurrentDateTimeFormatted(): String {
            return SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        }
    }
}