package com.productiva.android.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.productiva.android.api.ApiClient
import com.productiva.android.api.ApiResponse
import com.productiva.android.dao.UserDao
import com.productiva.android.database.AppDatabase
import com.productiva.android.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Repositorio para manejar operaciones relacionadas con usuarios
 */
class UserRepository(private val context: Context) {
    
    private val userDao: UserDao = AppDatabase.getDatabase(context).userDao()
    private val apiClient = ApiClient.getInstance(context)
    
    /**
     * Obtiene todos los usuarios como LiveData desde la base de datos local
     */
    fun getAllUsers(): LiveData<List<User>> {
        return userDao.getAllUsers()
    }
    
    /**
     * Obtiene un usuario por su ID desde la base de datos local
     */
    suspend fun getUserById(userId: Int): User? = withContext(Dispatchers.IO) {
        return@withContext userDao.getUserById(userId)
    }
    
    /**
     * Obtiene el usuario por nombre de usuario desde la base de datos local
     */
    suspend fun getUserByUsername(username: String): User? = withContext(Dispatchers.IO) {
        return@withContext userDao.getUserByUsername(username)
    }
    
    /**
     * Inicia sesión en el servidor
     */
    suspend fun login(username: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response: Response<ApiResponse<User>> = apiClient.apiService.login(username, password)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    // Guarda el usuario en la base de datos local
                    userDao.insert(apiResponse.data)
                    
                    // Si hay un token en la respuesta, guárdalo
                    apiResponse.data.id.let { userId ->
                        // Aquí podrías guardar información adicional del usuario si es necesario
                    }
                    
                    return@withContext Result.success(apiResponse.data)
                } else {
                    return@withContext Result.failure(Exception(apiResponse?.message ?: "Error en la respuesta"))
                }
            } else {
                return@withContext Result.failure(Exception("Error de autenticación: ${response.code()}"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Obtiene los usuarios por compañía desde el servidor y los almacena localmente
     */
    suspend fun fetchUsersByCompany(companyId: Int): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.apiService.getUsersByCompany(companyId)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    // Guarda los usuarios en la base de datos local
                    userDao.insertAll(apiResponse.data)
                    return@withContext Result.success(apiResponse.data)
                } else {
                    return@withContext Result.failure(Exception(apiResponse?.message ?: "Error en la respuesta"))
                }
            } else {
                return@withContext Result.failure(Exception("Error al obtener usuarios: ${response.code()}"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Sincroniza los usuarios con el servidor
     */
    suspend fun syncUsers(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // Obtener la última sincronización
            val lastSync = System.currentTimeMillis() - (24 * 60 * 60 * 1000) // 24 horas por defecto
            
            val response = apiClient.apiService.syncUsers(lastSync)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                    // Guarda los usuarios en la base de datos local
                    userDao.insertAll(apiResponse.data)
                    return@withContext Result.success(apiResponse.data.size)
                } else {
                    return@withContext Result.failure(Exception(apiResponse?.message ?: "Error en la respuesta"))
                }
            } else {
                return@withContext Result.failure(Exception("Error al sincronizar usuarios: ${response.code()}"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Cierra la sesión del usuario actual
     */
    suspend fun logout() = withContext(Dispatchers.IO) {
        // Limpiar el token de autenticación
        apiClient.clearAuthToken()
        
        // Aquí podrías realizar otras tareas de limpieza si es necesario
        // Por ejemplo, borrar datos sensibles de la base de datos local
    }
}