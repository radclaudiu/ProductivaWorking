package com.productiva.android.network

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.productiva.android.network.interceptor.AuthInterceptor
import com.productiva.android.network.interceptor.ConnectionInterceptor
import com.productiva.android.network.interceptor.LoggingInterceptor
import com.productiva.android.utils.Constants
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit para comunicación con la API REST.
 * Proporciona un cliente HTTP configurado con interceptores para autenticación,
 * verificación de conectividad y logging.
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
            // Crear directorio de caché si no existe
            val cacheDir = File(context.cacheDir, "http-cache")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            
            // Configurar caché HTTP
            val cache = Cache(cacheDir, ApiConfig.CACHE_SIZE_BYTES.toLong())
            
            // Crear interceptores
            val authInterceptor = AuthInterceptor(context)
            val connectionInterceptor = ConnectionInterceptor(context)
            val loggingInterceptor = LoggingInterceptor()
            
            // Crear cliente HTTP con interceptores y timeouts
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(ApiConfig.CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(ApiConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(ApiConfig.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .cache(cache)
                .addInterceptor(connectionInterceptor)  // Verificar conectividad primero
                .addInterceptor(authInterceptor)        // Luego autenticación
                .addInterceptor(loggingInterceptor)     // Por último logging
                .build()
            
            // Configurar Gson para deserialización correcta
            val gson: Gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") // ISO 8601
                .create()
            
            // Crear cliente Retrofit
            val retrofit = Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            
            Log.d(TAG, "ApiService creado con URL base: ${ApiConfig.BASE_URL}")
            
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