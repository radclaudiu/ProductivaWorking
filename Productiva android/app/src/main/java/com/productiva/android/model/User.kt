package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Entidad que representa un usuario en el sistema.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("is_active")
    val isActive: Boolean = true,
    
    @SerializedName("is_admin")
    val isAdmin: Boolean = false,
    
    @SerializedName("company_id")
    val companyId: Int? = null,
    
    @SerializedName("company_name")
    val companyName: String? = null,
    
    @SerializedName("location_id")
    val locationId: Int? = null,
    
    @SerializedName("location_name")
    val locationName: String? = null,
    
    @SerializedName("roles")
    val roles: List<String>? = null,
    
    @SerializedName("profile_photo")
    val profilePhoto: String? = null,
    
    @SerializedName("phone")
    val phone: String? = null
) {
    /**
     * Verifica si el usuario tiene un rol específico
     */
    fun hasRole(role: String): Boolean {
        return roles?.contains(role) ?: false
    }
    
    /**
     * Verifica si el usuario puede administrar tareas
     */
    fun canManageTasks(): Boolean {
        return isAdmin || hasRole("task_manager")
    }
    
    /**
     * Verifica si el usuario puede imprimir etiquetas
     */
    fun canPrintLabels(): Boolean {
        return isAdmin || hasRole("label_printer")
    }
    
    /**
     * Obtiene las iniciales del nombre del usuario
     */
    fun getInitials(): String {
        val parts = name.split(" ")
        return when {
            parts.isEmpty() -> ""
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "${parts[0].take(1)}${parts.last().take(1)}".uppercase()
        }
    }
}