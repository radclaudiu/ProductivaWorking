package com.productiva.android.api

import com.productiva.android.ProductivaApplication
import com.productiva.android.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente API para la comunicación con el servidor
 */
class ApiClient(private val app: ProductivaApplication) {
    
    private val sessionManager: SessionManager = app.sessionManager
    
    /**
     * Crea una instancia de Retrofit configurada
     */
    fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(sessionManager.getServerUrl())
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Crea un cliente OkHttp con interceptores para autenticación y logging
     */
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(loggingInterceptor)
            .build()
    }
    
    /**
     * Crea una instancia del servicio API
     */
    fun createApiService(): ApiService {
        return createRetrofit().create(ApiService::class.java)
    }
    
    /**
     * Interceptor para añadir token de autenticación a las peticiones
     */
    private class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            
            // Si no hay token, devuelve la petición original
            val token = sessionManager.getAuthToken() ?: return chain.proceed(originalRequest)
            
            // Añadir token a la cabecera
            val requestWithToken = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            
            return chain.proceed(requestWithToken)
        }
    }
}