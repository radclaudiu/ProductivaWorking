package com.productiva.android.repository

import android.util.Log
import com.productiva.android.database.dao.LabelTemplateDao
import com.productiva.android.model.LabelTemplate
import com.productiva.android.network.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

/**
 * Repositorio para gestionar plantillas de etiquetas, sincronizadas con el servidor.
 */
class LabelTemplateRepository(
    private val labelTemplateDao: LabelTemplateDao,
    private val apiService: ApiService
) {
    private val TAG = "LabelTemplateRepository"
    
    /**
     * Obtiene todas las plantillas de etiquetas almacenadas localmente.
     * 
     * @return Flow con la lista de plantillas.
     */
    fun getAllLabelTemplates(): Flow<List<LabelTemplate>> {
        return labelTemplateDao.getAllLabelTemplates()
    }
    
    /**
     * Obtiene una plantilla de etiqueta por su ID.
     * 
     * @param templateId ID de la plantilla.
     * @return Flow con la plantilla o null si no existe.
     */
    fun getLabelTemplateById(templateId: Int): Flow<LabelTemplate?> {
        return labelTemplateDao.getLabelTemplateById(templateId)
    }
    
    /**
     * Obtiene plantillas de etiquetas por tipo de impresora.
     * 
     * @param printerType Tipo de impresora.
     * @return Flow con la lista de plantillas para ese tipo de impresora.
     */
    fun getLabelTemplatesByPrinterType(printerType: String): Flow<List<LabelTemplate>> {
        return labelTemplateDao.getLabelTemplatesByPrinterType(printerType)
    }
    
    /**
     * Sincroniza las plantillas de etiquetas con el servidor.
     * 
     * @return Flow con el estado de la operación.
     */
    fun syncLabelTemplates(): Flow<ResourceState<List<LabelTemplate>>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // Obtener plantillas del servidor
            val response = apiService.getLabelTemplates()
            
            if (response.isSuccessful) {
                val templates = response.body()
                
                if (templates != null) {
                    // Guardar plantillas en la base de datos local
                    labelTemplateDao.deleteAllLabelTemplates() // Limpiar plantillas antiguas
                    labelTemplateDao.insertLabelTemplates(templates)
                    
                    Log.d(TAG, "Plantillas sincronizadas correctamente: ${templates.size}")
                    emit(ResourceState.Success(templates))
                } else {
                    emit(ResourceState.Error("Respuesta vacía del servidor"))
                }
            } else {
                emit(ResourceState.Error("Error al obtener plantillas: ${response.code()}"))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error de red al sincronizar plantillas", e)
            emit(ResourceState.Error("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar plantillas", e)
            emit(ResourceState.Error("Error: ${e.message}"))
        }
    }
    
    /**
     * Actualiza el contador de uso de una plantilla de etiqueta.
     * 
     * @param templateId ID de la plantilla.
     * @return Flow con el estado de la operación.
     */
    fun updateLabelTemplateUsage(templateId: Int): Flow<ResourceState<Boolean>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // Actualizar contador en el servidor
            val response = apiService.updateLabelTemplateUsage(templateId)
            
            if (response.isSuccessful) {
                // Actualizar contador localmente
                labelTemplateDao.incrementUsageCount(templateId)
                
                Log.d(TAG, "Contador de uso de plantilla $templateId actualizado correctamente")
                emit(ResourceState.Success(true))
            } else {
                emit(ResourceState.Error("Error al actualizar contador: ${response.code()}"))
            }
        } catch (e: IOException) {
            // Actualizar contador localmente aunque falle la sincronización
            // Se sincronizará en el futuro
            labelTemplateDao.incrementUsageCount(templateId)
            labelTemplateDao.markForSync(templateId, true)
            
            Log.e(TAG, "Error de red al actualizar contador, guardado localmente", e)
            emit(ResourceState.Error("Error de red, cambio guardado localmente", true))
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar contador de uso", e)
            emit(ResourceState.Error("Error: ${e.message}"))
        }
    }
    
    /**
     * Sincroniza los contadores de uso pendientes de plantillas con el servidor.
     * 
     * @return Flow con el estado de la operación.
     */
    fun syncPendingUsageCounts(): Flow<ResourceState<Int>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // Obtener plantillas pendientes de sincronización
            val pendingTemplates = labelTemplateDao.getTemplatesForSync()
            
            if (pendingTemplates.isEmpty()) {
                emit(ResourceState.Success(0))
                return@flow
            }
            
            var syncedCount = 0
            
            for (template in pendingTemplates) {
                // Enviar actualización al servidor
                val response = apiService.updateLabelTemplateUsage(template.id)
                
                if (response.isSuccessful) {
                    // Marcar como sincronizado
                    labelTemplateDao.markForSync(template.id, false)
                    syncedCount++
                } else {
                    Log.e(TAG, "Error al sincronizar plantilla ${template.id}: ${response.code()}")
                }
            }
            
            if (syncedCount == pendingTemplates.size) {
                emit(ResourceState.Success(syncedCount))
            } else {
                emit(ResourceState.Error("Algunas plantillas no se pudieron sincronizar", syncedCount))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error de red al sincronizar contadores", e)
            emit(ResourceState.Error("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar contadores", e)
            emit(ResourceState.Error("Error: ${e.message}"))
        }
    }
}