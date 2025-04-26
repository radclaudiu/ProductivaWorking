package com.productiva.android.activities

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.productiva.android.R
import com.productiva.android.view.SignatureView
import com.productiva.android.viewmodel.SignatureViewModel

/**
 * Actividad para capturar firmas
 */
class SignatureActivity : AppCompatActivity() {
    
    companion object {
        const val EXTRA_SIGNATURE_PATH = "extra_signature_path"
        const val EXTRA_TASK_ID = "extra_task_id"
    }
    
    private lateinit var viewModel: SignatureViewModel
    
    // UI components
    private lateinit var toolbar: Toolbar
    private lateinit var signatureView: SignatureView
    private lateinit var clearButton: Button
    private lateinit var saveButton: Button
    
    // Task ID
    private var taskId: Int = -1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signature)
        
        // Initialize UI components
        toolbar = findViewById(R.id.toolbar)
        signatureView = findViewById(R.id.signature_view)
        clearButton = findViewById(R.id.clear_button)
        saveButton = findViewById(R.id.save_button)
        
        // Setup ActionBar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Firma"
        
        // Get task ID if available
        taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(SignatureViewModel::class.java)
        
        // Set button click listeners
        setupButtons()
        
        // Set observers
        setupObservers()
    }
    
    /**
     * Configura los botones
     */
    private fun setupButtons() {
        // Clear button
        clearButton.setOnClickListener {
            signatureView.clear()
        }
        
        // Save button
        saveButton.setOnClickListener {
            if (signatureView.isEmpty) {
                Toast.makeText(this, "La firma no puede estar vacía", Toast.LENGTH_SHORT).show()
            } else {
                saveSignature()
            }
        }
    }
    
    /**
     * Configura los observadores para el ViewModel
     */
    private fun setupObservers() {
        // Observe signature file path
        viewModel.signatureFilePath.observe(this) { path ->
            if (!path.isNullOrEmpty()) {
                returnResult(path)
            }
        }
        
        // Observe error messages
        viewModel.errorMessage.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                showError(message)
            }
        }
    }
    
    /**
     * Guarda la firma como imagen
     */
    private fun saveSignature() {
        val bitmap = signatureView.getSignatureBitmap()
        if (bitmap != null) {
            val savedFile = viewModel.saveSignature(bitmap, if (taskId != -1) taskId else 0)
            
            if (savedFile == null) {
                showError("Error al guardar la firma")
            }
        } else {
            showError("Error al capturar la firma")
        }
    }
    
    /**
     * Retorna el resultado a la actividad que llamó
     */
    private fun returnResult(signaturePath: String) {
        val resultIntent = Intent().apply {
            putExtra(EXTRA_SIGNATURE_PATH, signaturePath)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
    
    /**
     * Muestra un mensaje de error
     */
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * Muestra diálogo de confirmación al salir sin guardar
     */
    private fun showExitConfirmationDialog() {
        if (!signatureView.isEmpty) {
            AlertDialog.Builder(this)
                .setTitle("Firma sin guardar")
                .setMessage("¿Estás seguro de que quieres salir? La firma no se guardará.")
                .setPositiveButton("Salir") { _, _ -> finish() }
                .setNegativeButton("Cancelar", null)
                .show()
        } else {
            finish()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        showExitConfirmationDialog()
        return true
    }
    
    override fun onBackPressed() {
        showExitConfirmationDialog()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.signature, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear -> {
                signatureView.clear()
                true
            }
            R.id.action_save -> {
                if (signatureView.isEmpty) {
                    Toast.makeText(this, "La firma no puede estar vacía", Toast.LENGTH_SHORT).show()
                } else {
                    saveSignature()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}