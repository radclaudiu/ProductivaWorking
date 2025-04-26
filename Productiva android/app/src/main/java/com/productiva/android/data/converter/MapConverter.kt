package com.productiva.android.data.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.productiva.android.data.model.FieldPosition

/**
 * Conversor para mapas tipo Map<String, FieldPosition>, que permite almacenar
 * mapas complejos en Room.
 * Convierte entre Map<String, FieldPosition> y String para persistencia en la base de datos.
 */
class MapConverter {
    private val gson = Gson()
    
    /**
     * Convierte un mapa de posiciones de campos a una cadena JSON.
     *
     * @param map Mapa a convertir.
     * @return Cadena JSON o null si el mapa es null.
     */
    @TypeConverter
    fun fromMap(map: Map<String, FieldPosition>?): String? {
        return if (map == null) null else gson.toJson(map)
    }
    
    /**
     * Convierte una cadena JSON a un mapa de posiciones de campos.
     *
     * @param json Cadena JSON.
     * @return Mapa de posiciones de campos o un mapa vacío si la cadena es null.
     */
    @TypeConverter
    fun toMap(json: String?): Map<String, FieldPosition> {
        if (json == null) return emptyMap()
        
        val type = object : TypeToken<Map<String, FieldPosition>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }
}