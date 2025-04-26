package com.productiva.android.network.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de petición para sincronización de datos.
 * Contiene las entidades a sincronizar (a enviar al servidor).
 *
 * @param T Tipo de la entidad a sincronizar.
 * @property lastSyncTimestamp Marca de tiempo de la última sincronización.
 * @property clientChanges Lista de entidades con cambios pendientes de sincronizar.
 */
data class SyncRequest<T>(
    @SerializedName("last_sync_timestamp") val lastSyncTimestamp: Long,
    @SerializedName("client_changes") val clientChanges: List<T>
)