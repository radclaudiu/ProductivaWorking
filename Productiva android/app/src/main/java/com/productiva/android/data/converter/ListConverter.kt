package com.productiva.android.data.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Conversor para listas de String, que permite almacenar listas en Room.
 * Convierte entre List<String> y String para persistencia en la base de datos.
 */
class ListConverter {
    private val gson = Gson()
    
    /**
     * Convierte una lista de strings a una cadena JSON.
     *
     * @param list Lista a convertir.
     * @return Cadena JSON o null si la lista es null.
     */
    @TypeConverter
    fun fromList(list: List<String>?): String? {
        return if (list == null) null else gson.toJson(list)
    }
    
    /**
     * Convierte una cadena JSON a una lista de strings.
     *
     * @param json Cadena JSON.
     * @return Lista de strings o una lista vacía si la cadena es null.
     */
    @TypeConverter
    fun toList(json: String?): List<String> {
        if (json == null) return emptyList()
        
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}