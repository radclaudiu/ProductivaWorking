package com.productiva.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.productiva.android.data.converter.DateConverter
import com.productiva.android.data.converter.MapConverter
import java.util.Date

/**
 * Modelo que representa una plantilla de etiqueta para impresión.
 *
 * @property id ID único de la plantilla.
 * @property name Nombre de la plantilla.
 * @property description Descripción de la plantilla (opcional).
 * @property width Ancho de la etiqueta en milímetros.
 * @property height Alto de la etiqueta en milímetros (opcional para etiquetas continuas).
 * @property dpi Resolución de impresión en puntos por pulgada.
 * @property printerModel Modelo de impresora recomendado (opcional).
 * @property fields Mapa con la definición de campos y sus posiciones.
 * @property isDefault Indica si es la plantilla por defecto.
 * @property companyId ID de la empresa a la que pertenece la plantilla.
 * @property createdAt Fecha de creación de la plantilla.
 * @property updatedAt Fecha de última actualización de la plantilla.
 * @property syncStatus Estado de sincronización de la plantilla.
 * @property pendingChanges Indica si hay cambios pendientes de sincronización.
 */
@Entity(tableName = "label_templates")
@TypeConverters(DateConverter::class, MapConverter::class)
data class LabelTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String? = null,
    val width: Int,
    val height: Int? = null,
    val dpi: Int = 300,
    val printerModel: String? = null,
    val fields: Map<String, FieldPosition> = emptyMap(),
    val isDefault: Boolean = false,
    val companyId: Int,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD,
    val pendingChanges: Boolean = false
) {
    /**
     * Estado de sincronización de la plantilla.
     */
    enum class SyncStatus {
        /** Sincronizada con el servidor. */
        SYNCED,
        /** Pendiente de subir al servidor. */
        PENDING_UPLOAD,
        /** Pendiente de actualizar en el servidor. */
        PENDING_UPDATE,
        /** Pendiente de eliminar en el servidor. */
        PENDING_DELETE
    }
    
    /**
     * Crea una copia de la plantilla con un estado de sincronización específico.
     *
     * @param syncStatus Nuevo estado de sincronización.
     * @return Copia de la plantilla con el nuevo estado.
     */
    fun withSyncStatus(syncStatus: SyncStatus): LabelTemplate {
        return this.copy(syncStatus = syncStatus)
    }
}

/**
 * Clase que define la posición y formato de un campo en una etiqueta.
 *
 * @property x Posición X en milímetros desde el borde izquierdo.
 * @property y Posición Y en milímetros desde el borde superior.
 * @property width Ancho máximo del campo en milímetros (opcional).
 * @property fontSize Tamaño de la fuente en puntos (opcional).
 * @property isBold Indica si el texto debe estar en negrita.
 * @property isItalic Indica si el texto debe estar en cursiva.
 * @property alignment Alineación del texto ('left', 'center', 'right').
 * @property rotation Rotación del texto en grados.
 */
data class FieldPosition(
    val x: Float,
    val y: Float,
    val width: Int? = null,
    val fontSize: Int? = null,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val alignment: String = "left",
    val rotation: Int = 0
)