package com.productiva.android.network.model

/**
 * Clase que representa la respuesta de un inicio de sesión exitoso.
 *
 * @property token Token de autenticación JWT.
 * @property user Datos del usuario autenticado.
 * @property companies Lista de empresas a las que tiene acceso el usuario.
 */
data class LoginResponse(
    val token: String,
    val user: UserData,
    val companies: List<CompanyData>
)

/**
 * Datos básicos del usuario.
 *
 * @property id ID único del usuario.
 * @property username Nombre de usuario.
 * @property email Correo electrónico.
 * @property name Nombre completo del usuario.
 * @property role Rol del usuario en el sistema.
 */
data class UserData(
    val id: Int,
    val username: String,
    val email: String,
    val name: String,
    val role: String
)

/**
 * Datos básicos de una empresa.
 *
 * @property id ID único de la empresa.
 * @property name Nombre de la empresa.
 * @property logo URL del logo de la empresa (opcional).
 * @property active Indica si la empresa está activa.
 */
data class CompanyData(
    val id: Int,
    val name: String,
    val logo: String? = null,
    val active: Boolean = true
)