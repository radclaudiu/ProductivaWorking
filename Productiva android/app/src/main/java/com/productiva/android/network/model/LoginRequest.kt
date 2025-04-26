package com.productiva.android.network.model

/**
 * Clase que representa una solicitud de inicio de sesión.
 *
 * @property username Nombre de usuario o email.
 * @property password Contraseña del usuario.
 */
data class LoginRequest(
    val username: String,
    val password: String
)