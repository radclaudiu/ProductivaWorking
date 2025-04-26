package com.productiva.android.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.productiva.android.model.User

/**
 * Clase de conversores para Room.
 * Proporciona métodos para convertir tipos complejos a tipos primitivos y viceversa.
 */
class Converters {
    private val gson = Gson()
    
    /**
     * Convierte una lista de strings a un string JSON.
     */
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }
    
    /**
     * Convierte un string JSON a una lista de strings.
     */
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(value, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Convierte un mapa de string a string a un string JSON.
     */
    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String {
        return gson.toJson(value)
    }
    
    /**
     * Convierte un string JSON a un mapa de string a string.
     */
    @TypeConverter
    fun toStringMap(value: String): Map<String, String> {
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return try {
            gson.fromJson(value, mapType) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    /**
     * Convierte una lista de ubicaciones de usuario a un string JSON.
     */
    @TypeConverter
    fun fromUserLocationList(value: List<User.UserLocation>): String {
        return gson.toJson(value)
    }
    
    /**
     * Convierte un string JSON a una lista de ubicaciones de usuario.
     */
    @TypeConverter
    fun toUserLocationList(value: String): List<User.UserLocation> {
        val listType = object : TypeToken<List<User.UserLocation>>() {}.type
        return try {
            gson.fromJson(value, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Convierte una lista de strings (tags) a un string JSON.
     */
    @TypeConverter
    fun fromTags(value: List<String>): String {
        return gson.toJson(value)
    }
    
    /**
     * Convierte un string JSON a una lista de strings (tags).
     */
    @TypeConverter
    fun toTags(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(value, listType) ?: emptyList()
        } catch (e: Exception) {
            // Si el valor es una cadena simple, intentar dividirla
            if (value.contains(",")) {
                return value.split(",").map { it.trim() }
            }
            if (value.contains("|")) {
                return value.split("|").map { it.trim() }
            }
            if (value.isNotBlank()) {
                return listOf(value)
            }
            emptyList()
        }
    }
}