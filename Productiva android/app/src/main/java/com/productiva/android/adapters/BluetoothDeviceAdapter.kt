package com.productiva.android.adapters

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.productiva.android.R

/**
 * Adaptador para mostrar dispositivos Bluetooth en un RecyclerView
 */
class BluetoothDeviceAdapter(
    private val onDeviceClickListener: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder>() {
    
    private val deviceList = mutableListOf<BluetoothDevice>()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bluetooth_device, parent, false)
        return DeviceViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = deviceList[position]
        holder.bind(device)
    }
    
    override fun getItemCount(): Int = deviceList.size
    
    /**
     * Actualiza la lista de dispositivos
     */
    fun updateDevices(devices: List<BluetoothDevice>) {
        deviceList.clear()
        deviceList.addAll(devices)
        notifyDataSetChanged()
    }
    
    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewDeviceName: TextView = itemView.findViewById(R.id.textViewDeviceName)
        private val textViewDeviceAddress: TextView = itemView.findViewById(R.id.textViewDeviceAddress)
        private val imageViewDeviceIcon: ImageView = itemView.findViewById(R.id.imageViewDeviceIcon)
        
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeviceClickListener(deviceList[position])
                }
            }
        }
        
        fun bind(device: BluetoothDevice) {
            // Nombre del dispositivo (o dirección si no tiene nombre)
            val deviceName = device.name ?: "Dispositivo desconocido"
            textViewDeviceName.text = deviceName
            
            // Dirección MAC
            textViewDeviceAddress.text = device.address
            
            // Seleccionar ícono según tipo de dispositivo
            when (device.type) {
                BluetoothDevice.DEVICE_TYPE_CLASSIC -> {
                    imageViewDeviceIcon.setImageResource(R.drawable.ic_bluetooth_classic)
                }
                BluetoothDevice.DEVICE_TYPE_LE -> {
                    imageViewDeviceIcon.setImageResource(R.drawable.ic_bluetooth_le)
                }
                BluetoothDevice.DEVICE_TYPE_DUAL -> {
                    imageViewDeviceIcon.setImageResource(R.drawable.ic_bluetooth_dual)
                }
                else -> {
                    imageViewDeviceIcon.setImageResource(R.drawable.ic_bluetooth)
                }
            }
        }
    }
}