package com.productiva.android.api

import com.google.gson.annotations.SerializedName

/**
 * Clase para encapsular las respuestas de la API
 * 
 * @param T Tipo de datos en el campo "data" de la respuesta
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("data")
    val data: T? = null,
    
    @SerializedName("errors")
    val errors: List<String>? = null,
    
    @SerializedName("count")
    val count: Int? = null,
    
    @SerializedName("pagination")
    val pagination: PaginationInfo? = null
)

/**
 * Clase para la información de paginación en respuestas
 */
data class PaginationInfo(
    @SerializedName("current_page")
    val currentPage: Int,
    
    @SerializedName("last_page")
    val lastPage: Int,
    
    @SerializedName("per_page")
    val perPage: Int,
    
    @SerializedName("total")
    val total: Int
)