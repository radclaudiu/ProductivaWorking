package com.productiva.android.repository

/**
 * Clase sellada para representar los diferentes estados de un recurso de datos.
 * Utilizada para manejar el estado de las operaciones asíncronas como las peticiones a la API.
 */
sealed class ResourceState<T>(
    val data: T? = null,
    val message: String? = null,
    val isFromCache: Boolean = false
) {
    /**
     * Estado que representa que la operación está cargando.
     */
    class Loading<T> : ResourceState<T>()
    
    /**
     * Estado que representa que la operación ha sido exitosa.
     */
    class Success<T>(data: T, isFromCache: Boolean = false) : ResourceState<T>(data, isFromCache = isFromCache)
    
    /**
     * Estado que representa que la operación ha fallado.
     */
    class Error<T>(message: String, data: T? = null) : ResourceState<T>(data, message)
    
    /**
     * Función para verificar si el estado actual es de éxito.
     */
    fun isSuccess(): Boolean = this is Success
    
    /**
     * Función para verificar si el estado actual es de error.
     */
    fun isError(): Boolean = this is Error
    
    /**
     * Función para verificar si el estado actual es de carga.
     */
    fun isLoading(): Boolean = this is Loading
}