package com.productiva.android.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.productiva.android.database.dao.LabelTemplateDao
import com.productiva.android.model.LabelTemplate
import com.productiva.android.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Repositorio para gestionar plantillas de etiquetas tanto en la base de datos local como en el servidor.
 */
class LabelTemplateRepository(
    private val labelTemplateDao: LabelTemplateDao,
    private val apiService: ApiService
) {
    private val TAG = "LabelTemplateRepository"
    
    /**
     * Obtiene todas las plantillas de etiquetas desde la base de datos local.
     */
    fun getAllTemplates(): LiveData<List<LabelTemplate>> {
        return labelTemplateDao.getAllTemplates()
    }
    
    /**
     * Obtiene plantillas de etiquetas por compañía.
     */
    fun getTemplatesByCompany(companyId: Int): LiveData<List<LabelTemplate>> {
        return labelTemplateDao.getTemplatesByCompany(companyId)
    }
    
    /**
     * Obtiene plantillas de etiquetas favoritas.
     */
    fun getFavoriteTemplates(): LiveData<List<LabelTemplate>> {
        return labelTemplateDao.getFavoriteTemplates()
    }
    
    /**
     * Obtiene plantillas de etiquetas recientemente usadas.
     */
    fun getRecentlyUsedTemplates(limit: Int = 5): LiveData<List<LabelTemplate>> {
        return labelTemplateDao.getRecentlyUsedTemplates(limit)
    }
    
    /**
     * Busca plantillas de etiquetas por nombre o descripción.
     */
    fun searchTemplates(query: String): LiveData<List<LabelTemplate>> {
        return labelTemplateDao.searchTemplates(query)
    }
    
    /**
     * Obtiene una plantilla de etiqueta por su ID.
     */
    suspend fun getTemplateById(templateId: Int): LabelTemplate? {
        return withContext(Dispatchers.IO) {
            labelTemplateDao.getTemplateById(templateId)
        }
    }
    
    /**
     * Marca una plantilla como favorita o no.
     */
    suspend fun setFavorite(templateId: Int, isFavorite: Boolean): Int {
        return withContext(Dispatchers.IO) {
            labelTemplateDao.setFavorite(templateId, isFavorite)
        }
    }
    
    /**
     * Actualiza el contador de uso y la fecha de último uso de una plantilla.
     */
    suspend fun updateUsage(templateId: Int): Int {
        return withContext(Dispatchers.IO) {
            labelTemplateDao.updateUsage(templateId)
        }
    }
    
    /**
     * Sincroniza las plantillas de etiquetas desde el servidor con la base de datos local.
     */
    suspend fun syncLabelTemplates(companyId: Int? = null): Flow<ResourceState<List<LabelTemplate>>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Obtener plantillas del servidor
            val response = if (companyId != null) {
                apiService.getLabelTemplatesByCompany(companyId)
            } else {
                apiService.getLabelTemplates()
            }
            
            if (response.isSuccessful) {
                val templates = response.body() ?: emptyList()
                
                // Guardar en base de datos local
                withContext(Dispatchers.IO) {
                    // Preservar estados de favoritos para templates existentes
                    val existingTemplates = templates.map { template ->
                        val existingTemplate = labelTemplateDao.getTemplateById(template.id)
                        if (existingTemplate != null) {
                            // Conservar atributos locales como favorito y contador de uso
                            template.copy(
                                isFavorite = existingTemplate.isFavorite, 
                                timesUsed = existingTemplate.timesUsed,
                                lastUsed = existingTemplate.lastUsed
                            )
                        } else {
                            template
                        }
                    }
                    
                    labelTemplateDao.insertAll(existingTemplates)
                }
                
                emit(ResourceState.Success(templates))
            } else {
                emit(ResourceState.Error("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar plantillas de etiquetas", e)
            
            // En caso de error, intentar devolver lo que hay en caché
            val cachedTemplates = withContext(Dispatchers.IO) {
                if (companyId != null) {
                    labelTemplateDao.getTemplatesByCompany(companyId).value
                } else {
                    labelTemplateDao.getAllTemplates().value
                }
            }
            
            if (!cachedTemplates.isNullOrEmpty()) {
                emit(ResourceState.Success(cachedTemplates, isFromCache = true))
            } else {
                emit(ResourceState.Error("Error de red: ${e.message}"))
            }
        }
    }
    
    /**
     * Obtiene las plantillas que coinciden con un tamaño de papel específico.
     */
    fun getTemplatesForPaperSize(paperWidth: Int, paperHeight: Int): LiveData<List<LabelTemplate>> {
        return labelTemplateDao.getTemplatesForPaperSize(paperWidth, paperHeight)
    }
}