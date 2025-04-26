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
     * Actualiza la información de una plantilla existente.
     */
    @Update
    suspend fun update(template: LabelTemplate)
    
    /**
     * Obtiene una plantilla por su ID.
     */
    @Query("SELECT * FROM label_templates WHERE id = :templateId")
    suspend fun getTemplateById(templateId: Int): LabelTemplate?
    
    /**
     * Obtiene todas las plantillas de etiquetas.
     */
    @Query("SELECT * FROM label_templates ORDER BY name")
    fun getAllTemplates(): LiveData<List<LabelTemplate>>
    
    /**
     * Obtiene plantillas favoritas.
     */
    @Query("SELECT * FROM label_templates WHERE is_favorite = 1 ORDER BY name")
    fun getFavoriteTemplates(): LiveData<List<LabelTemplate>>
    
    /**
     * Obtiene plantillas por compañía.
     */
    @Query("SELECT * FROM label_templates WHERE company_id = :companyId OR company_id IS NULL ORDER BY name")
    fun getTemplatesByCompany(companyId: Int): LiveData<List<LabelTemplate>>
    
    /**
     * Busca plantillas por nombre o descripción.
     */
    @Query("SELECT * FROM label_templates WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY name")
    fun searchTemplates(query: String): LiveData<List<LabelTemplate>>
    
    /**
     * Elimina una plantilla por su ID.
     */
    @Query("DELETE FROM label_templates WHERE id = :templateId")
    suspend fun deleteTemplateById(templateId: Int): Int
    
    /**
     * Elimina todas las plantillas.
     */
    @Query("DELETE FROM label_templates")
    suspend fun deleteAllTemplates()
    
    /**
     * Marca una plantilla como favorita.
     */
    @Query("UPDATE label_templates SET is_favorite = 1 WHERE id = :templateId")
    suspend fun markAsFavorite(templateId: Int): Int
    
    /**
     * Quita una plantilla de favoritos.
     */
    @Query("UPDATE label_templates SET is_favorite = 0 WHERE id = :templateId")
    suspend fun unmarkAsFavorite(templateId: Int): Int
    
    /**
     * Incrementa el contador de uso de una plantilla.
     */
    @Query("UPDATE label_templates SET times_used = times_used + 1, last_used = :timestamp WHERE id = :templateId")
    suspend fun incrementUsageCount(templateId: Int, timestamp: Long): Int
    
    /**
     * Obtiene las plantillas más utilizadas.
     */
    @Query("SELECT * FROM label_templates ORDER BY times_used DESC LIMIT :limit")
    fun getMostUsedTemplates(limit: Int = 5): LiveData<List<LabelTemplate>>
    
    /**
     * Obtiene las plantillas usadas recientemente.
     */
    @Query("SELECT * FROM label_templates WHERE last_used > 0 ORDER BY last_used DESC LIMIT :limit")
    fun getRecentlyUsedTemplates(limit: Int = 5): LiveData<List<LabelTemplate>>
    
    /**
     * Verifica si existe alguna plantilla en la base de datos.
     */
    @Query("SELECT COUNT(*) FROM label_templates")
    suspend fun getTemplatesCount(): Int
}