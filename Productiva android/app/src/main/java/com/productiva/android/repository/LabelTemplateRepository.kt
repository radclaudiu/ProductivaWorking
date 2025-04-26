package com.productiva.android.repository

import android.util.Log
import com.productiva.android.dao.LabelTemplateDao
import com.productiva.android.model.LabelTemplate
import com.productiva.android.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Repositorio para gestionar plantillas de etiquetas.
 */
class LabelTemplateRepository(
    private val labelTemplateDao: LabelTemplateDao,
    private val apiService: ApiService
) {
    private val TAG = "LabelTemplateRepository"
    
    /**
     * Obtiene todas las plantillas de etiquetas.
     */
    fun getAllLabelTemplates(): Flow<List<LabelTemplate>> {
        return labelTemplateDao.getAllLabelTemplates()
    }
    
    /**
     * Obtiene una plantilla de etiqueta por su ID.
     */
    fun getLabelTemplateById(templateId: Int): Flow<LabelTemplate?> {
        return labelTemplateDao.getLabelTemplateById(templateId)
    }
    
    /**
     * Sincroniza plantillas de etiquetas con el servidor.
     */
    suspend fun syncLabelTemplates(): Flow<ResourceState<List<LabelTemplate>>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // Primero sincronizar contadores de uso pendientes
            syncLabelTemplateUsageStats()
            
            // Obtener plantillas del servidor
            val response = apiService.getLabelTemplates()
            
            if (response.isSuccessful) {
                val serverTemplates = response.body() ?: emptyList()
                
                // Obtener plantillas locales que necesitan sincronización
                val localTemplates = labelTemplateDao.getLabelTemplatesNeedingSyncSync()
                
                // Actualizar plantillas locales con datos del servidor, preservando cambios locales pendientes
                withContext(Dispatchers.IO) {
                    // Filtrar solo plantillas que no están pendientes de sincronización
                    val templatesToUpdate = serverTemplates.filter { serverTemplate ->
                        localTemplates.none { it.id == serverTemplate.id && it.needsSync }
                    }
                    
                    // Insertar o actualizar plantillas
                    labelTemplateDao.upsertLabelTemplates(templatesToUpdate)
                    
                    // También incluir las plantillas locales que necesitan sincronización
                    // en la lista de plantillas resultante
                    val resultTemplates = ArrayList<LabelTemplate>(templatesToUpdate)
                    resultTemplates.addAll(localTemplates)
                    
                    emit(ResourceState.Success(resultTemplates))
                }
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Sesión expirada"
                    403 -> "Sin permiso para acceder a las plantillas"
                    404 -> "No se encontraron plantillas"
                    else -> "Error del servidor: ${response.code()}"
                }
                
                emit(ResourceState.Error(errorMessage))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error de conexión al sincronizar plantillas", e)
            
            // En caso de error de conexión, devolver las plantillas locales
            val localTemplates = labelTemplateDao.getAllLabelTemplatesSync()
            emit(ResourceState.Success(localTemplates, "Usando datos locales"))
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar plantillas", e)
            emit(ResourceState.Error("Error al sincronizar plantillas: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Sincroniza estadísticas de uso de plantillas con el servidor.
     */
    private suspend fun syncLabelTemplateUsageStats() {
        try {
            // Obtener plantillas con contadores de uso pendientes
            val templatesNeedingSync = labelTemplateDao.getLabelTemplatesNeedingSyncSync()
            
            if (templatesNeedingSync.isEmpty()) {
                return
            }
            
            for (template in templatesNeedingSync) {
                try {
                    // Solo sincronizar si hay uso local registrado
                    if (template.localUsageCount > 0) {
                        val response = apiService.updateLabelTemplateUsage(
                            template.id,
                            mapOf("usage_count" to template.localUsageCount)
                        )
                        
                        if (response.isSuccessful) {
                            // Actualizar plantilla local con datos del servidor
                            response.body()?.let { serverTemplate ->
                                // Crear una versión actualizada que mantiene el uso local en 0
                                val updatedTemplate = serverTemplate.copy(
                                    localUsageCount = 0,
                                    needsSync = false
                                )
                                labelTemplateDao.updateLabelTemplate(updatedTemplate)
                            }
                        }
                    } else {
                        // Si no hay uso local, simplemente marcar como sincronizado
                        labelTemplateDao.updateLabelTemplate(template.copy(needsSync = false))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al sincronizar uso de plantilla ${template.id}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar estadísticas de uso", e)
        }
    }
    
    /**
     * Actualiza el contador de uso de una plantilla.
     */
    suspend fun updateLabelTemplateUsage(templateId: Int): Flow<ResourceState<LabelTemplate>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // Obtener la plantilla
            val template = labelTemplateDao.getLabelTemplateByIdSync(templateId)
            
            if (template == null) {
                emit(ResourceState.Error("Plantilla no encontrada"))
                return@flow
            }
            
            // Incrementar contador de uso local
            val updatedTemplate = template.incrementUsage()
            labelTemplateDao.updateLabelTemplate(updatedTemplate)
            
            // Intentar sincronizar inmediatamente si hay conexión
            try {
                val response = apiService.updateLabelTemplateUsage(
                    templateId,
                    mapOf("usage_count" to 1)
                )
                
                if (response.isSuccessful) {
                    response.body()?.let { serverTemplate ->
                        // Actualizar con datos del servidor, manteniendo el estado de sincronización
                        val syncedTemplate = if (updatedTemplate.localUsageCount > 1) {
                            // Si hay más de un uso local pendiente, mantener la necesidad de sincronización
                            serverTemplate.copy(
                                localUsageCount = updatedTemplate.localUsageCount - 1,
                                needsSync = true
                            )
                        } else {
                            // Si solo había un uso pendiente, ya está sincronizado
                            serverTemplate.copy(
                                localUsageCount = 0,
                                needsSync = false
                            )
                        }
                        
                        labelTemplateDao.updateLabelTemplate(syncedTemplate)
                        emit(ResourceState.Success(syncedTemplate))
                    } ?: emit(ResourceState.Success(updatedTemplate))
                } else {
                    emit(ResourceState.Success(updatedTemplate))
                }
            } catch (e: IOException) {
                // Error de conexión, mantener cambios locales
                Log.d(TAG, "No se pudo sincronizar uso de plantilla, guardado localmente", e)
                emit(ResourceState.Success(updatedTemplate, "Uso registrado localmente"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar uso de plantilla", e)
            emit(ResourceState.Error("Error al actualizar uso: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
}