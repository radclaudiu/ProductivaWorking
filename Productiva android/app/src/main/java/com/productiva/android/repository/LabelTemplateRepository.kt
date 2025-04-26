package com.productiva.android.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.productiva.android.api.ApiClient
import com.productiva.android.api.ApiResponse
import com.productiva.android.dao.LabelTemplateDao
import com.productiva.android.database.AppDatabase
import com.productiva.android.model.LabelTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Repositorio para manejar operaciones relacionadas con plantillas de etiquetas
 */
class LabelTemplateRepository(private val context: Context) {
    
    private val labelTemplateDao: LabelTemplateDao = AppDatabase.getDatabase(context).labelTemplateDao()
    private val apiClient = ApiClient.getInstance(context)
    
    /**
     * Obtiene todas las plantillas como LiveData desde la base de datos local
     */
    fun getAllTemplates(): LiveData<List<LabelTemplate>> {
        return labelTemplateDao.getAllTemplates()
    }
    
    /**
     * Obtiene las plantillas por usuario como LiveData desde la base de datos local
     */
    fun getTemplatesByUser(userId: Int): LiveData<List<LabelTemplate>> {
        return labelTemplateDao.getTemplatesByUser(userId)
    }
    
    /**
     * Obtiene las plantillas por compañía como LiveData desde la base de datos local
     */
    fun getTemplatesByCompany(companyId: Int): LiveData<List<LabelTemplate>> {
        return labelTemplateDao.getTemplatesByCompany(companyId)
    }
    
    /**
     * Obtiene una plantilla por su ID desde la base de datos local
     */
    suspend fun getTemplateById(templateId: Int): LabelTemplate? = withContext(Dispatchers.IO) {
        return@withContext labelTemplateDao.getTemplateById(templateId)
    }
    
    /**
     * Obtiene la plantilla predeterminada desde la base de datos local
     */
    suspend fun getDefaultTemplate(): LabelTemplate? = withContext(Dispatchers.IO) {
        return@withContext labelTemplateDao.getDefaultTemplate()
    }
    
    /**
     * Establece una plantilla como predeterminada
     */
    suspend fun setDefaultTemplate(templateId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            labelTemplateDao.clearDefaultTemplates()
            labelTemplateDao.setDefaultTemplate(templateId)
            return@withContext Result.success(true)
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Obtiene las plantillas desde el servidor y las almacena localmente
     */
    suspend fun fetchLabelTemplates(): Result<List<LabelTemplate>> = withContext(Dispatchers.IO) {
        try {
            val response: Response<ApiResponse<List<LabelTemplate>>> = apiClient.apiService.getLabelTemplates()
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    // Guarda las plantillas en la base de datos local
                    labelTemplateDao.insertAll(apiResponse.data)
                    return@withContext Result.success(apiResponse.data)
                } else {
                    return@withContext Result.failure(Exception(apiResponse?.message ?: "Error en la respuesta"))
                }
            } else {
                return@withContext Result.failure(Exception("Error al obtener plantillas: ${response.code()}"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Obtiene las plantillas por usuario desde el servidor
     */
    suspend fun fetchLabelTemplatesByUser(userId: Int): Result<List<LabelTemplate>> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.apiService.getLabelTemplatesByUser(userId)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    // Guarda las plantillas en la base de datos local
                    labelTemplateDao.insertAll(apiResponse.data)
                    return@withContext Result.success(apiResponse.data)
                } else {
                    return@withContext Result.failure(Exception(apiResponse?.message ?: "Error en la respuesta"))
                }
            } else {
                return@withContext Result.failure(Exception("Error al obtener plantillas: ${response.code()}"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Obtiene las plantillas por compañía desde el servidor
     */
    suspend fun fetchLabelTemplatesByCompany(companyId: Int): Result<List<LabelTemplate>> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.apiService.getLabelTemplatesByCompany(companyId)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    // Guarda las plantillas en la base de datos local
                    labelTemplateDao.insertAll(apiResponse.data)
                    return@withContext Result.success(apiResponse.data)
                } else {
                    return@withContext Result.failure(Exception(apiResponse?.message ?: "Error en la respuesta"))
                }
            } else {
                return@withContext Result.failure(Exception("Error al obtener plantillas: ${response.code()}"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Crea una nueva plantilla en el servidor y localmente
     */
    suspend fun createLabelTemplate(template: LabelTemplate): Result<LabelTemplate> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.apiService.createLabelTemplate(template)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    // Guarda la plantilla en la base de datos local
                    labelTemplateDao.insert(apiResponse.data)
                    return@withContext Result.success(apiResponse.data)
                } else {
                    return@withContext Result.failure(Exception(apiResponse?.message ?: "Error en la respuesta"))
                }
            } else {
                return@withContext Result.failure(Exception("Error al crear plantilla: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Si hay un error de conexión, guardamos localmente y marcamos para sincronizar después
            val templateWithSyncFlag = template.copy(isSynced = false, lastSync = System.currentTimeMillis())
            val id = labelTemplateDao.insert(templateWithSyncFlag)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Actualiza una plantilla en el servidor y localmente
     */
    suspend fun updateLabelTemplate(template: LabelTemplate): Result<LabelTemplate> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.apiService.updateLabelTemplate(template.id, template)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    // Actualiza la plantilla en la base de datos local
                    labelTemplateDao.update(apiResponse.data)
                    return@withContext Result.success(apiResponse.data)
                } else {
                    return@withContext Result.failure(Exception(apiResponse?.message ?: "Error en la respuesta"))
                }
            } else {
                return@withContext Result.failure(Exception("Error al actualizar plantilla: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Si hay un error de conexión, actualizamos localmente y marcamos para sincronizar después
            val templateWithSyncFlag = template.copy(isSynced = false, lastSync = System.currentTimeMillis())
            labelTemplateDao.update(templateWithSyncFlag)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Elimina una plantilla del servidor y localmente
     */
    suspend fun deleteLabelTemplate(templateId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.apiService.deleteLabelTemplate(templateId)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    // Elimina la plantilla de la base de datos local
                    labelTemplateDao.deleteTemplateById(templateId)
                    return@withContext Result.success(true)
                } else {
                    return@withContext Result.failure(Exception(apiResponse?.message ?: "Error en la respuesta"))
                }
            } else {
                return@withContext Result.failure(Exception("Error al eliminar plantilla: ${response.code()}"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Sincroniza las plantillas con el servidor
     */
    suspend fun syncLabelTemplates(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // Obtener la última sincronización
            val lastSync = System.currentTimeMillis() - (24 * 60 * 60 * 1000) // 24 horas por defecto
            
            val response = apiClient.apiService.syncLabelTemplates(lastSync)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    // Guarda las plantillas en la base de datos local
                    labelTemplateDao.insertAll(apiResponse.data)
                    
                    // Sincroniza las plantillas modificadas localmente
                    val templatesToSync = labelTemplateDao.getTemplatesToSync(lastSync)
                    // Aquí deberías implementar la lógica para enviar las plantillas al servidor
                    
                    return@withContext Result.success(apiResponse.data.size)
                } else {
                    return@withContext Result.failure(Exception(apiResponse?.message ?: "Error en la respuesta"))
                }
            } else {
                return@withContext Result.failure(Exception("Error al sincronizar plantillas: ${response.code()}"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
}