package com.productiva.android.api

import com.google.gson.annotations.SerializedName

/**
 * Respuesta de login
 */
data class LoginResponse(
    @SerializedName("token")
    val token: String,
    
    @SerializedName("user")
    val user: UserResponse
)

/**
 * Usuario en respuesta API
 */
data class UserResponse(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("role")
    val role: String
)

/**
 * Respuesta de subida de archivos
 */
data class UploadResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("file_path")
    val filePath: String?,
    
    @SerializedName("error")
    val error: String?
)

/**
 * Respuesta de error genérico
 */
data class ErrorResponse(
    @SerializedName("error")
    val error: String,
    
    @SerializedName("message")
    val message: String?
)