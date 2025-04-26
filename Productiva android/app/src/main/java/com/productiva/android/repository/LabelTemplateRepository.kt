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
import retrofit2.HttpException
import java.io.IOException

/**
 * Repositorio para la gestión de plantillas de etiquetas.
 * Proporciona métodos para obtener, sincronizar y actualizar plantillas.
 */
class LabelTemplateRepository(
    private val labelTemplateDao: LabelTemplateDao,
    private val apiService: ApiService
) {
    private val TAG = "LabelTemplateRepository"
    
    /**
     * Obtiene todas las plantillas de etiquetas de la base de datos local.
     */
    fun getAllTemplates(): Flow<List<LabelTemplate>> {
        return labelTemplateDao.getAllTemplates()
    }
    
    /**
     * Obtiene una plantilla específica por su ID.
     */
    fun getTemplateById(templateId: Int): Flow<LabelTemplate?> {
        return labelTemplateDao.getTemplateById(templateId)
    }
    
    /**
     * Obtiene plantillas por tipo.
     */
    fun getTemplatesByType(type: String): Flow<List<LabelTemplate>> {
        return labelTemplateDao.getTemplatesByType(type)
    }
    
    /**
     * Obtiene la plantilla predeterminada para un tipo específico.
     */
    fun getDefaultTemplateForType(type: String): Flow<LabelTemplate?> {
        return labelTemplateDao.getDefaultTemplateForType(type)
    }
    
    /**
     * Sincroniza las plantillas de etiquetas con el servidor.
     * Primero sube los contadores de uso pendientes y luego obtiene las últimas plantillas.
     */
    fun syncLabelTemplates(): Flow<ResourceState<List<LabelTemplate>>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // 1. Sincronizar contadores de uso pendientes
            val pendingTemplates = labelTemplateDao.getTemplatesPendingSync()
            if (pendingTemplates.isNotEmpty()) {
                Log.d(TAG, "Sincronizando ${pendingTemplates.size} plantillas con contadores de uso pendientes")
                
                for (template in pendingTemplates) {
                    try {
                        // Actualizar contadores de uso en el servidor
                        for (i in 1..template.localUseCount) {
                            apiService.incrementLabelTemplateUseCount(template.id)
                        }
                        
                        // Marcar como sincronizada
                        labelTemplateDao.markAsSynced(template.id)
                        
                        Log.d(TAG, "Plantilla ${template.id} sincronizada con ${template.localUseCount} usos")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al sincronizar contador de uso para plantilla ${template.id}", e)
                        // Continuamos con la siguiente plantilla aunque haya error
                    }
                }
            }
            
            // 2. Obtener todas las plantillas del servidor
            val response = apiService.getAllLabelTemplates()
            
            if (response.success && response.data != null) {
                val templates = response.data
                
                // 3. Actualizar plantillas en la base de datos local
                withContext(Dispatchers.IO) {
                    for (template in templates) {
                        labelTemplateDao.upsertFromServer(template)
                    }
                }
                
                // Emitir las plantillas actualizadas
                val updatedTemplates = labelTemplateDao.getAllTemplates().value ?: emptyList()
                
                emit(ResourceState.Success(updatedTemplates))
            } else {
                emit(ResourceState.Error(response.message ?: "Error desconocido al obtener plantillas"))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "Error HTTP al sincronizar plantillas", e)
            emit(ResourceState.Error("Error de red: ${e.message()}"))
        } catch (e: IOException) {
            Log.e(TAG, "Error IO al sincronizar plantillas", e)
            emit(ResourceState.Error("Error de conexión: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar plantillas", e)
            emit(ResourceState.Error("Error al sincronizar: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Sincroniza una plantilla específica con el servidor.
     */
    fun syncTemplate(templateId: Int): Flow<ResourceState<LabelTemplate>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // 1. Verificar si hay contadores de uso pendientes
            val existingTemplate = labelTemplateDao.getTemplateByIdSync(templateId)
            
            if (existingTemplate?.needsSync == true && existingTemplate.localUseCount > 0) {
                // Sincronizar contador de uso
                try {
                    for (i in 1..existingTemplate.localUseCount) {
                        apiService.incrementLabelTemplateUseCount(templateId)
                    }
                    
                    // Marcar como sincronizada
                    labelTemplateDao.markAsSynced(templateId)
                } catch (e: Exception) {
                    Log.e(TAG, "Error al sincronizar contador de uso para plantilla $templateId", e)
                    // Continuamos aunque haya error
                }
            }
            
            // 2. Obtener la plantilla actualizada del servidor
            val response = apiService.getLabelTemplateById(templateId)
            
            if (response.success && response.data != null) {
                val serverTemplate = response.data
                
                // 3. Actualizar en la base de datos local
                val updatedTemplate = labelTemplateDao.upsertFromServer(serverTemplate)
                
                emit(ResourceState.Success(updatedTemplate))
            } else {
                // Si no se puede obtener del servidor pero existe localmente,
                // devolvemos la versión local
                if (existingTemplate != null) {
                    emit(ResourceState.Success(existingTemplate))
                } else {
                    emit(ResourceState.Error(response.message ?: "Error al obtener la plantilla"))
                }
            }
        } catch (e: HttpException) {
            Log.e(TAG, "Error HTTP al sincronizar plantilla $templateId", e)
            emit(ResourceState.Error("Error de red: ${e.message()}"))
        } catch (e: IOException) {
            Log.e(TAG, "Error IO al sincronizar plantilla $templateId", e)
            emit(ResourceState.Error("Error de conexión: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar plantilla $templateId", e)
            emit(ResourceState.Error("Error al sincronizar: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Incrementa el contador de uso de una plantilla y lo marca para sincronización.
     */
    fun incrementTemplateUseCount(templateId: Int): Flow<ResourceState<LabelTemplate>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // Incrementar contador de uso local
            val result = labelTemplateDao.incrementUseCount(templateId)
            
            if (result > 0) {
                val updatedTemplate = labelTemplateDao.getTemplateByIdSync(templateId)
                
                if (updatedTemplate != null) {
                    emit(ResourceState.Success(updatedTemplate))
                    
                    // Intentar sincronizar inmediatamente si es posible
                    try {
                        apiService.incrementLabelTemplateUseCount(templateId)
                        labelTemplateDao.markAsSynced(templateId)
                        
                        // Obtener la plantilla actualizada
                        val syncedTemplate = labelTemplateDao.getTemplateByIdSync(templateId)
                        if (syncedTemplate != null) {
                            emit(ResourceState.Success(syncedTemplate, "Contador de uso sincronizado"))
                        }
                    } catch (e: Exception) {
                        // No hacemos nada si falla la sincronización inmediata,
                        // se sincronizará más tarde
                        Log.d(TAG, "La sincronización inmediata del contador falló, se intentará más tarde")
                    }
                } else {
                    emit(ResourceState.Error("No se pudo encontrar la plantilla después de incrementar el contador"))
                }
            } else {
                emit(ResourceState.Error("No se pudo incrementar el contador de uso"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al incrementar contador de uso", e)
            emit(ResourceState.Error("Error al incrementar contador: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
}