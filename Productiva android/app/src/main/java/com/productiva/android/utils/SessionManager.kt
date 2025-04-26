package com.productiva.android.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.IOException
import java.security.GeneralSecurityException

/**
 * Gestor de sesión para manejar la autenticación y tokens de usuario.
 */
class SessionManager(private val context: Context) {
    
    private val TAG = "SessionManager"
    
    // Constantes para las claves de preferencias
    companion object {
        private const val PREF_NAME = "productiva_session"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_SELECTED_LOCATION_ID = "selected_location_id"
        private const val KEY_SELECTED_COMPANY_ID = "selected_company_id"
    }
    
    // Preferencias cifradas para almacenamiento seguro
    private val preferences: SharedPreferences by lazy {
        try {
            // Crear clave maestra para cifrado
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            // Crear preferencias cifradas
            EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: GeneralSecurityException) {
            Log.e(TAG, "Error al crear preferencias cifradas", e)
            // Fallback a preferencias normales en caso de error
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        } catch (e: IOException) {
            Log.e(TAG, "Error de IO al crear preferencias cifradas", e)
            // Fallback a preferencias normales en caso de error
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }
    
    /**
     * Guarda la información de sesión del usuario.
     */
    fun saveUserSession(
        authToken: String,
        userId: Int,
        username: String,
        expiresAt: Long,
        refreshToken: String? = null
    ) {
        preferences.edit().apply {
            putString(KEY_AUTH_TOKEN, authToken)
            putInt(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putLong(KEY_EXPIRES_AT, expiresAt)
            refreshToken?.let { putString(KEY_REFRESH_TOKEN, it) }
        }.apply()
        
        Log.d(TAG, "Sesión de usuario guardada: $username (ID: $userId)")
    }
    
    /**
     * Guarda solo el token de autenticación.
     */
    fun saveAuthToken(token: String) {
        preferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }
    
    /**
     * Obtiene el token de autenticación actual.
     */
    fun getAuthToken(): String? {
        return preferences.getString(KEY_AUTH_TOKEN, null)
    }
    
    /**
     * Obtiene el ID del usuario actual.
     */
    fun getUserId(): Int {
        return preferences.getInt(KEY_USER_ID, -1)
    }
    
    /**
     * Obtiene el nombre de usuario actual.
     */
    fun getUsername(): String? {
        return preferences.getString(KEY_USERNAME, null)
    }
    
    /**
     * Obtiene la fecha de expiración del token.
     */
    fun getExpiresAt(): Long {
        return preferences.getLong(KEY_EXPIRES_AT, 0)
    }
    
    /**
     * Obtiene el token de actualización, si existe.
     */
    fun getRefreshToken(): String? {
        return preferences.getString(KEY_REFRESH_TOKEN, null)
    }
    
    /**
     * Comprueba si el usuario está autenticado.
     */
    fun isLoggedIn(): Boolean {
        val token = getAuthToken()
        val expiresAt = getExpiresAt()
        val currentTime = System.currentTimeMillis()
        
        // Verificar si hay token y no ha expirado
        return !token.isNullOrEmpty() && (expiresAt == 0L || expiresAt > currentTime)
    }
    
    /**
     * Cierra la sesión del usuario.
     */
    fun logout() {
        preferences.edit().clear().apply()
        Log.d(TAG, "Sesión de usuario cerrada")
    }
    
    /**
     * Guarda la ubicación seleccionada por el usuario.
     */
    fun saveSelectedLocationId(locationId: Int) {
        preferences.edit().putInt(KEY_SELECTED_LOCATION_ID, locationId).apply()
    }
    
    /**
     * Obtiene la ubicación seleccionada por el usuario.
     */
    fun getSelectedLocationId(): Int {
        return preferences.getInt(KEY_SELECTED_LOCATION_ID, -1)
    }
    
    /**
     * Guarda la empresa seleccionada por el usuario.
     */
    fun saveSelectedCompanyId(companyId: Int) {
        preferences.edit().putInt(KEY_SELECTED_COMPANY_ID, companyId).apply()
    }
    
    /**
     * Obtiene la empresa seleccionada por el usuario.
     */
    fun getSelectedCompanyId(): Int {
        return preferences.getInt(KEY_SELECTED_COMPANY_ID, -1)
    }
    
    /**
     * Verifica si el token actual ha expirado.
     */
    fun isTokenExpired(): Boolean {
        val expiresAt = getExpiresAt()
        return expiresAt > 0 && System.currentTimeMillis() >= expiresAt
    }
}