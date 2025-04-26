package com.productiva.android.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.model.LabelTemplate

/**
 * Interfaz de acceso a datos para la entidad LabelTemplate
 */
@Dao
interface LabelTemplateDao {
    
    /**
     * Obtiene todas las plantillas de etiquetas
     */
    @Query("SELECT * FROM label_templates ORDER BY name ASC")
    fun getAllTemplates(): LiveData<List<LabelTemplate>>
    
    /**
     * Obtiene una plantilla por su ID
     */
    @Query("SELECT * FROM label_templates WHERE id = :templateId LIMIT 1")
    suspend fun getTemplateById(templateId: Int): LabelTemplate?
    
    /**
     * Obtiene plantillas por usuario
     */
    @Query("SELECT * FROM label_templates WHERE userId = :userId ORDER BY name ASC")
    fun getTemplatesByUser(userId: Int): LiveData<List<LabelTemplate>>
    
    /**
     * Obtiene plantillas por empresa
     */
    @Query("SELECT * FROM label_templates WHERE companyId = :companyId ORDER BY name ASC")
    fun getTemplatesByCompany(companyId: Int): LiveData<List<LabelTemplate>>
    
    /**
     * Busca plantillas por nombre o descripción
     */
    @Query("SELECT * FROM label_templates WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchTemplates(query: String): LiveData<List<LabelTemplate>>
    
    /**
     * Inserta una nueva plantilla
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: LabelTemplate): Long
    
    /**
     * Inserta múltiples plantillas
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<LabelTemplate>): List<Long>
    
    /**
     * Actualiza una plantilla existente
     */
    @Update
    suspend fun update(template: LabelTemplate): Int
    
    /**
     * Elimina una plantilla por su ID
     */
    @Query("DELETE FROM label_templates WHERE id = :templateId")
    suspend fun deleteTemplateById(templateId: Int): Int
    
    /**
     * Elimina todas las plantillas
     */
    @Query("DELETE FROM label_templates")
    suspend fun deleteAll(): Int
    
    /**
     * Obtiene plantillas pendientes de sincronización
     */
    @Query("SELECT * FROM label_templates WHERE pendingUpload = 1")
    suspend fun getPendingUpload(): List<LabelTemplate>
    
    /**
     * Marca una plantilla como sincronizada
     */
    @Query("UPDATE label_templates SET synced = 1, pendingUpload = 0 WHERE id = :templateId")
    suspend fun markAsSynced(templateId: Int): Int
}