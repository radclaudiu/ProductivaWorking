package com.productiva.android.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.productiva.android.R
import com.productiva.android.viewmodel.LoginViewModel

/**
 * Activity para el inicio de sesión en la aplicación
 */
class LoginActivity : AppCompatActivity() {
    
    private lateinit var viewModel: LoginViewModel
    
    // Componentes de UI
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var serverUrlTextView: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Inicializar ViewModel
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        
        // Configurar componentes de UI
        setupUI()
        
        // Configurar observadores
        setupObservers()
        
        // Verificar si hay una sesión activa
        viewModel.checkActiveSession()
    }
    
    /**
     * Configura las referencias y eventos de UI
     */
    private fun setupUI() {
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)
        serverUrlTextView = findViewById(R.id.serverUrlTextView)
        
        // Rellenar campo de usuario si hay uno guardado
        usernameEditText.setText(viewModel.getLastUsername())
        
        // Mostrar URL del servidor
        serverUrlTextView.text = viewModel.getServerUrl()
        
        // Configurar evento de clic para botón de login
        loginButton.setOnClickListener {
            login()
        }
        
        // Configurar evento de clic para configuración de servidor
        serverUrlTextView.setOnClickListener {
            showServerUrlDialog()
        }
    }
    
    /**
     * Configura los observadores de LiveData
     */
    private fun setupObservers() {
        // Observar estado de carga
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            loginButton.isEnabled = !isLoading
        }
        
        // Observar mensajes de error
        viewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                errorText.text = errorMessage
                errorText.visibility = View.VISIBLE
            } else {
                errorText.visibility = View.GONE
            }
        }
        
        // Observar estado de autenticación
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginViewModel.LoginState.Success -> {
                    // Ir a la pantalla de selección de usuario
                    val intent = Intent(this, UserSelectionActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is LoginViewModel.LoginState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {
                    // Estado inicial, no hacer nada
                }
            }
        }
    }
    
    /**
     * Realiza el proceso de inicio de sesión
     */
    private fun login() {
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        
        if (username.isEmpty() || password.isEmpty()) {
            errorText.text = getString(R.string.error_empty_fields)
            errorText.visibility = View.VISIBLE
            return
        }
        
        errorText.visibility = View.GONE
        viewModel.login(username, password)
    }
    
    /**
     * Muestra un diálogo para configurar la URL del servidor
     */
    private fun showServerUrlDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.server_url_title))
        
        val input = EditText(this)
        input.setText(viewModel.getServerUrl())
        builder.setView(input)
        
        builder.setPositiveButton(getString(R.string.save)) { _, _ ->
            val newUrl = input.text.toString().trim()
            if (newUrl.isNotEmpty()) {
                viewModel.updateServerUrl(newUrl)
                serverUrlTextView.text = newUrl
                Toast.makeText(this, getString(R.string.server_url_updated), Toast.LENGTH_SHORT).show()
            }
        }
        
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }
        
        builder.show()
    }
}