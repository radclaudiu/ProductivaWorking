package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Modelo de usuario
 * Representa tanto a usuarios de autenticación como perfiles de empleados
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: Int,
    
    // Datos de autenticación
    val username: String,
    val email: String? = null,
    
    // Datos personales
    val name: String? = null,
    val lastName: String? = null,
    @SerializedName("profile_image")
    val profileImage: String? = null,
    val phone: String? = null,
    val address: String? = null,
    
    // Rol en el sistema
    val role: String,
    
    // Datos organizacionales
    @SerializedName("company_id")
    val companyId: Int? = null,
    @SerializedName("location_id")
    val locationId: Int? = null,
    
    // Información laboral
    val position: String? = null,
    @SerializedName("employee_id")
    val employeeId: String? = null,
    
    // Estado
    val active: Boolean = true
)