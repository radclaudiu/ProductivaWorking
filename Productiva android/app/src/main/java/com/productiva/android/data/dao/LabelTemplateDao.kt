package com.productiva.android.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.data.model.LabelTemplate
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * DAO (Data Access Object) para operaciones relacionadas con plantillas de etiquetas en la base de datos Room.
 */
@Dao
interface LabelTemplateDao {
    
    /**
     * Inserta una plantilla de etiqueta en la base de datos.
     *
     * @param template Plantilla a insertar.
     * @return ID de la plantilla insertada.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: LabelTemplate): Long
    
    /**
     * Inserta varias plantillas de etiqueta en la base de datos.
     *
     * @param templates Lista de plantillas a insertar.
     * @return Lista de IDs de las plantillas insertadas.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<LabelTemplate>): List<Long>
    
    /**
     * Actualiza una plantilla de etiqueta existente en la base de datos.
     *
     * @param template Plantilla a actualizar.
     * @return Número de filas actualizadas.
     */
    @Update
    suspend fun update(template: LabelTemplate): Int
    
    /**
     * Actualiza varias plantillas de etiqueta existentes en la base de datos.
     *
     * @param templates Lista de plantillas a actualizar.
     * @return Número de filas actualizadas.
     */
    @Update
    suspend fun updateAll(templates: List<LabelTemplate>): Int
    
    /**
     * Elimina una plantilla de etiqueta de la base de datos.
     *
     * @param template Plantilla a eliminar.
     * @return Número de filas eliminadas.
     */
    @Delete
    suspend fun delete(template: LabelTemplate): Int
    
    /**
     * Obtiene todas las plantillas de etiqueta como flujo observable.
     *
     * @return Flujo de lista de todas las plantillas.
     */
    @Query("SELECT * FROM label_templates WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllTemplatesFlow(): Flow<List<LabelTemplate>>
    
    /**
     * Obtiene todas las plantillas de etiqueta.
     *
     * @return Lista de todas las plantillas.
     */
    @Query("SELECT * FROM label_templates WHERE isDeleted = 0 ORDER BY name ASC")
    suspend fun getAllTemplates(): List<LabelTemplate>
    
    /**
     * Obtiene una plantilla de etiqueta por su ID.
     *
     * @param templateId ID de la plantilla.
     * @return Plantilla correspondiente al ID o null si no existe.
     */
    @Query("SELECT * FROM label_templates WHERE id = :templateId AND isDeleted = 0")
    suspend fun getTemplateById(templateId: Int): LabelTemplate?
    
    /**
     * Obtiene todas las plantillas de etiqueta para una empresa.
     *
     * @param companyId ID de la empresa.
     * @return Lista de plantillas de la empresa.
     */
    @Query("SELECT * FROM label_templates WHERE companyId = :companyId AND isDeleted = 0 ORDER BY name ASC")
    suspend fun getTemplatesByCompany(companyId: Int): List<LabelTemplate>
    
    /**
     * Obtiene todas las plantillas de etiqueta para una empresa como flujo observable.
     *
     * @param companyId ID de la empresa.
     * @return Flujo de lista de plantillas de la empresa.
     */
    @Query("SELECT * FROM label_templates WHERE companyId = :companyId AND isDeleted = 0 ORDER BY name ASC")
    fun getTemplatesByCompanyFlow(companyId: Int): Flow<List<LabelTemplate>>
    
    /**
     * Obtiene todas las plantillas de etiqueta creadas por un usuario.
     *
     * @param createdBy ID del usuario.
     * @return Lista de plantillas creadas por el usuario.
     */
    @Query("SELECT * FROM label_templates WHERE createdBy = :createdBy AND isDeleted = 0 ORDER BY name ASC")
    suspend fun getTemplatesByUser(createdBy: Int): List<LabelTemplate>
    
    /**
     * Obtiene todas las plantillas de etiqueta de un tipo específico.
     *
     * @param templateType Tipo de plantilla.
     * @return Lista de plantillas del tipo especificado.
     */
    @Query("SELECT * FROM label_templates WHERE templateType = :templateType AND isDeleted = 0 ORDER BY name ASC")
    suspend fun getTemplatesByType(templateType: String): List<LabelTemplate>
    
    /**
     * Obtiene todas las plantillas de etiqueta de un tipo específico como flujo observable.
     *
     * @param templateType Tipo de plantilla.
     * @return Flujo de lista de plantillas del tipo especificado.
     */
    @Query("SELECT * FROM label_templates WHERE templateType = :templateType AND isDeleted = 0 ORDER BY name ASC")
    fun getTemplatesByTypeFlow(templateType: String): Flow<List<LabelTemplate>>
    
    /**
     * Obtiene la plantilla de etiqueta predeterminada.
     *
     * @return Plantilla predeterminada o null si no existe.
     */
    @Query("SELECT * FROM label_templates WHERE isDefault = 1 AND isDeleted = 0 LIMIT 1")
    suspend fun getDefaultTemplate(): LabelTemplate?
    
    /**
     * Obtiene la plantilla de etiqueta predeterminada para una empresa.
     *
     * @param companyId ID de la empresa.
     * @return Plantilla predeterminada de la empresa o null si no existe.
     */
    @Query("SELECT * FROM label_templates WHERE isDefault = 1 AND companyId = :companyId AND isDeleted = 0 LIMIT 1")
    suspend fun getDefaultTemplateForCompany(companyId: Int): LabelTemplate?
    
    /**
     * Establece una plantilla como predeterminada y quita el estado predeterminado de las demás.
     *
     * @param templateId ID de la plantilla a establecer como predeterminada.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE label_templates SET isDefault = (id = :templateId), syncStatus = 'pending_update', pendingChanges = 1, updatedAt = :updatedAt WHERE companyId = (SELECT companyId FROM label_templates WHERE id = :templateId)")
    suspend fun setAsDefault(templateId: Int, updatedAt: Date = Date()): Int
    
    /**
     * Obtiene todas las plantillas de etiqueta pendientes de sincronizar con el servidor.
     *
     * @return Lista de plantillas pendientes de sincronizar.
     */
    @Query("SELECT * FROM label_templates WHERE syncStatus != 'synced' ORDER BY updatedAt DESC")
    suspend fun getPendingSyncTemplates(): List<LabelTemplate>
    
    /**
     * Obtiene el número de plantillas de etiqueta pendientes de sincronizar con el servidor.
     *
     * @return Número de plantillas pendientes de sincronizar.
     */
    @Query("SELECT COUNT(*) FROM label_templates WHERE syncStatus != 'synced'")
    suspend fun getPendingSyncTemplatesCount(): Int
    
    /**
     * Obtiene todas las plantillas de etiqueta pendientes de sincronizar con el servidor como flujo observable.
     *
     * @return Flujo de lista de plantillas pendientes de sincronizar.
     */
    @Query("SELECT * FROM label_templates WHERE syncStatus != 'synced' ORDER BY updatedAt DESC")
    fun getPendingSyncTemplatesFlow(): Flow<List<LabelTemplate>>
    
    /**
     * Actualiza el estado de sincronización de una plantilla de etiqueta.
     *
     * @param templateId ID de la plantilla.
     * @param syncStatus Nuevo estado de sincronización.
     * @param lastSyncTime Marca de tiempo de la última sincronización.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE label_templates SET syncStatus = :syncStatus, lastSyncTime = :lastSyncTime, pendingChanges = :syncStatus != 'synced' WHERE id = :templateId")
    suspend fun updateSyncStatus(templateId: Int, syncStatus: String, lastSyncTime: Long = System.currentTimeMillis()): Int
    
    /**
     * Marca varias plantillas de etiqueta como sincronizadas.
     *
     * @param templateIds Lista de IDs de plantillas.
     * @param lastSyncTime Marca de tiempo de la última sincronización.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE label_templates SET syncStatus = 'synced', lastSyncTime = :lastSyncTime, pendingChanges = 0 WHERE id IN (:templateIds)")
    suspend fun markAsSynced(templateIds: List<Int>, lastSyncTime: Long = System.currentTimeMillis()): Int
    
    /**
     * Marca una plantilla de etiqueta como eliminada (borrado lógico).
     *
     * @param templateId ID de la plantilla.
     * @return Número de filas actualizadas.
     */
    @Query("UPDATE label_templates SET isDeleted = 1, syncStatus = 'pending_delete', pendingChanges = 1, updatedAt = :updatedAt WHERE id = :templateId")
    suspend fun markAsDeleted(templateId: Int, updatedAt: Date = Date()): Int
    
    /**
     * Elimina físicamente las plantillas de etiqueta marcadas como eliminadas y ya sincronizadas.
     *
     * @return Número de filas eliminadas.
     */
    @Query("DELETE FROM label_templates WHERE isDeleted = 1 AND syncStatus = 'synced'")
    suspend fun deleteMarkedTemplates(): Int
    
    /**
     * Obtiene las plantillas de etiqueta actualizadas después de una fecha específica.
     *
     * @param timestamp Marca de tiempo a partir de la cual buscar actualizaciones.
     * @return Lista de plantillas actualizadas después de la fecha especificada.
     */
    @Query("SELECT * FROM label_templates WHERE updatedAt >= :timestamp AND isDeleted = 0")
    suspend fun getTemplatesUpdatedAfter(timestamp: Date): List<LabelTemplate>
    
    /**
     * Limpia la base de datos de plantillas de etiqueta (elimina todas las plantillas).
     * Utilizar con precaución.
     */
    @Query("DELETE FROM label_templates")
    suspend fun clearAll()
}