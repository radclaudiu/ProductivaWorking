package com.productiva.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.productiva.android.model.User
import com.productiva.android.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de login
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {
    
    private val userRepository = UserRepository(application)
    
    // Estado de autenticación
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> get() = _authState
    
    // Mensaje de error
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
    
    // URL del servidor
    private val _serverUrl = MutableLiveData<String>()
    val serverUrl: LiveData<String> get() = _serverUrl
    
    /**
     * Intenta iniciar sesión con las credenciales proporcionadas
     */
    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _errorMessage.value = "Usuario y contraseña son requeridos"
            return
        }
        
        _authState.value = AuthState.LOADING
        
        viewModelScope.launch {
            try {
                val result = userRepository.login(username, password)
                
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    if (user != null) {
                        _authState.value = AuthState.AUTHENTICATED
                    } else {
                        _authState.value = AuthState.UNAUTHENTICATED
                        _errorMessage.value = "Error de inicio de sesión"
                    }
                } else {
                    _authState.value = AuthState.UNAUTHENTICATED
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Error de conexión"
                }
            } catch (e: Exception) {
                _authState.value = AuthState.UNAUTHENTICATED
                _errorMessage.value = e.message ?: "Error desconocido"
            }
        }
    }
    
    /**
     * Verifica si hay un token guardado y valida la sesión
     */
    fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                val result = userRepository.getCurrentUser()
                
                if (result.isSuccess) {
                    _authState.value = AuthState.AUTHENTICATED
                } else {
                    _authState.value = AuthState.UNAUTHENTICATED
                }
            } catch (e: Exception) {
                _authState.value = AuthState.UNAUTHENTICATED
            }
        }
    }
    
    /**
     * Actualiza la URL del servidor
     */
    fun updateServerUrl(url: String) {
        _serverUrl.value = url
    }
    
    /**
     * Cierra la sesión del usuario actual
     */
    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
            _authState.value = AuthState.UNAUTHENTICATED
        }
    }
    
    /**
     * Estados de autenticación
     */
    enum class AuthState {
        LOADING,
        AUTHENTICATED,
        UNAUTHENTICATED
    }
}