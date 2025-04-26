package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.productiva.android.database.Converters

/**
 * Modelo para usuarios.
 * Representa un usuario en el sistema con todos sus permisos y ubicaciones asignadas.
 */
@Entity(tableName = "users")
@TypeConverters(Converters::class)
data class User(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("role")
    val role: String, // admin, manager, employee, etc.
    
    @SerializedName("company_id")
    val companyId: Int? = null,
    
    @SerializedName("company_name")
    val companyName: String? = null,
    
    @SerializedName("permissions")
    val permissions: List<String> = emptyList(),
    
    @SerializedName("locations")
    val locations: List<UserLocation> = emptyList(),
    
    @SerializedName("last_login")
    val lastLogin: String? = null,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    
    @SerializedName("avatar_url")
    val avatarUrl: String? = null,
    
    @SerializedName("is_active")
    val isActive: Boolean = true,
    
    @SerializedName("settings")
    val settings: Map<String, String> = emptyMap(),
    
    // Campos locales
    var lastSyncTime: Long = 0
) {
    /**
     * Verifica si el usuario tiene un permiso específico.
     */
    fun hasPermission(permission: String): Boolean {
        // Los administradores tienen todos los permisos
        if (role == "admin") return true
        
        return permissions.contains(permission)
    }
    
    /**
     * Verifica si el usuario tiene acceso a una ubicación específica.
     */
    fun canAccessLocation(locationId: Int): Boolean {
        // Los administradores pueden acceder a todas las ubicaciones
        if (role == "admin") return true
        
        return locations.any { it.id == locationId }
    }
    
    /**
     * Obtiene las IDs de todas las ubicaciones a las que el usuario tiene acceso.
     */
    fun getAccessibleLocationIds(): List<Int> {
        return locations.map { it.id }
    }
    
    /**
     * Obtiene un valor de configuración específico.
     */
    fun getSetting(key: String, defaultValue: String = ""): String {
        return settings[key] ?: defaultValue
    }
    
    /**
     * Clase anidada para representar una ubicación asignada a un usuario.
     */
    data class UserLocation(
        @SerializedName("id")
        val id: Int,
        
        @SerializedName("name")
        val name: String,
        
        @SerializedName("address")
        val address: String? = null,
        
        @SerializedName("is_default")
        val isDefault: Boolean = false
    )
}