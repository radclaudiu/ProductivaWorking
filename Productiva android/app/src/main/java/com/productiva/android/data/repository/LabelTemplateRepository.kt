package com.productiva.android.data.repository

import android.content.Context
import android.util.Log
import com.productiva.android.data.database.AppDatabase
import com.productiva.android.data.model.LabelTemplate
import com.productiva.android.network.NetworkResult
import com.productiva.android.network.model.SyncResponse
import com.productiva.android.network.safeApiCall
import com.productiva.android.repository.ResourceState
import com.productiva.android.session.SessionManager
import com.productiva.android.sync.SyncResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.collections.HashMap

/**
 * Repositorio que gestiona el acceso a datos de plantillas de etiquetas,
 * tanto desde la base de datos local como desde el servidor remoto.
 */
class LabelTemplateRepository private constructor(context: Context) : BaseRepository(context) {
    
    private val templateDao = AppDatabase.getDatabase(context, kotlinx.coroutines.MainScope()).labelTemplateDao()
    private val sessionManager = SessionManager.getInstance()
    
    companion object {
        private const val TAG = "LabelTemplateRepository"
        
        @Volatile
        private var instance: LabelTemplateRepository? = null
        
        /**
         * Obtiene la instancia única del repositorio.
         *
         * @param context Contexto de la aplicación.
         * @return Instancia del repositorio.
         */
        fun getInstance(context: Context): LabelTemplateRepository {
            return instance ?: synchronized(this) {
                instance ?: LabelTemplateRepository(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Obtiene todas las plantillas de etiquetas como flujo de ResourceState.
     *
     * @param forceRefresh Si es true, fuerza una actualización desde el servidor.
     * @return Flujo de ResourceState con las plantillas.
     */
    fun getAllTemplates(forceRefresh: Boolean = false): Flow<ResourceState<List<LabelTemplate>>> {
        return networkBoundResource(
            shouldFetch = { templates -> forceRefresh || templates.isNullOrEmpty() },
            remoteFetch = {
                val companyId = sessionManager.getCurrentCompanyId()
                safeApiCall {
                    apiService.getLabelTemplates(companyId = companyId)
                }
            },
            localFetch = {
                templateDao.getAllTemplates()
            },
            saveFetchResult = { response ->
                withContext(Dispatchers.IO) {
                    // Guardar plantillas añadidas y actualizadas
                    val templatesToSave = mutableListOf<LabelTemplate>()
                    
                    response.added.forEach { template ->
                        templatesToSave.add(template.withSyncStatus(LabelTemplate.SyncStatus.SYNCED))
                    }
                    
                    response.updated.forEach { template ->
                        templatesToSave.add(template.withSyncStatus(LabelTemplate.SyncStatus.SYNCED))
                    }
                    
                    if (templatesToSave.isNotEmpty()) {
                        templateDao.insertAll(templatesToSave)
                    }
                    
                    // Procesar eliminaciones
                    if (response.deleted.isNotEmpty()) {
                        for (templateId in response.deleted) {
                            templateDao.markAsDeleted(templateId)
                        }
                        templateDao.deleteMarkedTemplates()
                    }
                }
            }
        )
    }
    
    /**
     * Obtiene una plantilla de etiqueta por su ID como flujo de ResourceState.
     *
     * @param templateId ID de la plantilla.
     * @param forceRefresh Si es true, fuerza una actualización desde el servidor.
     * @return Flujo de ResourceState con la plantilla.
     */
    fun getTemplateById(templateId: Int, forceRefresh: Boolean = false): Flow<ResourceState<LabelTemplate>> {
        return networkBoundResource(
            shouldFetch = { template -> forceRefresh || template == null },
            remoteFetch = {
                safeApiCall {
                    apiService.getLabelTemplateById(templateId)
                }
            },
            localFetch = {
                templateDao.getTemplateById(templateId)
            },
            saveFetchResult = { template ->
                withContext(Dispatchers.IO) {
                    templateDao.insert(template.withSyncStatus(LabelTemplate.SyncStatus.SYNCED))
                }
            }
        )
    }
    
    /**
     * Obtiene la plantilla de etiqueta predeterminada como flujo de ResourceState.
     *
     * @param forceRefresh Si es true, fuerza una actualización desde el servidor.
     * @return Flujo de ResourceState con la plantilla predeterminada.
     */
    fun getDefaultTemplate(forceRefresh: Boolean = false): Flow<ResourceState<LabelTemplate>> {
        return networkBoundResource(
            shouldFetch = { template -> forceRefresh || template == null },
            remoteFetch = {
                val companyId = sessionManager.getCurrentCompanyId()
                // No hay endpoint específico para la plantilla predeterminada, obtener todas y filtrar
                val result = safeApiCall {
                    apiService.getLabelTemplates(companyId = companyId)
                }
                
                when (result) {
                    is NetworkResult.Success -> {
                        val templates = result.data
                        val defaultTemplate = templates.added.find { it.isDefault } ?: 
                                            templates.updated.find { it.isDefault }
                        
                        if (defaultTemplate != null) {
                            NetworkResult.Success(defaultTemplate)
                        } else {
                            NetworkResult.Error("No se encontró plantilla predeterminada")
                        }
                    }
                    is NetworkResult.Error -> result
                    NetworkResult.Loading -> NetworkResult.Loading
                }
            },
            localFetch = {
                templateDao.getDefaultTemplate()
            },
            saveFetchResult = { template ->
                withContext(Dispatchers.IO) {
                    templateDao.insert(template.withSyncStatus(LabelTemplate.SyncStatus.SYNCED))
                }
            }
        )
    }
    
    /**
     * Crea una nueva plantilla de etiqueta.
     *
     * @param template Plantilla a crear.
     * @return Flujo de ResourceState con la plantilla creada.
     */
    suspend fun createTemplate(template: LabelTemplate): ResourceState<LabelTemplate> = withContext(Dispatchers.IO) {
        try {
            // Preparar la plantilla para creación local
            val templateWithStatus = template.copy(
                syncStatus = LabelTemplate.SyncStatus.PENDING_UPLOAD
            )
            
            // Guardar la plantilla en la base de datos local
            val templateId = templateDao.insert(templateWithStatus).toInt()
            val savedTemplate = templateDao.getTemplateById(templateId)
                ?: return@withContext ResourceState.Error("Error al guardar la plantilla")
            
            // Intentar sincronizar si hay conexión
            if (connectivityMonitor.isNetworkAvailable()) {
                when (val result = safeApiCall { apiService.createLabelTemplate(savedTemplate) }) {
                    is NetworkResult.Success -> {
                        // Actualizar la plantilla con los datos del servidor
                        val serverTemplate = result.data
                        templateDao.insert(serverTemplate.withSyncStatus(LabelTemplate.SyncStatus.SYNCED))
                        
                        return@withContext ResourceState.Success(serverTemplate)
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Error al sincronizar plantilla creada: ${result.message}")
                        return@withContext ResourceState.Success(savedTemplate, isFromCache = true)
                    }
                    is NetworkResult.Loading -> {
                        return@withContext ResourceState.Success(savedTemplate, isFromCache = true)
                    }
                }
            } else {
                // Sin conexión, devolver éxito pero con datos locales
                return@withContext ResourceState.Success(savedTemplate, isFromCache = true)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear plantilla", e)
            ResourceState.Error(e.message ?: "Error desconocido")
        }
    }
    
    /**
     * Actualiza una plantilla de etiqueta existente.
     *
     * @param template Plantilla actualizada.
     * @return Flujo de ResourceState con la plantilla actualizada.
     */
    suspend fun updateTemplate(template: LabelTemplate): ResourceState<LabelTemplate> = withContext(Dispatchers.IO) {
        try {
            // Preparar la plantilla para actualización local
            val templateWithStatus = template.copy(
                syncStatus = LabelTemplate.SyncStatus.PENDING_UPDATE,
                pendingChanges = true,
                updatedAt = Date()
            )
            
            // Actualizar la plantilla en la base de datos local
            templateDao.update(templateWithStatus)
            val savedTemplate = templateDao.getTemplateById(template.id)
                ?: return@withContext ResourceState.Error("Plantilla no encontrada")
            
            // Intentar sincronizar si hay conexión
            if (connectivityMonitor.isNetworkAvailable()) {
                when (val result = safeApiCall { apiService.updateLabelTemplate(template.id, savedTemplate) }) {
                    is NetworkResult.Success -> {
                        // Actualizar la plantilla con los datos del servidor
                        val serverTemplate = result.data
                        templateDao.insert(serverTemplate.withSyncStatus(LabelTemplate.SyncStatus.SYNCED))
                        
                        return@withContext ResourceState.Success(serverTemplate)
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Error al sincronizar plantilla actualizada: ${result.message}")
                        return@withContext ResourceState.Success(savedTemplate, isFromCache = true)
                    }
                    is NetworkResult.Loading -> {
                        return@withContext ResourceState.Success(savedTemplate, isFromCache = true)
                    }
                }
            } else {
                // Sin conexión, devolver éxito pero con datos locales
                return@withContext ResourceState.Success(savedTemplate, isFromCache = true)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar plantilla", e)
            ResourceState.Error(e.message ?: "Error desconocido")
        }
    }
    
    /**
     * Establece una plantilla como predeterminada.
     *
     * @param templateId ID de la plantilla a establecer como predeterminada.
     * @return Flujo de ResourceState con el resultado de la operación.
     */
    suspend fun setAsDefault(templateId: Int): ResourceState<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Establecer como predeterminada localmente
            templateDao.setAsDefault(templateId)
            
            // Obtener la plantilla actualizada
            val template = templateDao.getTemplateById(templateId)
                ?: return@withContext ResourceState.Error("Plantilla no encontrada")
            
            // Intentar sincronizar si hay conexión
            if (connectivityMonitor.isNetworkAvailable()) {
                when (val result = safeApiCall { apiService.updateLabelTemplate(templateId, template) }) {
                    is NetworkResult.Success -> {
                        // Actualizar la plantilla con los datos del servidor
                        val serverTemplate = result.data
                        templateDao.insert(serverTemplate.withSyncStatus(LabelTemplate.SyncStatus.SYNCED))
                        
                        return@withContext ResourceState.Success(true)
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Error al sincronizar plantilla predeterminada: ${result.message}")
                        return@withContext ResourceState.Success(true, isFromCache = true)
                    }
                    is NetworkResult.Loading -> {
                        return@withContext ResourceState.Success(true, isFromCache = true)
                    }
                }
            } else {
                // Sin conexión, devolver éxito pero con datos locales
                return@withContext ResourceState.Success(true, isFromCache = true)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al establecer plantilla como predeterminada", e)
            ResourceState.Error(e.message ?: "Error desconocido")
        }
    }
    
    /**
     * Elimina una plantilla de etiqueta.
     *
     * @param templateId ID de la plantilla a eliminar.
     * @return Flujo de ResourceState con el resultado de la operación.
     */
    suspend fun deleteTemplate(templateId: Int): ResourceState<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Marcar la plantilla como eliminada localmente
            templateDao.markAsDeleted(templateId)
            
            // Intentar sincronizar si hay conexión
            if (connectivityMonitor.isNetworkAvailable()) {
                when (val result = safeApiCall { apiService.deleteLabelTemplate(templateId) }) {
                    is NetworkResult.Success -> {
                        // La plantilla se eliminó correctamente en el servidor, eliminarla físicamente
                        val template = templateDao.getTemplateById(templateId)
                        if (template != null) {
                            templateDao.updateSyncStatus(templateId, LabelTemplate.SyncStatus.SYNCED)
                            templateDao.deleteMarkedTemplates()
                        }
                        
                        return@withContext ResourceState.Success(true)
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Error al sincronizar eliminación de plantilla: ${result.message}")
                        return@withContext ResourceState.Success(true, isFromCache = true)
                    }
                    is NetworkResult.Loading -> {
                        return@withContext ResourceState.Success(true, isFromCache = true)
                    }
                }
            } else {
                // Sin conexión, devolver éxito pero con datos locales
                return@withContext ResourceState.Success(true, isFromCache = true)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar plantilla", e)
            ResourceState.Error(e.message ?: "Error desconocido")
        }
    }
    
    /**
     * Sincroniza todas las plantillas pendientes con el servidor.
     *
     * @param lastSyncTime Marca de tiempo de la última sincronización.
     * @return Resultado de la sincronización.
     */
    suspend fun syncWithServer(lastSyncTime: Long): SyncResult = executeSyncOperation {
        val companyId = sessionManager.getCurrentCompanyId()
        
        // 1. Obtener todas las plantillas pendientes de sincronización
        val pendingTemplates = templateDao.getPendingSyncTemplates()
        
        // 2. Preparar los datos para la sincronización
        val syncData = HashMap<String, Any>()
        
        // 2.1. Añadir plantillas a crear/actualizar
        val templatesToUpload = pendingTemplates.filter { 
            it.syncStatus == LabelTemplate.SyncStatus.PENDING_UPLOAD || 
            it.syncStatus == LabelTemplate.SyncStatus.PENDING_UPDATE 
        }
        syncData["templates"] = templatesToUpload
        
        // 2.2. Añadir plantillas a eliminar
        val templatesToDelete = pendingTemplates.filter { 
            it.syncStatus == LabelTemplate.SyncStatus.PENDING_DELETE 
        }.map { it.id }
        syncData["deleted_ids"] = templatesToDelete
        
        // 2.3. Añadir última vez sincronizado para recibir actualizaciones del servidor
        syncData["last_sync"] = lastSyncTime
        
        // 2.4. Añadir ID de empresa
        syncData["company_id"] = companyId
        
        // 3. Realizar la sincronización con el servidor
        val result = safeApiCall {
            apiService.syncLabelTemplates(syncData)
        }
        
        when (result) {
            is NetworkResult.Success -> {
                // 4. Procesar la respuesta del servidor
                val response = result.data.data ?: throw Exception("Respuesta vacía del servidor")
                
                // 4.1. Guardar plantillas añadidas y actualizadas
                val templatesToSave = mutableListOf<LabelTemplate>()
                
                response.added.forEach { template ->
                    templatesToSave.add(template.withSyncStatus(LabelTemplate.SyncStatus.SYNCED))
                }
                
                response.updated.forEach { template ->
                    templatesToSave.add(template.withSyncStatus(LabelTemplate.SyncStatus.SYNCED))
                }
                
                if (templatesToSave.isNotEmpty()) {
                    templateDao.insertAll(templatesToSave)
                }
                
                // 4.2. Marcar como sincronizadas las plantillas que enviamos
                val syncedIds = templatesToUpload.map { it.id }
                if (syncedIds.isNotEmpty()) {
                    templateDao.markAsSynced(syncedIds)
                }
                
                // 4.3. Procesar eliminaciones
                response.deleted.forEach { templateId ->
                    templateDao.markAsDeleted(templateId)
                }
                
                // 4.4. Eliminar físicamente las plantillas marcadas como eliminadas y ya sincronizadas
                templateDao.deleteMarkedTemplates()
                
                SyncResult.Success(
                    addedCount = response.added.size,
                    updatedCount = response.updated.size,
                    deletedCount = response.deleted.size
                )
            }
            is NetworkResult.Error -> {
                throw Exception(result.message)
            }
            is NetworkResult.Loading -> {
                throw Exception("Estado de carga inesperado")
            }
        }
    }
    
    /**
     * Obtiene el número de plantillas pendientes de sincronización.
     *
     * @return Número de plantillas pendientes de sincronización.
     */
    suspend fun getPendingSyncCount(): Int = withContext(Dispatchers.IO) {
        templateDao.getPendingSyncTemplatesCount()
    }
}