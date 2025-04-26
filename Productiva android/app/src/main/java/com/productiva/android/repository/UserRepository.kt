package com.productiva.android.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.productiva.android.api.ApiClient
import com.productiva.android.api.ApiResponse
import com.productiva.android.database.AppDatabase
import com.productiva.android.database.UserDao
import com.productiva.android.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.Date

/**
 * Repositorio para gestionar usuarios
 */
class UserRepository(context: Context) {
    
    private val apiClient = ApiClient.getInstance(context)
    private val userDao: UserDao
    
    init {
        val database = AppDatabase.getInstance(context)
        userDao = database.userDao()
    }
    
    /**
     * Obtiene todos los usuarios de la base de datos local
     */
    fun getAllUsers(): LiveData<List<User>> {
        return userDao.getAllUsers()
    }
    
    /**
     * Obtiene un usuario por su ID de la base de datos local
     */
    suspend fun getUserById(userId: Int): User? {
        return withContext(Dispatchers.IO) {
            userDao.getUserById(userId)
        }
    }
    
    /**
     * Obtiene usuarios de una empresa específica
     */
    fun getUsersByCompany(companyId: Int): LiveData<List<User>> {
        return userDao.getUsersByCompany(companyId)
    }
    
    /**
     * Busca usuarios por nombre, email o username
     */
    fun searchUsers(query: String): LiveData<List<User>> {
        return userDao.searchUsers(query)
    }
    
    /**
     * Inserta un usuario en la base de datos local
     */
    suspend fun insertUser(user: User): Long {
        return withContext(Dispatchers.IO) {
            userDao.insert(user)
        }
    }
    
    /**
     * Actualiza un usuario en la base de datos local
     */
    suspend fun updateUser(user: User): Int {
        return withContext(Dispatchers.IO) {
            userDao.update(user)
        }
    }
    
    /**
     * Inicia sesión con usuario y contraseña
     */
    suspend fun login(username: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiClient.apiService.login(username, password)
                handleUserResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Cierra la sesión del usuario actual
     */
    suspend fun logout(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiClient.apiService.logout()
                apiClient.clearAuthToken()
                Result.success(response.isSuccessful)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Obtiene información del usuario actual (logueado)
     */
    suspend fun getCurrentUser(): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiClient.apiService.getCurrentUser()
                handleUserResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Obtiene un usuario por su ID desde la API
     */
    suspend fun fetchUserById(userId: Int): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiClient.apiService.getUserById(userId)
                handleUserResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Obtiene usuarios desde la API y los guarda en la base de datos local
     */
    suspend fun syncUsers(companyId: Int? = null): Result<List<User>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiClient.apiService.getUsers(companyId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { users ->
                        userDao.insertAll(users)
                        return@withContext Result.success(users)
                    }
                }
                
                Result.failure(Exception(response.message() ?: "Error al sincronizar usuarios"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Sincroniza usuarios actualizados desde una fecha específica
     */
    suspend fun syncUsersUpdatedSince(since: Date, companyId: Int? = null): Result<List<User>> {
        return withContext(Dispatchers.IO) {
            try {
                val sinceStr = since.time.toString()
                val response = apiClient.apiService.syncUsers(sinceStr, companyId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { users ->
                        userDao.insertAll(users)
                        return@withContext Result.success(users)
                    }
                }
                
                Result.failure(Exception(response.message() ?: "Error al sincronizar usuarios"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Gestiona la respuesta de la API para operaciones con usuarios
     */
    private fun handleUserResponse(response: Response<ApiResponse<User>>): Result<User> {
        if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                // Almacenar token si se recibe
                apiResponse.token?.let { token ->
                    apiClient.saveAuthToken(token)
                    apiResponse.data.authToken = token
                }
                
                // Guardar el usuario en la base de datos local
                userDao.insert(apiResponse.data)
                
                return Result.success(apiResponse.data)
            }
            return Result.failure(Exception(apiResponse?.message ?: "Respuesta sin datos"))
        }
        return Result.failure(Exception(response.message() ?: "Error desconocido"))
    }
}