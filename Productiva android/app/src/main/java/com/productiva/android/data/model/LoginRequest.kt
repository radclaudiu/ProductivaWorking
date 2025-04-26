package com.productiva.android.data.model

/**
 * Clase de datos para enviar peticiones de login al servidor.
 * 
 * @property username Nombre de usuario
 * @property password Contraseña del usuario
 * @property rememberMe Indica si se debe recordar la sesión
 */
data class LoginRequest(
    val username: String,
    val password: String,
    val rememberMe: Boolean = false
)