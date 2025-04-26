package com.productiva.android.network

import retrofit2.Response
import java.io.IOException

/**
 * Clase genérica que representa la respuesta de la API.
 * Permite unificar el formato de las respuestas y manejar errores.
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errors: Map<String, List<String>>? = null
)

/**
 * Enum que representa los posibles estados de una operación de red.
 */
sealed class NetworkResult<T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val message: String, val code: Int? = null, val data: T? = null) : NetworkResult<T>()
    class Loading<T> : NetworkResult<T>()
}

/**
 * Función de extensión que convierte una respuesta de Retrofit en un NetworkResult.
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> Response<ApiResponse<T>>): NetworkResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                if (body.success) {
                    val data = body.data
                    if (data != null) {
                        NetworkResult.Success(data)
                    } else {
                        NetworkResult.Error("Respuesta exitosa pero sin datos")
                    }
                } else {
                    val errorMessage = body.message ?: body.errors?.values?.flatten()?.joinToString(", ") ?: "Error desconocido"
                    NetworkResult.Error(errorMessage)
                }
            } else {
                NetworkResult.Error("Respuesta del servidor vacía")
            }
        } else {
            val errorBody = response.errorBody()
            val errorMessage = errorBody?.string() ?: "Error en la petición HTTP: ${response.code()}"
            NetworkResult.Error(errorMessage, response.code())
        }
    } catch (e: IOException) {
        NetworkResult.Error("Error de conexión: ${e.message}", null)
    } catch (e: Exception) {
        NetworkResult.Error("Error: ${e.message}", null)
    }
}