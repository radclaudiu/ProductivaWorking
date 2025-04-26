package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para usuarios
 * Representa un usuario del sistema Productiva
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: Int,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("email")
    val email: String?,
    
    @SerializedName("role")
    val role: String,
    
    @SerializedName("profile_id")
    val profileId: Int?,
    
    @SerializedName("location_id")
    val locationId: Int?,
    
    @SerializedName("company_id")
    val companyId: Int?,
    
    @SerializedName("last_sync")
    var lastSync: Long = System.currentTimeMillis()
)