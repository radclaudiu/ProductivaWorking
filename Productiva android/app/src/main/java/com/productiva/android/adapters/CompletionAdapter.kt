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
import com.productiva.android.model.TaskCompletion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adaptador para mostrar completaciones de tareas en un RecyclerView
 */
class CompletionAdapter : ListAdapter<TaskCompletion, CompletionAdapter.CompletionViewHolder>(CompletionDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_completion, parent, false)
        return CompletionViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CompletionViewHolder, position: Int) {
        val completion = getItem(position)
        holder.bind(completion)
    }
    
    /**
     * ViewHolder para las completaciones
     */
    inner class CompletionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.completion_date)
        private val userTextView: TextView = itemView.findViewById(R.id.completion_user)
        private val notesTextView: TextView = itemView.findViewById(R.id.completion_notes)
        private val statusTextView: TextView = itemView.findViewById(R.id.completion_status)
        private val timeSpentTextView: TextView = itemView.findViewById(R.id.completion_time_spent)
        private val clientNameTextView: TextView = itemView.findViewById(R.id.completion_client_name)
        private val hasSignatureIcon: ImageView = itemView.findViewById(R.id.has_signature_icon)
        private val hasPhotoIcon: ImageView = itemView.findViewById(R.id.has_photo_icon)
        
        /**
         * Vincula los datos de la completación con la vista
         */
        fun bind(completion: TaskCompletion) {
            // Fecha y hora
            dateTextView.text = completion.completionDate?.let { formatDate(it) } ?: ""
            
            // Usuario
            userTextView.text = completion.userName ?: "ID: ${completion.userId}"
            
            // Notas
            notesTextView.text = completion.notes ?: itemView.context.getString(R.string.no_notes)
            notesTextView.visibility = if (completion.notes.isNullOrEmpty()) View.GONE else View.VISIBLE
            
            // Estado
            statusTextView.text = when (completion.status) {
                "completed" -> itemView.context.getString(R.string.status_completed)
                "in_progress" -> itemView.context.getString(R.string.status_in_progress)
                "pending" -> itemView.context.getString(R.string.status_pending)
                else -> completion.status
            }
            
            // Tiempo empleado
            if (completion.timeSpent != null && completion.timeSpent > 0) {
                timeSpentTextView.text = itemView.context.getString(
                    R.string.time_spent_minutes,
                    completion.timeSpent
                )
                timeSpentTextView.visibility = View.VISIBLE
            } else {
                timeSpentTextView.visibility = View.GONE
            }
            
            // Nombre del cliente
            if (!completion.clientName.isNullOrEmpty()) {
                clientNameTextView.text = itemView.context.getString(
                    R.string.client_name,
                    completion.clientName
                )
                clientNameTextView.visibility = View.VISIBLE
            } else {
                clientNameTextView.visibility = View.GONE
            }
            
            // Iconos de firma y foto
            hasSignatureIcon.visibility = if (completion.hasSignature == true) View.VISIBLE else View.GONE
            hasPhotoIcon.visibility = if (completion.hasPhoto == true) View.VISIBLE else View.GONE
        }
        
        /**
         * Formatea una fecha para mostrarla
         */
        private fun formatDate(date: Date): String {
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return format.format(date)
        }
    }
    
    /**
     * DiffUtil para comparar completaciones eficientemente
     */
    class CompletionDiffCallback : DiffUtil.ItemCallback<TaskCompletion>() {
        override fun areItemsTheSame(oldItem: TaskCompletion, newItem: TaskCompletion): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: TaskCompletion, newItem: TaskCompletion): Boolean {
            return oldItem == newItem
        }
    }
}