package com.productiva.android.repository

/**
 * Clase sellada que representa los diferentes estados de un recurso (datos)
 * durante su ciclo de vida en la aplicación.
 *
 * @param T Tipo de datos que contiene el recurso.
 */
sealed class ResourceState<out T> {
    /**
     * Estado de carga. Puede contener datos anteriores mientras se carga la nueva información.
     *
     * @property data Datos opcionales que pueden mostrarse mientras se carga.
     * @property isRefreshing Indica si esta carga es un refresco de datos existentes.
     */
    data class Loading<T>(
        val data: T? = null,
        val isRefreshing: Boolean = false
    ) : ResourceState<T>()
    
    /**
     * Estado de éxito con los datos cargados.
     *
     * @property data Datos cargados exitosamente.
     * @property isFromCache Indica si los datos provienen de la caché local.
     * @property lastSyncTime Marca de tiempo de la última sincronización con el servidor.
     */
    data class Success<T>(
        val data: T,
        val isFromCache: Boolean = false,
        val lastSyncTime: Long = System.currentTimeMillis()
    ) : ResourceState<T>()
    
    /**
     * Estado de error. Puede contener datos anteriores que pueden mostrarse como fallback.
     *
     * @property message Mensaje de error.
     * @property errorCode Código de error opcional (por ejemplo, código HTTP).
     * @property data Datos opcionales anteriores que pueden mostrarse como fallback.
     */
    data class Error<T>(
        val message: String? = null,
        val errorCode: Int? = null,
        val data: T? = null
    ) : ResourceState<T>()
    
    /**
     * Extensión que transforma un ResourceState a otro tipo mientras preserva su estado.
     */
    fun <R> map(transform: (T) -> R): ResourceState<R> {
        return when (this) {
            is Loading -> Loading(data?.let { transform(it) }, isRefreshing)
            is Success -> Success(transform(data), isFromCache, lastSyncTime)
            is Error -> Error(message, errorCode, data?.let { transform(it) })
        }
    }
    
    /**
     * Comprueba si el estado actual contiene datos que pueden mostrarse.
     */
    fun hasData(): Boolean {
        return when (this) {
            is Loading -> data != null
            is Success -> true
            is Error -> data != null
        }
    }
    
    /**
     * Obtiene los datos actuales si están disponibles, o null en caso contrario.
     */
    fun getData(): T? {
        return when (this) {
            is Loading -> data
            is Success -> data
            is Error -> data
        }
    }
}