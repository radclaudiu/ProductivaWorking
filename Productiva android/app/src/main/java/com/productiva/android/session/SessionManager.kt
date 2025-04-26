package com.productiva.android.session

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.productiva.android.network.RetrofitClient
import com.productiva.android.network.model.CompanyData
import com.productiva.android.network.model.UserData
import com.productiva.android.utils.Constants

/**
 * Gestor de sesión del usuario.
 * Maneja el almacenamiento y recuperación de información de sesión como token,
 * usuario actual y empresa seleccionada.
 */
class SessionManager private constructor() {
    
    private lateinit var prefs: SharedPreferences
    private var isInitialized = false
    
    companion object {
        private const val TAG = "SessionManager"
        
        @Volatile
        private var instance: SessionManager? = null
        
        /**
         * Obtiene la instancia única del gestor de sesión.
         *
         * @return Instancia del gestor de sesión.
         */
        fun getInstance(): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager().also { instance = it }
            }
        }
    }
    
    /**
     * Inicializa el gestor de sesión.
     * Debe llamarse antes de usar cualquier otro método.
     *
     * @param context Contexto de la aplicación.
     */
    fun init(context: Context) {
        if (!isInitialized) {
            prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            isInitialized = true
            Log.d(TAG, "SessionManager inicializado")
        }
    }
    
    /**
     * Verifica si el usuario ha iniciado sesión.
     *
     * @return true si hay un token de autenticación válido, false en caso contrario.
     */
    fun isLoggedIn(): Boolean {
        validateInitialization()
        return getAuthToken().isNotEmpty()
    }
    
    /**
     * Obtiene el token de autenticación actual.
     *
     * @return Token de autenticación o cadena vacía si no hay sesión.
     */
    fun getAuthToken(): String {
        validateInitialization()
        return prefs.getString(Constants.PREF_AUTH_TOKEN, "") ?: ""
    }
    
    /**
     * Obtiene el ID del usuario actual.
     *
     * @return ID del usuario o 0 si no hay sesión.
     */
    fun getCurrentUserId(): Int {
        validateInitialization()
        return prefs.getInt(Constants.PREF_USER_ID, 0)
    }
    
    /**
     * Obtiene el nombre de usuario actual.
     *
     * @return Nombre de usuario o cadena vacía si no hay sesión.
     */
    fun getCurrentUsername(): String {
        validateInitialization()
        return prefs.getString(Constants.PREF_USERNAME, "") ?: ""
    }
    
    /**
     * Obtiene el rol del usuario actual.
     *
     * @return Rol del usuario o cadena vacía si no hay sesión.
     */
    fun getCurrentUserRole(): String {
        validateInitialization()
        return prefs.getString(Constants.PREF_USER_ROLE, "") ?: ""
    }
    
    /**
     * Obtiene el ID de la empresa seleccionada.
     *
     * @return ID de la empresa o 0 si no hay empresa seleccionada.
     */
    fun getCurrentCompanyId(): Int {
        validateInitialization()
        return prefs.getInt(Constants.PREF_COMPANY_ID, 0)
    }
    
    /**
     * Obtiene el nombre de la empresa seleccionada.
     *
     * @return Nombre de la empresa o cadena vacía si no hay empresa seleccionada.
     */
    fun getCurrentCompanyName(): String {
        validateInitialization()
        return prefs.getString(Constants.PREF_COMPANY_NAME, "") ?: ""
    }
    
    /**
     * Guarda los datos de sesión después de un inicio de sesión exitoso.
     *
     * @param token Token de autenticación.
     * @param user Datos del usuario.
     * @param company Empresa seleccionada por defecto (opcional).
     */
    fun saveUserSession(
        token: String,
        user: UserData,
        company: CompanyData? = null
    ) {
        validateInitialization()
        
        prefs.edit().apply {
            putString(Constants.PREF_AUTH_TOKEN, token)
            putInt(Constants.PREF_USER_ID, user.id)
            putString(Constants.PREF_USERNAME, user.username)
            putString(Constants.PREF_USER_ROLE, user.role)
            
            // Si se proporciona una empresa, guardarla como seleccionada
            if (company != null) {
                putInt(Constants.PREF_COMPANY_ID, company.id)
                putString(Constants.PREF_COMPANY_NAME, company.name)
            }
            
            apply()
        }
        
        Log.d(TAG, "Sesión guardada para usuario: ${user.username}")
    }
    
    /**
     * Selecciona una empresa como actual.
     *
     * @param company Datos de la empresa.
     */
    fun selectCompany(company: CompanyData) {
        validateInitialization()
        
        prefs.edit().apply {
            putInt(Constants.PREF_COMPANY_ID, company.id)
            putString(Constants.PREF_COMPANY_NAME, company.name)
            apply()
        }
        
        Log.d(TAG, "Empresa seleccionada: ${company.name}")
    }
    
    /**
     * Cierra la sesión actual y limpia todos los datos guardados.
     */
    fun logout() {
        validateInitialization()
        
        prefs.edit().clear().apply()
        RetrofitClient.clearApiService()
        
        Log.d(TAG, "Sesión cerrada")
    }
    
    /**
     * Verifica si el gestor ha sido inicializado.
     * Lanza una excepción si no lo está.
     */
    private fun validateInitialization() {
        if (!isInitialized) {
            throw IllegalStateException("SessionManager no ha sido inicializado. Llame a init() primero.")
        }
    }
}