package com.productiva.android.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para plantillas de etiquetas.
 * Representa una plantilla para impresión de etiquetas en impresoras Brother.
 * 
 * @property id Identificador único de la plantilla
 * @property companyId ID de la empresa a la que pertenece
 * @property name Nombre descriptivo de la plantilla
 * @property width Ancho de la etiqueta en mm
 * @property height Alto de la etiqueta en mm
 * @property labelType Tipo de etiqueta (product, employee_badge, shipping, etc.)
 * @property content Contenido de la plantilla en formato JSON
 * @property templateFile Archivo de plantilla binario (solo para plantillas Brother P-touch)
 * @property active Indica si la plantilla está activa
 * @property createdAt Fecha de creación
 * @property updatedAt Fecha de última actualización
 * @property syncStatus Estado de sincronización con el servidor
 */
@Entity(
    tableName = "label_templates",
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
data class LabelTemplate(
    @PrimaryKey
    @SerializedName("id")
    val id: Int = 0,  // 0 para plantillas locales aún no sincronizadas
    
    @SerializedName("company_id")
    val companyId: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("width")
    val width: Int,
    
    @SerializedName("height")
    val height: Int,
    
    @SerializedName("label_type")
    val labelType: String,
    
    @SerializedName("content")
    val content: String,  // JSON con la definición de la plantilla
    
    @SerializedName("template_file")
    val templateFile: ByteArray? = null,  // Binario para plantillas Brother P-touch
    
    @SerializedName("active")
    val active: Boolean = true,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    
    // Campos locales (no se envían al servidor)
    val syncStatus: String = "SYNCED"  // SYNCED, PENDING_SYNC, SYNC_ERROR
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LabelTemplate

        if (id != other.id) return false
        if (companyId != other.companyId) return false
        if (name != other.name) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (labelType != other.labelType) return false
        if (content != other.content) return false
        if (templateFile != null) {
            if (other.templateFile == null) return false
            if (!templateFile.contentEquals(other.templateFile)) return false
        } else if (other.templateFile != null) return false
        if (active != other.active) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false
        if (syncStatus != other.syncStatus) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + companyId
        result = 31 * result + name.hashCode()
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + labelType.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + (templateFile?.contentHashCode() ?: 0)
        result = 31 * result + active.hashCode()
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        result = 31 * result + (updatedAt?.hashCode() ?: 0)
        result = 31 * result + syncStatus.hashCode()
        return result
    }
}