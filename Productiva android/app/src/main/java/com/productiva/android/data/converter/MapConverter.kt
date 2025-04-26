package com.productiva.android.data.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.productiva.android.data.model.FieldPosition

/**
 * Conversor para almacenar y recuperar mapas de objetos en la base de datos Room.
 * Convierte entre mapas y su representación JSON como string.
 */
class MapConverter {
    private val gson = Gson()
    
    /**
     * Convierte un mapa de strings a su representación JSON.
     *
     * @param map Mapa de strings a convertir.
     * @return Representación JSON del mapa como string o null si el mapa es null.
     */
    @TypeConverter
    fun fromStringMap(map: Map<String, String>?): String? {
        return if (map.isNullOrEmpty()) {
            null
        } else {
            gson.toJson(map)
        }
    }
    
    /**
     * Convierte una representación JSON a un mapa de strings.
     *
     * @param json Representación JSON del mapa como string.
     * @return Mapa de strings reconstruido o mapa vacío si el JSON es null o vacío.
     */
    @TypeConverter
    fun toStringMap(json: String?): Map<String, String> {
        if (json.isNullOrEmpty()) {
            return emptyMap()
        }
        
        val type = object : TypeToken<Map<String, String>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    /**
     * Convierte un mapa de posiciones de campos a su representación JSON.
     *
     * @param map Mapa de posiciones de campos a convertir.
     * @return Representación JSON del mapa como string o null si el mapa es null.
     */
    @TypeConverter
    fun fromFieldPositionMap(map: Map<String, FieldPosition>?): String? {
        return if (map.isNullOrEmpty()) {
            null
        } else {
            gson.toJson(map)
        }
    }
    
    /**
     * Convierte una representación JSON a un mapa de posiciones de campos.
     *
     * @param json Representación JSON del mapa como string.
     * @return Mapa de posiciones de campos reconstruido o mapa vacío si el JSON es null o vacío.
     */
    @TypeConverter
    fun toFieldPositionMap(json: String?): Map<String, FieldPosition> {
        if (json.isNullOrEmpty()) {
            return emptyMap()
        }
        
        val type = object : TypeToken<Map<String, FieldPosition>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}