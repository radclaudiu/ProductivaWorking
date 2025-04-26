package com.productiva.android.activities

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.productiva.android.R
import com.productiva.android.view.SignatureView
import com.productiva.android.viewmodel.SignatureViewModel

/**
 * Activity para capturar la firma del cliente
 */
class SignatureActivity : AppCompatActivity() {
    
    private lateinit var viewModel: SignatureViewModel
    
    // Componentes de UI
    private lateinit var toolbar: Toolbar
    private lateinit var signatureView: SignatureView
    private lateinit var clearButton: Button
    private lateinit var saveButton: Button
    private lateinit var clientNameEditText: EditText
    private lateinit var progressBar: ProgressBar
    
    // Datos de la tarea
    private var taskId: Int = -1
    private var notes: String? = null
    private var timeSpent: Int? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signature)
        
        // Obtener datos de la tarea
        taskId = intent.getIntExtra("task_id", -1)
        notes = intent.getStringExtra("notes")
        timeSpent = intent.getIntExtra("time_spent", -1).takeIf { it != -1 }
        
        if (taskId == -1) {
            Toast.makeText(this, R.string.error_task_not_found, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        // Inicializar ViewModel
        viewModel = ViewModelProvider(this).get(SignatureViewModel::class.java)
        
        // Configurar información de la tarea
        viewModel.setTaskCompletionData(taskId, notes, timeSpent, null)
        
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.capture_signature)
        
        signatureView = findViewById(R.id.signature_view)
        clearButton = findViewById(R.id.clear_button)
        saveButton = findViewById(R.id.save_button)
        clientNameEditText = findViewById(R.id.client_name_edittext)
        progressBar = findViewById(R.id.progressBar)
        
        // Configurar eventos de botones
        clearButton.setOnClickListener {
            signatureView.clear()
        }
        
        saveButton.setOnClickListener {
            if (signatureView.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_signature, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val clientName = clientNameEditText.text.toString().trim().takeIf { it.isNotEmpty() }
            
            // Actualizar nombre del cliente en ViewModel
            viewModel.setTaskCompletionData(taskId, notes, timeSpent, clientName)
            
            // Obtener bitmap de la firma y guardarlo
            val signatureBitmap = signatureView.getSignatureBitmap()
            viewModel.saveSignatureAndCompleteTask(signatureBitmap)
        }
    }
    
    /**
     * Configura los observadores de LiveData
     */
    private fun setupObservers() {
        // Observar estado de carga
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            saveButton.isEnabled = !isLoading
            clearButton.isEnabled = !isLoading
        }
        
        // Observar mensajes de error
        viewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
        
        // Observar estado de firma
        viewModel.signatureState.observe(this) { state ->
            when (state) {
                is SignatureViewModel.SignatureState.Success -> {
                    Toast.makeText(this, R.string.signature_saved_successfully, Toast.LENGTH_SHORT).show()
                    
                    // Volver a la pantalla de detalles
                    val intent = Intent(this, TaskDetailActivity::class.java)
                    intent.putExtra("task_id", taskId)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    finish()
                }
                is SignatureViewModel.SignatureState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {
                    // No hacer nada con el estado inicial
                }
            }
        }
    }
    
    /**
     * Maneja las selecciones en el menú de opciones
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    // La clase SignatureView se ha movido a un archivo separado en el paquete com.productiva.android.view
}