package com.productiva.android.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.productiva.android.model.LabelTemplate

/**
 * DAO para acceso a plantillas de etiquetas
 */
@Dao
interface LabelTemplateDao {
    
    /**
     * Obtiene todas las plantillas de etiquetas
     */
    @Query("SELECT * FROM label_templates ORDER BY name ASC")
    suspend fun getAllLabelTemplates(): List<LabelTemplate>
    
    /**
     * Obtiene todas las plantillas de etiquetas como LiveData para observar cambios
     */
    @Query("SELECT * FROM label_templates ORDER BY name ASC")
    fun observeAllLabelTemplates(): LiveData<List<LabelTemplate>>
    
    /**
     * Obtiene la plantilla predeterminada
     */
    @Query("SELECT * FROM label_templates WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultLabelTemplate(): LabelTemplate?
    
    /**
     * Obtiene una plantilla de etiqueta por su ID
     */
    @Query("SELECT * FROM label_templates WHERE id = :id LIMIT 1")
    suspend fun getLabelTemplateByIdSync(id: Int): LabelTemplate?
    
    /**
     * Inserta o actualiza una plantilla de etiqueta
     * 
     * @return ID de la plantilla insertada/actualizada
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabelTemplate(template: LabelTemplate): Long
    
    /**
     * Elimina una plantilla de etiqueta por su ID
     */
    @Query("DELETE FROM label_templates WHERE id = :id")
    suspend fun deleteLabelTemplate(id: Int)
    
    /**
     * Limpia el estado de plantilla predeterminada de todas las plantillas
     */
    @Query("UPDATE label_templates SET isDefault = 0")
    suspend fun clearDefaultTemplates()
}