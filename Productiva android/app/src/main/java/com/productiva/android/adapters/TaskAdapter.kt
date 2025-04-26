package com.productiva.android.adapters

import android.graphics.Color
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
import com.productiva.android.models.Task
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adaptador para la lista de tareas
 */
class TaskAdapter(private val onTaskClick: (Task) -> Unit) :
    ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view, onTaskClick)
    }
    
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class TaskViewHolder(
        itemView: View,
        private val onTaskClick: (Task) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewTaskTitle)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.textViewTaskDescription)
        private val frequencyTextView: TextView = itemView.findViewById(R.id.textViewTaskFrequency)
        private val cardView: CardView = itemView.findViewById(R.id.cardViewTask)
        private val priorityIndicator: View = itemView.findViewById(R.id.viewPriorityIndicator)
        private val labelIcon: ImageView = itemView.findViewById(R.id.imageViewLabelIcon)
        private val photoIcon: ImageView = itemView.findViewById(R.id.imageViewPhotoIcon)
        private val signatureIcon: ImageView = itemView.findViewById(R.id.imageViewSignatureIcon)
        
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        fun bind(task: Task) {
            titleTextView.text = task.title
            
            // Descripción o fecha si está disponible
            if (!task.description.isNullOrEmpty()) {
                descriptionTextView.text = task.description
                descriptionTextView.visibility = View.VISIBLE
            } else if (!task.dueDate.isNullOrEmpty()) {
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val date = inputFormat.parse(task.dueDate)
                    if (date != null) {
                        descriptionTextView.text = "Vence: ${dateFormat.format(date)}"
                        descriptionTextView.visibility = View.VISIBLE
                    } else {
                        descriptionTextView.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    descriptionTextView.visibility = View.GONE
                }
            } else {
                descriptionTextView.visibility = View.GONE
            }
            
            // Frecuencia
            frequencyTextView.text = formatFrequency(task.frequency)
            
            // Indicador de prioridad
            when (task.priority) {
                2 -> { // Alta
                    priorityIndicator.setBackgroundColor(Color.RED)
                    priorityIndicator.visibility = View.VISIBLE
                }
                1 -> { // Media
                    priorityIndicator.setBackgroundColor(Color.YELLOW)
                    priorityIndicator.visibility = View.VISIBLE
                }
                else -> { // Baja o normal
                    priorityIndicator.visibility = View.GONE
                }
            }
            
            // Mostrar íconos relevantes
            labelIcon.visibility = if (task.printLabel) View.VISIBLE else View.GONE
            photoIcon.visibility = if (task.needsPhoto) View.VISIBLE else View.GONE
            signatureIcon.visibility = if (task.needsSignature) View.VISIBLE else View.GONE
            
            // Color de fondo según estado
            when (task.status.lowercase()) {
                "pending" -> cardView.setCardBackgroundColor(Color.WHITE)
                "in_progress" -> cardView.setCardBackgroundColor(Color.parseColor("#E3F2FD")) // Azul claro
                "overdue" -> cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE")) // Rojo claro
                else -> cardView.setCardBackgroundColor(Color.WHITE)
            }
            
            // Configurar clic para ver detalle de la tarea
            itemView.setOnClickListener {
                onTaskClick(task)
            }
        }
        
        /**
         * Formatea la frecuencia de la tarea para mostrarla
         */
        private fun formatFrequency(frequency: String): String {
            return when (frequency.lowercase()) {
                "daily" -> "Diaria"
                "weekly" -> "Semanal"
                "monthly" -> "Mensual"
                "work_days" -> "Días laborables"
                "weekends" -> "Fines de semana"
                else -> frequency.replaceFirstChar { it.uppercase() }
            }
        }
    }
    
    /**
     * DiffUtil para optimizar actualizaciones en RecyclerView
     */
    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}