package com.productiva.android.repository

/**
 * Clase genérica que representa el estado de un recurso durante operaciones asíncronas.
 * Utilizada para transmitir el estado de carga, éxito, error o inactividad.
 * 
 * @param T el tipo de datos que se maneja en este estado
 * @property data los datos opcionales asociados con este estado
 * @property message mensaje opcional para estados de error o información
 */
sealed class ResourceState<T>(
    open val data: T? = null,
    open val message: String? = null
) {
    /**
     * Estado inactivo: no se está realizando ninguna operación.
     */
    class Idle<T> : ResourceState<T>()
    
    /**
     * Estado de carga: se está realizando una operación asíncrona.
     * 
     * @param data datos opcionales que pueden estar disponibles durante la carga
     */
    class Loading<T>(override val data: T? = null) : ResourceState<T>()
    
    /**
     * Estado de éxito: la operación se completó correctamente.
     * 
     * @param data los datos resultantes de la operación
     */
    class Success<T>(override val data: T) : ResourceState<T>()
    
    /**
     * Estado de error: la operación falló.
     * 
     * @param message descripción del error
     * @param data datos opcionales que pueden estar disponibles a pesar del error
     */
    class Error<T>(override val message: String, override val data: T? = null) : ResourceState<T>()
    
    /**
     * Determina si el estado actual es de tipo Loading.
     */
    fun isLoading(): Boolean = this is Loading
    
    /**
     * Determina si el estado actual es de tipo Success.
     */
    fun isSuccess(): Boolean = this is Success
    
    /**
     * Determina si el estado actual es de tipo Error.
     */
    fun isError(): Boolean = this is Error
    
    /**
     * Determina si el estado actual es de tipo Idle.
     */
    fun isIdle(): Boolean = this is Idle
}