package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para ubicaciones
 * Representa una ubicación en el sistema Productiva
 */
@Entity(tableName = "locations")
data class Location(
    @PrimaryKey
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("address")
    val address: String? = null,
    
    @SerializedName("company_id")
    val companyId: Int,
    
    @SerializedName("is_active")
    val isActive: Boolean = true,
    
    @SerializedName("last_sync")
    var lastSync: Long = System.currentTimeMillis()
)