package com.productiva.android.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.model.LabelTemplate

/**
 * DAO para operaciones con plantillas de etiquetas en la base de datos local.
 */
@Dao
interface LabelTemplateDao {
    
    /**
     * Inserta una plantilla de etiqueta en la base de datos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: LabelTemplate): Long
    
    /**
     * Inserta varias plantillas de etiquetas en la base de datos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<LabelTemplate>): List<Long>
    
    /**
     * Actualiza la información de una plantilla de etiqueta existente.
     */
    @Update
    suspend fun update(template: LabelTemplate)
    
    /**
     * Obtiene una plantilla de etiqueta por su ID.
     */
    @Query("SELECT * FROM label_templates WHERE id = :templateId")
    suspend fun getTemplateById(templateId: Int): LabelTemplate?
    
    /**
     * Obtiene todas las plantillas de etiquetas.
     */
    @Query("SELECT * FROM label_templates ORDER BY name")
    fun getAllTemplates(): LiveData<List<LabelTemplate>>
    
    /**
     * Obtiene plantillas de etiquetas por compañía.
     */
    @Query("SELECT * FROM label_templates WHERE company_id = :companyId OR company_id IS NULL ORDER BY name")
    fun getTemplatesByCompany(companyId: Int): LiveData<List<LabelTemplate>>
    
    /**
     * Obtiene plantillas de etiquetas favoritas.
     */
    @Query("SELECT * FROM label_templates WHERE is_favorite = 1 ORDER BY name")
    fun getFavoriteTemplates(): LiveData<List<LabelTemplate>>
    
    /**
     * Obtiene las plantillas de etiquetas recientemente usadas.
     */
    @Query("SELECT * FROM label_templates ORDER BY last_used DESC LIMIT :limit")
    fun getRecentlyUsedTemplates(limit: Int = 5): LiveData<List<LabelTemplate>>
    
    /**
     * Busca plantillas de etiquetas por nombre o descripción.
     */
    @Query("SELECT * FROM label_templates WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY name")
    fun searchTemplates(query: String): LiveData<List<LabelTemplate>>
    
    /**
     * Marca una plantilla como favorita o no.
     */
    @Query("UPDATE label_templates SET is_favorite = :isFavorite WHERE id = :templateId")
    suspend fun setFavorite(templateId: Int, isFavorite: Boolean): Int
    
    /**
     * Actualiza el contador de uso y la fecha de último uso de una plantilla.
     */
    @Query("UPDATE label_templates SET times_used = times_used + 1, last_used = :timestamp WHERE id = :templateId")
    suspend fun updateUsage(templateId: Int, timestamp: Long = System.currentTimeMillis()): Int
    
    /**
     * Obtiene las plantillas que coinciden con un tamaño de papel específico.
     */
    @Query("SELECT * FROM label_templates WHERE width <= :paperWidth AND height <= :paperHeight ORDER BY name")
    fun getTemplatesForPaperSize(paperWidth: Int, paperHeight: Int): LiveData<List<LabelTemplate>>
    
    /**
     * Elimina una plantilla de etiqueta por su ID.
     */
    @Query("DELETE FROM label_templates WHERE id = :templateId")
    suspend fun deleteTemplateById(templateId: Int): Int
    
    /**
     * Elimina todas las plantillas de etiquetas.
     */
    @Query("DELETE FROM label_templates")
    suspend fun deleteAllTemplates()
    
    /**
     * Cuenta el número de plantillas de etiquetas.
     */
    @Query("SELECT COUNT(*) FROM label_templates")
    suspend fun getTemplatesCount(): Int
}