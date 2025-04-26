package com.productiva.android.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.productiva.android.R
import com.productiva.android.adapters.CompletionAdapter
import com.productiva.android.model.Task
import com.productiva.android.viewmodel.TaskDetailViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Activity para mostrar el detalle de una tarea y permitir completarla
 */
class TaskDetailActivity : AppCompatActivity() {
    
    private lateinit var viewModel: TaskDetailViewModel
    
    // Componentes de UI
    private lateinit var toolbar: Toolbar
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var statusTextView: TextView
    private lateinit var dueDateTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var priorityTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var completionsRecyclerView: RecyclerView
    private lateinit var completeButton: Button
    private lateinit var startButton: Button
    
    // Adaptador para la lista de completaciones
    private lateinit var completionAdapter: CompletionAdapter
    
    // ID de la tarea
    private var taskId: Int = -1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)
        
        // Obtener ID de la tarea
        taskId = intent.getIntExtra("task_id", -1)
        
        if (taskId == -1) {
            Toast.makeText(this, R.string.error_task_not_found, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        // Inicializar ViewModel
        viewModel = ViewModelProvider(this).get(TaskDetailViewModel::class.java)
        
        // Configurar componentes de UI
        setupUI()
        
        // Configurar observadores
        setupObservers()
        
        // Cargar tarea
        viewModel.loadTask(taskId)
    }
    
    /**
     * Configura las referencias y eventos de UI
     */
    private fun setupUI() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.task_details)
        
        titleTextView = findViewById(R.id.task_title)
        descriptionTextView = findViewById(R.id.task_description)
        statusTextView = findViewById(R.id.task_status)
        dueDateTextView = findViewById(R.id.task_due_date)
        locationTextView = findViewById(R.id.task_location)
        priorityTextView = findViewById(R.id.task_priority)
        progressBar = findViewById(R.id.progressBar)
        completionsRecyclerView = findViewById(R.id.completions_recyclerview)
        completeButton = findViewById(R.id.complete_button)
        startButton = findViewById(R.id.start_button)
        
        // Configurar RecyclerView
        completionsRecyclerView.layoutManager = LinearLayoutManager(this)
        completionAdapter = CompletionAdapter()
        completionsRecyclerView.adapter = completionAdapter
        
        // Configurar eventos de botones
        completeButton.setOnClickListener {
            showCompletionDialog()
        }
        
        startButton.setOnClickListener {
            viewModel.startTask()
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
        
        // Observar tarea
        lifecycleScope.launch {
            viewModel.task.collect { task ->
                task?.let { updateTaskUI(it) }
            }
        }
        
        // Observar completaciones
        viewModel.completions.observe(this) { completions ->
            completionAdapter.submitList(completions)
        }
        
        // Observar estado de completado
        viewModel.completionState.observe(this) { state ->
            when (state) {
                is TaskDetailViewModel.CompletionState.Success -> {
                    Toast.makeText(this, R.string.task_completed_successfully, Toast.LENGTH_SHORT).show()
                    
                    // Reiniciar estado tras un éxito
                    viewModel.resetCompletionState()
                    
                    // Recargar tarea para mostrar el nuevo estado
                    viewModel.loadTask(taskId)
                }
                is TaskDetailViewModel.CompletionState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    
                    // Reiniciar estado tras un error
                    viewModel.resetCompletionState()
                }
                else -> {
                    // No hacer nada con el estado inicial
                }
            }
        }
    }
    
    /**
     * Actualiza la UI con los datos de la tarea
     */
    private fun updateTaskUI(task: Task) {
        titleTextView.text = task.title
        descriptionTextView.text = task.description ?: getString(R.string.no_description)
        statusTextView.text = getStatusText(task.status)
        
        // Formatear fecha
        dueDateTextView.text = task.dueDate?.let { formatDate(it) } ?: getString(R.string.no_due_date)
        
        locationTextView.text = task.locationName ?: getString(R.string.no_location)
        priorityTextView.text = getPriorityText(task.priority)
        
        // Actualizar estado de los botones según el estado de la tarea
        updateButtons(task.status)
    }
    
    /**
     * Actualiza los botones según el estado de la tarea
     */
    private fun updateButtons(status: String) {
        when (status) {
            "pending" -> {
                startButton.visibility = View.VISIBLE
                completeButton.visibility = View.GONE
            }
            "in_progress" -> {
                startButton.visibility = View.GONE
                completeButton.visibility = View.VISIBLE
            }
            "completed" -> {
                startButton.visibility = View.GONE
                completeButton.visibility = View.GONE
            }
            else -> {
                startButton.visibility = View.VISIBLE
                completeButton.visibility = View.GONE
            }
        }
    }
    
    /**
     * Obtiene el texto para el estado de la tarea
     */
    private fun getStatusText(status: String): String {
        return when (status) {
            "pending" -> getString(R.string.status_pending)
            "in_progress" -> getString(R.string.status_in_progress)
            "completed" -> getString(R.string.status_completed)
            else -> status
        }
    }
    
    /**
     * Obtiene el texto para la prioridad de la tarea
     */
    private fun getPriorityText(priority: Int?): String {
        return when (priority) {
            1 -> getString(R.string.priority_low)
            2 -> getString(R.string.priority_medium)
            3 -> getString(R.string.priority_high)
            else -> getString(R.string.priority_unknown)
        }
    }
    
    /**
     * Formatea una fecha para mostrarla
     */
    private fun formatDate(date: Date): String {
        val format = android.text.format.DateFormat.getDateFormat(this)
        return format.format(date)
    }
    
    /**
     * Muestra un diálogo para completar la tarea
     */
    private fun showCompletionDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_task_completion, null)
        val notesEditText = view.findViewById<EditText>(R.id.notes_edittext)
        val timeSpentEditText = view.findViewById<EditText>(R.id.time_spent_edittext)
        
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.complete_task)
            .setView(view)
            .setPositiveButton(R.string.complete) { _, _ ->
                val notes = notesEditText.text.toString().trim().takeIf { it.isNotEmpty() }
                val timeSpent = timeSpentEditText.text.toString().trim().toIntOrNull()
                
                viewModel.completeTask(notes, timeSpent)
            }
            .setNegativeButton(R.string.cancel, null)
            .setNeutralButton(R.string.add_signature) { _, _ ->
                val notes = notesEditText.text.toString().trim().takeIf { it.isNotEmpty() }
                val timeSpent = timeSpentEditText.text.toString().trim().toIntOrNull()
                
                // Guardar notas y tiempo para enviarlos a la actividad de firma
                val intent = Intent(this, SignatureActivity::class.java)
                intent.putExtra("task_id", taskId)
                intent.putExtra("notes", notes)
                intent.putExtra("time_spent", timeSpent)
                startActivity(intent)
            }
            .create()
        
        dialog.show()
    }
    
    /**
     * Infla el menú de opciones
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.task_detail_menu, menu)
        return true
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
            R.id.action_refresh -> {
                viewModel.loadTask(taskId)
                true
            }
            R.id.action_take_photo -> {
                navigateToPhotoCapture()
                true
            }
            R.id.action_print_label -> {
                navigateToPrintLabel()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    /**
     * Navega a la actividad de captura de foto
     */
    private fun navigateToPhotoCapture() {
        // Implementación pendiente
        Toast.makeText(this, R.string.feature_not_implemented, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Navega a la actividad de impresión de etiqueta
     */
    private fun navigateToPrintLabel() {
        val intent = Intent(this, PrintLabelActivity::class.java)
        intent.putExtra("task_id", taskId)
        startActivity(intent)
    }
}