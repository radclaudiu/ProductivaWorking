package com.productiva.android.utils

import androidx.room.TypeConverter
import java.util.Date

/**
 * Conversor de tipos para Room que maneja la conversión entre Date y Long
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
    
    companion object {
        /**
         * Formatea una fecha en formato europeo (DD/MM/YYYY)
         */
        fun formatToEuropeanDate(date: Date?): String {
            if (date == null) return ""
            val day = date.date.toString().padStart(2, '0')
            val month = (date.month + 1).toString().padStart(2, '0')
            val year = date.year + 1900
            return "$day/$month/$year"
        }
        
        /**
         * Formatea una fecha con hora (DD/MM/YYYY HH:MM)
         */
        fun formatToEuropeanDateTime(date: Date?): String {
            if (date == null) return ""
            val day = date.date.toString().padStart(2, '0')
            val month = (date.month + 1).toString().padStart(2, '0')
            val year = date.year + 1900
            val hour = date.hours.toString().padStart(2, '0')
            val minute = date.minutes.toString().padStart(2, '0')
            return "$day/$month/$year $hour:$minute"
        }
        
        /**
         * Formatea una hora (HH:MM)
         */
        fun formatToTime(date: Date?): String {
            if (date == null) return ""
            val hour = date.hours.toString().padStart(2, '0')
            val minute = date.minutes.toString().padStart(2, '0')
            return "$hour:$minute"
        }
        
        /**
         * Convierte una cadena de fecha europea (DD/MM/YYYY) a Date
         */
        fun parseEuropeanDate(dateStr: String): Date? {
            return try {
                val parts = dateStr.split("/")
                if (parts.size != 3) return null
                val day = parts[0].toInt()
                val month = parts[1].toInt() - 1
                val year = parts[2].toInt() - 1900
                Date(year, month, day)
            } catch (e: Exception) {
                null
            }
        }
        
        /**
         * Convierte una cadena de fecha y hora europea (DD/MM/YYYY HH:MM) a Date
         */
        fun parseEuropeanDateTime(dateTimeStr: String): Date? {
            return try {
                val dateParts = dateTimeStr.split(" ")
                if (dateParts.size != 2) return null
                
                val dateComponents = dateParts[0].split("/")
                if (dateComponents.size != 3) return null
                
                val timeComponents = dateParts[1].split(":")
                if (timeComponents.size != 2) return null
                
                val day = dateComponents[0].toInt()
                val month = dateComponents[1].toInt() - 1
                val year = dateComponents[2].toInt() - 1900
                val hour = timeComponents[0].toInt()
                val minute = timeComponents[1].toInt()
                
                Date(year, month, day, hour, minute)
            } catch (e: Exception) {
                null
            }
        }
    }
}