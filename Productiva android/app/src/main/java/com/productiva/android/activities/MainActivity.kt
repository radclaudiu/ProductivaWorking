package com.productiva.android.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.productiva.android.R
import com.productiva.android.adapters.TaskAdapter
import com.productiva.android.model.Task
import com.productiva.android.model.User
import com.productiva.android.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Activity principal que muestra la lista de tareas
 */
class MainActivity : AppCompatActivity(), TaskAdapter.OnTaskClickListener, NavigationView.OnNavigationItemSelectedListener {
    
    private lateinit var viewModel: MainViewModel
    
    // Componentes de UI
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: TextView
    private lateinit var fab: FloatingActionButton
    private lateinit var searchView: SearchView
    
    // Adaptador para la lista de tareas
    private lateinit var taskAdapter: TaskAdapter
    
    // Usuario actual
    private var currentUser: User? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Inicializar ViewModel
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        
        // Configurar componentes de UI
        setupUI()
        
        // Configurar observadores
        setupObservers()
    }
    
    /**
     * Configura las referencias y eventos de UI
     */
    private fun setupUI() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        recyclerView = findViewById(R.id.tasksRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        emptyView = findViewById(R.id.emptyView)
        fab = findViewById(R.id.fab)
        
        // Configurar drawer
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        
        navigationView.setNavigationItemSelectedListener(this)
        
        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(this)
        recyclerView.adapter = taskAdapter
        
        // Configurar SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.syncTasks()
        }
        
        // Configurar FAB
        fab.setOnClickListener {
            goToScannerOrCamera()
        }
    }
    
    /**
     * Configura los observadores de LiveData
     */
    private fun setupObservers() {
        // Observar estado de carga
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading && !swipeRefreshLayout.isRefreshing) View.VISIBLE else View.GONE
            if (!isLoading) {
                swipeRefreshLayout.isRefreshing = false
            }
        }
        
        // Observar mensajes de error
        viewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
        
        // Observar tareas filtradas
        viewModel.filteredTasks.observe(this) { tasks ->
            updateTaskList(tasks)
        }
        
        // Observar usuario actual
        lifecycleScope.launch {
            viewModel.currentUser.collect { user ->
                currentUser = user
                updateUserInformation(user)
            }
        }
    }
    
    /**
     * Actualiza la lista de tareas en el adaptador
     */
    private fun updateTaskList(tasks: List<Task>) {
        taskAdapter.submitList(tasks)
        
        // Mostrar vista vacía si no hay tareas
        if (tasks.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
    
    /**
     * Actualiza la información del usuario en el drawer
     */
    private fun updateUserInformation(user: User?) {
        val headerView = navigationView.getHeaderView(0)
        val nameTextView = headerView.findViewById<TextView>(R.id.user_name)
        val emailTextView = headerView.findViewById<TextView>(R.id.user_email)
        val companyTextView = headerView.findViewById<TextView>(R.id.user_company)
        
        user?.let {
            nameTextView.text = user.name
            emailTextView.text = user.email
            companyTextView.text = user.companyName
            
            // Actualizar título de la toolbar
            title = getString(R.string.app_name)
        }
    }
    
    /**
     * Navega a la actividad de escaneo o cámara
     */
    private fun goToScannerOrCamera() {
        // Implementación pendiente - podría ser para escanear códigos QR/barcode o tomar fotos
        Toast.makeText(this, "Funcionalidad de escáner pendiente", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Maneja el clic en una tarea
     */
    override fun onTaskClick(task: Task) {
        val intent = Intent(this, TaskDetailActivity::class.java)
        intent.putExtra("task_id", task.id)
        startActivity(intent)
    }
    
    /**
     * Infla el menú de opciones
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        
        // Configurar SearchView
        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView
        
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.setSearchQuery(query ?: "")
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
        
        return true
    }
    
    /**
     * Maneja las selecciones en el menú de opciones
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sync -> {
                viewModel.syncTasks()
                true
            }
            R.id.action_filter_all -> {
                viewModel.setTaskFilter(MainViewModel.TaskFilter.ALL)
                true
            }
            R.id.action_filter_pending -> {
                viewModel.setTaskFilter(MainViewModel.TaskFilter.PENDING)
                true
            }
            R.id.action_filter_in_progress -> {
                viewModel.setTaskFilter(MainViewModel.TaskFilter.IN_PROGRESS)
                true
            }
            R.id.action_filter_completed -> {
                viewModel.setTaskFilter(MainViewModel.TaskFilter.COMPLETED)
                true
            }
            R.id.action_filter_today -> {
                viewModel.setTaskFilter(MainViewModel.TaskFilter.TODAY)
                true
            }
            R.id.action_filter_overdue -> {
                viewModel.setTaskFilter(MainViewModel.TaskFilter.OVERDUE)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    /**
     * Maneja las selecciones en el drawer
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_tasks -> {
                // Ya estamos en la pantalla de tareas
            }
            R.id.nav_print_label -> {
                // Ir a la pantalla de impresión de etiquetas
                val intent = Intent(this, PrintLabelActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_printers -> {
                // Ir a la pantalla de gestión de impresoras
                val intent = Intent(this, PrinterSettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_templates -> {
                // Ir a la pantalla de gestión de plantillas
                val intent = Intent(this, LabelTemplatesActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_change_user -> {
                // Volver a la pantalla de selección de usuario
                val intent = Intent(this, UserSelectionActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_logout -> {
                // Cerrar sesión y volver a login
                viewModel.logout()
                val intent = Intent(this, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    
    /**
     * Comportamiento al pulsar el botón Atrás
     */
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}