package com.productiva.android.repository

/**
 * Clase que representa el estado de un recurso en el repositorio.
 * Permite gestionar diferentes estados de carga, éxito y error de forma uniforme
 * a través de toda la aplicación.
 */
sealed class ResourceState<out T> {
    /**
     * Estado de carga, se está obteniendo el recurso.
     */
    object Loading : ResourceState<Nothing>()
    
    /**
     * Estado de éxito, el recurso se ha obtenido correctamente.
     * 
     * @param data Los datos del recurso.
     * @param message Mensaje opcional de éxito.
     */
    data class Success<T>(val data: T, val message: String? = null) : ResourceState<T>()
    
    /**
     * Estado de error, no se ha podido obtener el recurso.
     * 
     * @param message Mensaje descriptivo del error.
     * @param data Datos parciales o en caché que pueden estar disponibles a pesar del error.
     */
    data class Error(val message: String, val data: Any? = null) : ResourceState<Nothing>()
    
    /**
     * Comprueba si el estado es de tipo Loading.
     */
    fun isLoading(): Boolean = this is Loading
    
    /**
     * Comprueba si el estado es de tipo Success.
     */
    fun isSuccess(): Boolean = this is Success
    
    /**
     * Comprueba si el estado es de tipo Error.
     */
    fun isError(): Boolean = this is Error
    
    /**
     * Obtiene el mensaje asociado al estado, solo para Success y Error.
     */
    fun getMessage(): String? {
        return when (this) {
            is Success -> message
            is Error -> message
            else -> null
        }
    }
}