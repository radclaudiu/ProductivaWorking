package com.productiva.android.data.model

/**
 * Clase de datos para las respuestas de login del servidor.
 * 
 * @property userId ID del usuario autenticado
 * @property username Nombre de usuario
 * @property token Token JWT para autenticación
 * @property role Rol del usuario en el sistema
 * @property companies Lista de empresas a las que tiene acceso el usuario
 */
data class LoginResponse(
    val userId: Int,
    val username: String,
    val token: String,
    val role: String,
    val companies: List<Company>? = null
)