package com.productiva.android.repository

/**
 * Clase sellada que representa el estado actual de un recurso que se está cargando.
 * Utilizada para comunicar el estado de carga, éxito o error a la UI.
 *
 * @param T Tipo de datos del recurso.
 * @property data Datos del recurso (pueden ser nulos).
 */
sealed class ResourceState<T>(
    open val data: T? = null
) {
    /**
     * Estado de carga.
     * Los datos pueden contener el último valor conocido mientras se carga nueva información.
     *
     * @property data Datos actuales (pueden ser nulos).
     */
    class Loading<T>(override val data: T? = null) : ResourceState<T>(data)
    
    /**
     * Estado de éxito con datos.
     *
     * @property data Datos cargados exitosamente.
     * @property isFromCache Indica si los datos provienen de caché local (sin verificación reciente del servidor).
     */
    class Success<T>(
        override val data: T,
        val isFromCache: Boolean = false
    ) : ResourceState<T>(data)
    
    /**
     * Estado de error.
     *
     * @property message Mensaje de error.
     * @property errorCode Código de error (opcional).
     * @property data Datos actuales que podrían estar disponibles a pesar del error.
     */
    class Error<T>(
        val message: String,
        val errorCode: Int? = null,
        override val data: T? = null
    ) : ResourceState<T>(data)
}