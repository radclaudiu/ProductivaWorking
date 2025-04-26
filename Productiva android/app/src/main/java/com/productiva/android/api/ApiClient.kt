package com.productiva.android.api

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente API para comunicación con el servidor
 */
class ApiClient(private val context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences("productiva_prefs", Context.MODE_PRIVATE)
    private val serverUrl: String
        get() = preferences.getString("server_url", DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
    
    // Crear el interceptor para añadir el token a las peticiones
    private val authInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val token = preferences.getString("auth_token", "")
            val request = chain.request().newBuilder()
            
            // Añadir token de autenticación si existe
            if (!token.isNullOrEmpty()) {
                request.addHeader("Authorization", "Bearer $token")
            }
            
            // Añadir cabeceras adicionales
            request.addHeader("Content-Type", "application/json")
            request.addHeader("Accept", "application/json")
            request.addHeader("User-Agent", "Productiva-Android")
            
            return chain.proceed(request.build())
        }
    }
    
    // Crear el cliente OkHttp con los interceptores
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()
    
    // Crear la instancia de Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(serverUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    // Crear el servicio API
    val apiService: ApiService = retrofit.create(ApiService::class.java)
    
    // Métodos para gestionar el token de autenticación
    fun saveAuthToken(token: String) {
        preferences.edit().putString("auth_token", token).apply()
    }
    
    fun getAuthToken(): String? {
        return preferences.getString("auth_token", null)
    }
    
    fun clearAuthToken() {
        preferences.edit().remove("auth_token").apply()
    }
    
    // Método para actualizar la URL del servidor
    fun updateServerUrl(url: String) {
        preferences.edit().putString("server_url", url).apply()
    }
    
    // Método para comprobar si hay un token guardado
    fun hasAuthToken(): Boolean {
        return !preferences.getString("auth_token", "").isNullOrEmpty()
    }
    
    companion object {
        private const val CONNECTION_TIMEOUT = 30L
        private const val READ_TIMEOUT = 30L
        private const val WRITE_TIMEOUT = 30L
        private const val DEFAULT_SERVER_URL = "https://productiva.example.com/api/"
        
        @Volatile
        private var instance: ApiClient? = null
        
        fun getInstance(context: Context): ApiClient {
            return instance ?: synchronized(this) {
                instance ?: ApiClient(context.applicationContext).also { instance = it }
            }
        }
    }
}