package com.productiva.android.activities

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.productiva.android.ProductivaApplication
import com.productiva.android.R
import com.productiva.android.adapters.BluetoothDeviceAdapter
import com.productiva.android.bluetooth.BluetoothPrinterManager
import com.productiva.android.model.SavedPrinter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Actividad para imprimir etiquetas en impresoras Bluetooth
 */
class PrintLabelActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var recyclerViewDevices: RecyclerView
    private lateinit var editTextExtraText: EditText
    private lateinit var buttonPrint: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewStatus: TextView
    private lateinit var buttonScanDevices: Button
    
    private lateinit var bluetoothPrinterManager: BluetoothPrinterManager
    private lateinit var deviceAdapter: BluetoothDeviceAdapter
    private lateinit var app: ProductivaApplication
    
    private var taskId: Int = -1
    private var taskTitle: String? = null
    private var selectedDevice: BluetoothDevice? = null
    
    // Lanzadores para solicitud de permisos
    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            initializeBluetoothScanning()
        } else {
            Toast.makeText(
                this,
                "Se requieren permisos de Bluetooth para escanear dispositivos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    // Lanzador para activar Bluetooth
    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            scanForDevices()
        } else {
            Toast.makeText(this, "Bluetooth necesario para imprimir etiquetas", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_print_label)
        
        // Obtener la aplicación
        app = application as ProductivaApplication
        
        // Obtener datos de la tarea y verificar si estamos en modo escaneo solamente
        val scanOnly = intent.getBooleanExtra("scan_only", false)
        taskId = intent.getIntExtra("task_id", -1)
        taskTitle = intent.getStringExtra("task_title")
        
        if (!scanOnly && taskId == -1) {
            Toast.makeText(this, "Error: Tarea no especificada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Si estamos en modo de escaneo, cambiar el título y ocultar algunos elementos
        if (scanOnly) {
            supportActionBar?.title = "Buscar impresoras"
            findViewById<View>(R.id.editTextExtraText).visibility = View.GONE
            findViewById<View>(R.id.buttonPrint).visibility = View.GONE
            findViewById<TextView>(R.id.textViewExtraText).visibility = View.GONE
        }
        
        // Inicializar el gestor de impresora Bluetooth
        bluetoothPrinterManager = BluetoothPrinterManager(this)
        
        // Inicializar vistas
        toolbar = findViewById(R.id.toolbar)
        recyclerViewDevices = findViewById(R.id.recyclerViewDevices)
        editTextExtraText = findViewById(R.id.editTextExtraText)
        buttonPrint = findViewById(R.id.buttonPrint)
        progressBar = findViewById(R.id.progressBar)
        textViewStatus = findViewById(R.id.textViewStatus)
        buttonScanDevices = findViewById(R.id.buttonScanDevices)
        
        // Configurar toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Imprimir etiqueta"
        
        // Configurar RecyclerView
        recyclerViewDevices.layoutManager = LinearLayoutManager(this)
        recyclerViewDevices.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        
        deviceAdapter = BluetoothDeviceAdapter { device ->
            selectDevice(device)
        }
        recyclerViewDevices.adapter = deviceAdapter
        
        // Configurar botones
        buttonPrint.setOnClickListener {
            printLabel()
        }
        
        buttonScanDevices.setOnClickListener {
            requestBluetoothPermissions()
        }
        
        // Verificar permisos de Bluetooth
        requestBluetoothPermissions()
        
        // Cargar dispositivos guardados
        loadSavedPrinters()
    }
    
    /**
     * Solicita los permisos necesarios para Bluetooth
     */
    private fun requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ requiere permisos especiales para Bluetooth
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
            
            val permissionsToRequest = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }.toTypedArray()
            
            if (permissionsToRequest.isNotEmpty()) {
                bluetoothPermissionLauncher.launch(permissionsToRequest)
            } else {
                initializeBluetoothScanning()
            }
        } else {
            // Versiones anteriores de Android
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            
            val permissionsToRequest = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }.toTypedArray()
            
            if (permissionsToRequest.isNotEmpty()) {
                bluetoothPermissionLauncher.launch(permissionsToRequest)
            } else {
                initializeBluetoothScanning()
            }
        }
    }
    
    /**
     * Inicializa el escaneo de dispositivos Bluetooth
     */
    private fun initializeBluetoothScanning() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        
        if (bluetoothAdapter == null) {
            // El dispositivo no soporta Bluetooth
            Toast.makeText(this, "Bluetooth no disponible en este dispositivo", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!bluetoothAdapter.isEnabled) {
            // Solicitar activar Bluetooth
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        } else {
            // Bluetooth está activo, escanear dispositivos
            scanForDevices()
        }
    }
    
    /**
     * Escanea dispositivos Bluetooth
     */
    private fun scanForDevices() {
        setLoading(true)
        textViewStatus.text = "Buscando dispositivos..."
        
        lifecycleScope.launch {
            try {
                val devices = bluetoothPrinterManager.discoverDevices()
                deviceAdapter.updateDevices(devices)
                
                if (devices.isEmpty()) {
                    textViewStatus.text = "No se encontraron dispositivos"
                } else {
                    textViewStatus.text = "Seleccione una impresora"
                }
            } catch (e: Exception) {
                textViewStatus.text = "Error al buscar dispositivos: ${e.message}"
            } finally {
                setLoading(false)
            }
        }
    }
    
    /**
     * Carga las impresoras guardadas previamente
     */
    private fun loadSavedPrinters() {
        lifecycleScope.launch {
            val savedPrinters = app.database.savedPrinterDao().getAllPrinters()
            if (savedPrinters.isNotEmpty()) {
                val devices = bluetoothPrinterManager.getSavedDevices(savedPrinters)
                deviceAdapter.updateDevices(devices)
                textViewStatus.text = "Impresoras guardadas"
            }
        }
    }
    
    /**
     * Selecciona un dispositivo para imprimir
     */
    private fun selectDevice(device: BluetoothDevice) {
        selectedDevice = device
        textViewStatus.text = "Impresora seleccionada: ${device.name ?: device.address}"
        buttonPrint.isEnabled = true
        
        // Guardar la impresora seleccionada como la última usada
        app.sessionManager.saveLastPrinterAddress(device.address)
        
        // Guardar dispositivo en la base de datos
        lifecycleScope.launch {
            val savedPrinter = SavedPrinter(
                address = device.address,
                name = device.name ?: "Desconocido"
            )
            app.database.savedPrinterDao().insertPrinter(savedPrinter)
            
            // Si estamos en modo escaneo, abrir la pantalla de configuración
            if (intent.getBooleanExtra("scan_only", false)) {
                // Esperar un momento para que se pueda ver la selección
                delay(500)
                
                // Abrir configuración de la impresora
                val intent = Intent(this@PrintLabelActivity, PrinterSettingsActivity::class.java)
                intent.putExtra("printer_address", device.address)
                startActivity(intent)
                finish()
            }
        }
    }
    
    /**
     * Imprime la etiqueta
     */
    private fun printLabel() {
        val device = selectedDevice ?: return
        val extraText = editTextExtraText.text.toString().trim()
        
        setLoading(true)
        textViewStatus.text = "Conectando con la impresora..."
        
        lifecycleScope.launch {
            try {
                textViewStatus.text = "Enviando datos a la impresora..."
                
                // Obtener la impresora guardada y la plantilla predeterminada
                val savedPrinter = app.database.savedPrinterDao().getPrinterByAddressSync(device.address)
                val labelTemplate = app.database.labelTemplateDao().getDefaultLabelTemplate()
                
                val success = bluetoothPrinterManager.printLabel(
                    device = device,
                    title = taskTitle ?: "Tarea #$taskId",
                    extraText = extraText,
                    date = System.currentTimeMillis(),
                    printer = savedPrinter,
                    template = labelTemplate
                )
                
                if (success) {
                    textViewStatus.text = "Etiqueta impresa correctamente"
                    
                    // Esperar un momento antes de cerrar la actividad
                    delay(1500)
                    
                    // Cerrar la actividad con resultado OK
                    setResult(RESULT_OK)
                    finish()
                } else {
                    textViewStatus.text = "Error al imprimir la etiqueta"
                }
            } catch (e: Exception) {
                textViewStatus.text = "Error: ${e.message}"
            } finally {
                setLoading(false)
            }
        }
    }
    
    /**
     * Actualiza la UI para mostrar/ocultar indicadores de carga
     */
    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        buttonPrint.isEnabled = !loading && selectedDevice != null
        buttonScanDevices.isEnabled = !loading
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