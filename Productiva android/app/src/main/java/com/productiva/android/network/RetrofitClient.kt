package com.productiva.android.network

import android.content.Context
import com.productiva.android.BuildConfig
import com.productiva.android.session.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit para comunicación con la API del servidor.
 * Configura Retrofit con los parámetros necesarios y proporciona el servicio API.
 */
object RetrofitClient {
    private const val BASE_URL = "https://productiva.mydomain.com/api/"
    private const val TIMEOUT_SECONDS = 60L
    
    /**
     * Obtiene una instancia configurada del servicio API.
     *
     * @param context Contexto de la aplicación.
     * @return Instancia del servicio API.
     */
    fun getApiService(context: Context): ApiService {
        return getRetrofit(context).create(ApiService::class.java)
    }
    
    /**
     * Obtiene una instancia configurada de Retrofit.
     *
     * @param context Contexto de la aplicación.
     * @return Instancia de Retrofit.
     */
    private fun getRetrofit(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getOkHttpClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Obtiene un cliente OkHttpClient configurado con interceptores y timeouts.
     *
     * @param context Contexto de la aplicación.
     * @return Instancia de OkHttpClient.
     */
    private fun getOkHttpClient(context: Context): OkHttpClient {
        val builder = OkHttpClient.Builder()
            
        // Añadir interceptor de logging en modo debug
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }
        
        // Añadir interceptor de autenticación
        builder.addInterceptor(AuthInterceptor(context))
        
        // Añadir interceptor para caché
        builder.addInterceptor(CacheInterceptor(context))
        
        // Configurar timeouts
        builder.connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        builder.readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        builder.writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        
        return builder.build()
    }
}

/**
 * Interceptor para añadir cabeceras de autenticación a las peticiones.
 */
class AuthInterceptor(private val context: Context) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val sessionManager = SessionManager.getInstance()
        
        // Si no es una ruta de autenticación y tenemos token, añadir cabecera Authorization
        val token = sessionManager.getAccessToken()
        val authenticatedRequest = if (token != null && !isAuthRoute(request)) {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }
        
        // Continuar con la cadena de interceptores
        val response = chain.proceed(authenticatedRequest)
        
        // Si la respuesta es 401 (Unauthorized), intentar renovar el token
        if (response.code == 401 && !isRefreshTokenRequest(request) && sessionManager.hasRefreshToken()) {
            response.close()
            
            // Renovar token
            val newToken = sessionManager.refreshToken(context)
            
            // Si se ha renovado el token, reintentar la petición original
            return if (newToken != null) {
                chain.proceed(
                    authenticatedRequest.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                )
            } else {
                // Si no se ha podido renovar el token, devolver la respuesta 401 original
                // para que la aplicación maneje el caso (normalmente, redirección a login)
                chain.proceed(request)
            }
        }
        
        return response
    }
    
    /**
     * Determina si la petición es para una ruta de autenticación.
     */
    private fun isAuthRoute(request: Request): Boolean {
        val url = request.url.toString()
        return url.contains("/auth/login") || url.contains("/auth/refresh")
    }
    
    /**
     * Determina si la petición es para renovar el token.
     */
    private fun isRefreshTokenRequest(request: Request): Boolean {
        return request.url.toString().contains("/auth/refresh")
    }
}

/**
 * Interceptor para gestionar la caché de respuestas HTTP.
 */
class CacheInterceptor(private val context: Context) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val connectivityMonitor = ConnectivityMonitor.getInstance(context)
        
        // Modificar la solicitud según la conectividad
        val modifiedRequest = if (connectivityMonitor.isNetworkAvailable()) {
            // Si hay conexión, no usar caché
            request.newBuilder()
                .cacheControl(okhttp3.CacheControl.FORCE_NETWORK)
                .build()
        } else {
            // Si no hay conexión, intentar usar caché
            request.newBuilder()
                .cacheControl(okhttp3.CacheControl.FORCE_CACHE)
                .build()
        }
        
        // Proceder con la solicitud modificada
        val response = chain.proceed(modifiedRequest)
        
        // Modificar la respuesta para configurar la caché
        return if (connectivityMonitor.isNetworkAvailable()) {
            // Si hay conexión, cachear por 5 minutos
            response.newBuilder()
                .header("Cache-Control", "public, max-age=300")
                .build()
        } else {
            // Si no hay conexión, usar caché por hasta 7 días
            response.newBuilder()
                .header("Cache-Control", "public, only-if-cached, max-stale=604800")
                .build()
        }
    }
}