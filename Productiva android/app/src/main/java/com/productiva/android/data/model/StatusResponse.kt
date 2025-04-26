package com.productiva.android.data.model

/**
 * Clase de datos para las respuestas genéricas de estado.
 * 
 * @property success Indica si la operación fue exitosa
 * @property message Mensaje descriptivo sobre el resultado
 * @property code Código de resultado opcional
 */
data class StatusResponse(
    val success: Boolean,
    val message: String,
    val code: String? = null
)