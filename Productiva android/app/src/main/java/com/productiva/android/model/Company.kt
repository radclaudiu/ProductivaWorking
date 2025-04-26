package com.productiva.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para empresas
 * Representa una empresa en el sistema Productiva
 */
@Entity(tableName = "companies")
data class Company(
    @PrimaryKey
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("fiscal_id")
    val fiscalId: String? = null,
    
    @SerializedName("address")
    val address: String? = null,
    
    @SerializedName("is_active")
    val isActive: Boolean = true,
    
    @SerializedName("last_sync")
    var lastSync: Long = System.currentTimeMillis()
)