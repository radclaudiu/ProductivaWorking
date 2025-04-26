package com.productiva.android.network.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de respuesta para el inicio de sesión.
 * Contiene el token de autenticación y los datos del usuario.
 *
 * @property token Token de autenticación JWT.
 * @property userId ID del usuario.
 * @property username Nombre de usuario.
 * @property email Correo electrónico del usuario.
 * @property isAdmin Indica si el usuario es administrador.
 * @property companyId ID de la empresa a la que pertenece el usuario.
 * @property companyName Nombre de la empresa.
 * @property employeeId ID del empleado asociado al usuario (puede ser nulo).
 */
data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String?,
    @SerializedName("is_admin") val isAdmin: Boolean,
    @SerializedName("company_id") val companyId: Int,
    @SerializedName("company_name") val companyName: String,
    @SerializedName("employee_id") val employeeId: Int?
)