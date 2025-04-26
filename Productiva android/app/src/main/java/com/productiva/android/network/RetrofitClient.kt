package com.productiva.android.network

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.productiva.android.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit que proporciona la instancia para realizar peticiones a la API.
 */
object RetrofitClient {
    private const val TAG = "RetrofitClient"
    
    // Cache de la instancia de ApiService
    private var apiService: ApiService? = null
    
    /**
     * Obtiene la URL base de la API.
     */
    fun getApiBaseUrl(context: Context): String {
        // En producción, obtener la URL del BuildConfig
        return BuildConfig.API_BASE_URL
    }
    
    /**
     * Obtiene una instancia de ApiService para realizar peticiones.
     * 
     * @param context Contexto de la aplicación.
     * @return Instancia de ApiService.
     */
    fun getApiService(context: Context): ApiService {
        // Si ya existe una instancia, devolverla
        apiService?.let { return it }
        
        // Crear cliente OkHttp con interceptores
        val client = createOkHttpClient()
        
        // Crear convertidor Gson
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create()
        
        // Crear instancia de Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(getApiBaseUrl(context))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        
        // Crear instancia de ApiService
        return retrofit.create(ApiService::class.java).also {
            apiService = it
            Log.d(TAG, "ApiService creado")
        }
    }
    
    /**
     * Crea una instancia configurada del cliente OkHttp.
     * 
     * @return Cliente OkHttp configurado.
     */
    private fun createOkHttpClient(): OkHttpClient {
        // Interceptor para logging
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        // Interceptor para añadir headers comunes
        val headerInterceptor = HeaderInterceptor()
        
        // Interceptor para gestionar el token de autenticación
        val authInterceptor = AuthInterceptor()
        
        // Construcción del cliente
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(headerInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }
    
    /**
     * Invalida la caché del cliente para forzar la creación de una nueva instancia.
     */
    fun invalidateCache() {
        apiService = null
        Log.d(TAG, "Caché de ApiService invalidada")
    }
}