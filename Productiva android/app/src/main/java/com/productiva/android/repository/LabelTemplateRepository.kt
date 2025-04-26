package com.productiva.android.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.productiva.android.api.ApiClient
import com.productiva.android.api.ApiResponse
import com.productiva.android.database.AppDatabase
import com.productiva.android.database.LabelTemplateDao
import com.productiva.android.model.LabelTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.Date

/**
 * Repositorio para gestionar plantillas de etiquetas
 */
class LabelTemplateRepository(context: Context) {
    
    private val apiClient = ApiClient.getInstance(context)
    private val templateDao: LabelTemplateDao
    
    init {
        val database = AppDatabase.getInstance(context)
        templateDao = database.labelTemplateDao()
    }
    
    /**
     * Obtiene todas las plantillas
     */
    fun getAllTemplates(): LiveData<List<LabelTemplate>> {
        return templateDao.getAllTemplates()
    }
    
    /**
     * Obtiene una plantilla por su ID
     */
    suspend fun getTemplateById(templateId: Int): LabelTemplate? {
        return withContext(Dispatchers.IO) {
            templateDao.getTemplateById(templateId)
        }
    }
    
    /**
     * Obtiene plantillas por usuario
     */
    fun getTemplatesByUser(userId: Int): LiveData<List<LabelTemplate>> {
        return templateDao.getTemplatesByUser(userId)
    }
    
    /**
     * Obtiene plantillas por empresa
     */
    fun getTemplatesByCompany(companyId: Int): LiveData<List<LabelTemplate>> {
        return templateDao.getTemplatesByCompany(companyId)
    }
    
    /**
     * Busca plantillas por nombre o descripción
     */
    fun searchTemplates(query: String): LiveData<List<LabelTemplate>> {
        return templateDao.searchTemplates(query)
    }
    
    /**
     * Inserta una plantilla
     */
    suspend fun insertTemplate(template: LabelTemplate): Long {
        return withContext(Dispatchers.IO) {
            templateDao.insert(template)
        }
    }
    
    /**
     * Actualiza una plantilla
     */
    suspend fun updateTemplate(template: LabelTemplate): Int {
        return withContext(Dispatchers.IO) {
            templateDao.update(template)
        }
    }
    
    /**
     * Elimina una plantilla
     */
    suspend fun deleteTemplate(templateId: Int): Int {
        return withContext(Dispatchers.IO) {
            templateDao.deleteTemplateById(templateId)
        }
    }
    
    /**
     * Obtiene una plantilla por su ID desde la API
     */
    suspend fun fetchTemplateById(templateId: Int): Result<LabelTemplate> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiClient.apiService.getLabelTemplateById(templateId)
                handleTemplateResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Obtiene plantillas desde la API y las guarda en la base de datos local
     */
    suspend fun fetchTemplates(userId: Int? = null, companyId: Int? = null): Result<List<LabelTemplate>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiClient.apiService.getLabelTemplates(userId, companyId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { templates ->
                        templateDao.insertAll(templates)
                        return@withContext Result.success(templates)
                    }
                }
                
                Result.failure(Exception(response.message() ?: "Error al obtener plantillas"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Crea una nueva plantilla en la API
     */
    suspend fun createTemplate(template: LabelTemplate): Result<LabelTemplate> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiClient.apiService.createLabelTemplate(template)
                handleTemplateResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Actualiza una plantilla existente en la API
     */
    suspend fun updateTemplateOnServer(template: LabelTemplate): Result<LabelTemplate> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiClient.apiService.updateLabelTemplate(template.id, template)
                handleTemplateResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Elimina una plantilla en la API
     */
    suspend fun deleteTemplateOnServer(templateId: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiClient.apiService.deleteLabelTemplate(templateId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    templateDao.deleteTemplateById(templateId)
                    return@withContext Result.success(true)
                }
                
                Result.failure(Exception(response.message() ?: "Error al eliminar plantilla"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Sincroniza las plantillas con el servidor
     */
    suspend fun syncTemplates(since: Date? = null): Result<List<LabelTemplate>> {
        return withContext(Dispatchers.IO) {
            try {
                val sinceStr = since?.time?.toString()
                val response = apiClient.apiService.syncTemplates(sinceStr)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { templates ->
                        templateDao.insertAll(templates)
                        return@withContext Result.success(templates)
                    }
                }
                
                Result.failure(Exception(response.message() ?: "Error al sincronizar plantillas"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Obtiene plantillas pendientes de subir al servidor
     */
    suspend fun getPendingUploadTemplates(): List<LabelTemplate> {
        return withContext(Dispatchers.IO) {
            templateDao.getPendingUpload()
        }
    }
    
    /**
     * Marca una plantilla como sincronizada
     */
    suspend fun markAsSynced(templateId: Int): Int {
        return withContext(Dispatchers.IO) {
            templateDao.markAsSynced(templateId)
        }
    }
    
    /**
     * Gestiona la respuesta de la API para operaciones con plantillas
     */
    private fun handleTemplateResponse(response: Response<ApiResponse<LabelTemplate>>): Result<LabelTemplate> {
        if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                // Guardar la plantilla en la base de datos local
                templateDao.insert(apiResponse.data)
                
                return Result.success(apiResponse.data)
            }
            return Result.failure(Exception(apiResponse?.message ?: "Respuesta sin datos"))
        }
        return Result.failure(Exception(response.message() ?: "Error desconocido"))
    }
}