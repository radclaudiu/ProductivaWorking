package com.productiva.android.session

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.productiva.android.model.User
import java.util.Date

/**
 * Gestor de sesión para manejar la autenticación y datos del usuario actual.
 * Utiliza EncryptedSharedPreferences para almacenar de forma segura la información de sesión.
 */
class SessionManager private constructor() {
    
    private val TAG = "SessionManager"
    
    // Claves para SharedPreferences
    companion object {
        private const val PREF_NAME = "productiva_secure_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER = "current_user"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
        private const val KEY_LAST_SYNC = "last_sync"
        private const val KEY_SERVER_URL = "server_url"
        
        // Instancia singleton
        @Volatile
        private var instance: SessionManager? = null
        
        // Preferencias cifradas
        private var prefs: SharedPreferences? = null
        
        /**
         * Obtiene la instancia singleton del SessionManager.
         */
        fun getInstance(): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager().also { instance = it }
            }
        }
        
        /**
         * Inicializa el SessionManager con el contexto de la aplicación.
         * Debe llamarse una vez al inicio de la aplicación.
         */
        fun init(context: Context) {
            if (prefs == null) {
                try {
                    // Crear clave maestra para cifrado
                    val masterKey = MasterKey.Builder(context)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build()
                    
                    // Crear preferencias cifradas
                    prefs = EncryptedSharedPreferences.create(
                        context,
                        PREF_NAME,
                        masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    )
                    
                    Log.d("SessionManager", "SharedPreferences cifradas inicializadas")
                } catch (e: Exception) {
                    Log.e("SessionManager", "Error al inicializar preferencias cifradas", e)
                    
                    // Fallback a preferencias normales si falla el cifrado
                    prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    Log.d("SessionManager", "Usando preferencias normales como fallback")
                }
            }
        }
    }
    
    /**
     * Guarda el token de autenticación y su tiempo de expiración.
     */
    fun saveAuthToken(token: String, expiresAt: Long) {
        val editor = prefs?.edit() ?: return
        editor.putString(KEY_AUTH_TOKEN, token)
        editor.putLong(KEY_TOKEN_EXPIRY, expiresAt)
        editor.apply()
        
        Log.d(TAG, "Token de autenticación guardado")
    }
    
    /**
     * Obtiene el token de autenticación.
     */
    fun getAuthToken(): String? {
        val token = prefs?.getString(KEY_AUTH_TOKEN, null)
        val expiryTime = prefs?.getLong(KEY_TOKEN_EXPIRY, 0) ?: 0
        
        // Verificar si el token ha expirado
        if (expiryTime > 0 && expiryTime < System.currentTimeMillis()) {
            Log.d(TAG, "Token de autenticación expirado")
            clearSession() // Limpiar sesión si el token ha expirado
            return null
        }
        
        return token
    }
    
    /**
     * Verifica si el usuario está autenticado.
     */
    fun isLoggedIn(): Boolean {
        return getAuthToken() != null && getCurrentUser() != null
    }
    
    /**
     * Guarda la información del usuario actual.
     */
    fun saveUser(user: User) {
        val userJson = Gson().toJson(user)
        prefs?.edit()?.putString(KEY_USER, userJson)?.apply()
        
        Log.d(TAG, "Información de usuario guardada: ${user.id}")
    }
    
    /**
     * Obtiene la información del usuario actual.
     */
    fun getCurrentUser(): User? {
        val userJson = prefs?.getString(KEY_USER, null) ?: return null
        
        return try {
            Gson().fromJson(userJson, User::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error al deserializar usuario", e)
            null
        }
    }
    
    /**
     * Guarda la URL del servidor.
     */
    fun saveServerUrl(url: String) {
        prefs?.edit()?.putString(KEY_SERVER_URL, url)?.apply()
        Log.d(TAG, "URL del servidor guardada: $url")
    }
    
    /**
     * Obtiene la URL del servidor.
     */
    fun getServerUrl(): String? {
        return prefs?.getString(KEY_SERVER_URL, null)
    }
    
    /**
     * Guarda el timestamp de la última sincronización.
     */
    fun saveLastSyncTimestamp(timestamp: Long) {
        prefs?.edit()?.putLong(KEY_LAST_SYNC, timestamp)?.apply()
        Log.d(TAG, "Timestamp de última sincronización guardado: ${Date(timestamp)}")
    }
    
    /**
     * Obtiene el timestamp de la última sincronización.
     */
    fun getLastSyncTimestamp(): Long {
        return prefs?.getLong(KEY_LAST_SYNC, 0) ?: 0
    }
    
    /**
     * Limpia la información de sesión (logout).
     */
    fun clearSession() {
        prefs?.edit()?.apply {
            remove(KEY_AUTH_TOKEN)
            remove(KEY_USER)
            remove(KEY_TOKEN_EXPIRY)
            apply()
        }
        
        Log.d(TAG, "Sesión cerrada")
    }
}