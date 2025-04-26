package com.productiva.android.repository

import androidx.lifecycle.LiveData
import com.productiva.android.ProductivaApplication
import com.productiva.android.api.ApiService
import com.productiva.android.dao.UserDao
import com.productiva.android.model.User
import com.productiva.android.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.Date

/**
 * Repositorio para la gestión de usuarios
 */
class UserRepository(private val app: ProductivaApplication) {
    
    private val userDao: UserDao = app.database.userDao()
    private val apiService: ApiService = app.apiService
    private val sessionManager: SessionManager = app.sessionManager
    
    /**
     * Intenta autenticar a un usuario con sus credenciales
     * @param username Nombre de usuario
     * @param password Contraseña
     * @return Respuesta de la API con el token y datos de usuario
     */
    suspend fun login(username: String, password: String): Response<Map<String, Any>> {
        return withContext(Dispatchers.IO) {
            apiService.login(username, password)
        }
    }
    
    /**
     * Obtiene todos los usuarios de la base de datos local
     * @return LiveData con la lista de usuarios
     */
    fun getAllUsers(): LiveData<List<User>> {
        return userDao.getAllUsers()
    }
    
    /**
     * Obtiene todos los usuarios activos de la base de datos local
     * @return LiveData con la lista de usuarios activos
     */
    fun getActiveUsers(): LiveData<List<User>> {
        return userDao.getActiveUsers()
    }
    
    /**
     * Obtiene un usuario por su ID
     * @param userId ID del usuario
     * @return Usuario encontrado o null
     */
    suspend fun getUserById(userId: Int): User? {
        return withContext(Dispatchers.IO) {
            userDao.getUserById(userId)
        }
    }
    
    /**
     * Obtiene usuarios por compañía
     * @param companyId ID de la compañía
     * @return LiveData con la lista de usuarios de la compañía
     */
    fun getUsersByCompany(companyId: Int): LiveData<List<User>> {
        return userDao.getUsersByCompany(companyId)
    }
    
    /**
     * Obtiene usuarios por ubicación
     * @param locationId ID de la ubicación
     * @return LiveData con la lista de usuarios de la ubicación
     */
    fun getUsersByLocation(locationId: Int): LiveData<List<User>> {
        return userDao.getUsersByLocation(locationId)
    }
    
    /**
     * Sincroniza usuarios desde el servidor
     * @return True si la sincronización fue exitosa
     */
    suspend fun syncUsers(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUsers()
                if (response.isSuccessful) {
                    response.body()?.let { users ->
                        userDao.deleteAllUsers()
                        userDao.insertUsers(users)
                        return@withContext true
                    }
                }
                false
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Sincroniza usuarios de una ubicación específica desde el servidor
     * @param locationId ID de la ubicación
     * @return True si la sincronización fue exitosa
     */
    suspend fun syncUsersByLocation(locationId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUsersByLocation(locationId)
                if (response.isSuccessful) {
                    response.body()?.let { users ->
                        userDao.insertUsers(users)
                        return@withContext true
                    }
                }
                false
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Sincroniza usuarios de una compañía específica desde el servidor
     * @param companyId ID de la compañía
     * @return True si la sincronización fue exitosa
     */
    suspend fun syncUsersByCompany(companyId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUsersByCompany(companyId)
                if (response.isSuccessful) {
                    response.body()?.let { users ->
                        userDao.insertUsers(users)
                        return@withContext true
                    }
                }
                false
            } catch (e: Exception) {
                false
            }
        }
    }
}