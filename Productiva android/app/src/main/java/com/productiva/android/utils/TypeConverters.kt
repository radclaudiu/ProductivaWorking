package com.productiva.android.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Convertidor para fechas y horas en la base de datos Room.
 */
class DateTimeConverter {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    
    /**
     * Convierte una fecha a su representación en string para almacenar en BD.
     * 
     * @param date Fecha a convertir.
     * @return String con la fecha en formato ISO 8601.
     */
    @TypeConverter
    fun fromDate(date: Date?): String? {
        return date?.let { dateFormat.format(it) }
    }
    
    /**
     * Convierte un string con fecha a un objeto Date.
     * 
     * @param dateString String con la fecha en formato ISO 8601.
     * @return Objeto Date.
     */
    @TypeConverter
    fun toDate(dateString: String?): Date? {
        return dateString?.let {
            try {
                dateFormat.parse(it)
            } catch (e: Exception) {
                null
            }
        }
    }
}

/**
 * Convertidor para listas en la base de datos Room.
 */
class ListTypeConverter {
    private val gson = Gson()
    
    /**
     * Convierte una lista de strings a JSON para almacenar en BD.
     * 
     * @param list Lista de strings.
     * @return String con la lista en formato JSON.
     */
    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.let { gson.toJson(it) }
    }
    
    /**
     * Convierte un string JSON a una lista de strings.
     * 
     * @param value String con la lista en formato JSON.
     * @return Lista de strings.
     */
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(it, type)
        }
    }
    
    /**
     * Convierte una lista de adjuntos de tarea a JSON para almacenar en BD.
     * 
     * @param attachments Lista de adjuntos.
     * @return String con la lista en formato JSON.
     */
    @TypeConverter
    fun fromAttachmentList(attachments: List<Task.Attachment>?): String? {
        return attachments?.let { gson.toJson(it) }
    }
    
    /**
     * Convierte un string JSON a una lista de adjuntos de tarea.
     * 
     * @param value String con la lista en formato JSON.
     * @return Lista de adjuntos.
     */
    @TypeConverter
    fun toAttachmentList(value: String?): List<Task.Attachment>? {
        return value?.let {
            val type = object : TypeToken<List<Task.Attachment>>() {}.type
            gson.fromJson(it, type)
        }
    }
    
    /**
     * Convierte una lista de campos de etiqueta a JSON para almacenar en BD.
     * 
     * @param fields Lista de campos de etiqueta.
     * @return String con la lista en formato JSON.
     */
    @TypeConverter
    fun fromLabelFieldList(fields: List<LabelTemplate.LabelField>?): String? {
        return fields?.let { gson.toJson(it) }
    }
    
    /**
     * Convierte un string JSON a una lista de campos de etiqueta.
     * 
     * @param value String con la lista en formato JSON.
     * @return Lista de campos de etiqueta.
     */
    @TypeConverter
    fun toLabelFieldList(value: String?): List<LabelTemplate.LabelField>? {
        return value?.let {
            val type = object : TypeToken<List<LabelTemplate.LabelField>>() {}.type
            gson.fromJson(it, type)
        }
    }
}