package com.productiva.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.productiva.android.model.User
import com.productiva.android.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de selección de usuario
 */
class UserSelectionViewModel(application: Application) : AndroidViewModel(application) {
    
    private val userRepository = UserRepository(application)
    
    // Lista de usuarios
    val users = userRepository.getAllUsers()
    
    // Estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Mensaje de error
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    // Usuario seleccionado
    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser
    
    // Empresa seleccionada (para filtrar usuarios)
    private val _selectedCompanyId = MutableLiveData<Int?>()
    val selectedCompanyId: LiveData<Int?> = _selectedCompanyId
    
    /**
     * Carga los usuarios desde el servidor
     */
    fun loadUsers() {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // Primero intentamos cargar desde la base de datos local
                val userCount = userRepository.getUsers().value?.size ?: 0
                
                if (userCount == 0) {
                    // Si no hay usuarios locales, los cargamos desde el servidor
                    syncUsers()
                } else {
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al cargar usuarios"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Sincroniza los usuarios con el servidor
     */
    fun syncUsers() {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val result = userRepository.syncUsers()
                
                result.fold(
                    onSuccess = { count ->
                        _isLoading.value = false
                        if (count == 0) {
                            _errorMessage.value = "No se encontraron usuarios para sincronizar"
                        }
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.message ?: "Error al sincronizar usuarios"
                        _isLoading.value = false
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al sincronizar usuarios"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Carga los usuarios por compañía
     */
    fun loadUsersByCompany(companyId: Int) {
        _isLoading.value = true
        _selectedCompanyId.value = companyId
        
        viewModelScope.launch {
            try {
                val result = userRepository.fetchUsersByCompany(companyId)
                
                result.fold(
                    onSuccess = {
                        _isLoading.value = false
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.message ?: "Error al cargar usuarios de la empresa"
                        _isLoading.value = false
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al cargar usuarios de la empresa"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Selecciona un usuario
     */
    fun selectUser(user: User) {
        _selectedUser.value = user
        
        // Guardar el ID del último usuario seleccionado
        getApplication<Application>().getSharedPreferences("productiva_prefs", Application.MODE_PRIVATE)
            .edit()
            .putInt("selected_user_id", user.id)
            .apply()
    }
    
    /**
     * Obtiene el ID del último usuario seleccionado
     */
    fun getLastSelectedUserId(): Int {
        return getApplication<Application>().getSharedPreferences("productiva_prefs", Application.MODE_PRIVATE)
            .getInt("selected_user_id", -1)
    }
    
    /**
     * Obtiene un usuario por su ID
     */
    suspend fun getUserById(userId: Int): User? {
        return userRepository.getUserById(userId)
    }
    
    /**
     * Filtra los usuarios por una consulta de búsqueda
     */
    fun filterUsers(query: String): List<User> {
        val allUsers = users.value ?: emptyList()
        
        if (query.isEmpty()) {
            return allUsers
        }
        
        val lowercaseQuery = query.lowercase()
        
        return allUsers.filter { user ->
            user.name.lowercase().contains(lowercaseQuery) ||
                    user.username.lowercase().contains(lowercaseQuery) ||
                    user.email.lowercase().contains(lowercaseQuery) ||
                    user.companyName.lowercase().contains(lowercaseQuery)
        }
    }
}