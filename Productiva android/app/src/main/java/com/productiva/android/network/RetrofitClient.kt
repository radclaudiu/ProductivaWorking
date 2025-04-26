package com.productiva.android.network

import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit para comunicación con la API REST.
 * 
 * Esta clase singleton configura y proporciona instancias de Retrofit
 * y sus servicios asociados para comunicarse con el backend.
 */
object RetrofitClient {

    private const val BASE_URL = "https://api.productiva.app/" // URL base de la API
    private const val TIMEOUT = 30L // Timeout en segundos
    
    private var apiService: ApiService? = null
    
    /**
     * Configura y devuelve un cliente OkHttpClient con todos los interceptores necesarios.
     *
     * @param context Contexto de la aplicación
     * @param networkStatusManager Administrador del estado de red
     * @param authToken Token de autenticación (opcional)
     * @return Cliente OkHttpClient configurado
     */
    private fun createOkHttpClient(
        context: Context,
        networkStatusManager: NetworkStatusManager,
        authToken: String? = null
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(NetworkConnectionInterceptor(context, networkStatusManager))
            .addInterceptor(loggingInterceptor)
        
        // Añadir el interceptor de autenticación si hay token
        if (!authToken.isNullOrEmpty()) {
            builder.addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $authToken")
                    .build()
                chain.proceed(request)
            }
        }
        
        return builder.build()
    }
    
    /**
     * Obtiene el servicio de API configurado con Retrofit.
     *
     * @param context Contexto de la aplicación
     * @param networkStatusManager Administrador del estado de red
     * @param authToken Token de autenticación (opcional)
     * @return Instancia del servicio ApiService
     */
    fun getApiService(
        context: Context,
        networkStatusManager: NetworkStatusManager,
        authToken: String? = null
    ): ApiService {
        // Si ya existe una instancia y no hay token (o el token no ha cambiado), la reutilizamos
        if (apiService != null && authToken == null) {
            return apiService!!
        }
        
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient(context, networkStatusManager, authToken))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        
        apiService = retrofit.create(ApiService::class.java)
        return apiService!!
    }
}