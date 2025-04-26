package com.productiva.android.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.productiva.android.R
import com.productiva.android.adapters.UserAdapter
import com.productiva.android.model.User
import com.productiva.android.viewmodel.UserSelectionViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Activity para seleccionar un usuario para trabajar
 */
class UserSelectionActivity : AppCompatActivity(), UserAdapter.OnUserClickListener {
    
    private lateinit var viewModel: UserSelectionViewModel
    
    // Componentes de UI
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var searchView: SearchView
    private lateinit var companySpinner: Spinner
    
    // Adaptador para la lista de usuarios
    private lateinit var userAdapter: UserAdapter
    
    // Lista de empresas para el spinner
    private val companies = mutableListOf<Pair<Int, String>>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_selection)
        
        // Configurar toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.select_user)
        
        // Inicializar ViewModel
        viewModel = ViewModelProvider(this).get(UserSelectionViewModel::class.java)
        
        // Configurar componentes de UI
        setupUI()
        
        // Configurar observadores
        setupObservers()
        
        // Cargar usuarios
        viewModel.loadUsers()
    }
    
    /**
     * Configura las referencias y eventos de UI
     */
    private fun setupUI() {
        recyclerView = findViewById(R.id.usersRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        emptyView = findViewById(R.id.emptyView)
        searchView = findViewById(R.id.searchView)
        companySpinner = findViewById(R.id.companySpinner)
        
        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(this)
        recyclerView.adapter = userAdapter
        
        // Configurar SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterUsers(query ?: "")
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                filterUsers(newText ?: "")
                return true
            }
        })
        
        // Configurar Spinner de empresas
        val spinnerAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            mutableListOf("Todas las empresas")
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        companySpinner.adapter = spinnerAdapter
        
        companySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    // Opción "Todas las empresas"
                    viewModel.loadUsers()
                } else {
                    // Filtrar por empresa seleccionada
                    val companyId = companies[position - 1].first
                    viewModel.loadUsersByCompany(companyId)
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }
    }
    
    /**
     * Configura los observadores de LiveData
     */
    private fun setupObservers() {
        // Observar estado de carga
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // Observar mensajes de error
        viewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
        
        // Observar lista de usuarios
        viewModel.users.observe(this) { userList ->
            updateUserList(userList)
            updateCompaniesSpinner(userList)
        }
        
        // Observar usuario seleccionado
        lifecycleScope.launch {
            viewModel.selectedUser.collect { user ->
                user?.let {
                    // Navegar a la pantalla principal
                    val intent = Intent(this@UserSelectionActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
    
    /**
     * Actualiza la lista de usuarios en el adaptador
     */
    private fun updateUserList(users: List<User>) {
        userAdapter.submitList(users)
        
        // Mostrar vista vacía si no hay usuarios
        if (users.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
        
        // Comprobar si hay un usuario seleccionado previamente
        val lastSelectedUserId = viewModel.getLastSelectedUserId()
        if (lastSelectedUserId != -1) {
            val user = users.find { it.id == lastSelectedUserId }
            if (user != null) {
                // Si encontramos el usuario, seleccionarlo automáticamente
                viewModel.selectUser(user)
            }
        }
    }
    
    /**
     * Actualiza el spinner de empresas basado en los usuarios disponibles
     */
    private fun updateCompaniesSpinner(users: List<User>) {
        // Extraer empresas únicas
        val uniqueCompanies = users
            .map { Pair(it.companyId, it.companyName) }
            .distinctBy { it.first }
            .sortedBy { it.second }
        
        companies.clear()
        companies.addAll(uniqueCompanies)
        
        // Actualizar adaptador del spinner
        val companyNames = mutableListOf("Todas las empresas")
        companyNames.addAll(companies.map { it.second })
        
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            companyNames
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        companySpinner.adapter = spinnerAdapter
    }
    
    /**
     * Filtra los usuarios por texto de búsqueda
     */
    private fun filterUsers(query: String) {
        val filteredUsers = viewModel.filterUsers(query)
        userAdapter.submitList(filteredUsers)
        
        // Mostrar vista vacía si no hay resultados
        if (filteredUsers.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            emptyView.text = getString(R.string.no_results_found)
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
    
    /**
     * Maneja el clic en un usuario
     */
    override fun onUserClick(user: User) {
        viewModel.selectUser(user)
    }
    
    /**
     * Infla el menú de opciones
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.user_selection_menu, menu)
        return true
    }
    
    /**
     * Maneja las selecciones en el menú de opciones
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Volver a la pantalla de login
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            R.id.action_sync -> {
                // Sincronizar usuarios
                viewModel.syncUsers()
                true
            }
            R.id.action_logout -> {
                // Cerrar sesión y volver a la pantalla de login
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}