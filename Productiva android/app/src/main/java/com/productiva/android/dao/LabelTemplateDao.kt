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
 * DAO para acceder y manipular plantillas de etiquetas en la base de datos.
 */
@Dao
interface LabelTemplateDao {
    /**
     * Obtiene todas las plantillas de etiquetas.
     */
    @Query("SELECT * FROM label_templates ORDER BY name ASC")
    fun getAllLabelTemplates(): Flow<List<LabelTemplate>>
    
    /**
     * Obtiene todas las plantillas de etiquetas (versión sincrónica).
     */
    @Query("SELECT * FROM label_templates ORDER BY name ASC")
    suspend fun getAllLabelTemplatesSync(): List<LabelTemplate>
    
    /**
     * Obtiene una plantilla de etiqueta por su ID.
     */
    @Query("SELECT * FROM label_templates WHERE id = :templateId")
    fun getLabelTemplateById(templateId: Int): Flow<LabelTemplate?>
    
    /**
     * Obtiene una plantilla de etiqueta por su ID (versión sincrónica).
     */
    @Query("SELECT * FROM label_templates WHERE id = :templateId")
    suspend fun getLabelTemplateByIdSync(templateId: Int): LabelTemplate?
    
    /**
     * Obtiene la plantilla por defecto.
     */
    @Query("SELECT * FROM label_templates WHERE is_default = 1 LIMIT 1")
    fun getDefaultLabelTemplate(): Flow<LabelTemplate?>
    
    /**
     * Obtiene la plantilla por defecto (versión sincrónica).
     */
    @Query("SELECT * FROM label_templates WHERE is_default = 1 LIMIT 1")
    suspend fun getDefaultLabelTemplateSync(): LabelTemplate?
    
    /**
     * Obtiene plantillas de etiquetas por tipo.
     */
    @Query("SELECT * FROM label_templates WHERE template_type = :templateType ORDER BY name ASC")
    fun getLabelTemplatesByType(templateType: String): Flow<List<LabelTemplate>>
    
    /**
     * Obtiene plantillas de etiquetas que necesitan sincronización.
     */
    @Query("SELECT * FROM label_templates WHERE needs_sync = 1")
    fun getLabelTemplatesNeedingSync(): Flow<List<LabelTemplate>>
    
    /**
     * Obtiene plantillas de etiquetas que necesitan sincronización (versión sincrónica).
     */
    @Query("SELECT * FROM label_templates WHERE needs_sync = 1")
    suspend fun getLabelTemplatesNeedingSyncSync(): List<LabelTemplate>
    
    /**
     * Inserta una nueva plantilla de etiqueta.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLabelTemplate(template: LabelTemplate)
    
    /**
     * Inserta múltiples plantillas de etiquetas.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLabelTemplates(templates: List<LabelTemplate>)
    
    /**
     * Actualiza una plantilla de etiqueta existente.
     */
    @Update
    suspend fun updateLabelTemplate(template: LabelTemplate)
    
    /**
     * Actualiza múltiples plantillas de etiquetas.
     */
    @Update
    suspend fun updateLabelTemplates(templates: List<LabelTemplate>)
    
    /**
     * Inserta o actualiza plantillas de etiquetas (upsert).
     */
    @Transaction
    suspend fun upsertLabelTemplates(templates: List<LabelTemplate>) {
        for (template in templates) {
            val existingTemplate = getLabelTemplateByIdSync(template.id)
            if (existingTemplate == null) {
                insertLabelTemplate(template)
            } else if (!existingTemplate.needsSync) {
                // Solo actualizar si la plantilla local no necesita sincronización
                updateLabelTemplate(template)
            }
        }
    }
    
    /**
     * Elimina todas las plantillas de etiquetas (solo para migraciones o resets).
     */
    @Query("DELETE FROM label_templates")
    suspend fun deleteAllLabelTemplates()
}