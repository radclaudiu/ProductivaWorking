package com.productiva.android.network.model

/**
 * Clase que representa la respuesta genérica de la API.
 * Se utiliza para encapsular la respuesta del servidor con un status y datos.
 *
 * @param T Tipo de los datos de la respuesta.
 * @property status Estado de la respuesta (success, error).
 * @property message Mensaje descriptivo (opcional).
 * @property data Datos de la respuesta (opcional).
 * @property code Código de estado HTTP (opcional).
 */
data class ApiResponse<T>(
    val status: String,
    val message: String? = null,
    val data: T? = null,
    val code: Int? = null
)