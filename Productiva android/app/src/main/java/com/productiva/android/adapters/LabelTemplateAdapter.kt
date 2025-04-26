package com.productiva.android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.productiva.android.R
import com.productiva.android.model.LabelTemplate

/**
 * Adaptador para mostrar plantillas de etiquetas en un RecyclerView
 */
class LabelTemplateAdapter(
    private val onTemplateClickListener: (LabelTemplate) -> Unit,
    private val onDeleteClickListener: (LabelTemplate) -> Unit
) : RecyclerView.Adapter<LabelTemplateAdapter.TemplateViewHolder>() {
    
    private val templateList = mutableListOf<LabelTemplate>()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_label_template, parent, false)
        return TemplateViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        val template = templateList[position]
        holder.bind(template)
    }
    
    override fun getItemCount(): Int = templateList.size
    
    /**
     * Actualiza la lista de plantillas
     */
    fun updateTemplates(templates: List<LabelTemplate>) {
        templateList.clear()
        templateList.addAll(templates)
        notifyDataSetChanged()
    }
    
    inner class TemplateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTemplateName: TextView = itemView.findViewById(R.id.textViewTemplateName)
        private val textViewTemplateSize: TextView = itemView.findViewById(R.id.textViewTemplateSize)
        private val textViewTemplateContent: TextView = itemView.findViewById(R.id.textViewTemplateContent)
        private val textViewDefaultTag: TextView = itemView.findViewById(R.id.textViewDefaultTag)
        private val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)
        
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTemplateClickListener(templateList[position])
                }
            }
            
            buttonDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClickListener(templateList[position])
                }
            }
        }
        
        fun bind(template: LabelTemplate) {
            textViewTemplateName.text = template.name
            
            // Mostrar tamaño
            textViewTemplateSize.text = "${template.widthMm}x${template.heightMm}mm"
            
            // Mostrar contenido habilitado
            val contentElements = mutableListOf<String>()
            if (template.showTitle) contentElements.add("Título")
            if (template.showExtraText) contentElements.add("Texto")
            if (template.showDate) contentElements.add("Fecha")
            if (template.showQrCode) contentElements.add("QR")
            if (template.showBarcode) contentElements.add("Código")
            
            textViewTemplateContent.text = contentElements.joinToString(", ")
            
            // Mostrar etiqueta de predeterminado si corresponde
            textViewDefaultTag.visibility = if (template.isDefault) View.VISIBLE else View.GONE
        }
    }
}