package com.productiva.android.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("role")
    val role: String,
    
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("pin")
    val pin: String? = null,
    
    @SerializedName("last_login")
    val lastLogin: String? = null,
    
    @SerializedName("is_active")
    val isActive: Boolean = true,
    
    @SerializedName("location_id")
    val locationId: Int? = null,
    
    @SerializedName("company_id")
    val companyId: Int? = null
)