package com.productiva.android.session

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.productiva.android.network.model.AuthRequest
import com.productiva.android.network.model.User
import com.productiva.android.network.RetrofitClient
import com.productiva.android.network.safeApiCall
import com.productiva.android.network.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Gestor de sesión para manejar la autenticación y el estado del usuario.
 * Implementa el patrón Singleton para garantizar una única instancia en la aplicación.
 */
class SessionManager private constructor() {
    companion object {
        private const val TAG = "SessionManager"
        private const val PREF_NAME = "ProductivaSession"
        
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_COMPANY_ID = "company_id"
        private const val KEY_COMPANY_NAME = "company_name"
        
        @Volatile
        private var instance: SessionManager? = null
        
        /**
         * Obtiene la instancia única del gestor de sesión.
         *
         * @return Instancia del gestor de sesión.
         */
        fun getInstance(): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager().also { instance = it }
            }
        }
    }
    
    private lateinit var sharedPreferences: SharedPreferences
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    // Estados observables
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    /**
     * Inicializa el gestor de sesión.
     *
     * @param context Contexto de la aplicación.
     */
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        
        // Verificar si hay una sesión activa
        val token = getAccessToken()
        if (token != null) {
            val expiryTime = sharedPreferences.getLong(KEY_TOKEN_EXPIRY, 0)
            if (expiryTime > System.currentTimeMillis()) {
                // Sesión válida, cargar datos del usuario desde preferencias
                loadUserFromPreferences()
                _isLoggedIn.value = true
            } else {
                // Token expirado, intentar renovarlo
                coroutineScope.launch {
                    refreshToken(context)
                }
            }
        }
    }
    
    /**
     * Carga la información del usuario desde las preferencias compartidas.
     */
    private fun loadUserFromPreferences() {
        val userId = sharedPreferences.getInt(KEY_USER_ID, -1)
        if (userId != -1) {
            val user = User(
                id = userId,
                username = sharedPreferences.getString(KEY_USERNAME, "") ?: "",
                email = sharedPreferences.getString(KEY_USER_EMAIL, "") ?: "",
                name = sharedPreferences.getString(KEY_USER_NAME, "") ?: "",
                role = sharedPreferences.getString(KEY_USER_ROLE, "") ?: "",
                companyId = sharedPreferences.getInt(KEY_COMPANY_ID, -1),
                companyName = sharedPreferences.getString(KEY_COMPANY_NAME, "") ?: "",
                avatarUrl = null,
                permissions = emptyList() // Las preferencias no guardan listas, se cargarán al actualizar el perfil
            )
            _currentUser.value = user
        }
    }
    
    /**
     * Guarda la información del usuario en las preferencias compartidas.
     *
     * @param user Datos del usuario.
     */
    private fun saveUserToPreferences(user: User) {
        sharedPreferences.edit().apply {
            putInt(KEY_USER_ID, user.id)
            putString(KEY_USERNAME, user.username)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_NAME, user.name)
            putString(KEY_USER_ROLE, user.role)
            putInt(KEY_COMPANY_ID, user.companyId)
            putString(KEY_COMPANY_NAME, user.companyName)
        }.apply()
        
        _currentUser.value = user
    }
    
    /**
     * Inicia sesión con credenciales de usuario.
     *
     * @param context Contexto de la aplicación.
     * @param username Nombre de usuario.
     * @param password Contraseña.
     * @return Resultado de la operación.
     */
    suspend fun login(context: Context, username: String, password: String): LoginResult {
        val apiService = RetrofitClient.getApiService(context)
        
        val result = safeApiCall {
            apiService.login(AuthRequest(username, password))
        }
        
        return when (result) {
            is NetworkResult.Success -> {
                val authResponse = result.data
                
                // Guardar tokens y calcular expiración
                sharedPreferences.edit().apply {
                    putString(KEY_ACCESS_TOKEN, authResponse.token)
                    putString(KEY_REFRESH_TOKEN, authResponse.refreshToken)
                    
                    // Calcular tiempo de expiración (expiresIn es en segundos)
                    val expiryTime = System.currentTimeMillis() + (authResponse.expiresIn * 1000)
                    putLong(KEY_TOKEN_EXPIRY, expiryTime)
                }.apply()
                
                // Guardar información del usuario
                saveUserToPreferences(authResponse.user)
                
                // Actualizar estado de sesión
                _isLoggedIn.value = true
                
                LoginResult.Success
            }
            is NetworkResult.Error -> {
                Log.e(TAG, "Login error: ${result.message}")
                LoginResult.Error(result.message)
            }
            is NetworkResult.Loading -> {
                // Este caso no debería ocurrir con safeApiCall
                LoginResult.Error("Estado de carga inesperado")
            }
        }
    }
    
    /**
     * Renueva el token de acceso usando el token de refresco.
     *
     * @param context Contexto de la aplicación.
     * @return Nuevo token de acceso o null si falló la renovación.
     */
    fun refreshToken(context: Context): String? {
        if (!::sharedPreferences.isInitialized) {
            Log.e(TAG, "SharedPreferences not initialized")
            return null
        }
        
        val refreshToken = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
        if (refreshToken == null) {
            Log.e(TAG, "No refresh token available")
            return null
        }
        
        val apiService = RetrofitClient.getApiService(context)
        
        return runBlocking {
            val result = safeApiCall {
                apiService.refreshToken()
            }
            
            when (result) {
                is NetworkResult.Success -> {
                    val authResponse = result.data
                    
                    // Guardar nuevo token y actualizar expiración
                    sharedPreferences.edit().apply {
                        putString(KEY_ACCESS_TOKEN, authResponse.token)
                        if (authResponse.refreshToken != null) {
                            putString(KEY_REFRESH_TOKEN, authResponse.refreshToken)
                        }
                        
                        // Calcular tiempo de expiración (expiresIn es en segundos)
                        val expiryTime = System.currentTimeMillis() + (authResponse.expiresIn * 1000)
                        putLong(KEY_TOKEN_EXPIRY, expiryTime)
                    }.apply()
                    
                    // Actualizar información del usuario si está disponible
                    if (authResponse.user != null) {
                        saveUserToPreferences(authResponse.user)
                    }
                    
                    // Actualizar estado de sesión
                    _isLoggedIn.value = true
                    
                    authResponse.token
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Token refresh error: ${result.message}")
                    if (result.errorCode == 401) {
                        // Si el refresco da error 401, la sesión ha caducado
                        logout()
                    }
                    null
                }
                is NetworkResult.Loading -> {
                    // Este caso no debería ocurrir con safeApiCall
                    null
                }
            }
        }
    }
    
    /**
     * Actualiza el perfil del usuario desde el servidor.
     *
     * @param context Contexto de la aplicación.
     */
    suspend fun updateUserProfile(context: Context) {
        if (!isLoggedIn.value) {
            return
        }
        
        val apiService = RetrofitClient.getApiService(context)
        
        val result = safeApiCall {
            apiService.getUserInfo()
        }
        
        when (result) {
            is NetworkResult.Success -> {
                val user = result.data
                saveUserToPreferences(user)
            }
            is NetworkResult.Error -> {
                Log.e(TAG, "Error updating user profile: ${result.message}")
            }
            is NetworkResult.Loading -> {
                // Este caso no debería ocurrir con safeApiCall
            }
        }
    }
    
    /**
     * Cierra la sesión del usuario actual.
     */
    fun logout() {
        sharedPreferences.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_TOKEN_EXPIRY)
            remove(KEY_USER_ID)
            remove(KEY_USERNAME)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_NAME)
            remove(KEY_USER_ROLE)
            remove(KEY_COMPANY_ID)
            remove(KEY_COMPANY_NAME)
        }.apply()
        
        _currentUser.value = null
        _isLoggedIn.value = false
    }
    
    /**
     * Obtiene el token de acceso actual.
     *
     * @return Token de acceso o null si no hay sesión activa.
     */
    fun getAccessToken(): String? {
        if (!::sharedPreferences.isInitialized) {
            return null
        }
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }
    
    /**
     * Verifica si hay un token de refresco disponible.
     *
     * @return true si hay un token de refresco, false en caso contrario.
     */
    fun hasRefreshToken(): Boolean {
        if (!::sharedPreferences.isInitialized) {
            return false
        }
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null) != null
    }
    
    /**
     * Verifica si el usuario actual tiene un permiso específico.
     *
     * @param permission Permiso a verificar.
     * @return true si el usuario tiene el permiso, false en caso contrario.
     */
    fun hasPermission(permission: String): Boolean {
        val user = _currentUser.value ?: return false
        return user.permissions.contains(permission)
    }
    
    /**
     * Obtiene el ID de la empresa del usuario actual.
     *
     * @return ID de la empresa o -1 si no hay sesión activa.
     */
    fun getCurrentCompanyId(): Int {
        val user = _currentUser.value ?: return -1
        return user.companyId
    }
}

/**
 * Resultados posibles de un intento de inicio de sesión.
 */
sealed class LoginResult {
    /**
     * Inicio de sesión exitoso.
     */
    object Success : LoginResult()
    
    /**
     * Error durante el inicio de sesión.
     *
     * @property message Mensaje de error.
     */
    data class Error(val message: String) : LoginResult()
}