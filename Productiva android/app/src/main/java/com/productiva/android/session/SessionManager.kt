package com.productiva.android.session

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.productiva.android.model.User
import com.productiva.android.network.RetrofitClient
import com.productiva.android.utils.PREF_AUTH_TOKEN
import com.productiva.android.utils.PREF_USER_DATA
import com.productiva.android.utils.PREFS_NAME

/**
 * Gestor de sesión del usuario.
 * Proporciona métodos para almacenar y recuperar datos de la sesión actual.
 */
class SessionManager private constructor() {
    private val TAG = "SessionManager"
    
    private var sharedPreferences: SharedPreferences? = null
    private var currentUser: User? = null
    private var authToken: String? = null
    
    /**
     * Inicializa el SessionManager con el contexto de la aplicación.
     * Debe llamarse en la inicialización de la aplicación.
     */
    fun init(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadSession()
        }
    }
    
    /**
     * Carga los datos de sesión desde las preferencias compartidas.
     */
    private fun loadSession() {
        authToken = sharedPreferences?.getString(PREF_AUTH_TOKEN, null)
        val userData = sharedPreferences?.getString(PREF_USER_DATA, null)
        
        if (!userData.isNullOrEmpty()) {
            try {
                currentUser = Gson().fromJson(userData, User::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar datos de usuario", e)
                // Si hay un error, limpiar los datos corruptos
                clearSession()
            }
        }
    }
    
    /**
     * Guarda los datos de sesión en preferencias compartidas.
     */
    private fun saveSession() {
        val editor = sharedPreferences?.edit() ?: return
        
        // Guardar token
        authToken?.let { token ->
            editor.putString(PREF_AUTH_TOKEN, token)
        } ?: editor.remove(PREF_AUTH_TOKEN)
        
        // Guardar datos de usuario
        currentUser?.let { user ->
            val userData = Gson().toJson(user)
            editor.putString(PREF_USER_DATA, userData)
        } ?: editor.remove(PREF_USER_DATA)
        
        editor.apply()
    }
    
    /**
     * Establece los datos de sesión después de un inicio de sesión exitoso.
     */
    fun createSession(user: User, token: String) {
        authToken = token
        currentUser = user
        
        // Actualizar token en RetrofitClient
        RetrofitClient.updateAuthToken(token)
        
        // Guardar en preferencias
        saveSession()
        
        Log.d(TAG, "Sesión creada para usuario: ${user.name}")
    }
    
    /**
     * Limpia los datos de sesión al cerrar sesión.
     */
    fun clearSession() {
        authToken = null
        currentUser = null
        
        // Actualizar token en RetrofitClient
        RetrofitClient.updateAuthToken(null)
        
        // Limpiar preferencias
        sharedPreferences?.edit()?.apply {
            remove(PREF_AUTH_TOKEN)
            remove(PREF_USER_DATA)
            apply()
        }
        
        Log.d(TAG, "Sesión cerrada")
    }
    
    /**
     * Actualiza los datos del usuario actual.
     */
    fun updateCurrentUser(user: User) {
        currentUser = user
        saveSession()
    }
    
    /**
     * Comprueba si el usuario está autenticado.
     */
    fun isLoggedIn(): Boolean {
        return authToken != null && currentUser != null
    }
    
    /**
     * Obtiene el token de autenticación.
     */
    fun getAuthToken(): String? {
        return authToken
    }
    
    /**
     * Obtiene los datos del usuario actual.
     */
    fun getCurrentUser(): User? {
        return currentUser
    }
    
    /**
     * Comprueba si el usuario tiene un permiso específico.
     */
    fun hasPermission(permission: String): Boolean {
        return currentUser?.hasPermission(permission) ?: false
    }
    
    /**
     * Comprueba si el usuario puede acceder a una ubicación específica.
     */
    fun canAccessLocation(locationId: Int): Boolean {
        return currentUser?.canAccessLocation(locationId) ?: false
    }
    
    companion object {
        @Volatile
        private var instance: SessionManager? = null
        
        /**
         * Obtiene la instancia única del gestor de sesión.
         */
        fun getInstance(): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager().also { instance = it }
            }
        }
    }
}