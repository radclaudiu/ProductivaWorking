package com.productiva.android.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.productiva.android.network.model.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

private const val TAG = "SafeApiCall"

/**
 * Función de extensión para realizar llamadas seguras a la API.
 * Maneja excepciones comunes y las convierte en resultados tipados.
 *
 * @param apiCall Función suspendida que realiza la llamada a la API.
 * @return NetworkResult con el resultado de la operación.
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> T): NetworkResult<T> = withContext(Dispatchers.IO) {
    try {
        // Intenta realizar la llamada a la API
        NetworkResult.Success(apiCall.invoke())
    } catch (throwable: Throwable) {
        Log.e(TAG, "Error en llamada a API", throwable)
        
        when (throwable) {
            is HttpException -> {
                // Error HTTP (código 400-500)
                val errorCode = throwable.code()
                val errorResponse = convertErrorBody(throwable)
                NetworkResult.Error(
                    message = errorResponse ?: "Error HTTP $errorCode",
                    errorCode = errorCode
                )
            }
            is SocketTimeoutException -> {
                // Timeout de conexión
                NetworkResult.Error("Tiempo de espera agotado. Verifique su conexión.")
            }
            is IOException -> {
                // Error de red o conectividad
                NetworkResult.Error("Error de red. Verifique su conexión a Internet.")
            }
            else -> {
                // Otros errores
                NetworkResult.Error(throwable.message ?: "Error desconocido")
            }
        }
    }
}

/**
 * Convierte el cuerpo de error de una excepción HTTP en un mensaje legible.
 *
 * @param httpException Excepción HTTP.
 * @return Mensaje de error extraído o null si no se puede procesar.
 */
private fun convertErrorBody(httpException: HttpException): String? {
    return try {
        httpException.response()?.errorBody()?.string()?.let { errorBody ->
            try {
                // Intentar parsear como ApiResponse
                val errorResponse = Gson().fromJson(errorBody, ApiResponse::class.java)
                errorResponse.message ?: "Error en el servidor"
            } catch (e: JsonSyntaxException) {
                // Si no es un JSON válido, devolver el error como texto plano
                errorBody.take(100) // Tomar solo los primeros 100 caracteres para evitar mensajes demasiado largos
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error al convertir cuerpo de error", e)
        null
    }
}