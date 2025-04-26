package com.productiva.android.network

import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente de Retrofit para gestionar las peticiones HTTP a la API de Productiva.
 */
object RetrofitClient {
    private const val CACHE_SIZE = 10 * 1024 * 1024L // 10 MB
    private const val BASE_URL = "https://app.productiva.es/api/"
    private const val TIMEOUT = 30L // segundos
    
    private var apiService: ApiService? = null
    private var authToken: String? = null
    
    /**
     * Configura el token de autenticación para las peticiones.
     */
    fun setAuthToken(token: String?) {
        authToken = token
    }
    
    /**
     * Obtiene o crea una instancia del servicio de API.
     */
    fun getApiService(context: Context): ApiService {
        if (apiService == null) {
            val cache = Cache(context.cacheDir, CACHE_SIZE)
            
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            val authInterceptor = Interceptor { chain ->
                val request = chain.request().newBuilder()
                
                // Añadir token de autenticación si está disponible
                authToken?.let {
                    request.header("Authorization", "Bearer $it")
                }
                
                // Añadir cabeceras comunes
                request.header("Accept", "application/json")
                request.header("User-Agent", "Productiva-Android-App")
                
                chain.proceed(request.build())
            }
            
            val client = OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .build()
            
            val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .setLenient()
                .create()
            
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            
            apiService = retrofit.create(ApiService::class.java)
        }
        
        return apiService!!
    }
    
    /**
     * Borra la instancia actual del servicio de API para forzar su recreación.
     */
    fun clearApiService() {
        apiService = null
        authToken = null
    }
}