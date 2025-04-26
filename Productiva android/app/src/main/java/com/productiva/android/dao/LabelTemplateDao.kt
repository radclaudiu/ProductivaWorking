package com.productiva.android.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.productiva.android.model.LabelTemplate
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para las plantillas de etiquetas.
 * Proporciona métodos para acceder y manipular la tabla de plantillas de etiquetas.
 */
@Dao
interface LabelTemplateDao {
    /**
     * Inserta una plantilla de etiqueta en la base de datos.
     * Si ya existe una plantilla con el mismo ID, la reemplaza.
     *
     * @param labelTemplate Plantilla de etiqueta a insertar.
     * @return ID de la plantilla insertada.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabelTemplate(labelTemplate: LabelTemplate): Long
    
    /**
     * Inserta múltiples plantillas de etiquetas en la base de datos.
     * Si ya existe alguna plantilla con el mismo ID, la reemplaza.
     *
     * @param labelTemplates Lista de plantillas de etiquetas a insertar.
     * @return Lista de IDs de las plantillas insertadas.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabelTemplates(labelTemplates: List<LabelTemplate>): List<Long>
    
    /**
     * Actualiza una plantilla de etiqueta existente.
     *
     * @param labelTemplate Plantilla de etiqueta a actualizar.
     * @return Número de filas actualizadas.
     */
    @Update
    suspend fun updateLabelTemplate(labelTemplate: LabelTemplate): Int
    
    /**
     * Obtiene todas las plantillas de etiquetas.
     *
     * @return Flow con la lista de todas las plantillas de etiquetas.
     */
    @Query("SELECT * FROM label_templates ORDER BY name")
    fun getAllLabelTemplates(): Flow<List<LabelTemplate>>
    
    /**
     * Obtiene una plantilla de etiqueta por su ID.
     *
     * @param templateId ID de la plantilla.
     * @return Flow con la plantilla, o null si no existe.
     */
    @Query("SELECT * FROM label_templates WHERE id = :templateId")
    fun getLabelTemplateById(templateId: Int): Flow<LabelTemplate?>
    
    /**
     * Obtiene la plantilla de etiqueta por defecto.
     *
     * @return Flow con la plantilla por defecto, o null si no hay ninguna.
     */
    @Query("SELECT * FROM label_templates WHERE isDefault = 1 LIMIT 1")
    fun getDefaultLabelTemplate(): Flow<LabelTemplate?>
    
    /**
     * Obtiene todas las plantillas de etiquetas para una empresa específica.
     *
     * @param companyId ID de la empresa.
     * @return Flow con la lista de plantillas de la empresa.
     */
    @Query("SELECT * FROM label_templates WHERE companyId = :companyId OR companyId IS NULL ORDER BY name")
    fun getLabelTemplatesByCompanyId(companyId: Int): Flow<List<LabelTemplate>>
    
    /**
     * Elimina una plantilla de etiqueta por su ID.
     *
     * @param templateId ID de la plantilla a eliminar.
     * @return Número de filas eliminadas.
     */
    @Query("DELETE FROM label_templates WHERE id = :templateId")
    suspend fun deleteLabelTemplateById(templateId: Int): Int
    
    /**
     * Elimina todas las plantillas de etiquetas.
     */
    @Query("DELETE FROM label_templates")
    suspend fun deleteAllLabelTemplates()
    
    /**
     * Obtiene una plantilla de etiqueta por su ID de forma síncrona.
     *
     * @param templateId ID de la plantilla.
     * @return La plantilla, o null si no existe.
     */
    @Query("SELECT * FROM label_templates WHERE id = :templateId")
    suspend fun getLabelTemplateByIdSync(templateId: Int): LabelTemplate?
    
    /**
     * Marca todas las plantillas como no predeterminadas.
     */
    @Query("UPDATE label_templates SET isDefault = 0")
    suspend fun clearDefaultLabelTemplate()
    
    /**
     * Establece una plantilla como predeterminada.
     *
     * @param templateId ID de la plantilla a establecer como predeterminada.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE label_templates SET isDefault = 1 WHERE id = :templateId")
    suspend fun setDefaultLabelTemplate(templateId: Int): Int
    
    /**
     * Establece una plantilla como predeterminada, asegurándose de que solo hay una.
     * Esta función ejecuta una transacción que primero elimina cualquier plantilla predeterminada
     * y luego establece la nueva plantilla como predeterminada.
     *
     * @param templateId ID de la plantilla a establecer como predeterminada.
     */
    @Transaction
    suspend fun setAsDefaultTemplate(templateId: Int) {
        clearDefaultLabelTemplate()
        setDefaultLabelTemplate(templateId)
    }
    
    /**
     * Transacción para sincronizar plantillas desde el servidor.
     * Inserta nuevas plantillas, actualiza existentes y elimina las que ya no existen.
     *
     * @param templates Lista de plantillas del servidor.
     * @param deletedIds Lista de IDs de plantillas eliminadas en el servidor.
     * @param syncTime Timestamp de la sincronización.
     */
    @Transaction
    suspend fun syncLabelTemplatesFromServer(templates: List<LabelTemplate>, deletedIds: List<Int>, syncTime: Long) {
        // Eliminar plantillas marcadas como eliminadas
        if (deletedIds.isNotEmpty()) {
            for (id in deletedIds) {
                deleteLabelTemplateById(id)
            }
        }
        
        // Insertar o actualizar plantillas
        val templatesWithSyncTime = templates.map { template ->
            template.copy(lastSyncTime = syncTime)
        }
        
        if (templatesWithSyncTime.isNotEmpty()) {
            insertLabelTemplates(templatesWithSyncTime)
        }
    }
}