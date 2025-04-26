package com.productiva.android.data.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Conversor para almacenar y recuperar listas de objetos en la base de datos Room.
 * Convierte entre listas de strings y su representación JSON como string.
 */
class ListConverter {
    private val gson = Gson()
    
    /**
     * Convierte una lista de strings a su representación JSON.
     *
     * @param list Lista de strings a convertir.
     * @return Representación JSON de la lista como string o null si la lista es null.
     */
    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return if (list.isNullOrEmpty()) {
            null
        } else {
            gson.toJson(list)
        }
    }
    
    /**
     * Convierte una representación JSON a una lista de strings.
     *
     * @param json Representación JSON de la lista como string.
     * @return Lista de strings reconstruida o lista vacía si el JSON es null o vacío.
     */
    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if (json.isNullOrEmpty()) {
            return emptyList()
        }
        
        val type = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Convierte una lista de enteros a su representación JSON.
     *
     * @param list Lista de enteros a convertir.
     * @return Representación JSON de la lista como string o null si la lista es null.
     */
    @TypeConverter
    fun fromIntList(list: List<Int>?): String? {
        return if (list.isNullOrEmpty()) {
            null
        } else {
            gson.toJson(list)
        }
    }
    
    /**
     * Convierte una representación JSON a una lista de enteros.
     *
     * @param json Representación JSON de la lista como string.
     * @return Lista de enteros reconstruida o lista vacía si el JSON es null o vacío.
     */
    @TypeConverter
    fun toIntList(json: String?): List<Int> {
        if (json.isNullOrEmpty()) {
            return emptyList()
        }
        
        val type = object : TypeToken<List<Int>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
}