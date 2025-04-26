package com.productiva.android.adapters

import android.graphics.BitmapFactory
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adaptador para mostrar completaciones de tareas en un RecyclerView
 */
class CompletionAdapter :
    ListAdapter<TaskCompletion, CompletionAdapter.CompletionViewHolder>(COMPLETION_COMPARATOR) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_completion, parent, false)
        return CompletionViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CompletionViewHolder, position: Int) {
        val completion = getItem(position)
        holder.bind(completion)
    }
    
    /**
     * ViewHolder para mostrar una completación de tarea
     */
    inner class CompletionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userNameText: TextView = itemView.findViewById(R.id.user_name_text)
        private val dateText: TextView = itemView.findViewById(R.id.date_text)
        private val notesText: TextView = itemView.findViewById(R.id.notes_text)
        private val locationText: TextView = itemView.findViewById(R.id.location_text)
        private val signatureImage: ImageView = itemView.findViewById(R.id.signature_image)
        private val photoImage: ImageView = itemView.findViewById(R.id.photo_image)
        private val signatureLabel: TextView = itemView.findViewById(R.id.signature_label)
        private val photoLabel: TextView = itemView.findViewById(R.id.photo_label)
        
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        
        /**
         * Vincula los datos de la completación a la vista
         */
        fun bind(completion: TaskCompletion) {
            // Información del usuario
            userNameText.text = completion.userName ?: "Usuario desconocido"
            
            // Fecha de completación
            dateText.text = completion.completionDate?.let { dateFormat.format(it) } ?: "Fecha desconocida"
            
            // Notas
            if (!completion.notes.isNullOrEmpty()) {
                notesText.visibility = View.VISIBLE
                notesText.text = completion.notes
            } else {
                notesText.visibility = View.GONE
            }
            
            // Ubicación
            if (!completion.locationName.isNullOrEmpty()) {
                locationText.visibility = View.VISIBLE
                locationText.text = "En: ${completion.locationName}"
            } else {
                locationText.visibility = View.GONE
            }
            
            // Firma
            if (completion.hasSignature && !completion.signaturePath.isNullOrEmpty()) {
                val signatureFile = File(completion.signaturePath)
                if (signatureFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(signatureFile.absolutePath)
                    signatureImage.setImageBitmap(bitmap)
                    signatureImage.visibility = View.VISIBLE
                    signatureLabel.visibility = View.VISIBLE
                } else {
                    signatureImage.visibility = View.GONE
                    signatureLabel.visibility = View.GONE
                }
            } else {
                signatureImage.visibility = View.GONE
                signatureLabel.visibility = View.GONE
            }
            
            // Foto
            if (completion.hasPhoto && !completion.photoPath.isNullOrEmpty()) {
                val photoFile = File(completion.photoPath)
                if (photoFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    photoImage.setImageBitmap(bitmap)
                    photoImage.visibility = View.VISIBLE
                    photoLabel.visibility = View.VISIBLE
                } else {
                    photoImage.visibility = View.GONE
                    photoLabel.visibility = View.GONE
                }
            } else {
                photoImage.visibility = View.GONE
                photoLabel.visibility = View.GONE
            }
        }
    }
    
    companion object {
        /**
         * Comparador para detectar cambios en la lista
         */
        private val COMPLETION_COMPARATOR = object : DiffUtil.ItemCallback<TaskCompletion>() {
            override fun areItemsTheSame(oldItem: TaskCompletion, newItem: TaskCompletion): Boolean {
                return oldItem.id == newItem.id
            }
            
            override fun areContentsTheSame(oldItem: TaskCompletion, newItem: TaskCompletion): Boolean {
                return oldItem == newItem
            }
        }
    }
}