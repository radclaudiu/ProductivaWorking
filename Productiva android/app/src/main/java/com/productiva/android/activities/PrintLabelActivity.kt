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
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.productiva.android.ProductivaApplication
import com.productiva.android.R
import com.productiva.android.adapters.PrinterListAdapter
import com.productiva.android.bluetooth.BluetoothPrinterManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.IOException

/**
 * Actividad para imprimir etiquetas de tareas
 */
class PrintLabelActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var recyclerViewPrinters: RecyclerView
    private lateinit var textViewNoDevices: TextView
    private lateinit var buttonEnableBluetooth: Button
    private lateinit var progressBar: ProgressBar
    
    private lateinit var bluetoothPrinterManager: BluetoothPrinterManager
    private var printerListAdapter: PrinterListAdapter? = null
    
    private val apiService by lazy {
        (application as ProductivaApplication).apiService
    }
    
    private val sessionManager by lazy {
        (application as ProductivaApplication).sessionManager
    }
    
    private var taskId: Int = -1
    private var labelData: ByteArray? = null
    
    // Lanzador para solicitar permisos de Bluetooth
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            loadPairedPrinters()
        } else {
            Toast.makeText(
                this,
                "Se requieren permisos de Bluetooth para imprimir etiquetas",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    // Lanzador para habilitar Bluetooth
    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadPairedPrinters()
        } else {
            Toast.makeText(
                this,
                "Se requiere Bluetooth para imprimir etiquetas",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_print_label)
        
        // Obtener ID de tarea
        taskId = intent.getIntExtra("task_id", -1)
        if (taskId == -1) {
            Toast.makeText(this, "Error: Tarea no encontrada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Inicializar views
        toolbar = findViewById(R.id.toolbar)
        recyclerViewPrinters = findViewById(R.id.recyclerViewPrinters)
        textViewNoDevices = findViewById(R.id.textViewNoDevices)
        buttonEnableBluetooth = findViewById(R.id.buttonEnableBluetooth)
        progressBar = findViewById(R.id.progressBar)
        
        // Configurar toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Imprimir Etiqueta"
        
        // Inicializar BluetoothPrinterManager
        bluetoothPrinterManager = BluetoothPrinterManager(this)
        
        // Configurar RecyclerView
        recyclerViewPrinters.layoutManager = LinearLayoutManager(this)
        printerListAdapter = PrinterListAdapter { device ->
            onPrinterSelected(device)
        }
        recyclerViewPrinters.adapter = printerListAdapter
        
        // Configurar botón para habilitar Bluetooth
        buttonEnableBluetooth.setOnClickListener {
            enableBluetooth()
        }
        
        // Verificar permisos de Bluetooth
        checkBluetoothPermissions()
        
        // Obtener datos de la etiqueta
        fetchLabelData()
    }
    
    /**
     * Obtiene los datos de la etiqueta desde el servidor
     */
    private fun fetchLabelData() {
        val token = sessionManager.getAuthToken()
        if (token == null) {
            Toast.makeText(this, "Error: No hay sesión activa", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        setLoading(true)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getTaskLabel(
                    token = "Bearer $token",
                    taskId = taskId
                )
                
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        labelData = responseBody.bytes()
                        setLoading(false)
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@PrintLabelActivity,
                                "Error: No se pudieron obtener los datos de la etiqueta",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@PrintLabelActivity,
                            "Error: ${response.code()} - ${response.message()}",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@PrintLabelActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        }
    }
    
    /**
     * Verifica si la app tiene los permisos necesarios para usar Bluetooth
     */
    private fun checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val bluetoothPermissions = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
            
            val hasPermissions = bluetoothPermissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
            
            if (!hasPermissions) {
                requestPermissionLauncher.launch(bluetoothPermissions)
                return
            }
        }
        
        // En Android 11 o inferior, solo necesitamos verificar si Bluetooth está habilitado
        if (!bluetoothPrinterManager.isBluetoothEnabled()) {
            showBluetoothDisabledUI()
        } else {
            loadPairedPrinters()
        }
    }
    
    /**
     * Solicita al usuario que habilite el Bluetooth
     */
    private fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableBluetoothLauncher.launch(enableBtIntent)
        } else {
            checkBluetoothPermissions()
        }
    }
    
    /**
     * Muestra la interfaz para cuando el Bluetooth está desactivado
     */
    private fun showBluetoothDisabledUI() {
        textViewNoDevices.text = "Bluetooth desactivado"
        textViewNoDevices.visibility = View.VISIBLE
        buttonEnableBluetooth.visibility = View.VISIBLE
        recyclerViewPrinters.visibility = View.GONE
    }
    
    /**
     * Carga la lista de impresoras Bluetooth emparejadas
     */
    private fun loadPairedPrinters() {
        if (!bluetoothPrinterManager.isBluetoothEnabled()) {
            showBluetoothDisabledUI()
            return
        }
        
        setLoading(true)
        
        lifecycleScope.launch(Dispatchers.IO) {
            val pairedPrinters = bluetoothPrinterManager.getPairedPrinters()
            
            withContext(Dispatchers.Main) {
                setLoading(false)
                
                if (pairedPrinters.isEmpty()) {
                    textViewNoDevices.text = "No se encontraron impresoras emparejadas"
                    textViewNoDevices.visibility = View.VISIBLE
                    recyclerViewPrinters.visibility = View.GONE
                } else {
                    textViewNoDevices.visibility = View.GONE
                    recyclerViewPrinters.visibility = View.VISIBLE
                    printerListAdapter?.updatePrinterList(pairedPrinters)
                }
                
                buttonEnableBluetooth.visibility = View.GONE
            }
        }
    }
    
    /**
     * Maneja la selección de una impresora
     */
    private fun onPrinterSelected(device: BluetoothDevice) {
        if (labelData == null) {
            Toast.makeText(
                this,
                "Error: No hay datos de etiqueta disponibles",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        AlertDialog.Builder(this)
            .setTitle("Imprimir etiqueta")
            .setMessage("¿Desea imprimir la etiqueta en ${device.name}?")
            .setPositiveButton("Imprimir") { _, _ ->
                printLabel(device)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Imprime la etiqueta en el dispositivo seleccionado
     */
    private fun printLabel(device: BluetoothDevice) {
        val data = labelData ?: return
        
        setLoading(true)
        
        lifecycleScope.launch {
            try {
                // Conectar a la impresora
                val socket = bluetoothPrinterManager.connectToDevice(device)
                
                if (socket != null) {
                    // Enviar datos a la impresora
                    val success = bluetoothPrinterManager.printRawData(socket, data)
                    
                    // Cerrar la conexión
                    bluetoothPrinterManager.closeConnection(socket)
                    
                    withContext(Dispatchers.Main) {
                        setLoading(false)
                        if (success) {
                            Toast.makeText(
                                this@PrintLabelActivity,
                                "Etiqueta enviada a la impresora",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this@PrintLabelActivity,
                                "Error al enviar datos a la impresora",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        setLoading(false)
                        Toast.makeText(
                            this@PrintLabelActivity,
                            "No se pudo conectar a la impresora",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    Toast.makeText(
                        this@PrintLabelActivity,
                        "Error de conexión: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    /**
     * Actualiza la UI para mostrar/ocultar indicadores de carga
     */
    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        recyclerViewPrinters.visibility = if (loading) View.GONE else View.VISIBLE
        
        if (!loading && !bluetoothPrinterManager.isBluetoothEnabled()) {
            showBluetoothDisabledUI()
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}