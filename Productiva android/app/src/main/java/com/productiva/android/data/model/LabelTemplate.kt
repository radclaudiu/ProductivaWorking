package com.productiva.android.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.productiva.android.data.converter.DateConverter
import com.productiva.android.data.converter.MapConverter
import java.util.Date

/**
 * Entidad que representa una plantilla de etiqueta para impresión.
 *
 * @property id Identificador único de la plantilla.
 * @property name Nombre descriptivo de la plantilla.
 * @property description Descripción detallada de la plantilla (opcional).
 * @property width Ancho de la etiqueta en milímetros.
 * @property height Alto de la etiqueta en milímetros (opcional para etiquetas continuas).
 * @property dpi Resolución de impresión en puntos por pulgada.
 * @property orientation Orientación de la etiqueta ("portrait" o "landscape").
 * @property templateType Tipo de plantilla ("zpl", "epl", "brother", "custom").
 * @property templateContent Contenido de la plantilla en el formato correspondiente.
 * @property fields Mapa de campos variables y sus coordenadas en la etiqueta.
 * @property defaultValues Valores predeterminados para los campos variables (opcional).
 * @property isDefault Indica si es la plantilla predeterminada.
 * @property companyId ID de la empresa a la que pertenece la plantilla.
 * @property createdBy ID del usuario que creó la plantilla.
 * @property createdAt Fecha de creación de la plantilla.
 * @property updatedAt Fecha de última actualización de la plantilla.
 * @property syncStatus Estado de sincronización con el servidor.
 * @property lastSyncTime Marca de tiempo de la última sincronización.
 * @property isDeleted Indica si la plantilla ha sido marcada para eliminación.
 * @property pendingChanges Indica si hay cambios locales pendientes de sincronizar.
 */
@Entity(
    tableName = "label_templates",
    indices = [
        Index("companyId"),
        Index("syncStatus")
    ]
)
@TypeConverters(DateConverter::class, MapConverter::class)
data class LabelTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val name: String,
    val description: String? = null,
    
    val width: Int, // Width in mm
    val height: Int? = null, // Height in mm, null for continuous labels
    val dpi: Int, // Resolution in DPI
    val orientation: String, // "portrait" or "landscape"
    
    val templateType: String, // "zpl", "epl", "brother", "custom"
    val templateContent: String, // Template content in the corresponding format
    
    val fields: Map<String, FieldPosition> = emptyMap(),
    val defaultValues: Map<String, String>? = null,
    
    val isDefault: Boolean = false,
    
    val companyId: Int,
    val createdBy: Int,
    val createdAt: Date,
    val updatedAt: Date,
    
    // Campos para sincronización
    val syncStatus: String = SyncStatus.SYNCED, // "synced", "pending_upload", "pending_update", "conflict"
    val lastSyncTime: Long = 0,
    val isDeleted: Boolean = false,
    val pendingChanges: Boolean = false
) {
    /**
     * Crea una copia de la plantilla con estado predeterminado actualizado.
     *
     * @param isDefault Nuevo estado predeterminado.
     * @return Plantilla actualizada con nuevo estado predeterminado.
     */
    fun setAsDefault(isDefault: Boolean): LabelTemplate {
        return copy(
            isDefault = isDefault,
            updatedAt = Date(),
            syncStatus = SyncStatus.PENDING_UPDATE,
            pendingChanges = true
        )
    }
    
    /**
     * Crea una copia de la plantilla marcada para eliminación.
     *
     * @return Plantilla actualizada marcada para eliminación.
     */
    fun markForDeletion(): LabelTemplate {
        return copy(
            isDeleted = true,
            syncStatus = SyncStatus.PENDING_DELETE,
            pendingChanges = true,
            updatedAt = Date()
        )
    }
    
    /**
     * Crea una copia de la plantilla con estado de sincronización actualizado.
     *
     * @param newSyncStatus Nuevo estado de sincronización.
     * @return Plantilla actualizada con nuevo estado de sincronización.
     */
    fun withSyncStatus(newSyncStatus: String, lastSyncTime: Long = System.currentTimeMillis()): LabelTemplate {
        return copy(
            syncStatus = newSyncStatus,
            lastSyncTime = lastSyncTime,
            pendingChanges = newSyncStatus != SyncStatus.SYNCED
        )
    }
    
    /**
     * Clase auxiliar que define constantes para los estados de sincronización.
     */
    object SyncStatus {
        const val SYNCED = "synced"
        const val PENDING_UPLOAD = "pending_upload"
        const val PENDING_UPDATE = "pending_update"
        const val PENDING_DELETE = "pending_delete"
        const val CONFLICT = "conflict"
    }
}

/**
 * Clase que representa la posición y características de un campo en una plantilla.
 *
 * @property x Posición X en puntos (1/72 de pulgada).
 * @property y Posición Y en puntos (1/72 de pulgada).
 * @property width Ancho del campo en puntos (opcional).
 * @property height Alto del campo en puntos (opcional).
 * @property fontSize Tamaño de la fuente en puntos (opcional).
 * @property alignment Alineación del texto ("left", "center", "right").
 * @property rotation Rotación del texto en grados (0, 90, 180, 270).
 * @property isBold Indica si el texto debe estar en negrita.
 * @property isItalic Indica si el texto debe estar en cursiva.
 * @property fontFamily Familia de fuente a utilizar (opcional).
 */
data class FieldPosition(
    val x: Int,
    val y: Int,
    val width: Int? = null,
    val height: Int? = null,
    val fontSize: Int? = 10,
    val alignment: String = "left", // "left", "center", "right"
    val rotation: Int = 0, // 0, 90, 180, 270
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val fontFamily: String? = null
)