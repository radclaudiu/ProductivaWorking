package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Modelo de datos para usuarios
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
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("company_id")
    val companyId: Int,
    
    @SerializedName("company_name")
    val companyName: String,
    
    @SerializedName("role")
    val role: String? = null,
    
    @SerializedName("is_admin")
    val isAdmin: Boolean = false,
    
    @SerializedName("is_active")
    val isActive: Boolean = true,
    
    @SerializedName("created_at")
    val createdAt: Date? = null,
    
    @SerializedName("updated_at")
    val updatedAt: Date? = null,
    
    // Campos para sincronización local
    val lastSyncedAt: Date? = null,
    val locallyModified: Boolean = false
)