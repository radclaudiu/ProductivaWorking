package com.productiva.android.network

import android.content.Context
import android.util.Log
import com.productiva.android.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit para realizar peticiones a la API.
 */
object RetrofitClient {
    private const val TAG = "RetrofitClient"
    private const val BASE_URL = "https://api.productiva.es/" // URL base de la API
    private const val TIMEOUT = 30L // Timeout en segundos
    
    private var apiService: ApiService? = null
    
    /**
     * Obtiene una instancia del ApiService.
     */
    fun getApiService(context: Context): ApiService {
        if (apiService == null) {
            apiService = createApiService(context)
        }
        return apiService!!
    }
    
    /**
     * Crea una nueva instancia del ApiService.
     */
    private fun createApiService(context: Context): ApiService {
        val sessionManager = SessionManager(context)
        
        // Interceptor para logging
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        // Interceptor para añadir token de autenticación
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            
            // Obtener token de sesión
            val token = sessionManager.getAuthToken()
            
            // Si hay token, añadirlo a los headers
            val requestBuilder = if (token != null) {
                original.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .method(original.method, original.body)
            } else {
                original.newBuilder()
                    .method(original.method, original.body)
            }
            
            // Añadir headers comunes
            val request = requestBuilder
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "Productiva-Android-App")
                .build()
            
            chain.proceed(request)
        }
        
        // Cliente OkHttp con interceptores
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .build()
        
        // Crear Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        return retrofit.create(ApiService::class.java)
    }
    
    /**
     * Invalida la instancia actual del ApiService, forzando una nueva creación.
     */
    fun invalidate() {
        apiService = null
    }
}