package com.productiva.android.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.OutputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Clase para gestionar la impresión a través de Bluetooth
 * Proporciona métodos para buscar impresoras, conectarse a ellas e imprimir datos
 */
class BluetoothPrinterManager(private val context: Context) {
    
    companion object {
        private const val TAG = "BluetoothPrinterMgr"
        
        // UUID genérico para comunicación SPP (Serial Port Profile)
        private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        
        // UUID alternativo para algunos dispositivos que no usan el SPP estándar
        private val ALTERNATIVE_UUID = UUID.fromString("00001000-0000-1000-8000-00805F9B34FB")
        
        // Tipos de servicio para detectar impresoras
        private val PRINTER_SERVICE_STRINGS = listOf(
            "printing", "print", "printer", "btp", "sprinter", "tspl"
        )
    }
    
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothManager.adapter
        } else {
            @Suppress("DEPRECATION")
            BluetoothAdapter.getDefaultAdapter()
        }
    }
    
    /**
     * Verifica si el Bluetooth está disponible y habilitado en el dispositivo
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    
    /**
     * Obtiene la lista de dispositivos Bluetooth emparejados
     */
    fun getPairedDevices(): List<BluetoothDevice> {
        if (!isBluetoothEnabled()) {
            return emptyList()
        }
        
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return emptyList()
        }
        
        return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }
    
    /**
     * Obtiene la lista de impresoras Bluetooth emparejadas
     * Filtra dispositivos que probablemente sean impresoras basado en sus servicios
     */
    fun getPairedPrinters(): List<BluetoothDevice> {
        val pairedDevices = getPairedDevices()
        return pairedDevices.filter { device ->
            isPrinter(device)
        }
    }
    
    /**
     * Intenta determinar si el dispositivo es una impresora
     * basado en su nombre, clase o servicios disponibles
     */
    private fun isPrinter(device: BluetoothDevice): Boolean {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        
        // Verificar por nombre (muchas impresoras tienen "print" en su nombre)
        val deviceName = device.name?.lowercase() ?: ""
        if (deviceName.contains("print") || deviceName.contains("pt-") || 
            deviceName.contains("brother") || deviceName.contains("sprinter") ||
            deviceName.contains("epson") || deviceName.contains("zebra")) {
            return true
        }
        
        // Verificar por clase de dispositivo (0x0680 es la clase para impresoras)
        val deviceClass = device.bluetoothClass?.majorDeviceClass ?: 0
        if (deviceClass == 0x0680) {
            return true
        }
        
        // Enfoque más amplio: considerar todos los dispositivos como potenciales impresoras
        // para evitar excluir impresoras que no siguen las convenciones
        return true
    }
    
    /**
     * Intenta conectar a un dispositivo Bluetooth y abre un socket para comunicación
     */
    @Throws(IOException::class)
    suspend fun connectToDevice(device: BluetoothDevice): BluetoothSocket? {
        return withContext(Dispatchers.IO) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@withContext null
            }
            
            // Intentar primero con UUID estándar SPP
            try {
                val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                socket.connect()
                return@withContext socket
            } catch (e: IOException) {
                Log.d(TAG, "Failed to connect with SPP_UUID: ${e.message}")
                // Si falla, intentar con UUID alternativo
                try {
                    val socket = device.createRfcommSocketToServiceRecord(ALTERNATIVE_UUID)
                    socket.connect()
                    return@withContext socket
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to connect with ALTERNATIVE_UUID: ${e.message}")
                    throw e
                }
            }
        }
    }
    
    /**
     * Imprime datos binarios directamente al dispositivo Bluetooth conectado
     */
    @Throws(IOException::class)
    suspend fun printRawData(socket: BluetoothSocket, data: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            var outputStream: OutputStream? = null
            try {
                outputStream = socket.outputStream
                outputStream.write(data)
                outputStream.flush()
                return@withContext true
            } catch (e: IOException) {
                Log.e(TAG, "Error writing to Bluetooth socket: ${e.message}")
                throw e
            } finally {
                try {
                    outputStream?.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Error closing OutputStream: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Cierra la conexión Bluetooth
     */
    @Throws(IOException::class)
    suspend fun closeConnection(socket: BluetoothSocket) {
        withContext(Dispatchers.IO) {
            try {
                socket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing Bluetooth socket: ${e.message}")
                throw e
            }
        }
    }
}