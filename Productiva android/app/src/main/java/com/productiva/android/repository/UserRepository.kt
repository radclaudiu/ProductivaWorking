package com.productiva.android.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.productiva.android.api.ApiService
import com.productiva.android.models.User
import com.productiva.android.utils.AppDatabase
import com.productiva.android.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio para operaciones relacionadas con usuarios
 */
class UserRepository(
    private val apiService: ApiService,
    private val database: AppDatabase,
    private val sessionManager: SessionManager
) {
    /**
     * Intenta autenticar al usuario con las credenciales proporcionadas
     */
    suspend fun login(username: String, password: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(username, password)
                
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        // Guardar token de autenticación
                        sessionManager.saveAuthToken(loginResponse.token)
                        
                        // Guardar información básica del usuario
                        sessionManager.saveUserInfo(
                            loginResponse.user.id,
                            loginResponse.user.username,
                            loginResponse.user.email,
                            loginResponse.user.role
                        )
                        
                        // Obtener datos completos del usuario
                        val userResponse = apiService.getCurrentUser("Bearer ${loginResponse.token}")
                        if (userResponse.isSuccessful && userResponse.body() != null) {
                            val user = userResponse.body()!!
                            database.userDao().insertUser(user)
                            
                            // Si el usuario tiene locationId y companyId, guardarlos
                            if (user.locationId != null && user.companyId != null) {
                                sessionManager.saveLocationInfo(
                                    user.locationId,
                                    user.companyId
                                )
                            }
                        }
                        
                        Result.success(true)
                    } else {
                        Result.failure(Exception("Respuesta vacía del servidor"))
                    }
                } else {
                    Result.failure(Exception("Error de autenticación: ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "Error en login", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Obtiene todos los usuarios de la ubicación actual
     */
    suspend fun refreshUsers(locationId: Int? = null) {
        withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.getAuthToken()
                if (token != null) {
                    val response = apiService.getUsers("Bearer $token", locationId)
                    if (response.isSuccessful && response.body() != null) {
                        database.userDao().insertAllUsers(response.body()!!)
                    }
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "Error al refrescar usuarios", e)
            }
        }
    }
    
    /**
     * Obtiene usuarios localmente
     */
    fun getUsers(): LiveData<List<User>> {
        return database.userDao().getAllUsers()
    }
    
    /**
     * Obtiene un usuario por ID
     */
    fun getUserById(userId: Int): LiveData<User> {
        return database.userDao().getUserById(userId)
    }
    
    /**
     * Cierra la sesión del usuario
     */
    suspend fun logout() {
        withContext(Dispatchers.IO) {
            // Simplemente borramos los datos de sesión
            sessionManager.clearSession()
            // Opcional: borrar base de datos local
            // database.userDao().deleteAllUsers()
        }
    }
}