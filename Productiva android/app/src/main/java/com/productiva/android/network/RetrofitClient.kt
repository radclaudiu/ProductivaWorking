package com.productiva.android.network

import android.content.Context
import android.util.Log
import com.productiva.android.session.SessionManager
import com.productiva.android.utils.Constants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit para comunicación con la API REST.
 * Proporciona un cliente HTTP configurado con interceptores para autenticación y logging.
 */
class RetrofitClient {
    
    companion object {
        private const val TAG = "RetrofitClient"
        private var apiService: ApiService? = null
        
        /**
         * Obtiene una instancia del servicio API configurado con Retrofit.
         *
         * @param context Contexto de la aplicación.
         * @return Instancia de ApiService.
         */
        fun getApiService(context: Context): ApiService {
            return apiService ?: synchronized(this) {
                val instance = createApiService(context)
                apiService = instance
                instance
            }
        }
        
        /**
         * Crea una nueva instancia del cliente Retrofit con todos los interceptores necesarios.
         *
         * @param context Contexto de la aplicación.
         * @return Instancia configurada de ApiService.
         */
        private fun createApiService(context: Context): ApiService {
            // Crear interceptor de logging para depuración
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = if (Constants.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }
            
            // Crear interceptor para añadir headers de autenticación
            val authInterceptor = Interceptor { chain ->
                val sessionManager = SessionManager.getInstance()
                val token = sessionManager.getAuthToken()
                
                val newRequest = if (token.isNotEmpty()) {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else {
                    chain.request()
                }
                
                chain.proceed(newRequest)
            }
            
            // Crear cliente HTTP con timeout e interceptores
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .build()
            
            // Crear cliente Retrofit
            val retrofit = Retrofit.Builder()
                .baseUrl(Constants.API_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            return retrofit.create(ApiService::class.java)
        }
        
        /**
         * Limpia la instancia actual del servicio API.
         * Debe llamarse cuando cambia la configuración (ej: logout).
         */
        fun clearApiService() {
            apiService = null
            Log.d(TAG, "ApiService limpiado")
        }
    }
}