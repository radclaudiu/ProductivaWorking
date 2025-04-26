package com.productiva.android.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Administrador de sesión para la aplicación.
 * Gestiona la autenticación y almacenamiento seguro de credenciales.
 */
class SessionManager(context: Context) {
    
    private val TAG = "SessionManager"
    
    // Preferencias encriptadas para datos sensibles
    private val encryptedPreferences: SharedPreferences
    
    // Preferencias normales para datos no sensibles
    private val preferences: SharedPreferences
    
    init {
        // Configurar preferencias encriptadas
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        encryptedPreferences = EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREF_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        
        // Configurar preferencias normales
        preferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        
        Log.d(TAG, "SessionManager inicializado")
    }
    
    /**
     * Guarda los datos de la sesión después de un login exitoso
     */
    fun saveLoginSession(userId: Int, username: String, token: String) {
        encryptedPreferences.edit().apply {
            putString(KEY_AUTH_TOKEN, token)
            putInt(KEY_USER_ID, userId)
            apply()
        }
        
        preferences.edit().apply {
            putString(KEY_USERNAME, username)
            putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
        
        Log.d(TAG, "Sesión guardada para el usuario: $username")
    }
    
    /**
     * Cierra la sesión y elimina los datos de autenticación
     */
    fun logout() {
        encryptedPreferences.edit().apply {
            remove(KEY_AUTH_TOKEN)
            remove(KEY_USER_ID)
            apply()
        }
        
        preferences.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
        
        Log.d(TAG, "Sesión cerrada")
    }
    
    /**
     * Verifica si hay una sesión activa
     */
    fun isLoggedIn(): Boolean {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    /**
     * Obtiene el token de autenticación
     */
    fun getAuthToken(): String? {
        return encryptedPreferences.getString(KEY_AUTH_TOKEN, null)
    }
    
    /**
     * Obtiene el ID del usuario actual
     */
    fun getUserId(): Int? {
        val userId = encryptedPreferences.getInt(KEY_USER_ID, -1)
        return if (userId != -1) userId else null
    }
    
    /**
     * Obtiene el nombre de usuario
     */
    fun getUsername(): String? {
        return preferences.getString(KEY_USERNAME, null)
    }
    
    /**
     * Guarda el timestamp de la última sincronización exitosa
     */
    fun saveLastSyncTimestamp(timestamp: Long) {
        preferences.edit().apply {
            putLong(KEY_LAST_SYNC, timestamp)
            apply()
        }
    }
    
    /**
     * Obtiene el timestamp de la última sincronización
     */
    fun getLastSyncTimestamp(): Long {
        return preferences.getLong(KEY_LAST_SYNC, 0)
    }
    
    companion object {
        private const val PREF_FILE = "productiva_preferences"
        private const val ENCRYPTED_PREF_FILE = "productiva_secure_preferences"
        
        // Claves para datos de sesión
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_LOGIN_TIME = "login_time"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        
        // Claves para sincronización
        private const val KEY_LAST_SYNC = "last_sync_timestamp"
    }
}