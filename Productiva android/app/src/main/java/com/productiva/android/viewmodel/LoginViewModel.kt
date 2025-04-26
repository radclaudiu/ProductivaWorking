package com.productiva.android.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.productiva.android.ProductivaApplication
import com.productiva.android.model.User
import com.productiva.android.network.RetrofitClient
import com.productiva.android.repository.ResourceState
import com.productiva.android.repository.UserRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de inicio de sesión.
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {
    
    private val TAG = "LoginViewModel"
    
    // Repositorios
    private val userRepository: UserRepository
    
    // Estado de la operación de inicio de sesión
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState
    
    // Estado actual del usuario autenticado
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser
    
    // Estado de conexión al servidor
    private val _isOfflineMode = MutableLiveData<Boolean>(false)
    val isOfflineMode: LiveData<Boolean> = _isOfflineMode
    
    /**
     * Estados posibles del proceso de inicio de sesión.
     */
    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val user: User) : LoginState()
        data class Error(val message: String) : LoginState()
        data class OfflineSuccess(val user: User, val lastSyncTime: String) : LoginState()
    }
    
    init {
        // Obtener instancias de la aplicación
        val app = getApplication<ProductivaApplication>()
        val apiService = RetrofitClient.getApiService(app)
        val database = app.database
        
        // Inicializar repositorios
        userRepository = UserRepository(database.userDao(), apiService)
        
        // Iniciar en estado Idle
        _loginState.value = LoginState.Idle
        
        // Verificar si hay un usuario guardado
        viewModelScope.launch {
            try {
                // En el futuro, podríamos usar un token almacenado para auto-login
                _currentUser.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error al verificar usuario guardado", e)
            }
        }
    }
    
    /**
     * Intenta iniciar sesión con las credenciales proporcionadas.
     */
    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Usuario y contraseña son obligatorios")
            return
        }
        
        _loginState.value = LoginState.Loading
        
        viewModelScope.launch {
            try {
                userRepository.login(username, password).collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            val user = state.data!!
                            _currentUser.value = user
                            
                            if (state.isFromCache) {
                                _isOfflineMode.value = true
                                _loginState.value = LoginState.OfflineSuccess(
                                    user, 
                                    "Modo sin conexión - Datos no sincronizados"
                                )
                            } else {
                                _isOfflineMode.value = false
                                _loginState.value = LoginState.Success(user)
                            }
                        }
                        is ResourceState.Error -> {
                            _loginState.value = LoginState.Error(
                                state.message ?: "Error desconocido al iniciar sesión"
                            )
                        }
                        is ResourceState.Loading -> {
                            _loginState.value = LoginState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en proceso de login", e)
                _loginState.value = LoginState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Cierra la sesión del usuario actual.
     */
    fun logout() {
        viewModelScope.launch {
            // Limpiar el token en RetrofitClient
            RetrofitClient.setAuthToken(null)
            
            // Limpiar usuario actual
            _currentUser.value = null
            _loginState.value = LoginState.Idle
            
            // Futura implementación: llamar a logout en el servidor
        }
    }
    
    /**
     * Verifica si hay un usuario con sesión activa.
     */
    fun isLoggedIn(): Boolean {
        return _currentUser.value != null
    }
}