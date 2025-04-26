package com.productiva.android.network

/**
 * Clase que encapsula el resultado de una operación de red.
 * Permite manejar de manera unificada los diferentes estados de una solicitud HTTP.
 */
sealed class NetworkResult<T> {
    /**
     * Estado de carga.
     */
    class Loading<T> : NetworkResult<T>()
    
    /**
     * Estado de éxito con los datos cargados.
     *
     * @property data Datos recibidos.
     */
    data class Success<T>(val data: T) : NetworkResult<T>()
    
    /**
     * Estado de error.
     *
     * @property message Mensaje de error.
     * @property errorCode Código de error HTTP (opcional).
     * @property errorBody Cuerpo de la respuesta de error (opcional).
     */
    data class Error<T>(
        val message: String,
        val errorCode: Int? = null,
        val errorBody: String? = null
    ) : NetworkResult<T>()
}