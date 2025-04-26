package com.productiva.android.repository

import android.content.Context
import com.productiva.android.api.ApiService
import com.productiva.android.data.dao.UserDao
import com.productiva.android.model.User
import com.productiva.android.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio para operaciones relacionadas con usuarios
 * Centraliza el acceso a los datos de usuarios, ya sea de la API o de la base de datos local
 */
class UserRepository(
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val context: Context
) {
    private val sessionManager = SessionManager(context)
    
    /**
     * Realiza el proceso de login con el servidor
     */
    suspend fun login(username: String, password: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(username, password)
                
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    
                    if (authResponse != null) {
                        // Guardar datos de sesión
                        sessionManager.saveAuthToken(authResponse.token)
                        sessionManager.saveUserId(authResponse.user.id)
                        
                        // Guardar usuario en la base de datos local
                        userDao.insertUser(authResponse.user)
                        
                        Result.success(true)
                    } else {
                        Result.failure(Exception("Respuesta vacía del servidor"))
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Credenciales incorrectas"
                        403 -> "Acceso denegado"
                        404 -> "Usuario no encontrado"
                        500 -> "Error en el servidor"
                        else -> "Error: ${response.code()} - ${response.message()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Error de conexión: ${e.message}"))
            }
        }
    }
    
    /**
     * Obtiene los usuarios disponibles para el usuario autenticado
     * Estos usuarios son los perfiles a los que tiene acceso
     */
    suspend fun getAvailableUsers(token: String): Result<List<User>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUsers(
                    token = "Bearer $token"
                )
                
                if (response.isSuccessful) {
                    val users = response.body() ?: emptyList()
                    
                    // Guardar usuarios en la base de datos local
                    if (users.isNotEmpty()) {
                        userDao.insertAllUsers(users)
                    }
                    
                    Result.success(users)
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Token expirado o inválido"
                        403 -> "Acceso denegado"
                        404 -> "No se encontraron usuarios"
                        500 -> "Error en el servidor"
                        else -> "Error: ${response.code()} - ${response.message()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                // Intentar obtener usuarios de la base de datos local
                val localUsers = userDao.getAllUsers().value ?: emptyList()
                if (localUsers.isNotEmpty()) {
                    return@withContext Result.success(localUsers)
                }
                
                Result.failure(Exception("Error de conexión: ${e.message}"))
            }
        }
    }
    
    /**
     * Selecciona un usuario (perfil) para la sesión actual
     */
    suspend fun selectUser(userId: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val user = userDao.getUserByIdSync(userId)
                
                if (user != null) {
                    // Guardar datos de usuario seleccionado
                    sessionManager.saveSelectedUserId(userId)
                    
                    // Si el usuario tiene ubicación, guardarla también
                    user.locationId?.let { locationId ->
                        sessionManager.saveLocationId(locationId)
                    }
                    
                    Result.success(true)
                } else {
                    Result.failure(Exception("Usuario no encontrado en la base de datos local"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Error al seleccionar usuario: ${e.message}"))
            }
        }
    }
    
    /**
     * Cierra la sesión del usuario
     */
    fun logout() {
        sessionManager.clearSession()
    }
}