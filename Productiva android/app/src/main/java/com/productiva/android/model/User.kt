package com.productiva.android.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Clase de entidad para representar un usuario en la base de datos
 */
@Entity(
    tableName = "users",
    indices = [
        Index("email", unique = true),
        Index("username", unique = true)
    ]
)
data class User(
    @PrimaryKey
    @ColumnInfo(name = "id")
    @SerializedName("id")
    val id: Int = 0,
    
    @ColumnInfo(name = "name")
    @SerializedName("name")
    val name: String,
    
    @ColumnInfo(name = "email")
    @SerializedName("email")
    val email: String,
    
    @ColumnInfo(name = "username")
    @SerializedName("username")
    val username: String,
    
    @ColumnInfo(name = "role")
    @SerializedName("role")
    val role: String,
    
    @ColumnInfo(name = "company_id")
    @SerializedName("company_id")
    val companyId: Int,
    
    @ColumnInfo(name = "company_name")
    @SerializedName("company_name")
    val companyName: String = "",
    
    @ColumnInfo(name = "location_id")
    @SerializedName("location_id")
    val locationId: Int? = null,
    
    @ColumnInfo(name = "location_name")
    @SerializedName("location_name")
    val locationName: String = "",
    
    @ColumnInfo(name = "is_active")
    @SerializedName("is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "profile_image")
    @SerializedName("profile_image")
    val profileImage: String? = null,
    
    @ColumnInfo(name = "phone")
    @SerializedName("phone")
    val phone: String? = null,
    
    @ColumnInfo(name = "last_sync")
    val lastSync: Long = System.currentTimeMillis()
)