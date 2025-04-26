package com.productiva.android.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.data.model.LabelTemplate

/**
 * DAO (Data Access Object) para las operaciones de base de datos relacionadas con plantillas de etiquetas.
 */
@Dao
interface LabelTemplateDao {
    
    /**
     * Inserta una plantilla de etiqueta en la base de datos.
     * Si ya existe un registro con el mismo ID, lo reemplaza.
     *
     * @param template Plantilla de etiqueta a insertar.
     * @return ID generado para la plantilla insertada.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: LabelTemplate): Long
    
    /**
     * Inserta varias plantillas de etiqueta en la base de datos.
     * Si ya existen registros con los mismos IDs, los reemplaza.
     *
     * @param templates Lista de plantillas de etiqueta a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<LabelTemplate>)
    
    /**
     * Actualiza una plantilla de etiqueta existente en la base de datos.
     *
     * @param template Plantilla de etiqueta a actualizar.
     */
    @Update
    suspend fun update(template: LabelTemplate)
    
    /**
     * Obtiene todas las plantillas de etiqueta de la base de datos.
     *
     * @return Lista de todas las plantillas de etiqueta.
     */
    @Query("SELECT * FROM label_templates ORDER BY name")
    suspend fun getAllTemplates(): List<LabelTemplate>
    
    /**
     * Obtiene una plantilla de etiqueta por su ID.
     *
     * @param templateId ID de la plantilla de etiqueta.
     * @return Plantilla de etiqueta con el ID especificado o null si no existe.
     */
    @Query("SELECT * FROM label_templates WHERE id = :templateId")
    suspend fun getTemplateById(templateId: Int): LabelTemplate?
    
    /**
     * Obtiene la plantilla de etiqueta predeterminada.
     *
     * @return Plantilla de etiqueta predeterminada o null si no hay ninguna.
     */
    @Query("SELECT * FROM label_templates WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultTemplate(): LabelTemplate?
    
    /**
     * Establece una plantilla de etiqueta como predeterminada y el resto como no predeterminadas.
     *
     * @param templateId ID de la plantilla de etiqueta a establecer como predeterminada.
     */
    @Query("UPDATE label_templates SET isDefault = (id = :templateId), syncStatus = 'PENDING_UPDATE', pendingChanges = 1, updatedAt = CURRENT_TIMESTAMP")
    suspend fun setAsDefault(templateId: Int)
    
    /**
     * Marca una plantilla de etiqueta como eliminada (para sincronización posterior).
     * No elimina físicamente la plantilla, solo actualiza su estado.
     *
     * @param templateId ID de la plantilla de etiqueta a marcar.
     */
    @Query("UPDATE label_templates SET syncStatus = 'PENDING_DELETE', pendingChanges = 1, updatedAt = CURRENT_TIMESTAMP WHERE id = :templateId")
    suspend fun markAsDeleted(templateId: Int)
    
    /**
     * Obtiene todas las plantillas de etiqueta con cambios pendientes de sincronización.
     *
     * @return Lista de plantillas de etiqueta pendientes de sincronizar.
     */
    @Query("SELECT * FROM label_templates WHERE syncStatus != 'SYNCED' OR pendingChanges = 1")
    suspend fun getPendingSyncTemplates(): List<LabelTemplate>
    
    /**
     * Obtiene la cantidad de plantillas de etiqueta pendientes de sincronización.
     *
     * @return Número de plantillas de etiqueta pendientes de sincronizar.
     */
    @Query("SELECT COUNT(*) FROM label_templates WHERE syncStatus != 'SYNCED' OR pendingChanges = 1")
    suspend fun getPendingSyncTemplatesCount(): Int
    
    /**
     * Marca varias plantillas de etiqueta como sincronizadas.
     *
     * @param templateIds Lista de IDs de plantillas de etiqueta a marcar.
     */
    @Query("UPDATE label_templates SET syncStatus = 'SYNCED', pendingChanges = 0 WHERE id IN (:templateIds)")
    suspend fun markAsSynced(templateIds: List<Int>)
    
    /**
     * Actualiza el estado de sincronización de una plantilla de etiqueta.
     *
     * @param templateId ID de la plantilla de etiqueta.
     * @param syncStatus Nuevo estado de sincronización.
     */
    @Query("UPDATE label_templates SET syncStatus = :syncStatus, pendingChanges = 0 WHERE id = :templateId")
    suspend fun updateSyncStatus(templateId: Int, syncStatus: LabelTemplate.SyncStatus)
    
    /**
     * Elimina físicamente todas las plantillas de etiqueta marcadas para eliminación y ya sincronizadas.
     */
    @Query("DELETE FROM label_templates WHERE syncStatus = 'PENDING_DELETE' OR (syncStatus = 'SYNCED' AND pendingChanges = 1)")
    suspend fun deleteMarkedTemplates()
}