package com.productiva.android.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.productiva.android.ProductivaApplication
import com.productiva.android.R
import com.productiva.android.adapters.LabelTemplateAdapter
import com.productiva.android.adapters.SavedPrinterAdapter
import kotlinx.coroutines.launch

/**
 * Actividad principal para configuración de impresoras y plantillas
 */
class PrinterConfigurationActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var recyclerViewPrinters: RecyclerView
    private lateinit var recyclerViewTemplates: RecyclerView
    private lateinit var buttonScanPrinters: Button
    private lateinit var fabAddTemplate: FloatingActionButton
    private lateinit var textViewNoPrinters: TextView
    private lateinit var textViewNoTemplates: TextView
    private lateinit var progressBar: ProgressBar
    
    private lateinit var printerAdapter: SavedPrinterAdapter
    private lateinit var templateAdapter: LabelTemplateAdapter
    
    private lateinit var app: ProductivaApplication
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_printer_configuration)
        
        // Obtener la aplicación
        app = application as ProductivaApplication
        
        // Inicializar vistas
        toolbar = findViewById(R.id.toolbar)
        recyclerViewPrinters = findViewById(R.id.recyclerViewPrinters)
        recyclerViewTemplates = findViewById(R.id.recyclerViewTemplates)
        buttonScanPrinters = findViewById(R.id.buttonScanPrinters)
        fabAddTemplate = findViewById(R.id.fabAddTemplate)
        textViewNoPrinters = findViewById(R.id.textViewNoPrinters)
        textViewNoTemplates = findViewById(R.id.textViewNoTemplates)
        progressBar = findViewById(R.id.progressBar)
        
        // Configurar toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Configuración de impresión"
        
        // Configurar RecyclerView para impresoras
        recyclerViewPrinters.layoutManager = LinearLayoutManager(this)
        recyclerViewPrinters.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        
        printerAdapter = SavedPrinterAdapter(
            onPrinterClickListener = { printer ->
                // Editar configuración de impresora
                val intent = Intent(this, PrinterSettingsActivity::class.java)
                intent.putExtra("printer_address", printer.address)
                startActivity(intent)
            },
            onDeleteClickListener = { printer ->
                confirmDeletePrinter(printer.address, printer.name)
            }
        )
        recyclerViewPrinters.adapter = printerAdapter
        
        // Configurar RecyclerView para plantillas
        recyclerViewTemplates.layoutManager = LinearLayoutManager(this)
        recyclerViewTemplates.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        
        templateAdapter = LabelTemplateAdapter(
            onTemplateClickListener = { template ->
                // Editar plantilla
                val intent = Intent(this, LabelTemplateActivity::class.java)
                intent.putExtra("template_id", template.id)
                startActivity(intent)
            },
            onDeleteClickListener = { template ->
                confirmDeleteTemplate(template.id, template.name)
            }
        )
        recyclerViewTemplates.adapter = templateAdapter
        
        // Configurar botones
        buttonScanPrinters.setOnClickListener {
            scanForPrinters()
        }
        
        fabAddTemplate.setOnClickListener {
            // Crear nueva plantilla
            val intent = Intent(this, LabelTemplateActivity::class.java)
            startActivity(intent)
        }
        
        // Cargar datos
        loadSavedPrinters()
        loadTemplates()
    }
    
    /**
     * Carga las impresoras guardadas
     */
    private fun loadSavedPrinters() {
        lifecycleScope.launch {
            val printers = app.database.savedPrinterDao().getAllPrinters()
            
            runOnUiThread {
                if (printers.isEmpty()) {
                    textViewNoPrinters.visibility = View.VISIBLE
                    recyclerViewPrinters.visibility = View.GONE
                } else {
                    textViewNoPrinters.visibility = View.GONE
                    recyclerViewPrinters.visibility = View.VISIBLE
                    printerAdapter.updatePrinters(printers)
                }
            }
        }
    }
    
    /**
     * Carga las plantillas guardadas
     */
    private fun loadTemplates() {
        lifecycleScope.launch {
            val templates = app.database.labelTemplateDao().getAllLabelTemplates()
            
            runOnUiThread {
                if (templates.isEmpty()) {
                    textViewNoTemplates.visibility = View.VISIBLE
                    recyclerViewTemplates.visibility = View.GONE
                } else {
                    textViewNoTemplates.visibility = View.GONE
                    recyclerViewTemplates.visibility = View.VISIBLE
                    templateAdapter.updateTemplates(templates)
                }
            }
        }
    }
    
    /**
     * Inicia el escaneo de impresoras Bluetooth
     */
    private fun scanForPrinters() {
        val intent = Intent(this, PrintLabelActivity::class.java)
        intent.putExtra("scan_only", true)
        intent.putExtra("task_id", -1)  // ID ficticio para el escaneo
        startActivity(intent)
    }
    
    /**
     * Confirma la eliminación de una impresora
     */
    private fun confirmDeletePrinter(address: String, name: String) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar impresora")
            .setMessage("¿Está seguro que desea eliminar la impresora '$name'?")
            .setPositiveButton("Eliminar") { _, _ ->
                deletePrinter(address)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Elimina una impresora de la base de datos
     */
    private fun deletePrinter(address: String) {
        lifecycleScope.launch {
            app.database.savedPrinterDao().deletePrinter(address)
            
            runOnUiThread {
                Toast.makeText(
                    this@PrinterConfigurationActivity,
                    "Impresora eliminada correctamente",
                    Toast.LENGTH_SHORT
                ).show()
                
                // Recargar lista de impresoras
                loadSavedPrinters()
            }
        }
    }
    
    /**
     * Confirma la eliminación de una plantilla
     */
    private fun confirmDeleteTemplate(id: Int, name: String) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar plantilla")
            .setMessage("¿Está seguro que desea eliminar la plantilla '$name'?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteTemplate(id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Elimina una plantilla de la base de datos
     */
    private fun deleteTemplate(id: Int) {
        lifecycleScope.launch {
            app.database.labelTemplateDao().deleteLabelTemplate(id)
            
            runOnUiThread {
                Toast.makeText(
                    this@PrinterConfigurationActivity,
                    "Plantilla eliminada correctamente",
                    Toast.LENGTH_SHORT
                ).show()
                
                // Recargar lista de plantillas
                loadTemplates()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // Actualizar listas al volver a la actividad
        loadSavedPrinters()
        loadTemplates()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}