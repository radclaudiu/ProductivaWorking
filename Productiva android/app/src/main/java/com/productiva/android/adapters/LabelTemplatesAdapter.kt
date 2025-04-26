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
import com.productiva.android.model.LabelTemplate

/**
 * Adaptador para mostrar plantillas de etiquetas en un RecyclerView.
 */
class LabelTemplatesAdapter(
    private val onTemplateClick: (LabelTemplate) -> Unit,
    private val onFavoriteClick: (LabelTemplate, Boolean) -> Unit
) : ListAdapter<LabelTemplate, LabelTemplatesAdapter.TemplateViewHolder>(TemplateDiffCallback()) {
    
    /**
     * ViewHolder para plantillas de etiquetas.
     */
    class TemplateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.templateName)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.templateDescription)
        private val sizeTextView: TextView = itemView.findViewById(R.id.templateSize)
        private val cardContainer: CardView = itemView.findViewById(R.id.templateCard)
        private val favoriteButton: ImageView = itemView.findViewById(R.id.favoriteButton)
        private val previewImage: ImageView = itemView.findViewById(R.id.templatePreview)
        
        /**
         * Vincula los datos de una plantilla a la vista.
         */
        fun bind(
            template: LabelTemplate, 
            onTemplateClick: (LabelTemplate) -> Unit,
            onFavoriteClick: (LabelTemplate, Boolean) -> Unit
        ) {
            nameTextView.text = template.name
            descriptionTextView.text = template.description ?: "Sin descripción"
            sizeTextView.text = template.getSizeDescription()
            
            // Configurar icono de favorito
            val favoriteIcon = if (template.isFavorite) {
                R.drawable.ic_star_filled
            } else {
                R.drawable.ic_star_outline
            }
            favoriteButton.setImageResource(favoriteIcon)
            
            // Cargar vista previa si está disponible
            template.previewUrl?.let { previewUrl ->
                // Aquí se cargaría la imagen con Glide, Picasso u otra biblioteca
                // Por ejemplo: Glide.with(itemView).load(previewUrl).into(previewImage)
                
                // Mientras tanto, mostrar un placeholder
                previewImage.setImageResource(R.drawable.placeholder_template)
                previewImage.visibility = View.VISIBLE
            } ?: run {
                previewImage.visibility = View.GONE
            }
            
            // Configurar eventos de clic
            cardContainer.setOnClickListener {
                onTemplateClick(template)
            }
            
            favoriteButton.setOnClickListener {
                val newState = !template.isFavorite
                onFavoriteClick(template, newState)
                
                // Actualizar la UI inmediatamente
                val newIcon = if (newState) {
                    R.drawable.ic_star_filled
                } else {
                    R.drawable.ic_star_outline
                }
                favoriteButton.setImageResource(newIcon)
            }
        }
    }
    
    /**
     * Crea un nuevo ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_label_template, parent, false)
        return TemplateViewHolder(view)
    }
    
    /**
     * Vincula los datos a un ViewHolder existente.
     */
    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        val template = getItem(position)
        holder.bind(template, onTemplateClick, onFavoriteClick)
    }
    
    /**
     * Callback para calcular las diferencias entre listas.
     */
    class TemplateDiffCallback : DiffUtil.ItemCallback<LabelTemplate>() {
        override fun areItemsTheSame(oldItem: LabelTemplate, newItem: LabelTemplate): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: LabelTemplate, newItem: LabelTemplate): Boolean {
            return oldItem == newItem
        }
    }
}