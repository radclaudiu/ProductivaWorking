package com.productiva.android.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.productiva.android.ProductivaApplication
import com.productiva.android.R
import com.productiva.android.adapters.UserListAdapter
import com.productiva.android.model.User
import com.productiva.android.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * Actividad para seleccionar un usuario (perfil) después del login
 */
class UserSelectionActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var recyclerViewUsers: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewEmpty: TextView
    private lateinit var userListAdapter: UserListAdapter
    
    private lateinit var userRepository: UserRepository
    private lateinit var app: ProductivaApplication
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_selection)
        
        // Obtener instancia de la aplicación
        app = application as ProductivaApplication
        
        // Inicializar el repositorio de usuarios
        userRepository = UserRepository(
            apiService = app.apiService,
            userDao = app.database.userDao(),
            context = this
        )
        
        // Inicializar vistas
        toolbar = findViewById(R.id.toolbar)
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        progressBar = findViewById(R.id.progressBar)
        textViewEmpty = findViewById(R.id.textViewEmpty)
        
        // Configurar toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.select_user_title)
        
        // Configurar RecyclerView
        recyclerViewUsers.layoutManager = LinearLayoutManager(this)
        userListAdapter = UserListAdapter { user ->
            onUserSelected(user)
        }
        recyclerViewUsers.adapter = userListAdapter
        
        // Configurar SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            loadUsers()
        }
        
        // Cargar usuarios
        loadUsers()
    }
    
    /**
     * Carga los usuarios disponibles
     */
    private fun loadUsers() {
        val token = app.sessionManager.getAuthToken()
        
        if (token == null) {
            // Sin token, volver a login
            goToLogin()
            return
        }
        
        setLoading(true)
        
        lifecycleScope.launch {
            try {
                val result = userRepository.getAvailableUsers(token)
                
                if (result.isSuccess) {
                    val users = result.getOrNull() ?: emptyList()
                    
                    if (users.isEmpty()) {
                        showEmptyState("No hay usuarios disponibles")
                    } else {
                        hideEmptyState()
                        userListAdapter.updateUserList(users)
                        
                        // Si solo hay un usuario disponible, seleccionarlo automáticamente
                        if (users.size == 1) {
                            onUserSelected(users.first())
                        }
                    }
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    showEmptyState(error)
                }
            } catch (e: Exception) {
                showEmptyState("Error de conexión: ${e.message}")
            } finally {
                setLoading(false)
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
    
    /**
     * Maneja la selección de un usuario
     */
    private fun onUserSelected(user: User) {
        setLoading(true)
        
        lifecycleScope.launch {
            try {
                val result = userRepository.selectUser(user.id)
                
                if (result.isSuccess) {
                    // Usuario seleccionado, ir a la pantalla principal
                    val intent = Intent(this@UserSelectionActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    Toast.makeText(this@UserSelectionActivity, error, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@UserSelectionActivity,
                    "Error al seleccionar usuario: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }
    
    /**
     * Muestra el estado vacío con un mensaje
     */
    private fun showEmptyState(message: String) {
        textViewEmpty.text = message
        textViewEmpty.visibility = View.VISIBLE
        recyclerViewUsers.visibility = View.GONE
    }
    
    /**
     * Oculta el estado vacío
     */
    private fun hideEmptyState() {
        textViewEmpty.visibility = View.GONE
        recyclerViewUsers.visibility = View.VISIBLE
    }
    
    /**
     * Actualiza la UI para mostrar/ocultar indicadores de carga
     */
    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }
    
    /**
     * Redirige a la pantalla de login
     */
    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_user_selection, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                confirmLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    /**
     * Muestra un diálogo de confirmación para cerrar sesión
     */
    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Está seguro que desea cerrar sesión?")
            .setPositiveButton("Cerrar sesión") { _, _ ->
                userRepository.logout()
                goToLogin()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}