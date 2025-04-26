package com.productiva.android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.productiva.android.R
import com.productiva.android.model.LabelTemplate

/**
 * Adaptador para mostrar plantillas de etiquetas en un RecyclerView
 */
class LabelTemplateAdapter(private val listener: OnTemplateClickListener) : ListAdapter<LabelTemplate, LabelTemplateAdapter.TemplateViewHolder>(TemplateDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_label_template, parent, false)
        return TemplateViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        val template = getItem(position)
        holder.bind(template)
    }
    
    /**
     * ViewHolder para las plantillas
     */
    inner class TemplateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.template_name)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.template_description)
        private val previewTextView: TextView = itemView.findViewById(R.id.template_preview)
        private val editButton: View = itemView.findViewById(R.id.edit_button)
        private val deleteButton: View = itemView.findViewById(R.id.delete_button)
        
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onTemplateClick(getItem(position))
                }
            }
            
            editButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onEditTemplate(getItem(position))
                }
            }
            
            deleteButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeleteTemplate(getItem(position))
                }
            }
        }
        
        /**
         * Vincula los datos de la plantilla con la vista
         */
        fun bind(template: LabelTemplate) {
            nameTextView.text = template.name
            
            // Descripción
            if (template.description.isNullOrEmpty()) {
                descriptionTextView.visibility = View.GONE
            } else {
                descriptionTextView.text = template.description
                descriptionTextView.visibility = View.VISIBLE
            }
            
            // Vista previa truncada del contenido
            val previewContent = template.content.take(100).let {
                if (template.content.length > 100) "$it..." else it
            }
            previewTextView.text = previewContent
        }
    }
    
    /**
     * DiffUtil para comparar plantillas eficientemente
     */
    class TemplateDiffCallback : DiffUtil.ItemCallback<LabelTemplate>() {
        override fun areItemsTheSame(oldItem: LabelTemplate, newItem: LabelTemplate): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: LabelTemplate, newItem: LabelTemplate): Boolean {
            return oldItem == newItem
        }
    }
    
    /**
     * Interfaz para manejar los clics en las plantillas
     */
    interface OnTemplateClickListener {
        fun onTemplateClick(template: LabelTemplate)
        fun onEditTemplate(template: LabelTemplate)
        fun onDeleteTemplate(template: LabelTemplate)
    }
}