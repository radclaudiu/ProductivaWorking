package com.productiva.android.session

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Gestor de sesión del usuario.
 * Almacena y recupera información de sesión de forma segura.
 */
class SessionManager(context: Context) {
    
    companion object {
        private const val TAG = "SessionManager"
        private const val PREFERENCE_NAME = "productiva_secure_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_IS_ADMIN = "is_admin"
        private const val KEY_COMPANY_ID = "company_id"
        private const val KEY_COMPANY_NAME = "company_name"
        private const val KEY_EMPLOYEE_ID = "employee_id"
        private const val KEY_LAST_SYNC_PREFIX = "last_sync_"
    }
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            PREFERENCE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Log.e(TAG, "Error al crear EncryptedSharedPreferences", e)
        // Fallback a SharedPreferences normales (no encriptadas)
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Guarda la información de sesión del usuario.
     *
     * @param token Token de autenticación JWT.
     * @param userId ID del usuario.
     * @param username Nombre de usuario.
     * @param email Correo electrónico del usuario.
     * @param isAdmin Indica si el usuario es administrador.
     * @param companyId ID de la empresa.
     * @param companyName Nombre de la empresa.
     * @param employeeId ID del empleado (puede ser nulo).
     */
    fun saveUserInfo(
        token: String,
        userId: Int,
        username: String,
        email: String?,
        isAdmin: Boolean,
        companyId: Int,
        companyName: String,
        employeeId: Int?
    ) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_AUTH_TOKEN, token)
        editor.putInt(KEY_USER_ID, userId)
        editor.putString(KEY_USERNAME, username)
        editor.putString(KEY_EMAIL, email)
        editor.putBoolean(KEY_IS_ADMIN, isAdmin)
        editor.putInt(KEY_COMPANY_ID, companyId)
        editor.putString(KEY_COMPANY_NAME, companyName)
        employeeId?.let { editor.putInt(KEY_EMPLOYEE_ID, it) } ?: editor.remove(KEY_EMPLOYEE_ID)
        editor.apply()
        
        Log.d(TAG, "Información de usuario guardada: $username (ID: $userId)")
    }
    
    /**
     * Obtiene el token de autenticación.
     *
     * @return Token de autenticación o null si no hay ninguno guardado.
     */
    fun getToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }
    
    /**
     * Obtiene el ID del usuario.
     *
     * @return ID del usuario o -1 si no hay ninguno guardado.
     */
    fun getUserId(): Int {
        return sharedPreferences.getInt(KEY_USER_ID, -1)
    }
    
    /**
     * Obtiene el nombre de usuario.
     *
     * @return Nombre de usuario o null si no hay ninguno guardado.
     */
    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }
    
    /**
     * Obtiene el correo electrónico del usuario.
     *
     * @return Correo electrónico o null si no hay ninguno guardado.
     */
    fun getEmail(): String? {
        return sharedPreferences.getString(KEY_EMAIL, null)
    }
    
    /**
     * Verifica si el usuario es administrador.
     *
     * @return true si el usuario es administrador, false en caso contrario.
     */
    fun isAdmin(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_ADMIN, false)
    }
    
    /**
     * Obtiene el ID de la empresa.
     *
     * @return ID de la empresa o -1 si no hay ninguno guardado.
     */
    fun getCompanyId(): Int {
        return sharedPreferences.getInt(KEY_COMPANY_ID, -1)
    }
    
    /**
     * Obtiene el nombre de la empresa.
     *
     * @return Nombre de la empresa o null si no hay ninguno guardado.
     */
    fun getCompanyName(): String? {
        return sharedPreferences.getString(KEY_COMPANY_NAME, null)
    }
    
    /**
     * Obtiene el ID del empleado asociado al usuario.
     *
     * @return ID del empleado o -1 si no hay ninguno guardado.
     */
    fun getEmployeeId(): Int {
        return sharedPreferences.getInt(KEY_EMPLOYEE_ID, -1)
    }
    
    /**
     * Verifica si el usuario tiene una sesión iniciada.
     *
     * @return true si hay una sesión activa, false en caso contrario.
     */
    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
    
    /**
     * Cierra la sesión del usuario.
     * Elimina toda la información de sesión.
     */
    fun logout() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        
        Log.d(TAG, "Sesión cerrada")
    }
    
    /**
     * Guarda la marca de tiempo de la última sincronización.
     *
     * @param entityType Tipo de entidad sincronizada.
     * @param timestamp Marca de tiempo de la sincronización.
     */
    fun saveLastSyncTimestamp(entityType: String, timestamp: Long) {
        val editor = sharedPreferences.edit()
        editor.putLong("$KEY_LAST_SYNC_PREFIX$entityType", timestamp)
        editor.apply()
        
        Log.d(TAG, "Marca de tiempo de sincronización guardada para $entityType: $timestamp")
    }
    
    /**
     * Obtiene la marca de tiempo de la última sincronización.
     *
     * @param entityType Tipo de entidad sincronizada.
     * @return Marca de tiempo de la última sincronización o 0 si no hay ninguna guardada.
     */
    fun getLastSyncTimestamp(entityType: String): Long {
        return sharedPreferences.getLong("$KEY_LAST_SYNC_PREFIX$entityType", 0)
    }
}