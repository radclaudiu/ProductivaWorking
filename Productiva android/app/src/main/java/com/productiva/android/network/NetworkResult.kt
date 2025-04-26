package com.productiva.android.network

/**
 * Clase sellada que representa el resultado de una operación de red.
 */
sealed class NetworkResult<out T> {
    /**
     * Datos cargados con éxito.
     *
     * @property data Datos obtenidos.
     */
    data class Success<T>(val data: T) : NetworkResult<T>()
    
    /**
     * Error durante la operación.
     *
     * @property message Mensaje de error.
     * @property errorCode Código de error HTTP (opcional).
     */
    data class Error(
        val message: String,
        val errorCode: Int? = null
    ) : NetworkResult<Nothing>()
    
    /**
     * Operación en progreso.
     */
    object Loading : NetworkResult<Nothing>()
}