package com.productiva.android.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.productiva.android.ProductivaApplication
import com.productiva.android.R
import com.productiva.android.adapters.TaskAdapter
import com.productiva.android.models.Task
import com.productiva.android.models.User
import kotlinx.coroutines.launch

/**
 * Actividad principal que muestra la lista de tareas
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var userInfoText: TextView
    private lateinit var syncButton: FloatingActionButton
    
    private val taskRepository by lazy {
        (application as ProductivaApplication).taskRepository
    }
    
    private val userRepository by lazy {
        (application as ProductivaApplication).userRepository
    }
    
    private val sessionManager by lazy {
        (application as ProductivaApplication).sessionManager
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Inicializar views
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerViewTasks)
        progressBar = findViewById(R.id.progressBar)
        emptyView = findViewById(R.id.textViewEmpty)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        userInfoText = findViewById(R.id.textViewUserInfo)
        syncButton = findViewById(R.id.fabSync)
        
        // Configurar toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Tareas Pendientes"
        
        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Configurar el adaptador con listener de clics
        val taskAdapter = TaskAdapter { task ->
            onTaskSelected(task)
        }
        recyclerView.adapter = taskAdapter
        
        // Configurar SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            refreshTasks()
        }
        
        // Botón de sincronización
        syncButton.setOnClickListener {
            syncPendingTasks()
        }
        
        // Mostrar información del usuario seleccionado
        val selectedUserId = sessionManager.getSelectedUserId()
        if (selectedUserId != -1) {
            userRepository.getUserById(selectedUserId).observe(this) { user ->
                if (user != null) {
                    displayUserInfo(user)
                }
            }
        }
        
        // Observar la lista de tareas
        val locationId = sessionManager.getLocationId()
        if (locationId != -1) {
            taskRepository.getTasksByLocation(locationId).observe(this) { tasks ->
                val pendingTasks = tasks.filter { it.status.lowercase() != "completed" }
                taskAdapter.submitList(pendingTasks)
                swipeRefreshLayout.isRefreshing = false
                
                if (pendingTasks.isEmpty()) {
                    emptyView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
        }
        
        // Cargar tareas
        refreshTasks()
    }
    
    /**
     * Muestra la información del usuario seleccionado
     */
    private fun displayUserInfo(user: User) {
        userInfoText.text = "Usuario: ${user.name ?: user.username}"
    }
    
    /**
     * Refresca la lista de tareas desde la API
     */
    private fun refreshTasks() {
        // Mostrar carga
        progressBar.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
        
        // Obtener ubicación actual
        val locationId = sessionManager.getLocationId()
        val token = sessionManager.getAuthToken() ?: return
        
        // Cargar tareas
        lifecycleScope.launch {
            taskRepository.refreshTasks(token, locationId)
            runOnUiThread {
                progressBar.visibility = View.GONE
            }
        }
    }
    
    /**
     * Sincroniza tareas pendientes
     */
    private fun syncPendingTasks() {
        val token = sessionManager.getAuthToken() ?: return
        
        // Mostrar progreso
        progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            taskRepository.syncPendingCompletions(token)
            runOnUiThread {
                progressBar.visibility = View.GONE
                // Opcional: mostrar mensaje de éxito
            }
        }
    }
    
    /**
     * Maneja la selección de una tarea
     */
    private fun onTaskSelected(task: Task) {
        val intent = Intent(this, TaskDetailActivity::class.java)
        intent.putExtra("task_id", task.id)
        startActivity(intent)
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            R.id.action_change_user -> {
                changeUser()
                true
            }
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    /**
     * Cierra la sesión del usuario
     */
    private fun logout() {
        lifecycleScope.launch {
            userRepository.logout()
            runOnUiThread {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
    
    /**
     * Cambia el usuario seleccionado
     */
    private fun changeUser() {
        // Borra solo el usuario seleccionado, mantiene la autenticación
        sessionManager.saveSelectedUserId(-1)
        
        val intent = Intent(this, UserSelectionActivity::class.java)
        startActivity(intent)
        finish()
    }
}