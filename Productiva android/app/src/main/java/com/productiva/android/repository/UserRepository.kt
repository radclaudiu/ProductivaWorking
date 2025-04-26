package com.productiva.android.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.productiva.android.database.dao.UserDao
import com.productiva.android.model.User
import com.productiva.android.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Repositorio para gestionar usuarios tanto en la base de datos local como en el servidor.
 */
class UserRepository(
    private val userDao: UserDao,
    private val apiService: ApiService
) {
    private val TAG = "UserRepository"
    
    /**
     * Obtiene todos los usuarios desde la base de datos local.
     */
    fun getAllUsers(): LiveData<List<User>> {
        return userDao.getAllUsers()
    }
    
    /**
     * Obtiene usuarios activos de una compañía específica.
     */
    fun getActiveUsersByCompany(companyId: Int): LiveData<List<User>> {
        return userDao.getActiveUsersByCompany(companyId)
    }
    
    /**
     * Busca usuarios por nombre o email.
     */
    fun searchUsers(query: String): LiveData<List<User>> {
        return userDao.searchUsers(query)
    }
    
    /**
     * Obtiene un usuario por su ID.
     */
    suspend fun getUserById(userId: Int): User? {
        return withContext(Dispatchers.IO) {
            userDao.getUserById(userId)
        }
    }
    
    /**
     * Obtiene un usuario por su nombre de usuario.
     */
    suspend fun getUserByUsername(username: String): User? {
        return withContext(Dispatchers.IO) {
            userDao.getUserByUsername(username)
        }
    }
    
    /**
     * Actualiza un usuario en la base de datos local.
     */
    suspend fun updateUser(user: User) {
        withContext(Dispatchers.IO) {
            userDao.update(user)
        }
    }
    
    /**
     * Sincroniza los usuarios desde el servidor con la base de datos local.
     */
    suspend fun syncUsers(): Flow<ResourceState<List<User>>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Obtener usuarios del servidor
            val response = apiService.getUsers()
            
            if (response.isSuccessful) {
                val users = response.body() ?: emptyList()
                
                // Guardar en base de datos local
                withContext(Dispatchers.IO) {
                    userDao.insertAll(users)
                }
                
                emit(ResourceState.Success(users))
            } else {
                emit(ResourceState.Error("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar usuarios", e)
            emit(ResourceState.Error("Error de red: ${e.message}"))
        }
    }
    
    /**
     * Autentica un usuario con sus credenciales.
     */
    suspend fun login(username: String, password: String): Flow<ResourceState<User>> = flow {
        emit(ResourceState.Loading())
        
        try {
            // Intentar login con el servidor
            val response = apiService.login(username, password)
            
            if (response.isSuccessful) {
                val user = response.body()
                
                if (user != null) {
                    // Guardar usuario en local
                    withContext(Dispatchers.IO) {
                        userDao.insert(user)
                    }
                    
                    emit(ResourceState.Success(user))
                } else {
                    emit(ResourceState.Error("Datos de usuario inválidos"))
                }
            } else {
                emit(ResourceState.Error("Error de autenticación: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en login", e)
            
            // Intentar recuperar usuario de la base de datos local
            val localUser = userDao.getUserByUsername(username)
            
            if (localUser != null) {
                emit(ResourceState.Success(localUser, isFromCache = true))
            } else {
                emit(ResourceState.Error("Error de red: ${e.message}"))
            }
        }
    }
}