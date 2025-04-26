package com.productiva.android.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para puntos de fichaje.
 * Representa la estructura de datos de un punto de fichaje en el sistema.
 * 
 * @property id Identificador único del punto de fichaje
 * @property companyId ID de la empresa a la que pertenece
 * @property name Nombre descriptivo del punto de fichaje
 * @property location Ubicación física del punto
 * @property ipAddress Dirección IP del dispositivo (opcional)
 * @property deviceId Identificador único del dispositivo
 * @property active Indica si el punto de fichaje está activo
 * @property operationStartTime Hora de inicio de operación (para cierre automático)
 * @property operationEndTime Hora de fin de operación (para cierre automático)
 * @property autoClose Indica si se debe realizar cierre automático
 * @property lastSyncTimestamp Timestamp de la última sincronización
 */
@Entity(
    tableName = "checkpoints",
    foreignKeys = [
        ForeignKey(
            entity = Company::class,
            parentColumns = ["id"],
            childColumns = ["companyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("companyId")]
)
data class Checkpoint(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("company_id")
    val companyId: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("location")
    val location: String? = null,
    
    @SerializedName("ip_address")
    val ipAddress: String? = null,
    
    @SerializedName("device_id")
    val deviceId: String? = null,
    
    @SerializedName("active")
    val active: Boolean = true,
    
    @SerializedName("operation_start_time")
    val operationStartTime: String? = null,
    
    @SerializedName("operation_end_time")
    val operationEndTime: String? = null,
    
    @SerializedName("auto_close")
    val autoClose: Boolean = false,
    
    @SerializedName("last_sync_timestamp")
    val lastSyncTimestamp: Long = 0
)