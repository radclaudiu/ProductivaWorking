package com.productiva.android.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor de preferencias para la aplicación
 * Almacena datos sensibles cifrados (tokens, credenciales) y no sensibles
 */
class PreferenceManager(private val context: Context) {
    
    // Preferencias no cifradas (datos no sensibles)
    private val standardPrefs: SharedPreferences = context.getSharedPreferences(
        STANDARD_PREFS_NAME, Context.MODE_PRIVATE
    )
    
    // Preferencias cifradas (datos sensibles)
    private val securePrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        EncryptedSharedPreferences.create(
            context,
            SECURE_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    // Flujos observables para cambios en configuraciones importantes
    private val _authTokenFlow = MutableStateFlow<String?>(getAuthToken())
    val authTokenFlow: Flow<String?> = _authTokenFlow.asStateFlow()
    
    private val _serverUrlFlow = MutableStateFlow<String?>(getServerUrl())
    val serverUrlFlow: Flow<String?> = _serverUrlFlow.asStateFlow()
    
    // Métodos para gestionar el token de autenticación
    fun saveAuthToken(token: String) {
        securePrefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
        _authTokenFlow.value = token
    }
    
    fun getAuthToken(): String? {
        return securePrefs.getString(KEY_AUTH_TOKEN, null)
    }
    
    fun clearAuthToken() {
        securePrefs.edit().remove(KEY_AUTH_TOKEN).apply()
        _authTokenFlow.value = null
    }
    
    // Métodos para gestionar credenciales de usuario
    fun saveCredentials(username: String, password: String) {
        securePrefs.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_PASSWORD, password)
            .apply()
    }
    
    fun getUsername(): String? {
        return securePrefs.getString(KEY_USERNAME, null)
    }
    
    fun getPassword(): String? {
        return securePrefs.getString(KEY_PASSWORD, null)
    }
    
    fun clearCredentials() {
        securePrefs.edit()
            .remove(KEY_USERNAME)
            .remove(KEY_PASSWORD)
            .apply()
    }
    
    // Métodos para gestionar el ID de usuario actual
    fun saveCurrentUserId(userId: Int) {
        standardPrefs.edit().putInt(KEY_CURRENT_USER_ID, userId).apply()
    }
    
    fun getCurrentUserId(): Int {
        return standardPrefs.getInt(KEY_CURRENT_USER_ID, -1)
    }
    
    fun clearCurrentUserId() {
        standardPrefs.edit().remove(KEY_CURRENT_USER_ID).apply()
    }
    
    // Métodos para gestionar la URL del servidor
    fun saveServerUrl(url: String) {
        standardPrefs.edit().putString(KEY_SERVER_URL, url).apply()
        _serverUrlFlow.value = url
    }
    
    fun getServerUrl(): String? {
        return standardPrefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL)
    }
    
    // Métodos para gestionar preferencias de impresora
    fun saveDefaultPrinterId(printerId: Int) {
        standardPrefs.edit().putInt(KEY_DEFAULT_PRINTER_ID, printerId).apply()
    }
    
    fun getDefaultPrinterId(): Int {
        return standardPrefs.getInt(KEY_DEFAULT_PRINTER_ID, -1)
    }
    
    // Limpieza completa de datos (logout)
    fun clearAllData() {
        clearAuthToken()
        clearCredentials()
        clearCurrentUserId()
        // No limpiar URL del servidor ni impresora predeterminada
    }
    
    companion object {
        private const val STANDARD_PREFS_NAME = "productiva_prefs"
        private const val SECURE_PREFS_NAME = "productiva_secure_prefs"
        
        // Claves para preferencias seguras
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        
        // Claves para preferencias estándar
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_DEFAULT_PRINTER_ID = "default_printer_id"
        
        // Valores predeterminados
        private const val DEFAULT_SERVER_URL = "http://192.168.1.1:5000"
    }
}