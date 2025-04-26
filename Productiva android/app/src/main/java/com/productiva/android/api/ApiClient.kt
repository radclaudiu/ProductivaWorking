package com.productiva.android.api

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DateFormat
import java.util.concurrent.TimeUnit

/**
 * Cliente para las llamadas a la API
 */
class ApiClient private constructor(context: Context) {
    
    companion object {
        private const val DEFAULT_SERVER_URL = "https://productiva.example.com/api/"
        private const val AUTH_TOKEN_PREF = "auth_token"
        private const val SERVER_URL_PREF = "server_url"
        private const val PREFS_NAME = "productiva_api"
        
        @Volatile
        private var instance: ApiClient? = null
        
        fun getInstance(context: Context): ApiClient {
            return instance ?: synchronized(this) {
                instance ?: ApiClient(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // OkHttp client con interceptores
    private val httpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            
            // Añadir token de autorización si existe
            val token = sharedPreferences.getString(AUTH_TOKEN_PREF, null)
            val request = if (token != null) {
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                originalRequest
            }
            
            chain.proceed(request)
        }
        
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    // Gson para serialización/deserialización con soporte de fechas
    private val gson: Gson by lazy {
        GsonBuilder()
            .setDateFormat(DateFormat.LONG)
            .create()
    }
    
    // URL del servidor
    private var serverUrl: String = sharedPreferences.getString(SERVER_URL_PREF, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
    
    // Retrofit con configuración
    private var retrofit: Retrofit = buildRetrofit()
    
    // Servicio API
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
    
    /**
     * Construye el cliente Retrofit
     */
    private fun buildRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(serverUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    /**
     * Actualiza la URL del servidor y reconstruye Retrofit
     */
    fun updateServerUrl(newServerUrl: String) {
        serverUrl = newServerUrl
        sharedPreferences.edit().putString(SERVER_URL_PREF, newServerUrl).apply()
        retrofit = buildRetrofit()
    }
    
    /**
     * Guarda el token de autorización
     */
    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(AUTH_TOKEN_PREF, token).apply()
    }
    
    /**
     * Obtiene el token de autorización
     */
    fun getAuthToken(): String? {
        return sharedPreferences.getString(AUTH_TOKEN_PREF, null)
    }
    
    /**
     * Verifica si hay un token de autorización guardado
     */
    fun hasAuthToken(): Boolean {
        return !sharedPreferences.getString(AUTH_TOKEN_PREF, null).isNullOrEmpty()
    }
    
    /**
     * Elimina el token de autorización
     */
    fun clearAuthToken() {
        sharedPreferences.edit().remove(AUTH_TOKEN_PREF).apply()
    }
}