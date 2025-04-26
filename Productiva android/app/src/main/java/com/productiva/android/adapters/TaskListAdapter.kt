package com.productiva.android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.productiva.android.R
import com.productiva.android.model.Task

/**
 * Adaptador para mostrar tareas en un RecyclerView
 */
class TaskListAdapter(
    private val onTaskClickListener: (Task) -> Unit
) : RecyclerView.Adapter<TaskListAdapter.TaskViewHolder>() {
    
    private val taskList = mutableListOf<Task>()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.bind(task)
    }
    
    override fun getItemCount(): Int = taskList.size
    
    /**
     * Actualiza la lista de tareas
     */
    fun updateTaskList(tasks: List<Task>) {
        taskList.clear()
        taskList.addAll(tasks)
        notifyDataSetChanged()
    }
    
    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardViewTask: CardView = itemView.findViewById(R.id.cardViewTask)
        private val viewPriorityIndicator: View = itemView.findViewById(R.id.viewPriorityIndicator)
        private val textViewTaskTitle: TextView = itemView.findViewById(R.id.textViewTaskTitle)
        private val textViewTaskDescription: TextView = itemView.findViewById(R.id.textViewTaskDescription)
        private val textViewTaskFrequency: TextView = itemView.findViewById(R.id.textViewTaskFrequency)
        private val imageViewLabelIcon: ImageView = itemView.findViewById(R.id.imageViewLabelIcon)
        private val imageViewPhotoIcon: ImageView = itemView.findViewById(R.id.imageViewPhotoIcon)
        private val imageViewSignatureIcon: ImageView = itemView.findViewById(R.id.imageViewSignatureIcon)
        
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTaskClickListener(taskList[position])
                }
            }
        }
        
        fun bind(task: Task) {
            textViewTaskTitle.text = task.title
            textViewTaskDescription.text = task.description ?: "Sin descripción"
            textViewTaskFrequency.text = formatFrequency(task.frequency)
            
            // Configurar indicador de prioridad
            val context = itemView.context
            if (task.priority > 0) {
                viewPriorityIndicator.visibility = View.VISIBLE
                val priorityColor = when (task.priority) {
                    3 -> ContextCompat.getColor(context, android.R.color.holo_red_dark)
                    2 -> ContextCompat.getColor(context, android.R.color.holo_orange_dark)
                    else -> ContextCompat.getColor(context, android.R.color.holo_green_dark)
                }
                viewPriorityIndicator.setBackgroundColor(priorityColor)
            } else {
                viewPriorityIndicator.visibility = View.GONE
            }
            
            // Mostrar iconos de requisitos
            imageViewLabelIcon.visibility = if (task.printLabel) View.VISIBLE else View.GONE
            imageViewPhotoIcon.visibility = if (task.needsPhoto) View.VISIBLE else View.GONE
            imageViewSignatureIcon.visibility = if (task.needsSignature) View.VISIBLE else View.GONE
        }
        
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
}