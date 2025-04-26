package com.productiva.android.network.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de petición para el inicio de sesión.
 * Contiene las credenciales del usuario.
 *
 * @property username Nombre de usuario.
 * @property password Contraseña del usuario.
 * @property rememberMe Indicador para mantener la sesión iniciada.
 */
data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("remember_me") val rememberMe: Boolean = true
)