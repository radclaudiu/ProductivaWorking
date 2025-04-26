package com.productiva.android.network

import com.productiva.android.BuildConfig
import com.productiva.android.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit para la comunicación con el servidor.
 * Configurado con interceptores para autenticación, gestión de red y logging.
 */
object RetrofitClient {
    // Base URL del servidor (en esta versión, utilizamos la URL de desarrollo)
    private const val BASE_URL = "https://productiva.replit.app/api/"
    
    /**
     * Obtiene una instancia de ApiService configurada para la comunicación con el servidor.
     * 
     * @param sessionManager para obtener el token de autenticación
     * @return ApiService configurado
     */
    fun getApiService(sessionManager: SessionManager): ApiService {
        val retrofit = getRetrofitInstance(sessionManager)
        return retrofit.create(ApiService::class.java)
    }
    
    /**
     * Crea y configura una instancia de Retrofit con todos los interceptores necesarios.
     * 
     * @param sessionManager para la autenticación
     * @return Instancia de Retrofit configurada
     */
    private fun getRetrofitInstance(sessionManager: SessionManager): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getOkHttpClient(sessionManager))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Configura el cliente OkHttp con todos los interceptores necesarios.
     * 
     * @param sessionManager para la autenticación
     * @return Cliente OkHttp configurado
     */
    private fun getOkHttpClient(sessionManager: SessionManager): OkHttpClient {
        // Crear interceptor de logging solo en modo debug
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) 
                HttpLoggingInterceptor.Level.BODY 
            else 
                HttpLoggingInterceptor.Level.NONE
        }
        
        // Construir cliente con todos los interceptores
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(NetworkConnectionInterceptor())
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
}