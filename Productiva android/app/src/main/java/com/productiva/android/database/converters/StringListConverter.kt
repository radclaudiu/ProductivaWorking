package com.productiva.android.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Conversor para listas de strings en Room
 */
class StringListConverter {
    
    /**
     * Convierte una lista de strings a un string JSON
     */
    @TypeConverter
    fun fromList(list: List<String>?): String? {
        return if (list == null) null else Gson().toJson(list)
    }
    
    /**
     * Convierte un string JSON a una lista de strings
     */
    @TypeConverter
    fun toList(value: String?): List<String>? {
        if (value == null) return null
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, type)
    }
}