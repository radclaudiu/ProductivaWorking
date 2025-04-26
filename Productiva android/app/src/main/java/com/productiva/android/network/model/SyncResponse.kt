package com.productiva.android.network.model

/**
 * Clase que representa la respuesta de una sincronización de datos.
 * Contiene listas para elementos añadidos, actualizados y eliminados.
 *
 * @param T Tipo de los datos de la respuesta.
 * @property added Lista de elementos añadidos desde la última sincronización.
 * @property updated Lista de elementos actualizados desde la última sincronización.
 * @property deleted Lista de IDs de elementos eliminados desde la última sincronización.
 */
data class SyncResponse<T>(
    val added: List<T> = emptyList(),
    val updated: List<T> = emptyList(),
    val deleted: List<Int> = emptyList()
)