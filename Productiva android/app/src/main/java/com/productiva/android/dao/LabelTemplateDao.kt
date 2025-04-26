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
     * Inserta nuevas plantillas en la base de datos.
     * Si ya existen plantillas con los mismos IDs, las reemplaza.
     *
     * @param templates Lista de plantillas a insertar.
     * @return Lista de IDs de las plantillas insertadas.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<LabelTemplate>): List<Long>
    
    /**
     * Inserta una nueva plantilla en la base de datos.
     * Si ya existe una plantilla con el mismo ID, la reemplaza.
     *
     * @param template Plantilla a insertar.
     * @return ID de la plantilla insertada.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: LabelTemplate): Long
    
    /**
     * Actualiza una plantilla existente.
     *
     * @param template Plantilla a actualizar.
     * @return Número de filas actualizadas.
     */
    @Update
    suspend fun updateTemplate(template: LabelTemplate): Int
    
    /**
     * Obtiene todas las plantillas de etiquetas.
     *
     * @return Flow con la lista de todas las plantillas.
     */
    @Query("SELECT * FROM label_templates ORDER BY name ASC")
    fun getAllTemplates(): Flow<List<LabelTemplate>>
    
    /**
     * Obtiene una plantilla específica por su ID.
     *
     * @param templateId ID de la plantilla.
     * @return Flow con la plantilla, o null si no existe.
     */
    @Query("SELECT * FROM label_templates WHERE id = :templateId")
    fun getTemplateById(templateId: Int): Flow<LabelTemplate?>
    
    /**
     * Obtiene plantillas por tipo.
     *
     * @param type Tipo de plantilla (product, location, asset, custom).
     * @return Flow con la lista de plantillas del tipo especificado.
     */
    @Query("SELECT * FROM label_templates WHERE type = :type ORDER BY name ASC")
    fun getTemplatesByType(type: String): Flow<List<LabelTemplate>>
    
    /**
     * Obtiene la plantilla predeterminada para un tipo específico.
     *
     * @param type Tipo de plantilla.
     * @return Flow con la plantilla predeterminada, o null si no hay ninguna.
     */
    @Query("SELECT * FROM label_templates WHERE type = :type AND isDefault = 1 LIMIT 1")
    fun getDefaultTemplateForType(type: String): Flow<LabelTemplate?>
    
    /**
     * Obtiene plantillas pendientes de sincronización.
     *
     * @return Lista de plantillas pendientes de sincronización.
     */
    @Query("SELECT * FROM label_templates WHERE needsSync = 1")
    suspend fun getTemplatesPendingSync(): List<LabelTemplate>
    
    /**
     * Marca una plantilla como sincronizada.
     *
     * @param templateId ID de la plantilla.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE label_templates SET needsSync = 0 WHERE id = :templateId")
    suspend fun markAsSynced(templateId: Int): Int
    
    /**
     * Incrementa el contador de uso local de una plantilla.
     *
     * @param templateId ID de la plantilla.
     * @return Número de filas actualizadas.
     */
    @Transaction
    suspend fun incrementUseCount(templateId: Int): Int {
        val template = getTemplateByIdSync(templateId) ?: return 0
        val updated = template.incrementUseCount()
        updateTemplate(updated)
        return 1
    }
    
    /**
     * Obtiene una plantilla por su ID de forma síncrona.
     *
     * @param templateId ID de la plantilla.
     * @return La plantilla, o null si no existe.
     */
    @Query("SELECT * FROM label_templates WHERE id = :templateId")
    suspend fun getTemplateByIdSync(templateId: Int): LabelTemplate?
    
    /**
     * Actualiza una plantilla desde el servidor, preservando los cambios locales.
     * Si la plantilla no existe, la inserta.
     *
     * @param serverTemplate Plantilla recibida del servidor.
     * @return Plantilla actualizada o insertada.
     */
    @Transaction
    suspend fun upsertFromServer(serverTemplate: LabelTemplate): LabelTemplate {
        val existingTemplate = getTemplateByIdSync(serverTemplate.id)
        
        val templateToSave = existingTemplate?.updateFromServer(serverTemplate) ?: serverTemplate
        
        insertTemplate(templateToSave)
        
        return templateToSave
    }
    
    /**
     * Elimina todas las plantillas de etiquetas.
     */
    @Query("DELETE FROM label_templates")
    suspend fun deleteAllTemplates()
}