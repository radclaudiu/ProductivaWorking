package com.productiva.android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.productiva.android.R
import com.productiva.android.model.SavedPrinter

/**
 * Adaptador para mostrar impresoras guardadas en un RecyclerView
 */
class PrinterAdapter(private val listener: OnPrinterClickListener) : ListAdapter<SavedPrinter, PrinterAdapter.PrinterViewHolder>(PrinterDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrinterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_printer, parent, false)
        return PrinterViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: PrinterViewHolder, position: Int) {
        val printer = getItem(position)
        holder.bind(printer)
    }
    
    /**
     * ViewHolder para las impresoras
     */
    inner class PrinterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.printer_name)
        private val typeTextView: TextView = itemView.findViewById(R.id.printer_type)
        private val addressTextView: TextView = itemView.findViewById(R.id.printer_address)
        private val defaultIndicator: ImageView = itemView.findViewById(R.id.default_indicator)
        private val editButton: View = itemView.findViewById(R.id.edit_button)
        private val deleteButton: View = itemView.findViewById(R.id.delete_button)
        
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onPrinterClick(getItem(position))
                }
            }
            
            editButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onEditPrinter(getItem(position))
                }
            }
            
            deleteButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeletePrinter(getItem(position))
                }
            }
        }
        
        /**
         * Vincula los datos de la impresora con la vista
         */
        fun bind(printer: SavedPrinter) {
            nameTextView.text = printer.name
            typeTextView.text = getPrinterTypeText(printer.printerType)
            addressTextView.text = printer.address
            
            // Mostrar indicador de impresora predeterminada
            defaultIndicator.visibility = if (printer.isDefault) View.VISIBLE else View.INVISIBLE
        }
        
        /**
         * Obtiene el texto para el tipo de impresora
         */
        private fun getPrinterTypeText(printerType: String): String {
            return when (printerType) {
                "BROTHER_CPCL" -> itemView.context.getString(R.string.printer_type_brother_cpcl)
                "BROTHER_ESC_POS" -> itemView.context.getString(R.string.printer_type_brother_esc_pos)
                "GENERIC_ESC_POS" -> itemView.context.getString(R.string.printer_type_generic_esc_pos)
                else -> printerType
            }
        }
    }
    
    /**
     * DiffUtil para comparar impresoras eficientemente
     */
    class PrinterDiffCallback : DiffUtil.ItemCallback<SavedPrinter>() {
        override fun areItemsTheSame(oldItem: SavedPrinter, newItem: SavedPrinter): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: SavedPrinter, newItem: SavedPrinter): Boolean {
            return oldItem == newItem
        }
    }
    
    /**
     * Interfaz para manejar los clics en las impresoras
     */
    interface OnPrinterClickListener {
        fun onPrinterClick(printer: SavedPrinter)
        fun onEditPrinter(printer: SavedPrinter)
        fun onDeletePrinter(printer: SavedPrinter)
    }
}