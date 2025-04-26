package com.productiva.android.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException

/**
 * Clase sellada que representa los diferentes estados de una llamada a la API.
 *
 * @param T Tipo de datos que contiene la respuesta.
 */
sealed class NetworkResult<out T> {
    /**
     * Estado de carga mientras se procesa la solicitud a la API.
     */
    object Loading : NetworkResult<Nothing>()
    
    /**
     * Estado de éxito con los datos recibidos de la API.
     *
     * @property data Datos recibidos de la API.
     */
    data class Success<T>(val data: T) : NetworkResult<T>()
    
    /**
     * Estado de error cuando falla la solicitud a la API.
     *
     * @property message Mensaje de error.
     * @property errorCode Código de error (por ejemplo, código HTTP).
     * @property exception Excepción que causó el error (opcional).
     */
    data class Error(
        val message: String,
        val errorCode: Int? = null,
        val exception: Exception? = null
    ) : NetworkResult<Nothing>()
}

/**
 * Función de extensión para convertir una respuesta Retrofit en un NetworkResult.
 *
 * @param T Tipo de datos que contiene la respuesta.
 * @return NetworkResult con los datos de la respuesta o un error.
 */
fun <T> Response<T>.toNetworkResult(): NetworkResult<T> {
    return try {
        if (isSuccessful) {
            val body = body()
            if (body != null) {
                NetworkResult.Success(body)
            } else {
                NetworkResult.Error("Respuesta vacía del servidor", code())
            }
        } else {
            val errorMessage = errorBody()?.string() ?: "Error desconocido"
            NetworkResult.Error(errorMessage, code())
        }
    } catch (e: Exception) {
        NetworkResult.Error("Error al procesar la respuesta: ${e.message}", exception = e)
    }
}

/**
 * Función de utilidad para realizar una llamada segura a la API y manejar errores.
 *
 * @param apiCall Lambda que realiza la llamada a la API.
 * @return NetworkResult con los datos de la respuesta o un error.
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> T): NetworkResult<T> {
    return withContext(Dispatchers.IO) {
        try {
            NetworkResult.Success(apiCall())
        } catch (e: IOException) {
            Log.e("ApiCall", "Error de red: ${e.message}", e)
            NetworkResult.Error("Error de red. Comprueba tu conexión a Internet.", exception = e)
        } catch (e: retrofit2.HttpException) {
            val errorCode = e.code()
            val errorMessage = e.message ?: "Error HTTP $errorCode"
            Log.e("ApiCall", "Error HTTP $errorCode: $errorMessage", e)
            NetworkResult.Error(errorMessage, errorCode, e)
        } catch (e: Exception) {
            Log.e("ApiCall", "Error inesperado: ${e.message}", e)
            NetworkResult.Error("Error inesperado: ${e.message}", exception = e)
        }
    }
}

/**
 * Función de utilidad para realizar una llamada a la API que devuelve una Response y convertirla en NetworkResult.
 *
 * @param apiCall Lambda que realiza la llamada a la API y devuelve una Response.
 * @return NetworkResult con los datos de la respuesta o un error.
 */
suspend fun <T> safeApiCallWithResponse(apiCall: suspend () -> Response<T>): NetworkResult<T> {
    return withContext(Dispatchers.IO) {
        try {
            val response = apiCall()
            response.toNetworkResult()
        } catch (e: IOException) {
            Log.e("ApiCall", "Error de red: ${e.message}", e)
            NetworkResult.Error("Error de red. Comprueba tu conexión a Internet.", exception = e)
        } catch (e: retrofit2.HttpException) {
            val errorCode = e.code()
            val errorMessage = e.message ?: "Error HTTP $errorCode"
            Log.e("ApiCall", "Error HTTP $errorCode: $errorMessage", e)
            NetworkResult.Error(errorMessage, errorCode, e)
        } catch (e: Exception) {
            Log.e("ApiCall", "Error inesperado: ${e.message}", e)
            NetworkResult.Error("Error inesperado: ${e.message}", exception = e)
        }
    }
}