package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Entidad que representa un usuario en la aplicación.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @ColumnInfo(name = "id")
    @SerializedName("id") 
    val id: Int,
    
    @ColumnInfo(name = "username")
    @SerializedName("username") 
    val username: String,
    
    @ColumnInfo(name = "email")
    @SerializedName("email") 
    val email: String,
    
    @ColumnInfo(name = "name")
    @SerializedName("name") 
    val name: String,
    
    @ColumnInfo(name = "role")
    @SerializedName("role") 
    val role: String,
    
    @ColumnInfo(name = "company_id")
    @SerializedName("company_id") 
    val companyId: Int?,
    
    @ColumnInfo(name = "location_id")
    @SerializedName("location_id") 
    val locationId: Int?,
    
    @ColumnInfo(name = "is_active")
    @SerializedName("is_active") 
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "is_admin")
    @SerializedName("is_admin") 
    val isAdmin: Boolean = false,
    
    @ColumnInfo(name = "last_login")
    @SerializedName("last_login") 
    val lastLogin: Date? = null,
    
    @ColumnInfo(name = "profile_image")
    @SerializedName("profile_image") 
    val profileImage: String? = null,
    
    @ColumnInfo(name = "phone")
    @SerializedName("phone") 
    val phone: String? = null,
    
    @ColumnInfo(name = "task_target")
    @SerializedName("task_target") 
    val taskTarget: Int? = null,
    
    @ColumnInfo(name = "last_sync")
    val lastSync: Long = System.currentTimeMillis()
) {
    /**
     * Devuelve las iniciales del nombre para mostrar un avatar de texto.
     */
    fun getInitials(): String {
        if (name.isBlank()) return "??"
        
        val parts = name.split(" ")
        
        return if (parts.size > 1) {
            (parts[0].firstOrNull()?.toString() ?: "") + 
            (parts.last().firstOrNull()?.toString() ?: "")
        } else {
            parts[0].take(2)
        }.uppercase()
    }
    
    /**
     * Devuelve un color basado en el ID para identificar visualmente al usuario.
     */
    fun getAvatarColor(): Int {
        // Genera un color material basado en el ID del usuario
        val colors = arrayOf(
            0xFF4CAF50, // Verde
            0xFF2196F3, // Azul
            0xFFFF9800, // Naranja
            0xFFE91E63, // Rosa
            0xFF9C27B0, // Púrpura
            0xFF795548, // Marrón
            0xFF607D8B  // Azul grisáceo
        )
        
        return colors[id % colors.size].toInt()
    }
}