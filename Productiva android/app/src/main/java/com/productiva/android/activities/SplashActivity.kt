package com.productiva.android.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.productiva.android.ProductivaApplication
import com.productiva.android.R

/**
 * Actividad de inicio (Splash Screen)
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // Verificar si ya hay una sesión activa
        val sessionManager = (application as ProductivaApplication).sessionManager
        
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = if (sessionManager.isLoggedIn()) {
                if (sessionManager.getSelectedUserId() != -1) {
                    // Ya hay un usuario seleccionado, ir directamente a la actividad principal
                    Intent(this, MainActivity::class.java)
                } else {
                    // Está autenticado pero no ha seleccionado usuario, ir a la selección
                    Intent(this, UserSelectionActivity::class.java)
                }
            } else {
                // No está autenticado, ir al login
                Intent(this, LoginActivity::class.java)
            }
            
            startActivity(intent)
            finish()
        }, 1500) // 1.5 segundos de splash screen
    }
}