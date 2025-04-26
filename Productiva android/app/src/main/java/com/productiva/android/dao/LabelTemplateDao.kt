package com.productiva.android.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.productiva.android.model.LabelTemplate
import kotlinx.coroutines.flow.Flow

/**
 * DAO para acceder y manipular plantillas de etiquetas en la base de datos.
 */
@Dao
interface LabelTemplateDao {
    
    /**
     * Obtiene todas las plantillas de etiquetas.
     *
     * @return Flow con la lista de plantillas.
     */
    @Query("SELECT * FROM label_templates ORDER BY name ASC")
    fun getAllLabelTemplates(): Flow<List<LabelTemplate>>
    
    /**
     * Obtiene la plantilla de etiqueta predeterminada.
     *
     * @return Flow con la plantilla predeterminada (o null si no existe).
     */
    @Query("SELECT * FROM label_templates WHERE isDefault = 1 LIMIT 1")
    fun getDefaultLabelTemplate(): Flow<LabelTemplate?>
    
    /**
     * Obtiene una plantilla de etiqueta por su ID.
     *
     * @param templateId ID de la plantilla.
     * @return Flow con la plantilla (o null si no existe).
     */
    @Query("SELECT * FROM label_templates WHERE id = :templateId")
    fun getLabelTemplateById(templateId: Int): Flow<LabelTemplate?>
    
    /**
     * Obtiene una plantilla de etiqueta por su ID de forma síncrona.
     *
     * @param templateId ID de la plantilla.
     * @return La plantilla (o null si no existe).
     */
    @Query("SELECT * FROM label_templates WHERE id = :templateId")
    suspend fun getLabelTemplateByIdSync(templateId: Int): LabelTemplate?
    
    /**
     * Obtiene plantillas de etiquetas para una empresa específica.
     *
     * @param companyId ID de la empresa.
     * @return Flow con la lista de plantillas.
     */
    @Query("SELECT * FROM label_templates WHERE companyId = :companyId ORDER BY name ASC")
    fun getLabelTemplatesByCompany(companyId: Int): Flow<List<LabelTemplate>>
    
    /**
     * Inserta una plantilla de etiqueta en la base de datos.
     *
     * @param template Plantilla a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabelTemplate(template: LabelTemplate)
    
    /**
     * Inserta varias plantillas de etiqueta en la base de datos.
     *
     * @param templates Lista de plantillas a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabelTemplates(templates: List<LabelTemplate>)
    
    /**
     * Elimina una plantilla de etiqueta por su ID.
     *
     * @param templateId ID de la plantilla a eliminar.
     * @return Número de filas eliminadas.
     */
    @Query("DELETE FROM label_templates WHERE id = :templateId")
    suspend fun deleteLabelTemplateById(templateId: Int): Int
    
    /**
     * Establece una plantilla como predeterminada y quita esta marca del resto.
     *
     * @param templateId ID de la plantilla a establecer como predeterminada.
     */
    @Transaction
    suspend fun setAsDefaultTemplate(templateId: Int) {
        // Quitar marca de plantilla predeterminada de todas las plantillas
        clearDefaultTemplate()
        
        // Establecer la plantilla especificada como predeterminada
        markAsDefaultTemplate(templateId)
    }
    
    /**
     * Quita la marca de plantilla predeterminada de todas las plantillas.
     */
    @Query("UPDATE label_templates SET isDefault = 0")
    suspend fun clearDefaultTemplate()
    
    /**
     * Marca una plantilla como predeterminada.
     *
     * @param templateId ID de la plantilla a marcar como predeterminada.
     */
    @Query("UPDATE label_templates SET isDefault = 1 WHERE id = :templateId")
    suspend fun markAsDefaultTemplate(templateId: Int)
    
    /**
     * Sincroniza las plantillas con los datos del servidor.
     * Inserta o actualiza las plantillas recibidas, y elimina las que ya no existen en el servidor.
     *
     * @param templates Plantillas recibidas del servidor.
     * @param templatesToDelete IDs de plantillas a eliminar (opcional).
     * @param syncTime Timestamp de la sincronización.
     */
    @Transaction
    suspend fun syncLabelTemplatesFromServer(templates: List<LabelTemplate>, templatesToDelete: List<Int>, syncTime: Long) {
        // Insertar o actualizar plantillas recibidas
        for (template in templates) {
            insertLabelTemplate(template.copy(lastSyncTime = syncTime))
        }
        
        // Eliminar plantillas que ya no existen en el servidor
        if (templatesToDelete.isNotEmpty()) {
            deleteLabelTemplatesByIds(templatesToDelete)
        }
    }
    
    /**
     * Elimina varias plantillas por sus IDs.
     *
     * @param templateIds Lista de IDs de plantillas a eliminar.
     */
    @Query("DELETE FROM label_templates WHERE id IN (:templateIds)")
    suspend fun deleteLabelTemplatesByIds(templateIds: List<Int>)
}