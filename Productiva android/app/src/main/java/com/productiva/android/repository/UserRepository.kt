package com.productiva.android.repository

import android.util.Log
import com.productiva.android.model.User
import com.productiva.android.network.ApiService
import com.productiva.android.network.RetrofitClient
import com.productiva.android.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

/**
 * Repositorio para gestionar usuarios y autenticación.
 */
class UserRepository(
    private val apiService: ApiService
) {
    private val TAG = "UserRepository"
    
    /**
     * Inicia sesión con las credenciales proporcionadas.
     * 
     * @param username Nombre de usuario.
     * @param password Contraseña.
     * @return Flow con el resultado del inicio de sesión.
     */
    fun login(username: String, password: String): Flow<ResourceState<User>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // Preparar datos para la petición
            val credentials = mapOf(
                "username" to username,
                "password" to password
            )
            
            // Realizar petición de login
            val response = apiService.login(credentials)
            
            if (response.isSuccessful) {
                val loginResponse = response.body()
                
                if (loginResponse != null) {
                    // Guardar información de sesión
                    SessionManager.getInstance().saveAuthToken(
                        loginResponse.token,
                        loginResponse.expiresAt
                    )
                    SessionManager.getInstance().saveUser(loginResponse.user)
                    
                    Log.d(TAG, "Inicio de sesión exitoso: ${loginResponse.user.username}")
                    emit(ResourceState.Success(loginResponse.user))
                } else {
                    emit(ResourceState.Error("Respuesta vacía del servidor"))
                }
            } else {
                val errorCode = response.code()
                val errorMessage = when (errorCode) {
                    401 -> "Credenciales incorrectas"
                    403 -> "Cuenta desactivada o sin permisos"
                    else -> "Error del servidor: $errorCode"
                }
                
                Log.e(TAG, "Error en inicio de sesión: $errorCode")
                emit(ResourceState.Error(errorMessage))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error de red en inicio de sesión", e)
            emit(ResourceState.Error("Error de conexión: comprueba tu conexión a Internet"))
        } catch (e: Exception) {
            Log.e(TAG, "Error en inicio de sesión", e)
            emit(ResourceState.Error("Error: ${e.message}"))
        }
    }
    
    /**
     * Cierra la sesión actual.
     */
    fun logout() {
        SessionManager.getInstance().clearSession()
        RetrofitClient.invalidateCache()
        Log.d(TAG, "Sesión cerrada correctamente")
    }
    
    /**
     * Obtiene el usuario actual.
     * 
     * @return Flow con el resultado de la obtención del usuario.
     */
    fun getCurrentUser(): Flow<ResourceState<User>> = flow {
        emit(ResourceState.Loading)
        
        // Primero intentar obtener usuario de sesión local
        val localUser = SessionManager.getInstance().getCurrentUser()
        
        if (localUser != null) {
            emit(ResourceState.Success(localUser))
        } else {
            // Si no hay usuario local, limpiar sesión
            logout()
            emit(ResourceState.Error("No hay sesión activa"))
        }
    }
    
    /**
     * Actualiza la información del usuario actual desde el servidor.
     * 
     * @return Flow con el resultado de la actualización.
     */
    fun refreshUserInfo(): Flow<ResourceState<User>> = flow {
        emit(ResourceState.Loading)
        
        try {
            // Realizar petición para obtener usuario actual
            val response = apiService.getCurrentUser()
            
            if (response.isSuccessful) {
                val user = response.body()
                
                if (user != null) {
                    // Actualizar información de usuario en sesión
                    SessionManager.getInstance().saveUser(user)
                    
                    Log.d(TAG, "Información de usuario actualizada: ${user.username}")
                    emit(ResourceState.Success(user))
                } else {
                    emit(ResourceState.Error("Respuesta vacía del servidor"))
                }
            } else {
                val errorCode = response.code()
                val errorMessage = when (errorCode) {
                    401 -> "Sesión expirada"
                    403 -> "Sin permisos para acceder a esta información"
                    else -> "Error del servidor: $errorCode"
                }
                
                if (errorCode == 401) {
                    // Sesión expirada, limpiar sesión
                    logout()
                }
                
                Log.e(TAG, "Error al actualizar información de usuario: $errorCode")
                emit(ResourceState.Error(errorMessage))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error de red al actualizar información de usuario", e)
            emit(ResourceState.Error("Error de conexión: comprueba tu conexión a Internet"))
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar información de usuario", e)
            emit(ResourceState.Error("Error: ${e.message}"))
        }
    }
}