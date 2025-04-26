package com.productiva.android.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente para la API de Productiva
 * Configura Retrofit para hacer llamadas a la API
 */
object ApiClient {
    private const val DEFAULT_TIMEOUT = 30L
    
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Crea una instancia de Retrofit configurada para la API
     */
    fun createRetrofit(baseUrl: String): Retrofit {
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create()
            
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    /**
     * Crea una instancia del servicio de API
     */
    fun createApiService(baseUrl: String): ApiService {
        return createRetrofit(baseUrl).create(ApiService::class.java)
    }
}