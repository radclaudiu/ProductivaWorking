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
import com.productiva.android.model.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adaptador para mostrar tareas en un RecyclerView
 */
class TaskAdapter(private val listener: OnTaskClickListener) :
    ListAdapter<Task, TaskAdapter.TaskViewHolder>(TASK_COMPARATOR) {
    
    /**
     * Interface para manejar clics en las tareas
     */
    interface OnTaskClickListener {
        fun onTaskClick(task: Task)
        fun onTaskStatusChange(task: Task, newStatus: String)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)
    }
    
    /**
     * ViewHolder para mostrar una tarea
     */
    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.card_view)
        private val titleText: TextView = itemView.findViewById(R.id.title_text)
        private val descriptionText: TextView = itemView.findViewById(R.id.description_text)
        private val statusText: TextView = itemView.findViewById(R.id.status_text)
        private val dueDateText: TextView = itemView.findViewById(R.id.due_date_text)
        private val locationText: TextView = itemView.findViewById(R.id.location_text)
        private val priorityIcon: ImageView = itemView.findViewById(R.id.priority_icon)
        private val statusChangeButton: View = itemView.findViewById(R.id.status_change_button)
        
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val task = getItem(position)
                    listener.onTaskClick(task)
                }
            }
            
            statusChangeButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val task = getItem(position)
                    // Determinar el siguiente estado basado en el estado actual
                    val newStatus = when (task.status) {
                        Task.STATUS_PENDING -> Task.STATUS_IN_PROGRESS
                        Task.STATUS_IN_PROGRESS -> Task.STATUS_COMPLETED
                        Task.STATUS_COMPLETED -> Task.STATUS_PENDING
                        else -> Task.STATUS_PENDING
                    }
                    listener.onTaskStatusChange(task, newStatus)
                }
            }
        }
        
        /**
         * Vincula los datos de la tarea a la vista
         */
        fun bind(task: Task) {
            titleText.text = task.title
            
            // Descripción (opcional)
            if (!task.description.isNullOrEmpty()) {
                descriptionText.visibility = View.VISIBLE
                descriptionText.text = task.description
            } else {
                descriptionText.visibility = View.GONE
            }
            
            // Estado
            statusText.text = getStatusDisplayName(task.status)
            statusText.setTextColor(getStatusColor(task.status))
            
            // Fecha de vencimiento
            if (task.dueDate != null) {
                dueDateText.visibility = View.VISIBLE
                dueDateText.text = dateFormat.format(task.dueDate)
                
                // Resaltar fecha vencida
                if (task.isPastDue()) {
                    dueDateText.setTextColor(Color.RED)
                } else {
                    dueDateText.setTextColor(Color.BLACK)
                }
            } else {
                dueDateText.visibility = View.GONE
            }
            
            // Ubicación
            if (!task.locationName.isNullOrEmpty()) {
                locationText.visibility = View.VISIBLE
                locationText.text = task.locationName
            } else {
                locationText.visibility = View.GONE
            }
            
            // Prioridad
            when (task.priority) {
                2 -> { // Alta
                    priorityIcon.visibility = View.VISIBLE
                    priorityIcon.setImageResource(R.drawable.ic_priority_high)
                    priorityIcon.setColorFilter(Color.RED)
                }
                1 -> { // Media
                    priorityIcon.visibility = View.VISIBLE
                    priorityIcon.setImageResource(R.drawable.ic_priority_medium)
                    priorityIcon.setColorFilter(Color.rgb(255, 165, 0)) // Orange
                }
                else -> { // Baja o sin prioridad
                    priorityIcon.visibility = View.GONE
                }
            }
            
            // Cambiar color de fondo según estado
            cardView.setCardBackgroundColor(getCardBackgroundColor(task.status))
        }
        
        /**
         * Obtiene el nombre de visualización del estado
         */
        private fun getStatusDisplayName(status: String): String {
            return when (status) {
                Task.STATUS_PENDING -> "Pendiente"
                Task.STATUS_IN_PROGRESS -> "En Progreso"
                Task.STATUS_COMPLETED -> "Completada"
                Task.STATUS_CANCELLED -> "Cancelada"
                else -> status
            }
        }
        
        /**
         * Obtiene el color para el texto del estado
         */
        private fun getStatusColor(status: String): Int {
            return when (status) {
                Task.STATUS_PENDING -> Color.parseColor("#FF9800") // Orange
                Task.STATUS_IN_PROGRESS -> Color.parseColor("#2196F3") // Blue
                Task.STATUS_COMPLETED -> Color.parseColor("#4CAF50") // Green
                Task.STATUS_CANCELLED -> Color.parseColor("#F44336") // Red
                else -> Color.BLACK
            }
        }
        
        /**
         * Obtiene el color de fondo para la tarjeta según el estado
         */
        private fun getCardBackgroundColor(status: String): Int {
            return when (status) {
                Task.STATUS_PENDING -> Color.parseColor("#FFF3E0") // Light Orange
                Task.STATUS_IN_PROGRESS -> Color.parseColor("#E3F2FD") // Light Blue
                Task.STATUS_COMPLETED -> Color.parseColor("#E8F5E9") // Light Green
                Task.STATUS_CANCELLED -> Color.parseColor("#FFEBEE") // Light Red
                else -> Color.WHITE
            }
        }
    }
    
    companion object {
        /**
         * Comparador para detectar cambios en la lista
         */
        private val TASK_COMPARATOR = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem.id == newItem.id
            }
            
            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem == newItem
            }
        }
    }
}