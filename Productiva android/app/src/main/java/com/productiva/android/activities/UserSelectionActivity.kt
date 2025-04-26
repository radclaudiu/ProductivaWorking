package com.productiva.android.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.productiva.android.R
import com.productiva.android.adapters.UserAdapter
import com.productiva.android.model.User
import com.productiva.android.viewmodel.UserSelectionViewModel

/**
 * Activity para la selección de usuario
 */
class UserSelectionActivity : AppCompatActivity(), UserAdapter.OnUserClickListener {
    
    private lateinit var viewModel: UserSelectionViewModel
    
    // UI components
    private lateinit var toolbar: Toolbar
    private lateinit var titleText: TextView
    private lateinit var searchInput: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    
    // Adapter
    private lateinit var userAdapter: UserAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_selection)
        
        // Initialize UI components
        toolbar = findViewById(R.id.toolbar)
        titleText = findViewById(R.id.title_text)
        searchInput = findViewById(R.id.search_input)
        recyclerView = findViewById(R.id.recycler_view)
        progressBar = findViewById(R.id.progress_bar)
        emptyView = findViewById(R.id.empty_view)
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        
        // Setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(UserSelectionViewModel::class.java)
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Setup SwipeRefreshLayout
        setupSwipeRefreshLayout()
        
        // Setup search
        setupSearch()
        
        // Set observers
        setupObservers()
    }
    
    /**
     * Configura el RecyclerView para mostrar usuarios
     */
    private fun setupRecyclerView() {
        userAdapter = UserAdapter(this)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@UserSelectionActivity)
            adapter = userAdapter
        }
    }
    
    /**
     * Configura el SwipeRefreshLayout para actualizar la lista
     */
    private fun setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshUsers()
        }
    }
    
    /**
     * Configura la búsqueda de usuarios
     */
    private fun setupSearch() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                viewModel.setSearchQuery(s.toString())
            }
        })
    }
    
    /**
     * Configura los observadores para el ViewModel
     */
    private fun setupObservers() {
        // Observe users list
        viewModel.filteredUsers.observe(this) { users ->
            if (users.isNullOrEmpty()) {
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                userAdapter.submitList(users)
            }
        }
        
        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false
            }
        }
        
        // Observe error messages
        viewModel.errorMessage.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                showError(message)
            }
        }
        
        // Observe selected user
        viewModel.selectedUser.observe(this) { user ->
            user?.let {
                navigateToMainActivity(it)
            }
        }
    }
    
    /**
     * Manejador para cuando se hace clic en un usuario
     */
    override fun onUserClick(user: User) {
        viewModel.selectUser(user)
    }
    
    /**
     * Navega a la actividad principal con el usuario seleccionado
     */
    private fun navigateToMainActivity(user: User) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_USER_ID, user.id)
        }
        startActivity(intent)
    }
    
    /**
     * Muestra un mensaje de error
     */
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}