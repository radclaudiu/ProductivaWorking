package com.productiva.android.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.model.LabelTemplate
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para operaciones con plantillas de etiquetas en la base de datos local.
 */
@Dao
interface LabelTemplateDao {
    /**
     * Obtiene todas las plantillas de etiquetas.
     */
    @Query("SELECT * FROM label_templates ORDER BY name ASC")
    fun getAllLabelTemplates(): Flow<List<LabelTemplate>>
    
    /**
     * Obtiene una plantilla de etiqueta por su ID.
     */
    @Query("SELECT * FROM label_templates WHERE id = :templateId")
    fun getLabelTemplateById(templateId: Int): Flow<LabelTemplate?>
    
    /**
     * Obtiene una plantilla de etiqueta por su ID de forma síncrona.
     */
    @Query("SELECT * FROM label_templates WHERE id = :templateId")
    suspend fun getLabelTemplateByIdSync(templateId: Int): LabelTemplate?
    
    /**
     * Obtiene la plantilla de etiqueta predeterminada.
     */
    @Query("SELECT * FROM label_templates WHERE isDefault = 1 LIMIT 1")
    fun getDefaultLabelTemplate(): Flow<LabelTemplate?>
    
    /**
     * Obtiene plantillas de etiquetas por tipo de impresora.
     */
    @Query("SELECT * FROM label_templates WHERE printerType = :printerType ORDER BY name ASC")
    fun getLabelTemplatesByPrinterType(printerType: String): Flow<List<LabelTemplate>>
    
    /**
     * Inserta una plantilla de etiqueta.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabelTemplate(template: LabelTemplate)
    
    /**
     * Inserta múltiples plantillas de etiquetas.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabelTemplates(templates: List<LabelTemplate>)
    
    /**
     * Actualiza una plantilla de etiqueta.
     */
    @Update
    suspend fun updateLabelTemplate(template: LabelTemplate)
    
    /**
     * Elimina todas las plantillas de etiquetas.
     */
    @Query("DELETE FROM label_templates")
    suspend fun deleteAllLabelTemplates()
    
    /**
     * Elimina una plantilla de etiqueta por su ID.
     */
    @Query("DELETE FROM label_templates WHERE id = :templateId")
    suspend fun deleteLabelTemplate(templateId: Int)
    
    /**
     * Incrementa el contador de uso de una plantilla de etiqueta.
     */
    @Query("UPDATE label_templates SET localUsageCount = localUsageCount + 1 WHERE id = :templateId")
    suspend fun incrementUsageCount(templateId: Int)
    
    /**
     * Marca una plantilla para sincronización.
     */
    @Query("UPDATE label_templates SET needsSync = :needsSync, lastSyncTimestamp = :timestamp WHERE id = :templateId")
    suspend fun markForSync(templateId: Int, needsSync: Boolean, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Obtiene plantillas que necesitan sincronización.
     */
    @Query("SELECT * FROM label_templates WHERE needsSync = 1 ORDER BY lastSyncTimestamp ASC")
    suspend fun getTemplatesForSync(): List<LabelTemplate>
    
    /**
     * Cuenta plantillas que necesitan sincronización.
     */
    @Query("SELECT COUNT(*) FROM label_templates WHERE needsSync = 1")
    fun countTemplatesForSync(): Flow<Int>
}