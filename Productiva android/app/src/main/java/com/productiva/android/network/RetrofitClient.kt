package com.productiva.android.network

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.reflect.TypeToken
import com.productiva.android.session.SessionManager
import com.productiva.android.utils.API_BASE_URL
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit para la comunicación con la API.
 * Configura y proporciona la instancia de Retrofit para realizar peticiones.
 */
object RetrofitClient {
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L
    
    private var retrofit: Retrofit? = null
    private var apiService: ApiService? = null
    
    /**
     * Obtiene la instancia del servicio de API.
     * Si no existe, la crea con la configuración adecuada.
     */
    fun getApiService(context: Context): ApiService {
        if (apiService == null) {
            val client = createHttpClient(context)
            val gson = createGsonConverter()
            
            retrofit = Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            
            apiService = retrofit?.create(ApiService::class.java)
        }
        
        return apiService ?: throw IllegalStateException("API Service no pudo ser inicializado")
    }
    
    /**
     * Crea el cliente HTTP con los interceptores necesarios.
     */
    private fun createHttpClient(context: Context): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            
            // Obtener token de autenticación del SessionManager
            val token = SessionManager.getInstance().getAuthToken()
            
            // Si hay token, añadirlo a las cabeceras
            val request = if (!token.isNullOrEmpty()) {
                original.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .header("Accept", "application/json")
                    .method(original.method, original.body)
                    .build()
            } else {
                original.newBuilder()
                    .header("Accept", "application/json")
                    .method(original.method, original.body)
                    .build()
            }
            
            chain.proceed(request)
        }
        
        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }
    
    /**
     * Crea el convertidor Gson con los deserializadores necesarios.
     */
    private fun createGsonConverter(): Gson {
        // Deserializador para listas de strings (para las etiquetas o tags)
        val stringListType = object : TypeToken<List<String>>() {}.type
        val stringListDeserializer = JsonDeserializer { json, _, _ ->
            val list = mutableListOf<String>()
            
            if (json.isJsonArray) {
                val jsonArray = json.asJsonArray
                jsonArray.forEach {
                    if (it.isJsonPrimitive) {
                        list.add(it.asString)
                    }
                }
            } else if (json.isJsonPrimitive) {
                // Si es una cadena, intentar separar por comas o barras
                val str = json.asString
                if (str.contains(",")) {
                    list.addAll(str.split(",").map { it.trim() })
                } else if (str.contains("|")) {
                    list.addAll(str.split("|").map { it.trim() })
                } else {
                    list.add(str)
                }
            }
            
            list
        }
        
        return GsonBuilder()
            .setLenient()
            .registerTypeAdapter(stringListType, stringListDeserializer)
            .create()
    }
    
    /**
     * Actualiza el token de autenticación.
     * Útil cuando se inicia sesión o se renueva el token.
     */
    fun updateAuthToken(token: String?) {
        // Forzar la recreación del cliente HTTP con el nuevo token
        apiService = null
        retrofit = null
    }
}