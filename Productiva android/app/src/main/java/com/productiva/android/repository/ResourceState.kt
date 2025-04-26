package com.productiva.android.repository

/**
 * Clase que representa los diferentes estados de un recurso durante operaciones asíncronas.
 * Utilizada para transmitir el estado actual de una operación a los observadores.
 */
sealed class ResourceState<out T> {
    /**
     * Estado de carga. Indica que la operación está en curso.
     */
    object Loading : ResourceState<Nothing>()
    
    /**
     * Estado de éxito. Contiene los datos resultantes de la operación.
     * 
     * @param data Datos obtenidos en la operación, pueden ser nulos.
     */
    data class Success<T>(val data: T?) : ResourceState<T>()
    
    /**
     * Estado de error. Contiene información sobre el error ocurrido.
     * 
     * @param message Mensaje descriptivo del error.
     * @param errorCode Código de error, útil para identificar tipos específicos de error.
     * @param exception Excepción que causó el error, si existe.
     */
    data class Error(
        val message: String? = null,
        val errorCode: Int? = null,
        val exception: Exception? = null
    ) : ResourceState<Nothing>()
    
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
     * Obtiene los datos si el estado es de éxito, de lo contrario retorna null.
     */
    fun getDataOrNull(): T? = if (this is Success) data else null
    
    /**
     * Obtiene el mensaje de error si el estado es de error, de lo contrario retorna null.
     */
    fun getErrorMessageOrNull(): String? = if (this is Error) message else null
    
    /**
     * Transforma los datos de un ResourceState usando la función proporcionada.
     * 
     * @param transform Función que transforma los datos de tipo T a tipo R.
     * @return Un nuevo ResourceState con los datos transformados o el mismo estado si no es Success.
     */
    fun <R> map(transform: (T?) -> R?): ResourceState<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> Loading
        }
    }
    
    companion object {
        /**
         * Crea un estado de éxito con los datos proporcionados.
         */
        fun <T> success(data: T? = null): ResourceState<T> = Success(data)
        
        /**
         * Crea un estado de error con el mensaje proporcionado.
         */
        fun error(message: String? = null, errorCode: Int? = null, exception: Exception? = null): ResourceState<Nothing> = 
            Error(message, errorCode, exception)
        
        /**
         * Crea un estado de carga.
         */
        fun <T> loading(): ResourceState<T> = Loading
    }
}