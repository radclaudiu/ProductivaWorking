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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar la autenticación y datos del usuario.
 */
class UserViewModel(application: Application) : AndroidViewModel(application) {
    
    private val TAG = "UserViewModel"
    
    // Repositorio de usuarios
    private val userRepository: UserRepository
    
    // Estado de autenticación
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState
    
    // Información del usuario actual
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser
    
    init {
        val app = getApplication<ProductivaApplication>()
        val apiService = RetrofitClient.getApiService(app)
        
        // Inicializar el repositorio
        userRepository = UserRepository(apiService)
        
        // Cargar usuario actual si hay sesión activa
        loadCurrentUser()
    }
    
    /**
     * Carga el usuario actual desde la sesión.
     */
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                userRepository.getCurrentUser().collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            _currentUser.value = state.data
                            _authState.value = AuthState.Authenticated(state.data)
                        }
                        is ResourceState.Error -> {
                            _currentUser.value = null
                            _authState.value = AuthState.Unauthenticated
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar usuario actual", e)
                _currentUser.value = null
                _authState.value = AuthState.Unauthenticated
            }
        }
    }
    
    /**
     * Inicia sesión con las credenciales proporcionadas.
     */
    fun login(username: String, password: String) {
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            try {
                userRepository.login(username, password).collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            _currentUser.value = state.data
                            _authState.value = AuthState.Authenticated(state.data)
                        }
                        is ResourceState.Error -> {
                            _authState.value = AuthState.Error(state.message ?: "Error desconocido")
                        }
                        is ResourceState.Loading -> {
                            _authState.value = AuthState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al iniciar sesión", e)
                _authState.value = AuthState.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    /**
     * Cierra la sesión actual.
     */
    fun logout() {
        viewModelScope.launch {
            try {
                userRepository.logout()
                _currentUser.value = null
                _authState.value = AuthState.Unauthenticated
            } catch (e: Exception) {
                Log.e(TAG, "Error al cerrar sesión", e)
                _authState.value = AuthState.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    /**
     * Actualiza la información del usuario actual desde el servidor.
     */
    fun refreshUserInfo() {
        viewModelScope.launch {
            try {
                userRepository.refreshUserInfo().collect { state ->
                    when (state) {
                        is ResourceState.Success -> {
                            _currentUser.value = state.data
                            _authState.value = AuthState.Authenticated(state.data)
                        }
                        is ResourceState.Error -> {
                            if (state.message?.contains("Sesión expirada") == true) {
                                _currentUser.value = null
                                _authState.value = AuthState.Unauthenticated
                            } else {
                                _authState.value = AuthState.Error(state.message ?: "Error desconocido")
                            }
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al actualizar información de usuario", e)
                _authState.value = AuthState.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    /**
     * Verifica si el usuario actual tiene un permiso específico.
     */
    fun hasPermission(permission: String): Boolean {
        return _currentUser.value?.hasPermission(permission) ?: false
    }
    
    /**
     * Verifica si el usuario actual está asignado a una ubicación específica.
     */
    fun isAssignedToLocation(locationId: Int): Boolean {
        return _currentUser.value?.isAssignedToLocation(locationId) ?: false
    }
}

/**
 * Estados posibles de autenticación.
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: User?) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}