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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.productiva.android.ProductivaApplication
import com.productiva.android.R
import com.productiva.android.adapters.TaskListAdapter
import com.productiva.android.model.Task
import com.productiva.android.model.User
import com.productiva.android.repository.TaskRepository
import com.productiva.android.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * Actividad principal que muestra la lista de tareas del usuario
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var textViewUserInfo: TextView
    private lateinit var recyclerViewTasks: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewEmpty: TextView
    private lateinit var fabSync: FloatingActionButton
    private lateinit var taskListAdapter: TaskListAdapter
    
    private lateinit var taskRepository: TaskRepository
    private lateinit var userRepository: UserRepository
    private lateinit var app: ProductivaApplication
    
    private var currentUser: User? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Obtener instancia de la aplicación
        app = application as ProductivaApplication
        
        // Inicializar repositorios
        taskRepository = app.taskRepository
        userRepository = UserRepository(
            apiService = app.apiService,
            userDao = app.database.userDao(),
            context = this
        )
        
        // Inicializar vistas
        toolbar = findViewById(R.id.toolbar)
        textViewUserInfo = findViewById(R.id.textViewUserInfo)
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        progressBar = findViewById(R.id.progressBar)
        textViewEmpty = findViewById(R.id.textViewEmpty)
        fabSync = findViewById(R.id.fabSync)
        
        // Configurar toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Mis Tareas"
        
        // Configurar RecyclerView
        recyclerViewTasks.layoutManager = LinearLayoutManager(this)
        taskListAdapter = TaskListAdapter { task ->
            onTaskSelected(task)
        }
        recyclerViewTasks.adapter = taskListAdapter
        
        // Configurar SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            loadTasks()
        }
        
        // Configurar FAB de sincronización
        fabSync.setOnClickListener {
            synchronizeTasks()
        }
        
        // Verificar sesión
        checkActiveSession()
        
        // Cargar información del usuario y tareas
        loadUserInfo()
        loadTasks()
    }
    
    /**
     * Verifica si hay una sesión activa
     */
    private fun checkActiveSession() {
        if (!app.sessionManager.isLoggedIn() || !app.sessionManager.isUserSelected()) {
            // No hay sesión activa o no hay usuario seleccionado, volver a login
            goToLogin()
            return
        }
    }
    
    /**
     * Carga la información del usuario seleccionado
     */
    private fun loadUserInfo() {
        val userId = app.sessionManager.getSelectedUserId()
        if (userId == -1) {
            goToLogin()
            return
        }
        
        val userDao = app.database.userDao()
        userDao.getUserById(userId).observe(this) { user ->
            if (user != null) {
                currentUser = user
                textViewUserInfo.text = "Usuario: ${user.name ?: user.username}"
            } else {
                goToLogin()
            }
        }
    }
    
    /**
     * Carga las tareas del usuario
     */
    private fun loadTasks() {
        val token = app.sessionManager.getAuthToken()
        val locationId = app.sessionManager.getLocationId()
        
        if (token == null) {
            goToLogin()
            return
        }
        
        setLoading(true)
        
        lifecycleScope.launch {
            try {
                val result = taskRepository.refreshTasks(token, locationId)
                
                if (result.isSuccess) {
                    val tasks = result.getOrNull() ?: emptyList()
                    
                    if (tasks.isEmpty()) {
                        showEmptyState("No hay tareas disponibles")
                    } else {
                        hideEmptyState()
                        taskListAdapter.updateTaskList(tasks)
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
     * Sincroniza los completados de tareas pendientes
     */
    private fun synchronizeTasks() {
        val token = app.sessionManager.getAuthToken()
        
        if (token == null) {
            goToLogin()
            return
        }
        
        setLoading(true)
        
        lifecycleScope.launch {
            try {
                val result = taskRepository.syncPendingTaskCompletions(token)
                
                if (result.isSuccess) {
                    val count = result.getOrNull() ?: 0
                    Toast.makeText(
                        this@MainActivity,
                        "Sincronización completada: $count tareas actualizadas",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Recargar tareas
                    loadTasks()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Error de sincronización: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
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
    
    /**
     * Muestra el estado vacío con un mensaje
     */
    private fun showEmptyState(message: String) {
        textViewEmpty.text = message
        textViewEmpty.visibility = View.VISIBLE
        recyclerViewTasks.visibility = View.GONE
    }
    
    /**
     * Oculta el estado vacío
     */
    private fun hideEmptyState() {
        textViewEmpty.visibility = View.GONE
        recyclerViewTasks.visibility = View.VISIBLE
    }
    
    /**
     * Actualiza la UI para mostrar/ocultar indicadores de carga
     */
    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        fabSync.isEnabled = !loading
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
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                confirmLogout()
                true
            }
            R.id.action_change_user -> {
                changeUser()
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
    
    /**
     * Cambia de usuario sin cerrar la sesión
     */
    private fun changeUser() {
        // Solo limpia la selección de usuario pero mantiene la sesión
        app.sessionManager.saveSelectedUserId(-1)
        app.sessionManager.saveLocationId(-1)
        
        val intent = Intent(this, UserSelectionActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    override fun onResume() {
        super.onResume()
        // Recargar tareas al volver a la actividad
        loadTasks()
    }
}