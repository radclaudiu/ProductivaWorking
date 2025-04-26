package com.productiva.android.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.productiva.android.ProductivaApplication
import com.productiva.android.R
import kotlinx.coroutines.launch

/**
 * Actividad de login para autenticación en el sistema Productiva
 */
class LoginActivity : AppCompatActivity() {
    
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var serverUrlEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var errorMessageText: TextView
    
    private val userRepository by lazy {
        (application as ProductivaApplication).userRepository
    }
    
    private val sessionManager by lazy {
        (application as ProductivaApplication).sessionManager
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Inicializar views
        usernameEditText = findViewById(R.id.editTextUsername)
        passwordEditText = findViewById(R.id.editTextPassword)
        serverUrlEditText = findViewById(R.id.editTextServerUrl)
        loginButton = findViewById(R.id.buttonLogin)
        progressBar = findViewById(R.id.progressBar)
        errorMessageText = findViewById(R.id.textViewError)
        
        // Cargar URL del servidor guardada (si existe)
        val savedUrl = sessionManager.getServerUrl()
        if (!savedUrl.isNullOrEmpty()) {
            serverUrlEditText.setText(savedUrl)
        } else {
            // URL por defecto
            serverUrlEditText.setText("https://productiva.replit.app")
        }
        
        // Agregar validación en tiempo real
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateForm()
            }
        }
        
        usernameEditText.addTextChangedListener(textWatcher)
        passwordEditText.addTextChangedListener(textWatcher)
        serverUrlEditText.addTextChangedListener(textWatcher)
        
        // Configurar botón de login
        loginButton.setOnClickListener {
            if (validateForm()) {
                performLogin()
            }
        }
    }
    
    /**
     * Valida que se hayan ingresado todos los campos requeridos
     */
    private fun validateForm(): Boolean {
        val isValid = usernameEditText.text.isNotEmpty() &&
                      passwordEditText.text.isNotEmpty() &&
                      serverUrlEditText.text.isNotEmpty()
        
        loginButton.isEnabled = isValid
        return isValid
    }
    
    /**
     * Realiza el proceso de login
     */
    private fun performLogin() {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()
        val serverUrl = serverUrlEditText.text.toString()
        
        // Guardar URL del servidor
        sessionManager.saveServerUrl(serverUrl)
        
        // Actualizar la UI para mostrar carga
        setLoading(true)
        
        // Intentar login
        lifecycleScope.launch {
            val result = userRepository.login(username, password)
            
            runOnUiThread {
                setLoading(false)
                
                if (result.isSuccess) {
                    // Login exitoso, ir a la selección de usuario
                    val intent = Intent(this@LoginActivity, UserSelectionActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Mostrar error
                    val errorMessage = result.exceptionOrNull()?.message ?: "Error de autenticación"
                    errorMessageText.text = errorMessage
                    errorMessageText.visibility = View.VISIBLE
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    /**
     * Actualiza la UI para mostrar/ocultar indicadores de carga
     */
    private fun setLoading(loading: Boolean) {
        if (loading) {
            loginButton.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
            errorMessageText.visibility = View.GONE
        } else {
            loginButton.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }
}