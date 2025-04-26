package com.productiva.android.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.productiva.android.ProductivaApplication
import com.productiva.android.R
import com.productiva.android.model.SavedPrinter
import kotlinx.coroutines.launch

/**
 * Actividad para configurar los ajustes de la impresora Brother
 */
class PrinterSettingsActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var editTextPrinterName: EditText
    private lateinit var editTextPaperWidth: EditText
    private lateinit var editTextPaperLength: EditText
    private lateinit var seekBarPrintDensity: SeekBar
    private lateinit var textViewPrintDensity: TextView
    private lateinit var switchDefaultPrinter: Switch
    private lateinit var spinnerPrinterModel: androidx.appcompat.widget.AppCompatSpinner
    private lateinit var seekBarPrintSpeed: SeekBar
    private lateinit var textViewPrintSpeed: TextView
    private lateinit var buttonSave: Button
    private lateinit var buttonTest: Button
    
    private lateinit var app: ProductivaApplication
    private var printerAddress: String? = null
    
    private var savedPrinter: SavedPrinter? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_printer_settings)
        
        // Obtener la dirección de la impresora
        printerAddress = intent.getStringExtra("printer_address") ?: run {
            Toast.makeText(this, "Error: Impresora no especificada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Obtener la aplicación
        app = application as ProductivaApplication
        
        // Inicializar vistas
        toolbar = findViewById(R.id.toolbar)
        editTextPrinterName = findViewById(R.id.editTextPrinterName)
        editTextPaperWidth = findViewById(R.id.editTextPaperWidth)
        editTextPaperLength = findViewById(R.id.editTextPaperLength)
        seekBarPrintDensity = findViewById(R.id.seekBarPrintDensity)
        textViewPrintDensity = findViewById(R.id.textViewPrintDensity)
        switchDefaultPrinter = findViewById(R.id.switchDefaultPrinter)
        spinnerPrinterModel = findViewById(R.id.spinnerPrinterModel)
        seekBarPrintSpeed = findViewById(R.id.seekBarPrintSpeed)
        textViewPrintSpeed = findViewById(R.id.textViewPrintSpeed)
        buttonSave = findViewById(R.id.buttonSave)
        buttonTest = findViewById(R.id.buttonTest)
        
        // Configurar toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Configuración de impresora"
        
        // Configurar seekbar para densidad de impresión (-5 a 5)
        seekBarPrintDensity.max = 10
        seekBarPrintDensity.progress = 5 // 0 por defecto
        seekBarPrintDensity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val density = progress - 5
                updateDensityText(density)
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // No se necesita implementación
            }
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // No se necesita implementación
            }
        })
        
        // Configurar seekbar para velocidad de impresión (0-2)
        seekBarPrintSpeed.max = 2
        seekBarPrintSpeed.progress = 1 // Normal por defecto
        seekBarPrintSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateSpeedText(progress)
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // No se necesita implementación
            }
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // No se necesita implementación
            }
        })
        
        // Configurar botones
        buttonSave.setOnClickListener {
            savePrinterSettings()
        }
        
        buttonTest.setOnClickListener {
            testPrint()
        }
        
        // Cargar configuración guardada
        loadPrinterSettings()
    }
    
    /**
     * Carga la configuración guardada para esta impresora
     */
    private fun loadPrinterSettings() {
        lifecycleScope.launch {
            val dao = app.database.savedPrinterDao()
            val printer = dao.getPrinterByAddressSync(printerAddress!!)
            
            runOnUiThread {
                if (printer != null) {
                    savedPrinter = printer
                    
                    // Establecer valores en la UI
                    editTextPrinterName.setText(printer.name)
                    editTextPaperWidth.setText(printer.paperWidth.toString())
                    editTextPaperLength.setText(printer.paperLength.toString())
                    
                    // Densidad de impresión (-5 a 5)
                    val densityProgress = printer.printDensity + 5
                    seekBarPrintDensity.progress = densityProgress
                    updateDensityText(printer.printDensity)
                    
                    // Velocidad de impresión (0-2)
                    seekBarPrintSpeed.progress = printer.printSpeed
                    updateSpeedText(printer.printSpeed)
                    
                    // Impresora predeterminada
                    switchDefaultPrinter.isChecked = printer.isDefault
                    
                    // Modelo de impresora
                    val modelPosition = when (printer.printerModel) {
                        "BROTHER_QL800" -> 1
                        "BROTHER_QL820" -> 2
                        "BROTHER_QL1100" -> 3
                        else -> 0 // BROTHER_GENERIC
                    }
                    spinnerPrinterModel.setSelection(modelPosition)
                }
            }
        }
    }
    
    /**
     * Actualiza el texto de densidad de impresión
     */
    private fun updateDensityText(density: Int) {
        val densityText = when {
            density < 0 -> "Menor (-$density)"
            density > 0 -> "Mayor (+$density)"
            else -> "Normal"
        }
        textViewPrintDensity.text = "Densidad de impresión: $densityText"
    }
    
    /**
     * Actualiza el texto de velocidad de impresión
     */
    private fun updateSpeedText(speed: Int) {
        val speedText = when (speed) {
            0 -> "Lenta"
            1 -> "Normal"
            2 -> "Rápida"
            else -> "Normal"
        }
        textViewPrintSpeed.text = "Velocidad de impresión: $speedText"
    }
    
    /**
     * Guarda la configuración de la impresora
     */
    private fun savePrinterSettings() {
        try {
            val name = editTextPrinterName.text.toString().trim()
            val paperWidth = editTextPaperWidth.text.toString().toInt()
            val paperLength = editTextPaperLength.text.toString().toInt()
            val printDensity = seekBarPrintDensity.progress - 5
            val printSpeed = seekBarPrintSpeed.progress
            val isDefault = switchDefaultPrinter.isChecked
            
            // Validar campos
            if (name.isEmpty()) {
                Toast.makeText(this, "El nombre de la impresora no puede estar vacío", Toast.LENGTH_SHORT).show()
                return
            }
            
            if (paperWidth <= 0 || paperLength <= 0) {
                Toast.makeText(this, "Las dimensiones del papel deben ser mayores que cero", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Obtener modelo de impresora seleccionado
            val printerModelArray = resources.getStringArray(R.array.printer_models_values)
            val printerModel = printerModelArray[spinnerPrinterModel.selectedItemPosition]
            
            // Crear objeto SavedPrinter
            val printer = SavedPrinter(
                address = printerAddress!!,
                name = name,
                isDefault = isDefault,
                printerModel = printerModel,
                paperWidth = paperWidth,
                paperLength = paperLength,
                printDensity = printDensity,
                printSpeed = printSpeed
            )
            
            // Guardar en la base de datos
            lifecycleScope.launch {
                val dao = app.database.savedPrinterDao()
                
                // Si esta impresora se configura como predeterminada, actualizar todas las demás
                if (isDefault) {
                    dao.clearDefaultPrinters()
                }
                
                dao.insertPrinter(printer)
                
                runOnUiThread {
                    Toast.makeText(
                        this@PrinterSettingsActivity,
                        "Configuración guardada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(
                this,
                "Por favor, ingrese valores numéricos válidos para las dimensiones",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    /**
     * Realiza una impresión de prueba
     */
    private fun testPrint() {
        // Obtener configuración actual
        try {
            val paperWidth = editTextPaperWidth.text.toString().toInt()
            val paperLength = editTextPaperLength.text.toString().toInt()
            val printDensity = seekBarPrintDensity.progress - 5
            val printSpeed = seekBarPrintSpeed.progress
            
            // Obtener modelo de impresora seleccionado
            val printerModelArray = resources.getStringArray(R.array.printer_models_values)
            val printerModel = printerModelArray[spinnerPrinterModel.selectedItemPosition]
            
            // Crear configuración de impresora temporal
            val tempPrinter = SavedPrinter(
                address = printerAddress!!,
                name = "Impresora de prueba",
                printerModel = printerModel,
                paperWidth = paperWidth,
                paperLength = paperLength,
                printDensity = printDensity,
                printSpeed = printSpeed
            )
            
            // Imprimir etiqueta de prueba
            val adapter = app.bluetoothAdapter
            
            if (adapter == null) {
                Toast.makeText(this, "Bluetooth no disponible", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Obtener dispositivo Bluetooth
            try {
                val device = adapter.getRemoteDevice(printerAddress)
                
                // Mostrar diálogo de carga
                Toast.makeText(this, "Enviando prueba a la impresora...", Toast.LENGTH_SHORT).show()
                
                // Imprimir etiqueta de prueba
                lifecycleScope.launch {
                    val printerManager = app.bluetoothPrinterManager
                    
                    val result = printerManager.printLabel(
                        device = device,
                        title = "ETIQUETA DE PRUEBA",
                        extraText = "Configuración de impresora",
                        date = System.currentTimeMillis(),
                        printer = tempPrinter
                    )
                    
                    runOnUiThread {
                        if (result) {
                            Toast.makeText(
                                this@PrinterSettingsActivity,
                                "Prueba enviada correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@PrinterSettingsActivity,
                                "Error al enviar la prueba",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Error al conectar con la impresora: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(
                this,
                "Por favor, ingrese valores numéricos válidos para las dimensiones",
                Toast.LENGTH_SHORT
            ).show()
        }
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