package com.productiva.android.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.productiva.android.R
import com.productiva.android.adapters.CompletionAdapter
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion
import com.productiva.android.viewmodel.TaskDetailViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Activity para mostrar y completar tareas
 */
class TaskDetailActivity : AppCompatActivity() {
    
    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val REQUEST_CODE_SIGNATURE = 101
        const val REQUEST_CODE_PHOTO = 102
    }
    
    private lateinit var viewModel: TaskDetailViewModel
    
    // UI components
    private lateinit var toolbar: Toolbar
    private lateinit var titleText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var statusChip: Chip
    private lateinit var dueDateText: TextView
    private lateinit var locationText: TextView
    private lateinit var userText: TextView
    private lateinit var completionsRecyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var completionCard: CardView
    private lateinit var notesInput: EditText
    private lateinit var submitButton: Button
    private lateinit var signatureButton: Button
    private lateinit var photoButton: Button
    private lateinit var signaturePreview: ImageView
    private lateinit var photoPreview: ImageView
    private lateinit var emptyCompletionsText: TextView
    
    // Adapter
    private lateinit var completionAdapter: CompletionAdapter
    
    // Date formatter
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)
        
        // Initialize UI components
        toolbar = findViewById(R.id.toolbar)
        titleText = findViewById(R.id.title_text)
        descriptionText = findViewById(R.id.description_text)
        statusChip = findViewById(R.id.status_chip)
        dueDateText = findViewById(R.id.due_date_text)
        locationText = findViewById(R.id.location_text)
        userText = findViewById(R.id.user_text)
        completionsRecyclerView = findViewById(R.id.completions_recycler_view)
        fab = findViewById(R.id.fab)
        completionCard = findViewById(R.id.completion_card)
        notesInput = findViewById(R.id.notes_input)
        submitButton = findViewById(R.id.submit_button)
        signatureButton = findViewById(R.id.signature_button)
        photoButton = findViewById(R.id.photo_button)
        signaturePreview = findViewById(R.id.signature_preview)
        photoPreview = findViewById(R.id.photo_preview)
        emptyCompletionsText = findViewById(R.id.empty_completions_text)
        
        // Setup ActionBar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(TaskDetailViewModel::class.java)
        
        // Set observers
        setupObservers()
        
        // Setup FAB
        setupFab()
        
        // Setup completion buttons
        setupCompletionButtons()
        
        // Load task data
        loadTaskData()
    }
    
    /**
     * Configura el RecyclerView para las completaciones
     */
    private fun setupRecyclerView() {
        completionAdapter = CompletionAdapter()
        completionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@TaskDetailActivity)
            adapter = completionAdapter
        }
    }
    
    /**
     * Configura los observadores para el ViewModel
     */
    private fun setupObservers() {
        // Observe current task
        viewModel.currentTask.observe(this) { task ->
            task?.let {
                updateTaskInfo(it)
            }
        }
        
        // Observe task completions
        viewModel.completions.observe(this) { completions ->
            if (completions.isNullOrEmpty()) {
                emptyCompletionsText.visibility = View.VISIBLE
                completionsRecyclerView.visibility = View.GONE
            } else {
                emptyCompletionsText.visibility = View.GONE
                completionsRecyclerView.visibility = View.VISIBLE
                completionAdapter.submitList(completions)
            }
        }
        
        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                // Show loading
            } else {
                // Hide loading
            }
        }
        
        // Observe error messages
        viewModel.errorMessage.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                showError(message)
            }
        }
        
        // Observe completion success
        viewModel.completionSuccess.observe(this) { success ->
            if (success) {
                hideCompletionCard()
                Toast.makeText(this, "Tarea completada correctamente", Toast.LENGTH_SHORT).show()
                viewModel.clearCompletionState()
            }
        }
        
        // Observe signature file path
        viewModel.signatureFilePath.observe(this) { path ->
            if (!path.isNullOrEmpty()) {
                showSignaturePreview(path)
            }
        }
        
        // Observe photo file path
        viewModel.photoFilePath.observe(this) { path ->
            if (!path.isNullOrEmpty()) {
                showPhotoPreview(path)
            }
        }
    }
    
    /**
     * Configura el FAB para mostrar/ocultar la tarjeta de completación
     */
    private fun setupFab() {
        fab.setOnClickListener {
            toggleCompletionCard()
        }
    }
    
    /**
     * Configura los botones de completación
     */
    private fun setupCompletionButtons() {
        // Submit button
        submitButton.setOnClickListener {
            submitCompletion()
        }
        
        // Signature button
        signatureButton.setOnClickListener {
            captureSignature()
        }
        
        // Photo button
        photoButton.setOnClickListener {
            capturePhoto()
        }
    }
    
    /**
     * Carga los datos de la tarea
     */
    private fun loadTaskData() {
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
        if (taskId != -1) {
            viewModel.loadTask(taskId)
        } else {
            showError("ID de tarea no válido")
            finish()
        }
    }
    
    /**
     * Actualiza la información de la tarea en la UI
     */
    private fun updateTaskInfo(task: Task) {
        titleText.text = task.title
        
        // Descripción
        if (!task.description.isNullOrEmpty()) {
            descriptionText.visibility = View.VISIBLE
            descriptionText.text = task.description
        } else {
            descriptionText.visibility = View.GONE
        }
        
        // Estado
        statusChip.text = getStatusDisplayName(task.status)
        statusChip.setChipBackgroundColorResource(getStatusColorResource(task.status))
        
        // Fecha de vencimiento
        if (task.dueDate != null) {
            dueDateText.visibility = View.VISIBLE
            dueDateText.text = "Vencimiento: ${dateFormat.format(task.dueDate)}"
        } else {
            dueDateText.visibility = View.GONE
        }
        
        // Ubicación
        if (!task.locationName.isNullOrEmpty()) {
            locationText.visibility = View.VISIBLE
            locationText.text = "Ubicación: ${task.locationName}"
        } else {
            locationText.visibility = View.GONE
        }
        
        // Usuario asignado
        if (!task.userName.isNullOrEmpty()) {
            userText.visibility = View.VISIBLE
            userText.text = "Asignada a: ${task.userName}"
        } else {
            userText.visibility = View.GONE
        }
        
        // Actualizar título
        supportActionBar?.title = "Tarea: ${task.title}"
        
        // Ocultar FAB si la tarea está completada o cancelada
        if (task.isCompleted() || task.isCancelled()) {
            fab.visibility = View.GONE
        } else {
            fab.visibility = View.VISIBLE
        }
    }
    
    /**
     * Muestra/oculta la tarjeta de completación
     */
    private fun toggleCompletionCard() {
        if (completionCard.visibility == View.VISIBLE) {
            hideCompletionCard()
        } else {
            showCompletionCard()
        }
    }
    
    /**
     * Muestra la tarjeta de completación
     */
    private fun showCompletionCard() {
        completionCard.visibility = View.VISIBLE
        fab.setImageResource(R.drawable.ic_close)
    }
    
    /**
     * Oculta la tarjeta de completación
     */
    private fun hideCompletionCard() {
        completionCard.visibility = View.GONE
        fab.setImageResource(R.drawable.ic_add)
        clearCompletionInputs()
    }
    
    /**
     * Limpia los inputs de completación
     */
    private fun clearCompletionInputs() {
        notesInput.setText("")
        signaturePreview.visibility = View.GONE
        photoPreview.visibility = View.GONE
    }
    
    /**
     * Inicia la actividad para capturar firma
     */
    private fun captureSignature() {
        val intent = Intent(this, SignatureActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_SIGNATURE)
    }
    
    /**
     * Inicia la actividad para capturar foto
     */
    private fun capturePhoto() {
        // TODO: Implementar captura de foto
        Toast.makeText(this, "Captura de foto no implementada", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Muestra la previsualización de la firma
     */
    private fun showSignaturePreview(path: String) {
        val signatureFile = File(path)
        if (signatureFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(signatureFile.absolutePath)
            signaturePreview.setImageBitmap(bitmap)
            signaturePreview.visibility = View.VISIBLE
        }
    }
    
    /**
     * Muestra la previsualización de la foto
     */
    private fun showPhotoPreview(path: String) {
        val photoFile = File(path)
        if (photoFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            photoPreview.setImageBitmap(bitmap)
            photoPreview.visibility = View.VISIBLE
        }
    }
    
    /**
     * Envía la completación de la tarea
     */
    private fun submitCompletion() {
        val notes = notesInput.text.toString()
        
        // Obtener el ID del usuario actual (mockup por ahora)
        val currentUserId = 1 // TODO: Obtener del usuario real logueado
        
        val signatureFile = viewModel.signatureFilePath.value?.let { File(it) }
        val photoFile = viewModel.photoFilePath.value?.let { File(it) }
        
        when {
            signatureFile != null -> {
                viewModel.createCompletionWithSignature(notes, currentUserId, signatureFile)
            }
            photoFile != null -> {
                viewModel.createCompletionWithPhoto(notes, currentUserId, photoFile)
            }
            else -> {
                viewModel.createCompletion(notes, currentUserId)
            }
        }
    }
    
    /**
     * Obtiene el nombre de visualización del estado
     */
    private fun getStatusDisplayName(status: String): String {
        return when (status) {
            Task.STATUS_PENDING -> "Pendiente"
            Task.STATUS_IN_PROGRESS -> "En Progreso"
            Task.STATUS_COMPLETED -> "Completada"
            Task.STATUS_CANCELLED -> "Cancelada"
            else -> status
        }
    }
    
    /**
     * Obtiene el recurso de color según el estado
     */
    private fun getStatusColorResource(status: String): Int {
        return when (status) {
            Task.STATUS_PENDING -> R.color.status_pending
            Task.STATUS_IN_PROGRESS -> R.color.status_in_progress
            Task.STATUS_COMPLETED -> R.color.status_completed
            Task.STATUS_CANCELLED -> R.color.status_cancelled
            else -> R.color.status_pending
        }
    }
    
    /**
     * Muestra un mensaje de error
     */
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * Muestra un diálogo para cambiar el estado de la tarea
     */
    private fun showChangeStatusDialog() {
        val statuses = arrayOf(
            "Pendiente",
            "En Progreso",
            "Completada",
            "Cancelada"
        )
        
        val statusValues = arrayOf(
            Task.STATUS_PENDING,
            Task.STATUS_IN_PROGRESS,
            Task.STATUS_COMPLETED,
            Task.STATUS_CANCELLED
        )
        
        AlertDialog.Builder(this)
            .setTitle("Cambiar estado")
            .setItems(statuses) { _, which ->
                viewModel.updateTaskStatus(statusValues[which])
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.task_detail, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_change_status -> {
                showChangeStatusDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_SIGNATURE -> {
                    data?.getStringExtra(SignatureActivity.EXTRA_SIGNATURE_PATH)?.let { path ->
                        viewModel.setSignatureFilePath(path)
                    }
                }
                REQUEST_CODE_PHOTO -> {
                    data?.getStringExtra("photo_path")?.let { path ->
                        viewModel.setPhotoFilePath(path)
                    }
                }
            }
        }
    }
}