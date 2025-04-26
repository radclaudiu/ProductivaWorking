package com.productiva.android.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.productiva.android.R
import com.productiva.android.model.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adaptador para mostrar tareas en un RecyclerView
 */
class TaskAdapter(private val listener: OnTaskClickListener) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)
    }
    
    /**
     * ViewHolder para las tareas
     */
    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.task_title)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.task_description)
        private val statusTextView: TextView = itemView.findViewById(R.id.task_status)
        private val dueDateTextView: TextView = itemView.findViewById(R.id.task_due_date)
        private val locationTextView: TextView = itemView.findViewById(R.id.task_location)
        private val priorityIndicator: View = itemView.findViewById(R.id.priority_indicator)
        private val cardView: CardView = itemView.findViewById(R.id.task_card)
        
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onTaskClick(getItem(position))
                }
            }
        }
        
        /**
         * Vincula los datos de la tarea con la vista
         */
        fun bind(task: Task) {
            // Título y descripción
            titleTextView.text = task.title
            descriptionTextView.text = task.description ?: itemView.context.getString(R.string.no_description)
            
            // Estado
            statusTextView.text = when (task.status) {
                "pending" -> itemView.context.getString(R.string.status_pending)
                "in_progress" -> itemView.context.getString(R.string.status_in_progress)
                "completed" -> itemView.context.getString(R.string.status_completed)
                else -> task.status
            }
            
            // Color según estado
            val statusColor = when (task.status) {
                "pending" -> Color.parseColor("#FFA000") // Ámbar
                "in_progress" -> Color.parseColor("#1976D2") // Azul
                "completed" -> Color.parseColor("#388E3C") // Verde
                else -> Color.GRAY
            }
            statusTextView.setTextColor(statusColor)
            
            // Fecha de vencimiento
            dueDateTextView.text = task.dueDate?.let { formatDate(it) } ?: 
                                   itemView.context.getString(R.string.no_due_date)
            
            // Ubicación
            locationTextView.text = task.locationName ?: 
                                   itemView.context.getString(R.string.no_location)
            
            // Indicador de prioridad
            val priorityColor = when (task.priority) {
                1 -> Color.parseColor("#4CAF50") // Verde - Baja
                2 -> Color.parseColor("#FFC107") // Amarillo - Media
                3 -> Color.parseColor("#F44336") // Rojo - Alta
                else -> Color.GRAY // Desconocida
            }
            priorityIndicator.setBackgroundColor(priorityColor)
            
            // Destacar tareas vencidas
            val isOverdue = task.dueDate?.before(Date()) == true && task.status != "completed"
            if (isOverdue) {
                cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE")) // Rojo muy claro
                dueDateTextView.setTextColor(Color.RED)
            } else {
                cardView.setCardBackgroundColor(Color.WHITE)
                dueDateTextView.setTextColor(Color.DKGRAY)
            }
        }
        
        /**
         * Formatea una fecha para mostrarla
         */
        private fun formatDate(date: Date): String {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return format.format(date)
        }
    }
    
    /**
     * DiffUtil para comparar tareas eficientemente
     */
    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
    
    /**
     * Interfaz para manejar los clics en las tareas
     */
    interface OnTaskClickListener {
        fun onTaskClick(task: Task)
    }
}