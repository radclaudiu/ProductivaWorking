package com.productiva.android.utils

import android.content.Context
import android.content.SharedPreferences
import com.productiva.android.model.User

/**
 * Gestor de sesión para la aplicación
 * Maneja la autenticación, almacenamiento de tokens y datos de sesión
 */
class SessionManager(context: Context) {
    
    companion object {
        private const val PREF_NAME = "ProductivaSession"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_SELECTED_USER_ID = "selected_user_id"
        private const val KEY_LOCATION_ID = "location_id"
        private const val KEY_SERVER_URL = "server_url"
        private const val DEFAULT_SERVER_URL = "https://productiva.replit.app/"
    }
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    /**
     * Guarda el token de autenticación
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
     * Guarda el ID del usuario autenticado (account)
     */
    fun saveUserId(userId: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(KEY_USER_ID, userId)
        editor.apply()
    }
    
    /**
     * Obtiene el ID del usuario autenticado (account)
     */
    fun getUserId(): Int {
        return sharedPreferences.getInt(KEY_USER_ID, -1)
    }
    
    /**
     * Guarda el ID del usuario seleccionado (profile)
     */
    fun saveSelectedUserId(userId: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(KEY_SELECTED_USER_ID, userId)
        editor.apply()
    }
    
    /**
     * Obtiene el ID del usuario seleccionado (profile)
     */
    fun getSelectedUserId(): Int {
        return sharedPreferences.getInt(KEY_SELECTED_USER_ID, -1)
    }
    
    /**
     * Guarda el ID de la ubicación
     */
    fun saveLocationId(locationId: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(KEY_LOCATION_ID, locationId)
        editor.apply()
    }
    
    /**
     * Obtiene el ID de la ubicación
     */
    fun getLocationId(): Int {
        return sharedPreferences.getInt(KEY_LOCATION_ID, -1)
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
    fun getServerUrl(): String {
        return sharedPreferences.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
    }
    
    /**
     * Limpia los datos de sesión (logout)
     */
    fun clearSession() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_AUTH_TOKEN)
        editor.remove(KEY_USER_ID)
        editor.remove(KEY_SELECTED_USER_ID)
        editor.remove(KEY_LOCATION_ID)
        // No eliminamos KEY_SERVER_URL para mantener la última URL utilizada
        editor.apply()
    }
    
    /**
     * Verifica si el usuario está autenticado
     */
    fun isLoggedIn(): Boolean {
        return !getAuthToken().isNullOrEmpty() && getUserId() != -1
    }
    
    /**
     * Verifica si se ha seleccionado un usuario
     */
    fun isUserSelected(): Boolean {
        return getSelectedUserId() != -1
    }
}