package com.productiva.android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.productiva.android.R
import com.productiva.android.model.SavedPrinter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adaptador para mostrar impresoras guardadas en un RecyclerView.
 */
class PrintersAdapter(
    private val onPrinterClick: (SavedPrinter) -> Unit,
    private val onDefaultClick: (SavedPrinter) -> Unit,
    private val onDeleteClick: (SavedPrinter) -> Unit
) : ListAdapter<SavedPrinter, PrintersAdapter.PrinterViewHolder>(PrinterDiffCallback()) {
    
    /**
     * ViewHolder para impresoras.
     */
    class PrinterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.printerName)
        private val modelTextView: TextView = itemView.findViewById(R.id.printerModel)
        private val connectionTextView: TextView = itemView.findViewById(R.id.printerConnection)
        private val paperSizeTextView: TextView = itemView.findViewById(R.id.printerPaperSize)
        private val defaultIndicator: ImageView = itemView.findViewById(R.id.defaultIndicator)
        private val connectionIcon: ImageView = itemView.findViewById(R.id.connectionIcon)
        private val cardContainer: CardView = itemView.findViewById(R.id.printerCard)
        private val setDefaultButton: View = itemView.findViewById(R.id.buttonSetDefault)
        private val deleteButton: View = itemView.findViewById(R.id.buttonDelete)
        
        /**
         * Vincula los datos de una impresora a la vista.
         */
        fun bind(
            printer: SavedPrinter,
            onPrinterClick: (SavedPrinter) -> Unit,
            onDefaultClick: (SavedPrinter) -> Unit,
            onDeleteClick: (SavedPrinter) -> Unit
        ) {
            nameTextView.text = printer.name
            modelTextView.text = printer.model
            connectionTextView.text = printer.getConnectionTypeName()
            paperSizeTextView.text = printer.getPaperSizeDescription()
            
            // Mostrar icono de impresora predeterminada
            defaultIndicator.visibility = if (printer.isDefault) View.VISIBLE else View.GONE
            
            // Mostrar icono de tipo de conexión
            connectionIcon.setImageResource(printer.getConnectionIcon())
            
            // Configurar eventos de clic
            cardContainer.setOnClickListener {
                onPrinterClick(printer)
            }
            
            setDefaultButton.setOnClickListener {
                onDefaultClick(printer)
            }
            
            deleteButton.setOnClickListener {
                onDeleteClick(printer)
            }
        }
    }
    
    /**
     * Crea un nuevo ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrinterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_printer, parent, false)
        return PrinterViewHolder(view)
    }
    
    /**
     * Vincula los datos a un ViewHolder existente.
     */
    override fun onBindViewHolder(holder: PrinterViewHolder, position: Int) {
        val printer = getItem(position)
        holder.bind(printer, onPrinterClick, onDefaultClick, onDeleteClick)
    }
    
    /**
     * Callback para calcular las diferencias entre listas.
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
     * Formatea una fecha timestamp para mostrarla.
     */
    private fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return "Nunca"
        
        val date = Date(timestamp)
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
    }
}