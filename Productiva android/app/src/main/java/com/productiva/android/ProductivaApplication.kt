package com.productiva.android

import android.app.Application
import androidx.room.Room
import com.productiva.android.api.ApiService
import com.productiva.android.repository.TaskRepository
import com.productiva.android.repository.UserRepository
import com.productiva.android.utils.AppDatabase
import com.productiva.android.utils.SessionManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProductivaApplication : Application() {
    
    // API_BASE_URL debe ser actualizado con la URL de tu servidor web
    private val API_BASE_URL = "https://tu-servidor-productiva.com/api/"
    
    // Instancia de la base de datos de Room
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "productiva_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    // Instancia de Retrofit para llamadas a la API
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // Servicio API
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
    
    // Administrador de sesiones para guardar token y datos de usuario
    val sessionManager: SessionManager by lazy {
        SessionManager(applicationContext)
    }
    
    // Repositorios
    val userRepository: UserRepository by lazy {
        UserRepository(apiService, database, sessionManager)
    }
    
    val taskRepository: TaskRepository by lazy {
        TaskRepository(apiService, database)
    }
    
    override fun onCreate() {
        super.onCreate()
        // Inicializaciones adicionales si son necesarias
    }
}