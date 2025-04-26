package com.productiva.android.repository

/**
 * Clase genérica que representa el estado de un recurso.
 * Permite manejar de manera unificada diferentes estados de carga, éxito y error.
 */
sealed class ResourceState<T> {
    /**
     * Estado inicial o de carga.
     */
    class Loading<T> : ResourceState<T>()
    
    /**
     * Estado de éxito con los datos cargados.
     *
     * @property data Datos del recurso.
     */
    data class Success<T>(val data: T) : ResourceState<T>()
    
    /**
     * Estado de error.
     *
     * @property message Mensaje de error.
     * @property throwable Excepción que causó el error (opcional).
     */
    data class Error<T>(
        val message: String,
        val throwable: Throwable? = null
    ) : ResourceState<T>()
    
    /**
     * Estado de datos en caché mientras se está cargando la actualización.
     *
     * @property data Datos del caché.
     */
    data class CachedData<T>(val data: T) : ResourceState<T>()
    
    /**
     * Estado sin conexión.
     */
    class Offline<T> : ResourceState<T>()
    
    /**
     * Verifica si el estado actual es de carga.
     */
    fun isLoading(): Boolean = this is Loading
    
    /**
     * Verifica si el estado actual es de éxito.
     */
    fun isSuccess(): Boolean = this is Success
    
    /**
     * Verifica si el estado actual es de error.
     */
    fun isError(): Boolean = this is Error
    
    /**
     * Verifica si el estado actual es de datos en caché.
     */
    fun isCachedData(): Boolean = this is CachedData
    
    /**
     * Verifica si el estado actual es sin conexión.
     */
    fun isOffline(): Boolean = this is Offline
    
    /**
     * Obtiene los datos si el estado es de éxito o de caché.
     */
    fun getDataOrNull(): T? {
        return when (this) {
            is Success -> data
            is CachedData -> data
            else -> null
        }
    }
}