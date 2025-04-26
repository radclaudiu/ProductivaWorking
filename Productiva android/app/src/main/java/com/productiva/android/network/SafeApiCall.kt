package com.productiva.android.network

import android.util.Log
import retrofit2.Response
import java.io.IOException

/**
 * Función auxiliar para realizar llamadas a API de manera segura.
 * Maneja excepciones y errores de red de forma unificada.
 *
 * @param apiCall Suspending function que realiza la llamada a la API.
 * @return NetworkResult con el resultado de la operación.
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
    try {
        val response = apiCall()
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(body)
            } else {
                NetworkResult.Error("Respuesta vacía del servidor")
            }
        } else {
            val errorMsg = response.errorBody()?.string()
            val errorMessage = errorMsg ?: "Error desconocido: ${response.code()}"
            Log.e("SafeApiCall", "API error: $errorMessage")
            NetworkResult.Error(
                message = getErrorMessage(response.code(), errorMsg),
                errorCode = response.code(),
                errorBody = errorMsg
            )
        }
    } catch (e: IOException) {
        Log.e("SafeApiCall", "IOException: ${e.message}", e)
        return NetworkResult.Error("Error de conexión: comprueba tu conexión a Internet")
    } catch (e: Exception) {
        Log.e("SafeApiCall", "Exception: ${e.message}", e)
        return NetworkResult.Error("Se produjo un error: ${e.message}")
    }
}

/**
 * Obtiene un mensaje de error amigable para el usuario basado en el código de error HTTP.
 *
 * @param code Código de error HTTP.
 * @param errorBody Cuerpo de la respuesta de error (opcional).
 * @return Mensaje de error amigable.
 */
private fun getErrorMessage(code: Int, errorBody: String?): String {
    return when (code) {
        400 -> "La solicitud es incorrecta. Contacte con soporte."
        401 -> "No estás autorizado. Por favor, inicia sesión de nuevo."
        403 -> "No tienes permisos para realizar esta acción."
        404 -> "El recurso solicitado no existe."
        409 -> "Conflicto con el estado actual del recurso."
        422 -> getValidationErrorMessage(errorBody) ?: "Datos no válidos."
        429 -> "Demasiadas solicitudes. Por favor, inténtalo más tarde."
        500 -> "Error interno del servidor. Contacte con soporte."
        502, 503, 504 -> "El servicio no está disponible temporalmente. Inténtalo más tarde."
        else -> "Error $code: ${errorBody ?: "Se produjo un error desconocido"}"
    }
}

/**
 * Extrae un mensaje de error más específico de las validaciones del servidor, si está disponible.
 *
 * @param errorBody Cuerpo de la respuesta de error JSON.
 * @return Mensaje de error de validación, o null si no se pudo extraer.
 */
private fun getValidationErrorMessage(errorBody: String?): String? {
    if (errorBody.isNullOrEmpty()) return null
    
    return try {
        // Extraer mensajes de validación específicos si siguen un formato conocido
        // Ejemplo de implementación: extraer errores de un objeto JSON con estructura conocida
        
        // Si no podemos analizar los errores específicos, devolvemos null
        // y se usará el mensaje genérico "Datos no válidos"
        null
    } catch (e: Exception) {
        null
    }
}