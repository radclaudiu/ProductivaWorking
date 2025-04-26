package com.productiva.android.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.productiva.android.ProductivaApplication
import com.productiva.android.R
import com.productiva.android.adapters.UserAdapter
import com.productiva.android.models.User
import kotlinx.coroutines.launch

/**
 * Actividad para selección de usuario en el portal de tareas
 */
class UserSelectionActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    
    private val userRepository by lazy {
        (application as ProductivaApplication).userRepository
    }
    
    private val sessionManager by lazy {
        (application as ProductivaApplication).sessionManager
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_selection)
        
        // Inicializar views
        recyclerView = findViewById(R.id.recyclerViewUsers)
        progressBar = findViewById(R.id.progressBar)
        emptyView = findViewById(R.id.textViewEmpty)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        
        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Configurar el adaptador con listener de clics
        val userAdapter = UserAdapter { user ->
            onUserSelected(user)
        }
        recyclerView.adapter = userAdapter
        
        // Configurar SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            refreshUsers()
        }
        
        // Observar la lista de usuarios
        userRepository.getUsers().observe(this) { users ->
            userAdapter.submitList(users)
            swipeRefreshLayout.isRefreshing = false
            
            if (users.isEmpty()) {
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
        
        // Cargar usuarios
        refreshUsers()
    }
    
    /**
     * Refresca la lista de usuarios desde la API
     */
    private fun refreshUsers() {
        // Mostrar carga
        progressBar.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
        
        // Obtener ubicación actual del usuario administrador
        val locationId = sessionManager.getLocationId()
        
        // Cargar usuarios
        lifecycleScope.launch {
            userRepository.refreshUsers(locationId)
            runOnUiThread {
                progressBar.visibility = View.GONE
            }
        }
    }
    
    /**
     * Maneja la selección de un usuario
     */
    private fun onUserSelected(user: User) {
        // Guardar el ID del usuario seleccionado
        sessionManager.saveSelectedUserId(user.id)
        
        // Ir a la actividad principal
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}