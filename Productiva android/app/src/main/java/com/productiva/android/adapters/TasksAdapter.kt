package com.productiva.android.adapters

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
import com.productiva.android.utils.DateConverters
import java.util.Date

/**
 * Adaptador para mostrar tareas en un RecyclerView.
 */
class TasksAdapter(
    private val onTaskClick: (Task) -> Unit
) : ListAdapter<Task, TasksAdapter.TaskViewHolder>(TaskDiffCallback()) {
    
    /**
     * ViewHolder para tareas.
     */
    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.taskTitle)
        private val statusTextView: TextView = itemView.findViewById(R.id.taskStatus)
        private val dueDateTextView: TextView = itemView.findViewById(R.id.taskDueDate)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.taskDescription)
        private val cardContainer: CardView = itemView.findViewById(R.id.taskCard)
        private val priorityIndicator: View = itemView.findViewById(R.id.priorityIndicator)
        
        /**
         * Vincula los datos de una tarea a la vista.
         */
        fun bind(task: Task, onTaskClick: (Task) -> Unit) {
            titleTextView.text = task.title
            statusTextView.text = task.getLocalizedStatus()
            
            // Formatear fecha
            dueDateTextView.text = if (task.dueDate != null) {
                DateConverters.formatDate(task.dueDate)
            } else {
                "Sin fecha"
            }
            
            // Mostrar descripción o un texto predeterminado
            descriptionTextView.text = task.description ?: "Sin descripción"
            
            // Configurar colores según estado y prioridad
            statusTextView.setBackgroundColor(task.getStatusColor())
            priorityIndicator.setBackgroundColor(task.getPriorityColor())
            
            // Destacar tareas vencidas
            if (task.isOverdue()) {
                dueDateTextView.setTextColor(0xFFE53935.toInt()) // Rojo
                dueDateTextView.text = "¡VENCIDA! ${dueDateTextView.text}"
            } else {
                dueDateTextView.setTextColor(0xFF757575.toInt()) // Gris
            }
            
            // Configurar evento de clic
            cardContainer.setOnClickListener {
                onTaskClick(task)
            }
        }
    }
    
    /**
     * Crea un nuevo ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }
    
    /**
     * Vincula los datos a un ViewHolder existente.
     */
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task, onTaskClick)
    }
    
    /**
     * Callback para calcular las diferencias entre listas.
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