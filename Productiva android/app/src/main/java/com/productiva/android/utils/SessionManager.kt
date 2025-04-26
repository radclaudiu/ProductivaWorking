package com.productiva.android.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestor de sesión para almacenar datos de autenticación y configuración
 */
class SessionManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()
    
    /**
     * Guarda el token de autenticación
     */
    fun saveAuthToken(token: String) {
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
     * Guarda el ID del usuario autenticado
     */
    fun saveUserId(userId: Int) {
        editor.putInt(KEY_USER_ID, userId)
        editor.apply()
    }
    
    /**
     * Obtiene el ID del usuario autenticado
     */
    fun getUserId(): Int {
        return sharedPreferences.getInt(KEY_USER_ID, -1)
    }
    
    /**
     * Guarda el ID del usuario seleccionado (perfil)
     */
    fun saveSelectedUserId(userId: Int) {
        editor.putInt(KEY_SELECTED_USER_ID, userId)
        editor.apply()
    }
    
    /**
     * Obtiene el ID del usuario seleccionado (perfil)
     */
    fun getSelectedUserId(): Int {
        return sharedPreferences.getInt(KEY_SELECTED_USER_ID, -1)
    }
    
    /**
     * Guarda el ID de la ubicación seleccionada
     */
    fun saveLocationId(locationId: Int) {
        editor.putInt(KEY_LOCATION_ID, locationId)
        editor.apply()
    }
    
    /**
     * Obtiene el ID de la ubicación seleccionada
     */
    fun getLocationId(): Int {
        return sharedPreferences.getInt(KEY_LOCATION_ID, -1)
    }
    
    /**
     * Guarda la URL del servidor
     */
    fun saveServerUrl(url: String) {
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
     * Verifica si el usuario está logueado
     */
    fun isLoggedIn(): Boolean {
        return getAuthToken() != null && getUserId() != -1
    }
    
    /**
     * Verifica si hay un usuario seleccionado
     */
    fun isUserSelected(): Boolean {
        return getSelectedUserId() != -1
    }
    
    /**
     * Limpia los datos de sesión
     */
    fun clearSession() {
        editor.clear()
        editor.putString(KEY_SERVER_URL, getServerUrl()) // Mantener la URL del servidor
        editor.apply()
    }
    
    /**
     * Guarda la dirección Bluetooth de la última impresora utilizada
     */
    fun saveLastPrinterAddress(address: String) {
        editor.putString(KEY_LAST_PRINTER, address)
        editor.apply()
    }
    
    /**
     * Obtiene la dirección Bluetooth de la última impresora utilizada
     */
    fun getLastPrinterAddress(): String? {
        return sharedPreferences.getString(KEY_LAST_PRINTER, null)
    }
    
    /**
     * Guarda el ID de la última plantilla de etiqueta utilizada
     */
    fun saveLastTemplateId(templateId: Int) {
        editor.putInt(KEY_LAST_TEMPLATE, templateId)
        editor.apply()
    }
    
    /**
     * Obtiene el ID de la última plantilla de etiqueta utilizada
     */
    fun getLastTemplateId(): Int {
        return sharedPreferences.getInt(KEY_LAST_TEMPLATE, -1)
    }
    
    companion object {
        private const val PREF_NAME = "ProductivaPrefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_SELECTED_USER_ID = "selected_user_id"
        private const val KEY_LOCATION_ID = "location_id"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_LAST_PRINTER = "last_printer"
        private const val KEY_LAST_TEMPLATE = "last_template"
        
        private const val DEFAULT_SERVER_URL = "https://productiva.repl.co/"
    }
}