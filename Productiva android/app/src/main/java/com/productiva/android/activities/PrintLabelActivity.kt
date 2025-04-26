package com.productiva.android.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.productiva.android.R
import com.productiva.android.model.LabelTemplate
import com.productiva.android.model.SavedPrinter
import com.productiva.android.viewmodel.PrintLabelViewModel

/**
 * Actividad para imprimir etiquetas
 */
class PrintLabelActivity : AppCompatActivity() {
    
    private lateinit var viewModel: PrintLabelViewModel
    
    // UI components
    private lateinit var toolbar: Toolbar
    private lateinit var printerSpinner: Spinner
    private lateinit var templateSpinner: Spinner
    private lateinit var formContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var printButton: Button
    private lateinit var addPrinterButton: FloatingActionButton
    private lateinit var noPrintersText: TextView
    private lateinit var noTemplatesText: TextView
    
    // Adapters
    private lateinit var printerAdapter: ArrayAdapter<SavedPrinter>
    private lateinit var templateAdapter: ArrayAdapter<LabelTemplate>
    
    // Current data
    private var fieldEditTexts = mutableListOf<EditText>()
    private var fieldNames = mutableListOf<String>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_print_label)
        
        // Initialize UI components
        toolbar = findViewById(R.id.toolbar)
        printerSpinner = findViewById(R.id.printer_spinner)
        templateSpinner = findViewById(R.id.template_spinner)
        formContainer = findViewById(R.id.form_container)
        progressBar = findViewById(R.id.progress_bar)
        printButton = findViewById(R.id.print_button)
        addPrinterButton = findViewById(R.id.add_printer_button)
        noPrintersText = findViewById(R.id.no_printers_text)
        noTemplatesText = findViewById(R.id.no_templates_text)
        
        // Setup ActionBar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Imprimir Etiqueta"
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(PrintLabelViewModel::class.java)
        
        // Setup spinners
        setupSpinners()
        
        // Setup buttons
        setupButtons()
        
        // Set observers
        setupObservers()
        
        // Refresh templates from server
        viewModel.refreshTemplates()
    }
    
    /**
     * Configura los spinners para seleccionar impresora y plantilla
     */
    private fun setupSpinners() {
        // Printer spinner
        printerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf<SavedPrinter>())
        printerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        printerSpinner.adapter = printerAdapter
        
        printerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val printer = printerAdapter.getItem(position)
                printer?.let {
                    viewModel.selectPrinter(it)
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Template spinner
        templateAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf<LabelTemplate>())
        templateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        templateSpinner.adapter = templateAdapter
        
        templateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val template = templateAdapter.getItem(position)
                template?.let {
                    viewModel.selectTemplate(it)
                    updateFormFields(it)
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    /**
     * Configura los botones
     */
    private fun setupButtons() {
        // Print button
        printButton.setOnClickListener {
            printLabel()
        }
        
        // Add printer button
        addPrinterButton.setOnClickListener {
            showAddPrinterDialog()
        }
    }
    
    /**
     * Configura los observadores para el ViewModel
     */
    private fun setupObservers() {
        // Observe saved printers
        viewModel.savedPrinters.observe(this) { printers ->
            updatePrintersList(printers)
        }
        
        // Observe label templates
        viewModel.labelTemplates.observe(this) { templates ->
            updateTemplatesList(templates)
        }
        
        // Observe selected printer
        viewModel.selectedPrinter.observe(this) { printer ->
            // Actualizar UI si es necesario
        }
        
        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // Observe error messages
        viewModel.errorMessage.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                showError(message)
            }
        }
        
        // Observe printing state
        viewModel.printingState.observe(this) { state ->
            when (state) {
                PrintLabelViewModel.PrintingState.PRINTING -> {
                    showPrintingStatus("Imprimiendo etiqueta...")
                }
                PrintLabelViewModel.PrintingState.SENT -> {
                    showPrintingStatus("Etiqueta enviada a la impresora")
                    viewModel.resetPrintingState()
                }
                PrintLabelViewModel.PrintingState.ERROR -> {
                    showPrintingStatus("Error al imprimir etiqueta", true)
                    viewModel.resetPrintingState()
                }
                else -> {}
            }
        }
    }
    
    /**
     * Actualiza la lista de impresoras
     */
    private fun updatePrintersList(printers: List<SavedPrinter>?) {
        printerAdapter.clear()
        
        if (printers.isNullOrEmpty()) {
            noPrintersText.visibility = View.VISIBLE
            printerSpinner.visibility = View.GONE
        } else {
            noPrintersText.visibility = View.GONE
            printerSpinner.visibility = View.VISIBLE
            printerAdapter.addAll(printers)
        }
    }
    
    /**
     * Actualiza la lista de plantillas
     */
    private fun updateTemplatesList(templates: List<LabelTemplate>?) {
        templateAdapter.clear()
        
        if (templates.isNullOrEmpty()) {
            noTemplatesText.visibility = View.VISIBLE
            templateSpinner.visibility = View.GONE
            formContainer.visibility = View.GONE
            printButton.isEnabled = false
        } else {
            noTemplatesText.visibility = View.GONE
            templateSpinner.visibility = View.VISIBLE
            formContainer.visibility = View.VISIBLE
            printButton.isEnabled = true
            templateAdapter.addAll(templates)
            
            // Seleccionar la primera plantilla
            templates.firstOrNull()?.let {
                viewModel.selectTemplate(it)
                updateFormFields(it)
            }
        }
    }
    
    /**
     * Actualiza los campos del formulario según la plantilla seleccionada
     */
    private fun updateFormFields(template: LabelTemplate) {
        formContainer.removeAllViews()
        fieldEditTexts.clear()
        fieldNames.clear()
        
        val fields = template.fields?.split(",") ?: emptyList()
        
        for (field in fields) {
            val fieldName = field.trim()
            if (fieldName.isNotEmpty()) {
                fieldNames.add(fieldName)
                
                // Crear TextView para label
                val labelView = TextView(this)
                labelView.text = fieldName
                labelView.setPadding(0, 16, 0, 8)
                formContainer.addView(labelView)
                
                // Crear EditText para el valor
                val editText = EditText(this)
                editText.hint = "Ingrese $fieldName"
                editText.tag = fieldName // Guardar nombre de campo como tag
                formContainer.addView(editText)
                
                fieldEditTexts.add(editText)
            }
        }
    }
    
    /**
     * Imprime la etiqueta con los datos del formulario
     */
    private fun printLabel() {
        if (fieldEditTexts.isEmpty()) {
            showError("No hay campos para imprimir")
            return
        }
        
        val data = mutableMapOf<String, String>()
        
        for (i in fieldEditTexts.indices) {
            val value = fieldEditTexts[i].text.toString()
            val name = fieldNames[i]
            data[name] = value
        }
        
        viewModel.printLabel(data)
    }
    
    /**
     * Muestra un diálogo para añadir una nueva impresora
     */
    private fun showAddPrinterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_printer, null)
        
        val nameEdit = dialogView.findViewById<EditText>(R.id.printer_name_edit)
        val addressEdit = dialogView.findViewById<EditText>(R.id.printer_address_edit)
        val modelEdit = dialogView.findViewById<EditText>(R.id.printer_model_edit)
        val widthEdit = dialogView.findViewById<EditText>(R.id.paper_width_edit)
        val heightEdit = dialogView.findViewById<EditText>(R.id.paper_height_edit)
        val defaultCheckBox = dialogView.findViewById<TextView>(R.id.default_checkbox)
        
        AlertDialog.Builder(this)
            .setTitle("Añadir impresora")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val name = nameEdit.text.toString()
                val address = addressEdit.text.toString()
                val model = modelEdit.text.toString()
                val widthText = widthEdit.text.toString()
                val heightText = heightEdit.text.toString()
                
                if (name.isEmpty() || address.isEmpty()) {
                    showError("Nombre y dirección son obligatorios")
                    return@setPositiveButton
                }
                
                val width = if (widthText.isEmpty()) 62 else widthText.toInt()
                val height = if (heightText.isEmpty()) 29 else heightText.toInt()
                val isDefault = defaultCheckBox.isSelected
                
                val printer = SavedPrinter(
                    id = 0, // ID temporal, se asignará en la BD
                    name = name,
                    address = address,
                    model = model,
                    paperWidth = width,
                    paperHeight = height,
                    isDefault = isDefault,
                    lastUsed = System.currentTimeMillis()
                )
                
                viewModel.savePrinter(printer)
                
                if (isDefault) {
                    viewModel.setDefaultPrinter(printer)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Muestra un mensaje de error
     */
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * Muestra el estado de la impresión
     */
    private fun showPrintingStatus(message: String, isError: Boolean = false) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}