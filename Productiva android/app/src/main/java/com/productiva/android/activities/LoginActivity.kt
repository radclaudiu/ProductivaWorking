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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.productiva.android.ProductivaApplication
import com.productiva.android.R
import com.productiva.android.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * Actividad para el login de usuarios
 * Permite autenticarse con las credenciales del sistema web Productiva
 */
class LoginActivity : AppCompatActivity() {
    
    private lateinit var inputLayoutUsername: TextInputLayout
    private lateinit var inputLayoutPassword: TextInputLayout
    private lateinit var inputLayoutServerUrl: TextInputLayout
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextServerUrl: EditText
    private lateinit var buttonLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewError: TextView
    
    private lateinit var userRepository: UserRepository
    private lateinit var app: ProductivaApplication
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Obtener instancia de la aplicación
        app = application as ProductivaApplication
        
        // Inicializar el repositorio de usuarios
        userRepository = UserRepository(
            apiService = app.apiService,
            userDao = app.database.userDao(),
            context = this
        )
        
        // Inicializar vistas
        inputLayoutUsername = findViewById(R.id.inputLayoutUsername)
        inputLayoutPassword = findViewById(R.id.inputLayoutPassword)
        inputLayoutServerUrl = findViewById(R.id.inputLayoutServerUrl)
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextServerUrl = findViewById(R.id.editTextServerUrl)
        buttonLogin = findViewById(R.id.buttonLogin)
        progressBar = findViewById(R.id.progressBar)
        textViewError = findViewById(R.id.textViewError)
        
        // Cargar URL del servidor guardada
        editTextServerUrl.setText(app.sessionManager.getServerUrl())
        
        // Configurar listeners
        setupTextWatchers()
        
        // Configurar botón de login
        buttonLogin.setOnClickListener {
            login()
        }
        
        // Verificar si ya hay sesión activa
        checkActiveSession()
    }
    
    /**
     * Configura los TextWatchers para validar la entrada
     */
    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No se necesita implementación
            }
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No se necesita implementación
            }
            
            override fun afterTextChanged(s: Editable?) {
                validateInputs()
            }
        }
        
        editTextUsername.addTextChangedListener(textWatcher)
        editTextPassword.addTextChangedListener(textWatcher)
        editTextServerUrl.addTextChangedListener(textWatcher)
    }
    
    /**
     * Valida los campos de entrada
     */
    private fun validateInputs() {
        val username = editTextUsername.text.toString().trim()
        val password = editTextPassword.text.toString()
        val serverUrl = editTextServerUrl.text.toString().trim()
        
        val isValid = username.isNotEmpty() && 
                     password.isNotEmpty() && 
                     serverUrl.isNotEmpty()
        
        buttonLogin.isEnabled = isValid
        
        // Limpiar errores
        inputLayoutUsername.error = null
        inputLayoutPassword.error = null
        inputLayoutServerUrl.error = null
        textViewError.visibility = View.GONE
    }
    
    /**
     * Realiza el proceso de login
     */
    private fun login() {
        val username = editTextUsername.text.toString().trim()
        val password = editTextPassword.text.toString()
        val serverUrl = editTextServerUrl.text.toString().trim()
        
        // Validar y formatear URL del servidor
        var formattedUrl = serverUrl
        if (!formattedUrl.startsWith("http://") && !formattedUrl.startsWith("https://")) {
            formattedUrl = "https://$formattedUrl"
        }
        if (!formattedUrl.endsWith("/")) {
            formattedUrl = "$formattedUrl/"
        }
        
        // Actualizar URL del servidor
        app.updateServerUrl(formattedUrl)
        
        // Mostrar progreso
        setLoading(true)
        
        // Realizar login
        lifecycleScope.launch {
            try {
                val result = userRepository.login(username, password)
                
                if (result.isSuccess) {
                    // Login exitoso, ir a seleccionar usuario (perfil)
                    val intent = Intent(this@LoginActivity, UserSelectionActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    showError(error)
                }
            } catch (e: Exception) {
                showError("Error de conexión: ${e.message}")
            } finally {
                setLoading(false)
            }
        }
    }
    
    /**
     * Verifica si hay una sesión activa y redirige según corresponda
     */
    private fun checkActiveSession() {
        if (app.sessionManager.isLoggedIn()) {
            if (app.sessionManager.isUserSelected()) {
                // Si hay un usuario seleccionado, ir directamente a la pantalla principal
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // Si no hay un usuario seleccionado, ir a la pantalla de selección
                val intent = Intent(this, UserSelectionActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
    
    /**
     * Muestra un mensaje de error
     */
    private fun showError(message: String) {
        textViewError.text = message
        textViewError.visibility = View.VISIBLE
    }
    
    /**
     * Actualiza la UI para mostrar/ocultar indicadores de carga
     */
    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        buttonLogin.visibility = if (loading) View.GONE else View.VISIBLE
        
        // Deshabilitar campos durante la carga
        editTextUsername.isEnabled = !loading
        editTextPassword.isEnabled = !loading
        editTextServerUrl.isEnabled = !loading
    }
}