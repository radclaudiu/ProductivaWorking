package com.productiva.android.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.productiva.android.R
import com.productiva.android.adapters.TaskAdapter
import com.productiva.android.model.Task
import com.productiva.android.model.User
import com.productiva.android.viewmodel.MainViewModel

/**
 * Actividad principal de la aplicación
 */
class MainActivity : AppCompatActivity(), TaskAdapter.OnTaskClickListener,
    NavigationView.OnNavigationItemSelectedListener {
    
    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
    }
    
    private lateinit var viewModel: MainViewModel
    
    // UI components
    private lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var fab: FloatingActionButton
    
    // Navigation header
    private lateinit var userNameText: TextView
    private lateinit var userEmailText: TextView
    
    // Adapter
    private lateinit var taskAdapter: TaskAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize UI components
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        recyclerView = findViewById(R.id.recycler_view)
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        emptyView = findViewById(R.id.empty_view)
        tabLayout = findViewById(R.id.tab_layout)
        fab = findViewById(R.id.fab)
        
        // Setup ActionBar
        setSupportActionBar(toolbar)
        
        // Setup Navigation Drawer
        setupNavigationDrawer()
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Setup SwipeRefreshLayout
        setupSwipeRefreshLayout()
        
        // Setup TabLayout for task status filtering
        setupTabLayout()
        
        // Setup FAB
        setupFab()
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        
        // Set observers
        setupObservers()
        
        // Load user data
        loadUserData()
    }
    
    /**
     * Configura el Navigation Drawer
     */
    private fun setupNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        
        navigationView.setNavigationItemSelectedListener(this)
        
        // Inicializar vistas del header del navigation drawer
        val headerView = navigationView.getHeaderView(0)
        userNameText = headerView.findViewById(R.id.user_name_text)
        userEmailText = headerView.findViewById(R.id.user_email_text)
    }
    
    /**
     * Configura el RecyclerView
     */
    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(this)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = taskAdapter
        }
    }
    
    /**
     * Configura el SwipeRefreshLayout
     */
    private fun setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshTasks()
        }
    }
    
    /**
     * Configura el TabLayout para filtrar tareas por estado
     */
    private fun setupTabLayout() {
        // Añadir pestañas para cada estado de tarea
        tabLayout.addTab(tabLayout.newTab().setText("Pendientes"))
        tabLayout.addTab(tabLayout.newTab().setText("En Progreso"))
        tabLayout.addTab(tabLayout.newTab().setText("Completadas"))
        
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> viewModel.setStatusFilter(Task.STATUS_PENDING)
                    1 -> viewModel.setStatusFilter(Task.STATUS_IN_PROGRESS)
                    2 -> viewModel.setStatusFilter(Task.STATUS_COMPLETED)
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
    
    /**
     * Configura el Floating Action Button
     */
    private fun setupFab() {
        fab.setOnClickListener {
            // Por ahora, mostrar mensaje
            Toast.makeText(this, "Función para crear tarea no implementada", Toast.LENGTH_SHORT).show()
            
            // TODO: Implementar creación de tareas
            // val intent = Intent(this, CreateTaskActivity::class.java)
            // startActivity(intent)
        }
    }
    
    /**
     * Configura los observadores para el ViewModel
     */
    private fun setupObservers() {
        // Observe current user
        viewModel.currentUser.observe(this) { user ->
            user?.let {
                updateUserInfo(it)
            }
        }
        
        // Observe tasks
        viewModel.tasks.observe(this) { tasks ->
            if (tasks.isNullOrEmpty()) {
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                taskAdapter.submitList(tasks)
            }
        }
        
        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            swipeRefreshLayout.isRefreshing = isLoading
        }
        
        // Observe error messages
        viewModel.errorMessage.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                showError(message)
            }
        }
        
        // Observe selected task
        viewModel.selectedTask.observe(this) { task ->
            task?.let {
                navigateToTaskDetail(it)
                viewModel.clearSelectedTask()
            }
        }
        
        // Observe current status filter
        viewModel.currentStatusFilter.observe(this) { status ->
            when (status) {
                Task.STATUS_PENDING -> tabLayout.getTabAt(0)?.select()
                Task.STATUS_IN_PROGRESS -> tabLayout.getTabAt(1)?.select()
                Task.STATUS_COMPLETED -> tabLayout.getTabAt(2)?.select()
            }
        }
    }
    
    /**
     * Actualiza la información del usuario en el drawer
     */
    private fun updateUserInfo(user: User) {
        userNameText.text = user.name
        userEmailText.text = user.email
        
        // Actualizar título de la toolbar
        supportActionBar?.title = "Tareas de ${user.name}"
    }
    
    /**
     * Carga los datos del usuario
     */
    private fun loadUserData() {
        val userId = intent.getIntExtra(EXTRA_USER_ID, -1)
        if (userId != -1) {
            viewModel.loadUserById(userId)
        } else {
            // Si no hay ID de usuario, intentar obtener usuario actual
            viewModel.loadCurrentUser()
        }
    }
    
    /**
     * Navega a la pantalla de detalle de tarea
     */
    private fun navigateToTaskDetail(task: Task) {
        val intent = Intent(this, TaskDetailActivity::class.java).apply {
            putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.id)
        }
        startActivity(intent)
    }
    
    /**
     * Muestra un mensaje de error
     */
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * Maneja los clics en los elementos del menú del drawer
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_tasks -> {
                // Ya estamos en la pantalla de tareas
            }
            R.id.nav_print -> {
                val intent = Intent(this, PrintLabelActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_settings -> {
                Toast.makeText(this, "Configuración no implementada", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_logout -> {
                logout()
            }
        }
        
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    
    /**
     * Cierra la sesión del usuario
     */
    private fun logout() {
        viewModel.logout()
        
        // Navegar a LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
    
    /**
     * Maneja los clics en las tareas
     */
    override fun onTaskClick(task: Task) {
        viewModel.selectTask(task)
    }
    
    /**
     * Maneja los cambios de estado de las tareas
     */
    override fun onTaskStatusChange(task: Task, newStatus: String) {
        viewModel.updateTaskStatus(task.id, newStatus)
    }
    
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                viewModel.refreshTasks()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}