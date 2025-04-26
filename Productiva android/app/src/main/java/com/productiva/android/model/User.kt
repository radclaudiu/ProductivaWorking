package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos que representa un usuario en la aplicación.
 * Se almacena en la base de datos local y se sincroniza con el servidor.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("first_name")
    val firstName: String?,
    
    @SerializedName("last_name")
    val lastName: String?,
    
    @SerializedName("role")
    val role: String, // ADMIN, MANAGER, EMPLOYEE
    
    @SerializedName("company_id")
    val companyId: Int?,
    
    @SerializedName("company_name")
    val companyName: String?,
    
    @SerializedName("is_active")
    val isActive: Boolean,
    
    @SerializedName("last_login")
    val lastLogin: String?,
    
    @SerializedName("created_at")
    val createdAt: String?,
    
    @SerializedName("profile_image")
    val profileImage: String?,
    
    @SerializedName("phone")
    val phone: String?,
    
    @SerializedName("locations")
    val locations: List<UserLocation>?,
    
    @SerializedName("permissions")
    val permissions: List<String>?
) {
    /**
     * Obtiene el nombre completo del usuario.
     */
    fun getFullName(): String {
        return if (firstName != null && lastName != null) {
            "$firstName $lastName"
        } else {
            username
        }
    }
    
    /**
     * Verifica si el usuario tiene un permiso específico.
     */
    fun hasPermission(permission: String): Boolean {
        return permissions?.contains(permission) == true || role == "ADMIN"
    }
    
    /**
     * Verifica si el usuario está asignado a una ubicación específica.
     */
    fun isAssignedToLocation(locationId: Int): Boolean {
        return locations?.any { it.id == locationId } == true
    }
    
    /**
     * Obtiene una lista con los IDs de las ubicaciones asignadas.
     */
    fun getAssignedLocationIds(): List<Int> {
        return locations?.map { it.id } ?: emptyList()
    }
    
    /**
     * Clase que representa una ubicación asignada a un usuario.
     */
    data class UserLocation(
        @SerializedName("id")
        val id: Int,
        
        @SerializedName("name")
        val name: String,
        
        @SerializedName("address")
        val address: String?,
        
        @SerializedName("is_primary")
        val isPrimary: Boolean
    )
}