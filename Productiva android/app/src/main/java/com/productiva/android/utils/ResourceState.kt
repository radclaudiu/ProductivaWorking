package com.productiva.android.repository

/**
 * Clase para representar el estado de un recurso durante la carga/sincronización.
 * Usada para propagar estados de carga, éxito o error.
 */
sealed class ResourceState<out T> {
    /**
     * Estado de carga. Se muestra cuando se está cargando o sincronizando un recurso.
     */
    object Loading : ResourceState<Nothing>()
    
    /**
     * Estado de éxito. Contiene los datos del recurso cargado o sincronizado.
     *
     * @param data Datos del recurso.
     */
    data class Success<T>(val data: T?) : ResourceState<T>()
    
    /**
     * Estado de error. Contiene información sobre el error ocurrido.
     *
     * @param message Mensaje de error.
     * @param data Datos parciales que se pudieron cargar, si existen.
     * @param exception Excepción original, si existe.
     */
    data class Error<T>(val message: String?, val data: T? = null, val exception: Exception? = null) : ResourceState<T>()
    
    /**
     * Comprueba si el estado actual es de carga.
     */
    fun isLoading(): Boolean = this is Loading
    
    /**
     * Comprueba si el estado actual es de éxito.
     */
    fun isSuccess(): Boolean = this is Success
    
    /**
     * Comprueba si el estado actual es de error.
     */
    fun isError(): Boolean = this is Error
}