package com.productiva.android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.productiva.android.R
import com.productiva.android.model.SavedPrinter

/**
 * Adaptador para mostrar impresoras guardadas en un RecyclerView
 */
class SavedPrinterAdapter(
    private val onPrinterClickListener: (SavedPrinter) -> Unit,
    private val onDeleteClickListener: (SavedPrinter) -> Unit
) : RecyclerView.Adapter<SavedPrinterAdapter.PrinterViewHolder>() {
    
    private val printerList = mutableListOf<SavedPrinter>()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrinterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_printer, parent, false)
        return PrinterViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: PrinterViewHolder, position: Int) {
        val printer = printerList[position]
        holder.bind(printer)
    }
    
    override fun getItemCount(): Int = printerList.size
    
    /**
     * Actualiza la lista de impresoras
     */
    fun updatePrinters(printers: List<SavedPrinter>) {
        printerList.clear()
        printerList.addAll(printers)
        notifyDataSetChanged()
    }
    
    inner class PrinterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewPrinterName: TextView = itemView.findViewById(R.id.textViewPrinterName)
        private val textViewPrinterAddress: TextView = itemView.findViewById(R.id.textViewPrinterAddress)
        private val textViewPrinterInfo: TextView = itemView.findViewById(R.id.textViewPrinterInfo)
        private val textViewDefaultTag: TextView = itemView.findViewById(R.id.textViewDefaultTag)
        private val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)
        
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onPrinterClickListener(printerList[position])
                }
            }
            
            buttonDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClickListener(printerList[position])
                }
            }
        }
        
        fun bind(printer: SavedPrinter) {
            textViewPrinterName.text = printer.name
            textViewPrinterAddress.text = printer.address
            
            // Mostrar información del modelo y tamaño
            val model = formatPrinterModel(printer.printerModel)
            val size = "${printer.paperWidth}x${printer.paperLength}mm"
            textViewPrinterInfo.text = "$model - $size"
            
            // Mostrar etiqueta de predeterminado si corresponde
            textViewDefaultTag.visibility = if (printer.isDefault) View.VISIBLE else View.GONE
        }
        
        private fun formatPrinterModel(model: String): String {
            return when (model) {
                "BROTHER_QL800" -> "Brother QL-800"
                "BROTHER_QL820" -> "Brother QL-820NWB"
                "BROTHER_QL1100" -> "Brother QL-1100"
                else -> "Brother (Genérica)"
            }
        }
    }
}