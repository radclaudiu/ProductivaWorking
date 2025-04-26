package com.productiva.android.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.productiva.android.R
import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.SavedPrinter
import com.productiva.android.viewmodel.PrintLabelViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Activity para imprimir etiquetas
 */
class PrintLabelActivity : AppCompatActivity() {
    
    private lateinit var viewModel: PrintLabelViewModel
    
    // Componentes de UI
    private lateinit var toolbar: Toolbar
    private lateinit var taskTitleTextView: TextView
    private lateinit var printerSpinner: Spinner
    private lateinit var templateSpinner: Spinner
    private lateinit var copiesSpinner: Spinner
    private lateinit var previewTextView: TextView
    private lateinit var printButton: Button
    private lateinit var settingsButton: Button
    private lateinit var progressBar: ProgressBar
    
    // Adaptadores para spinners
    private lateinit var printerAdapter: ArrayAdapter<String>
    private lateinit var templateAdapter: ArrayAdapter<String>
    
    // Listas de datos
    private val printers = mutableListOf<SavedPrinter>()
    private val templates = mutableListOf<LabelTemplate>()
    
    // ID de la tarea
    private var taskId: Int = -1
    
    // Código de solicitud para permiso de Bluetooth
    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 100
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_print_label)
        
        // Obtener ID de la tarea si existe
        taskId = intent.getIntExtra("task_id", -1)
        
        // Inicializar ViewModel
        viewModel = ViewModelProvider(this).get(PrintLabelViewModel::class.java)
        
        // Configurar componentes de UI
        setupUI()
        
        // Configurar observadores
        setupObservers()
        
        // Verificar permisos de Bluetooth
        checkBluetoothPermissions()
        
        // Cargar tarea si existe
        if (taskId != -1) {
            viewModel.loadTask(taskId)
        }
    }
    
    /**
     * Configura las referencias y eventos de UI
     */
    private fun setupUI() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.print_label)
        
        taskTitleTextView = findViewById(R.id.task_title)
        printerSpinner = findViewById(R.id.printer_spinner)
        templateSpinner = findViewById(R.id.template_spinner)
        copiesSpinner = findViewById(R.id.copies_spinner)
        previewTextView = findViewById(R.id.preview_text)
        printButton = findViewById(R.id.print_button)
        settingsButton = findViewById(R.id.settings_button)
        progressBar = findViewById(R.id.progressBar)
        
        // Configurar spinner de impresoras
        printerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf<String>())
        printerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        printerSpinner.adapter = printerAdapter
        
        printerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < printers.size) {
                    viewModel.selectPrinter(printers[position])
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }
        
        // Configurar spinner de plantillas
        templateAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf<String>())
        templateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        templateSpinner.adapter = templateAdapter
        
        templateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < templates.size) {
                    viewModel.selectTemplate(templates[position])
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }
        
        // Configurar spinner de copias
        val copiesOptions = (1..10).map { it.toString() }
        val copiesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, copiesOptions)
        copiesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        copiesSpinner.adapter = copiesAdapter
        
        copiesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val copies = (position + 1)
                viewModel.setCopies(copies)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }
        
        // Configurar botones
        printButton.setOnClickListener {
            viewModel.printLabel()
        }
        
        settingsButton.setOnClickListener {
            val intent = Intent(this, PrinterSettingsActivity::class.java)
            startActivity(intent)
        }
    }
    
    /**
     * Configura los observadores de LiveData
     */
    private fun setupObservers() {
        // Observar tarea
        lifecycleScope.launch {
            viewModel.task.collect { task ->
                task?.let {
                    taskTitleTextView.text = it.title
                    taskTitleTextView.visibility = View.VISIBLE
                } ?: run {
                    taskTitleTextView.visibility = View.GONE
                }
            }
        }
        
        // Observar impresoras
        viewModel.printers.observe(this) { printersList ->
            printers.clear()
            printers.addAll(printersList)
            
            val printerNames = printersList.map { "${it.name} (${it.printerType})" }
            printerAdapter.clear()
            printerAdapter.addAll(printerNames)
            printerAdapter.notifyDataSetChanged()
            
            // Actualizar UI según disponibilidad
            updatePrintButtonState()
        }
        
        // Observar plantillas
        viewModel.templates.observe(this) { templatesList ->
            templates.clear()
            templates.addAll(templatesList)
            
            val templateNames = templatesList.map { it.name }
            templateAdapter.clear()
            templateAdapter.addAll(templateNames)
            templateAdapter.notifyDataSetChanged()
            
            // Actualizar UI según disponibilidad
            updatePrintButtonState()
        }
        
        // Observar impresora seleccionada
        viewModel.selectedPrinter.observe(this) { printer ->
            printer?.let {
                val position = printers.indexOf(it)
                if (position >= 0) {
                    printerSpinner.setSelection(position)
                }
            }
        }
        
        // Observar plantilla seleccionada
        viewModel.selectedTemplate.observe(this) { template ->
            template?.let {
                val position = templates.indexOf(it)
                if (position >= 0) {
                    templateSpinner.setSelection(position)
                }
                
                // Mostrar vista previa
                previewTextView.text = it.content
            }
        }
        
        // Observar estado de carga
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            printButton.isEnabled = !isLoading
        }
        
        // Observar mensajes de error
        viewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
        
        // Observar estado de impresión
        viewModel.printState.observe(this) { state ->
            when (state) {
                is PrintLabelViewModel.PrintState.Success -> {
                    Toast.makeText(this, R.string.label_printed_successfully, Toast.LENGTH_SHORT).show()
                }
                is PrintLabelViewModel.PrintState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                is PrintLabelViewModel.PrintState.Printing -> {
                    Toast.makeText(this, R.string.printing_label, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // No hacer nada con el estado inicial
                }
            }
        }
    }
    
    /**
     * Actualiza el estado del botón de impresión según disponibilidad
     */
    private fun updatePrintButtonState() {
        printButton.isEnabled = printers.isNotEmpty() && templates.isNotEmpty()
    }
    
    /**
     * Verifica y solicita permisos de Bluetooth si es necesario
     */
    private fun checkBluetoothPermissions() {
        val bluetoothPermission = Manifest.permission.BLUETOOTH
        val bluetoothAdminPermission = Manifest.permission.BLUETOOTH_ADMIN
        
        val hasBluetoothPermission = ContextCompat.checkSelfPermission(this, bluetoothPermission) == PackageManager.PERMISSION_GRANTED
        val hasBluetoothAdminPermission = ContextCompat.checkSelfPermission(this, bluetoothAdminPermission) == PackageManager.PERMISSION_GRANTED
        
        if (!hasBluetoothPermission || !hasBluetoothAdminPermission) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(bluetoothPermission, bluetoothAdminPermission),
                BLUETOOTH_PERMISSION_REQUEST_CODE
            )
        }
    }
    
    /**
     * Maneja la respuesta a la solicitud de permisos
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permisos concedidos, recargar impresoras
                viewModel.loadPrinters()
            } else {
                // Permisos denegados
                Toast.makeText(this, R.string.bluetooth_permission_required, Toast.LENGTH_LONG).show()
                finish()
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
    
    /**
     * Se ejecuta al reanudar la actividad (por ejemplo, después de volver de Configuración)
     */
    override fun onResume() {
        super.onResume()
        
        // Recargar datos
        viewModel.loadPrinters()
        viewModel.loadTemplates()
    }
}