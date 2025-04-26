package com.productiva.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.productiva.android.api.ApiClient
import com.productiva.android.model.User
import com.productiva.android.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de inicio de sesión
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {
    
    private val userRepository = UserRepository(application)
    private val apiClient = ApiClient.getInstance(application)
    
    // Estado de la autenticación
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState
    
    // Estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Mensaje de error
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    /**
     * Realiza el proceso de inicio de sesión
     */
    fun login(username: String, password: String) {
        if (username.isEmpty() || password.isEmpty()) {
            _errorMessage.value = "El nombre de usuario y la contraseña son obligatorios"
            return
        }
        
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val result = userRepository.login(username, password)
                
                result.fold(
                    onSuccess = { user ->
                        _loginState.value = LoginState.Success(user)
                        _isLoading.value = false
                        
                        // Guardar información relevante para sesiones futuras si es necesario
                        saveLoginInfo(username)
                    },
                    onFailure = { error ->
                        _loginState.value = LoginState.Error(error.message ?: "Error de autenticación")
                        _errorMessage.value = error.message ?: "Error de autenticación"
                        _isLoading.value = false
                    }
                )
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Error de conexión")
                _errorMessage.value = e.message ?: "Error de conexión"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Verifica si hay una sesión activa
     */
    fun checkActiveSession() {
        _isLoading.value = true
        
        viewModelScope.launch {
            if (apiClient.hasAuthToken()) {
                // Si hay un token guardado, intentamos obtener el usuario actual
                try {
                    val response = apiClient.apiService.getCurrentUser()
                    
                    if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                        _loginState.value = LoginState.Success(response.body()?.data!!)
                    } else {
                        // Token inválido o expirado
                        apiClient.clearAuthToken()
                        _loginState.value = LoginState.Initial
                    }
                } catch (e: Exception) {
                    // Error de conexión, mantenemos el token por si es un problema temporal
                    _loginState.value = LoginState.Error(e.message ?: "Error de conexión")
                    _errorMessage.value = e.message ?: "Error de conexión"
                }
            } else {
                _loginState.value = LoginState.Initial
            }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Cierra la sesión actual
     */
    fun logout() {
        _isLoading.value = true
        
        viewModelScope.launch {
            userRepository.logout()
            _loginState.value = LoginState.Initial
            _isLoading.value = false
        }
    }
    
    /**
     * Guarda información de inicio de sesión para futuras sesiones
     */
    private fun saveLoginInfo(username: String) {
        // Guardar el nombre de usuario para autocompletar en futuros inicios de sesión
        getApplication<Application>().getSharedPreferences("productiva_prefs", Application.MODE_PRIVATE)
            .edit()
            .putString("last_username", username)
            .apply()
    }
    
    /**
     * Obtiene el último nombre de usuario utilizado
     */
    fun getLastUsername(): String {
        return getApplication<Application>().getSharedPreferences("productiva_prefs", Application.MODE_PRIVATE)
            .getString("last_username", "") ?: ""
    }
    
    /**
     * Obtiene la URL del servidor guardada
     */
    fun getServerUrl(): String {
        return getApplication<Application>().getSharedPreferences("productiva_prefs", Application.MODE_PRIVATE)
            .getString("server_url", "https://productiva.example.com/api/") ?: "https://productiva.example.com/api/"
    }
    
    /**
     * Actualiza la URL del servidor
     */
    fun updateServerUrl(url: String) {
        if (url.isNotEmpty()) {
            // Asegurarse de que termina con una barra
            val formattedUrl = if (url.endsWith("/")) url else "$url/"
            
            apiClient.updateServerUrl(formattedUrl)
            getApplication<Application>().getSharedPreferences("productiva_prefs", Application.MODE_PRIVATE)
                .edit()
                .putString("server_url", formattedUrl)
                .apply()
        }
    }
    
    /**
     * Estados posibles de la autenticación
     */
    sealed class LoginState {
        object Initial : LoginState()
        data class Success(val user: User) : LoginState()
        data class Error(val message: String) : LoginState()
    }
}