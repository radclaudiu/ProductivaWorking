package com.productiva.android.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "locations")
data class Location(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("address")
    val address: String? = null,
    
    @SerializedName("city")
    val city: String? = null,
    
    @SerializedName("phone")
    val phone: String? = null,
    
    @SerializedName("company_id")
    val companyId: Int,
    
    @SerializedName("is_active")
    val isActive: Boolean = true
)