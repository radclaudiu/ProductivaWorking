package com.productiva.android.network.model

/**
 * Modelo para una respuesta genérica de la API.
 * Incluye información sobre el éxito o fracaso de la operación y los datos resultantes.
 *
 * @param T Tipo de datos que contiene la respuesta.
 * @property success Indica si la operación fue exitosa.
 * @property message Mensaje informativo sobre la operación.
 * @property data Datos resultantes de la operación (opcional).
 */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)

/**
 * Modelo para una respuesta de sincronización que incluye elementos añadidos,
 * actualizados y eliminados.
 *
 * @param T Tipo de datos que contiene la respuesta.
 * @property added Lista de elementos añadidos durante la sincronización.
 * @property updated Lista de elementos actualizados durante la sincronización.
 * @property deleted Lista de IDs de elementos eliminados durante la sincronización.
 */
data class SyncResponse<T>(
    val added: List<T>,
    val updated: List<T>,
    val deleted: List<Int>
)

/**
 * Modelo para la solicitud de autenticación.
 *
 * @property username Nombre de usuario.
 * @property password Contraseña.
 */
data class AuthRequest(
    val username: String,
    val password: String
)

/**
 * Modelo para la respuesta de autenticación.
 *
 * @property token Token de acceso.
 * @property refreshToken Token de refresco (opcional).
 * @property expiresIn Tiempo de expiración del token en segundos.
 * @property tokenType Tipo de token (normalmente "Bearer").
 * @property user Información del usuario autenticado.
 */
data class AuthResponse(
    val token: String,
    val refreshToken: String? = null,
    val expiresIn: Int,
    val tokenType: String,
    val user: User
)

/**
 * Modelo para la información de usuario.
 *
 * @property id ID del usuario.
 * @property username Nombre de usuario.
 * @property email Correo electrónico.
 * @property name Nombre completo.
 * @property role Rol del usuario en el sistema.
 * @property companyId ID de la empresa a la que pertenece.
 * @property companyName Nombre de la empresa a la que pertenece.
 * @property avatarUrl URL del avatar del usuario (opcional).
 * @property permissions Lista de permisos del usuario.
 */
data class User(
    val id: Int,
    val username: String,
    val email: String,
    val name: String,
    val role: String,
    val companyId: Int,
    val companyName: String,
    val avatarUrl: String? = null,
    val permissions: List<String>
)

/**
 * Modelo para la información de una impresora.
 *
 * @property id ID de la impresora.
 * @property name Nombre amigable de la impresora.
 * @property model Modelo de la impresora.
 * @property serialNumber Número de serie de la impresora.
 * @property ipAddress Dirección IP de la impresora (para impresoras de red).
 * @property macAddress Dirección MAC de la impresora (opcional).
 * @property connection Tipo de conexión ("usb", "network", "bluetooth").
 * @property isDefault Indica si es la impresora predeterminada.
 * @property paperWidth Ancho del papel en mm.
 * @property paperHeight Alto del papel en mm (opcional para rollos continuos).
 * @property dpi Resolución en puntos por pulgada.
 * @property defaultTemplateId ID de la plantilla de etiqueta predeterminada (opcional).
 */
data class Printer(
    val id: Int,
    val name: String,
    val model: String,
    val serialNumber: String? = null,
    val ipAddress: String? = null,
    val macAddress: String? = null,
    val connection: String,
    val isDefault: Boolean = false,
    val paperWidth: Int,
    val paperHeight: Int? = null,
    val dpi: Int,
    val defaultTemplateId: Int? = null
)