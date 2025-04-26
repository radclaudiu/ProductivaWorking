package com.productiva.android.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.productiva.android.R
import com.productiva.android.api.ApiClient
import com.productiva.android.viewmodel.LoginViewModel
import com.google.android.material.textfield.TextInputLayout

/**
 * Activity para la pantalla de inicio de sesión
 */
class LoginActivity : AppCompatActivity() {
    
    private lateinit var viewModel: LoginViewModel
    
    // UI components
    private lateinit var usernameInput: TextInputLayout
    private lateinit var passwordInput: TextInputLayout
    private lateinit var loginButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var serverUrlText: TextView
    private lateinit var serverUrlInput: EditText
    private lateinit var saveUrlButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Initialize UI components
        usernameInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        progressBar = findViewById(R.id.progress_bar)
        serverUrlText = findViewById(R.id.server_url_text)
        serverUrlInput = findViewById(R.id.server_url_input)
        saveUrlButton = findViewById(R.id.save_url_button)
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        
        // Set observers
        setupObservers()
        
        // Set click listeners
        setupClickListeners()
        
        // Load server URL
        loadServerUrl()
        
        // Check authentication status
        viewModel.checkAuthStatus()
    }
    
    /**
     * Configura los observadores para el ViewModel
     */
    private fun setupObservers() {
        // Observe authentication state
        viewModel.authState.observe(this) { state ->
            when (state) {
                LoginViewModel.AuthState.LOADING -> {
                    showLoading(true)
                }
                LoginViewModel.AuthState.AUTHENTICATED -> {
                    showLoading(false)
                    navigateToUserSelection()
                }
                LoginViewModel.AuthState.UNAUTHENTICATED -> {
                    showLoading(false)
                }
            }
        }
        
        // Observe error messages
        viewModel.errorMessage.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                showError(message)
            }
        }
        
        // Observe server URL
        viewModel.serverUrl.observe(this) { url ->
            if (!url.isNullOrEmpty()) {
                serverUrlInput.setText(url)
            }
        }
    }
    
    /**
     * Configura los listeners para los botones
     */
    private fun setupClickListeners() {
        // Login button
        loginButton.setOnClickListener {
            val username = usernameInput.editText?.text.toString()
            val password = passwordInput.editText?.text.toString()
            
            // Validate inputs
            if (validateInputs(username, password)) {
                viewModel.login(username, password)
            }
        }
        
        // Save URL button
        saveUrlButton.setOnClickListener {
            val url = serverUrlInput.text.toString()
            if (url.isNotEmpty()) {
                saveServerUrl(url)
                Toast.makeText(this, "URL del servidor actualizada", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Valida los campos de entrada
     */
    private fun validateInputs(username: String, password: String): Boolean {
        var isValid = true
        
        if (username.isEmpty()) {
            usernameInput.error = "El nombre de usuario es requerido"
            isValid = false
        } else {
            usernameInput.error = null
        }
        
        if (password.isEmpty()) {
            passwordInput.error = "La contraseña es requerida"
            isValid = false
        } else {
            passwordInput.error = null
        }
        
        return isValid
    }
    
    /**
     * Muestra u oculta el indicador de carga
     */
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            loginButton.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            loginButton.isEnabled = true
        }
    }
    
    /**
     * Muestra un mensaje de error
     */
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * Navega a la pantalla de selección de usuario
     */
    private fun navigateToUserSelection() {
        val intent = Intent(this, UserSelectionActivity::class.java)
        startActivity(intent)
        // No finalizamos esta actividad para permitir volver al login si se cierra sesión
    }
    
    /**
     * Carga la URL del servidor desde ApiClient
     */
    private fun loadServerUrl() {
        val apiClient = ApiClient.getInstance(this)
        serverUrlInput.setText(apiClient.getServerUrl())
    }
    
    /**
     * Guarda y actualiza la URL del servidor
     */
    private fun saveServerUrl(url: String) {
        val apiClient = ApiClient.getInstance(this)
        apiClient.updateServerUrl(url)
        viewModel.updateServerUrl(url)
    }
}