package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Ignore
import java.util.Date

/**
 * Modelo de datos para usuarios
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: Int,
    
    @ColumnInfo(name = "username")
    val username: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "email")
    val email: String,
    
    @ColumnInfo(name = "companyId")
    val companyId: Int,
    
    @ColumnInfo(name = "companyName")
    val companyName: String?,
    
    @ColumnInfo(name = "phone")
    val phone: String?,
    
    @ColumnInfo(name = "role")
    val role: String,
    
    @ColumnInfo(name = "isActive")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "lastLogin")
    val lastLogin: Date? = null,
    
    @ColumnInfo(name = "locationId")
    val locationId: Int? = null,
    
    @ColumnInfo(name = "locationName")
    val locationName: String? = null,
    
    @ColumnInfo(name = "imageUrl")
    val imageUrl: String? = null,
    
    @ColumnInfo(name = "createdAt")
    val createdAt: Date? = null,
    
    @ColumnInfo(name = "updatedAt")
    val updatedAt: Date? = null,
    
    @ColumnInfo(name = "syncedAt")
    val syncedAt: Date? = null
) {
    @Ignore
    var authToken: String? = null
    
    @Ignore
    var isSelected: Boolean = false
    
    override fun toString(): String {
        return name
    }
}