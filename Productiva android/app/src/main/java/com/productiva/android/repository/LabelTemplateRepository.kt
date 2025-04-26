package com.productiva.android.repository

import android.util.Log
import com.productiva.android.dao.LabelTemplateDao
import com.productiva.android.model.LabelTemplate
import com.productiva.android.network.ApiService
import com.productiva.android.network.NetworkResult
import com.productiva.android.network.safeApiCall
import com.productiva.android.utils.ConnectivityMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repositorio para gestionar las plantillas de etiquetas.
 * Proporciona métodos para acceder y manipular las plantillas de etiquetas,
 * incluyendo sincronización con el servidor.
 */
class LabelTemplateRepository(
    private val labelTemplateDao: LabelTemplateDao,
    private val apiService: ApiService,
    private val connectivityMonitor: ConnectivityMonitor
) {
    private val TAG = "LabelTemplateRepository"
    
    /**
     * Obtiene todas las plantillas de etiquetas.
     *
     * @return Flow con el estado del recurso que contiene la lista de plantillas.
     */
    fun getAllLabelTemplates(): Flow<ResourceState<List<LabelTemplate>>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Emitir datos locales primero (caché)
            val localTemplates = labelTemplateDao.getAllLabelTemplates()
            localTemplates.collect { templates ->
                emit(ResourceState.CachedData(templates))
                
                // Si hay conexión a Internet, intentar sincronizar
                if (connectivityMonitor.isNetworkAvailable()) {
                    fetchAndSyncTemplates()
                } else {
                    emit(ResourceState.Offline<List<LabelTemplate>>())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener plantillas de etiquetas", e)
            emit(ResourceState.Error("Error al obtener plantillas: ${e.message}", e))
        }
    }
    
    /**
     * Obtiene una plantilla de etiqueta por su ID.
     *
     * @param templateId ID de la plantilla.
     * @return Flow con el estado del recurso que contiene la plantilla.
     */
    fun getLabelTemplateById(templateId: Int): Flow<ResourceState<LabelTemplate>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Emitir datos locales primero (caché)
            val localTemplate = labelTemplateDao.getLabelTemplateById(templateId)
            localTemplate.collect { template ->
                if (template != null) {
                    emit(ResourceState.Success(template))
                } else {
                    // Si hay conexión, intentar obtener del servidor
                    if (connectivityMonitor.isNetworkAvailable()) {
                        fetchAndSyncTemplates()
                        
                        // Verificar si después de la sincronización ya existe la plantilla
                        val updatedTemplate = labelTemplateDao.getLabelTemplateByIdSync(templateId)
                        if (updatedTemplate != null) {
                            emit(ResourceState.Success(updatedTemplate))
                        } else {
                            emit(ResourceState.Error("Plantilla no encontrada"))
                        }
                    } else {
                        emit(ResourceState.Error("Plantilla no encontrada y sin conexión a Internet"))
                        emit(ResourceState.Offline<LabelTemplate>())
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener plantilla por ID", e)
            emit(ResourceState.Error("Error al obtener plantilla: ${e.message}", e))
        }
    }
    
    /**
     * Obtiene la plantilla de etiqueta predeterminada.
     *
     * @return Flow con el estado del recurso que contiene la plantilla predeterminada.
     */
    fun getDefaultLabelTemplate(): Flow<ResourceState<LabelTemplate>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Emitir datos locales primero (caché)
            val localTemplate = labelTemplateDao.getDefaultLabelTemplate()
            localTemplate.collect { template ->
                if (template != null) {
                    emit(ResourceState.Success(template))
                } else {
                    // Si hay conexión, intentar obtener del servidor
                    if (connectivityMonitor.isNetworkAvailable()) {
                        fetchAndSyncTemplates()
                        
                        // Verificar si después de la sincronización ya existe una plantilla predeterminada
                        val updatedTemplate = labelTemplateDao.getDefaultLabelTemplate()
                        updatedTemplate.collect { defaultTemplate ->
                            if (defaultTemplate != null) {
                                emit(ResourceState.Success(defaultTemplate))
                            } else {
                                emit(ResourceState.Error("No hay plantilla predeterminada"))
                            }
                        }
                    } else {
                        emit(ResourceState.Error("No hay plantilla predeterminada y sin conexión a Internet"))
                        emit(ResourceState.Offline<LabelTemplate>())
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener plantilla predeterminada", e)
            emit(ResourceState.Error("Error al obtener plantilla predeterminada: ${e.message}", e))
        }
    }
    
    /**
     * Establece una plantilla como predeterminada.
     *
     * @param templateId ID de la plantilla a establecer como predeterminada.
     * @return Flow con el estado del recurso que indica el resultado de la operación.
     */
    fun setDefaultTemplate(templateId: Int): Flow<ResourceState<Boolean>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Verificar que la plantilla existe
            val template = labelTemplateDao.getLabelTemplateByIdSync(templateId)
            if (template == null) {
                emit(ResourceState.Error("Plantilla no encontrada"))
                return@flow
            }
            
            // Establecer como predeterminada
            labelTemplateDao.setAsDefaultTemplate(templateId)
            emit(ResourceState.Success(true))
            
            // Si hay conexión, intentamos sincronizar con el servidor
            // Nota: Esto requeriría un endpoint específico para establecer la plantilla predeterminada
            // que no está especificado en la API original
            if (connectivityMonitor.isNetworkAvailable()) {
                // Aquí iría la llamada a la API cuando esté disponible
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al establecer plantilla predeterminada", e)
            emit(ResourceState.Error("Error al establecer plantilla predeterminada: ${e.message}", e))
        }
    }
    
    /**
     * Obtiene las plantillas de etiquetas desde el servidor y las sincroniza con la base de datos local.
     */
    private suspend fun fetchAndSyncTemplates() {
        if (!connectivityMonitor.isNetworkAvailable()) {
            return
        }
        
        try {
            // Obtener plantillas desde el servidor
            val result = safeApiCall {
                apiService.getLabelTemplates()
            }
            
            when (result) {
                is NetworkResult.Success -> {
                    val templates = result.data
                    val currentTime = System.currentTimeMillis()
                    
                    // Sincronizar con la base de datos local
                    labelTemplateDao.syncLabelTemplatesFromServer(templates, emptyList(), currentTime)
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Error al obtener plantillas del servidor: ${result.message}")
                }
                is NetworkResult.Loading -> {
                    // No debería ocurrir, pero por si acaso
                    Log.d(TAG, "Loading state in fetchAndSyncTemplates network call")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar plantillas", e)
        }
    }
}