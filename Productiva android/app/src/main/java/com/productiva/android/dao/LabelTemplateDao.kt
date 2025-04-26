package com.productiva.android.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.model.LabelTemplate

/**
 * DAO para interactuar con la tabla de plantillas de etiquetas
 */
@Dao
interface LabelTemplateDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: LabelTemplate): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<LabelTemplate>)
    
    @Update
    suspend fun update(template: LabelTemplate)
    
    @Query("SELECT * FROM label_templates WHERE id = :templateId")
    suspend fun getTemplateById(templateId: Int): LabelTemplate?
    
    @Query("SELECT * FROM label_templates ORDER BY name ASC")
    fun getAllTemplates(): LiveData<List<LabelTemplate>>
    
    @Query("SELECT * FROM label_templates WHERE user_id = :userId ORDER BY name ASC")
    fun getTemplatesByUser(userId: Int): LiveData<List<LabelTemplate>>
    
    @Query("SELECT * FROM label_templates WHERE company_id = :companyId ORDER BY name ASC")
    fun getTemplatesByCompany(companyId: Int): LiveData<List<LabelTemplate>>
    
    @Query("SELECT * FROM label_templates WHERE is_default = 1 LIMIT 1")
    suspend fun getDefaultTemplate(): LabelTemplate?
    
    @Query("UPDATE label_templates SET is_default = 0")
    suspend fun clearDefaultTemplates()
    
    @Query("UPDATE label_templates SET is_default = 1 WHERE id = :templateId")
    suspend fun setDefaultTemplate(templateId: Int)
    
    @Query("DELETE FROM label_templates")
    suspend fun deleteAll()
    
    @Query("DELETE FROM label_templates WHERE id = :templateId")
    suspend fun deleteTemplateById(templateId: Int)
    
    @Query("SELECT COUNT(*) FROM label_templates")
    suspend fun getTemplateCount(): Int
    
    @Query("SELECT * FROM label_templates WHERE last_sync < :timestamp")
    suspend fun getTemplatesToSync(timestamp: Long): List<LabelTemplate>
}