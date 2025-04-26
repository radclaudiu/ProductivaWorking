package com.productiva.android.adapters

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.productiva.android.R

/**
 * Adaptador para mostrar dispositivos Bluetooth en un RecyclerView
 */
class PrinterListAdapter(
    private val onDeviceClickListener: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<PrinterListAdapter.PrinterViewHolder>() {
    
    private val printerList = mutableListOf<BluetoothDevice>()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrinterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_printer, parent, false)
        return PrinterViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: PrinterViewHolder, position: Int) {
        val device = printerList[position]
        holder.bind(device)
    }
    
    override fun getItemCount(): Int = printerList.size
    
    /**
     * Actualiza la lista de impresoras
     */
    fun updatePrinterList(devices: List<BluetoothDevice>) {
        printerList.clear()
        printerList.addAll(devices)
        notifyDataSetChanged()
    }
    
    inner class PrinterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewPrinterName: TextView = itemView.findViewById(R.id.textViewPrinterName)
        private val textViewPrinterAddress: TextView = itemView.findViewById(R.id.textViewPrinterAddress)
        
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeviceClickListener(printerList[position])
                }
            }
        }
        
        fun bind(device: BluetoothDevice) {
            if (ActivityCompat.checkSelfPermission(
                    itemView.context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                textViewPrinterName.text = device.name ?: "Dispositivo desconocido"
                textViewPrinterAddress.text = device.address
            } else {
                textViewPrinterName.text = "Dispositivo Bluetooth"
                textViewPrinterAddress.text = "Se requieren permisos"
            }
        }
    }
}