package com.productiva.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.productiva.android.model.User
import com.productiva.android.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de selección de usuario
 */
class UserSelectionViewModel(application: Application) : AndroidViewModel(application) {
    
    private val userRepository = UserRepository(application)
    
    // Lista de usuarios
    val allUsers = userRepository.getAllUsers()
    
    // Filtros
    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> get() = _searchQuery
    
    private val _selectedCompanyId = MutableLiveData<Int>()
    val selectedCompanyId: LiveData<Int> get() = _selectedCompanyId
    
    // Usuarios filtrados
    val filteredUsers = Transformations.switchMap(searchQuery) { query ->
        if (query.isNullOrBlank()) {
            if (_selectedCompanyId.value != null && _selectedCompanyId.value != 0) {
                userRepository.getUsersByCompany(_selectedCompanyId.value!!)
            } else {
                allUsers
            }
        } else {
            userRepository.searchUsers(query)
        }
    }
    
    // Estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading
    
    // Mensaje de error
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
    
    // Usuario seleccionado
    private val _selectedUser = MutableLiveData<User>()
    val selectedUser: LiveData<User> get() = _selectedUser
    
    init {
        refreshUsers()
    }
    
    /**
     * Actualiza la lista de usuarios desde el servidor
     */
    fun refreshUsers() {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val result = userRepository.syncUsers()
                if (result.isFailure) {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Error al sincronizar usuarios"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Actualiza la consulta de búsqueda
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * Selecciona una empresa para filtrar
     */
    fun setSelectedCompany(companyId: Int) {
        _selectedCompanyId.value = companyId
    }
    
    /**
     * Selecciona un usuario
     */
    fun selectUser(user: User) {
        _selectedUser.value = user
    }
    
    /**
     * Obtiene un usuario por ID
     */
    fun getUserById(userId: Int) {
        viewModelScope.launch {
            try {
                val user = userRepository.getUserById(userId)
                if (user != null) {
                    _selectedUser.value = user
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al obtener usuario"
            }
        }
    }
    
    /**
     * Limpia el usuario seleccionado
     */
    fun clearSelectedUser() {
        _selectedUser.value = null
    }
}