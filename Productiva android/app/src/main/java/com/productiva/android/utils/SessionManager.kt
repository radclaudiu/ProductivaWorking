package com.productiva.android.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Gestiona la sesión del usuario y almacena información segura
 */
class SessionManager(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
        
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "productiva_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_LOCATION_ID = "location_id"
        private const val KEY_COMPANY_ID = "company_id"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_SELECTED_USER_ID = "selected_user_id"
    }
    
    /**
     * Guarda la información de autenticación
     */
    fun saveAuthToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_AUTH_TOKEN, token)
        editor.apply()
    }
    
    /**
     * Obtiene el token de autenticación
     */
    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }
    
    /**
     * Guarda información del usuario autenticado
     */
    fun saveUserInfo(id: Int, name: String, email: String, role: String) {
        val editor = sharedPreferences.edit()
        editor.putInt(KEY_USER_ID, id)
        editor.putString(KEY_USER_NAME, name)
        editor.putString(KEY_USER_EMAIL, email)
        editor.putString(KEY_USER_ROLE, role)
        editor.apply()
    }
    
    /**
     * Guarda el ID del usuario seleccionado para el portal de tareas
     */
    fun saveSelectedUserId(userId: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(KEY_SELECTED_USER_ID, userId)
        editor.apply()
    }
    
    /**
     * Obtiene el ID del usuario seleccionado para el portal de tareas
     */
    fun getSelectedUserId(): Int {
        return sharedPreferences.getInt(KEY_SELECTED_USER_ID, -1)
    }
    
    /**
     * Guarda la información de ubicación y empresa
     */
    fun saveLocationInfo(locationId: Int, companyId: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(KEY_LOCATION_ID, locationId)
        editor.putInt(KEY_COMPANY_ID, companyId)
        editor.apply()
    }
    
    /**
     * Obtiene el ID de ubicación
     */
    fun getLocationId(): Int {
        return sharedPreferences.getInt(KEY_LOCATION_ID, -1)
    }
    
    /**
     * Obtiene el ID de la empresa
     */
    fun getCompanyId(): Int {
        return sharedPreferences.getInt(KEY_COMPANY_ID, -1)
    }
    
    /**
     * Guarda la URL del servidor
     */
    fun saveServerUrl(url: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_SERVER_URL, url)
        editor.apply()
    }
    
    /**
     * Obtiene la URL del servidor
     */
    fun getServerUrl(): String? {
        return sharedPreferences.getString(KEY_SERVER_URL, null)
    }
    
    /**
     * Borra todos los datos de sesión
     */
    fun clearSession() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
    
    /**
     * Verifica si el usuario está autenticado
     */
    fun isLoggedIn(): Boolean {
        return !getAuthToken().isNullOrEmpty()
    }
}