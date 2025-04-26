package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

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
    val companyId: Int,
    
    @ColumnInfo(name = "company_name")
    @SerializedName("company_name") 
    val companyName: String,
    
    @ColumnInfo(name = "location_id")
    @SerializedName("location_id") 
    val locationId: Int?,
    
    @ColumnInfo(name = "location_name")
    @SerializedName("location_name") 
    val locationName: String?,
    
    @ColumnInfo(name = "is_active")
    @SerializedName("is_active") 
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "last_sync")
    val lastSync: Long = System.currentTimeMillis()
)