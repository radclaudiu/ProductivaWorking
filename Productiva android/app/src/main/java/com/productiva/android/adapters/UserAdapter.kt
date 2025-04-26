package com.productiva.android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.productiva.android.R
import com.productiva.android.model.User

/**
 * Adaptador para mostrar usuarios en un RecyclerView
 */
class UserAdapter(private val listener: OnUserClickListener) :
    ListAdapter<User, UserAdapter.UserViewHolder>(USER_COMPARATOR) {
    
    /**
     * Interface para manejar clics en los usuarios
     */
    interface OnUserClickListener {
        fun onUserClick(user: User)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }
    
    /**
     * ViewHolder para mostrar un usuario
     */
    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.name_text)
        private val usernameText: TextView = itemView.findViewById(R.id.username_text)
        private val companyText: TextView = itemView.findViewById(R.id.company_text)
        private val locationText: TextView = itemView.findViewById(R.id.location_text)
        
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val user = getItem(position)
                    listener.onUserClick(user)
                }
            }
        }
        
        /**
         * Vincula los datos del usuario a la vista
         */
        fun bind(user: User) {
            nameText.text = user.name
            usernameText.text = user.username
            
            // Mostrar la empresa si está disponible
            if (!user.companyName.isNullOrEmpty()) {
                companyText.visibility = View.VISIBLE
                companyText.text = user.companyName
            } else {
                companyText.visibility = View.GONE
            }
            
            // Mostrar la ubicación si está disponible
            if (!user.locationName.isNullOrEmpty()) {
                locationText.visibility = View.VISIBLE
                locationText.text = user.locationName
            } else {
                locationText.visibility = View.GONE
            }
            
            // Marcar usuarios inactivos
            itemView.alpha = if (user.isActive) 1.0f else 0.5f
        }
    }
    
    companion object {
        /**
         * Comparador para detectar cambios en la lista
         */
        private val USER_COMPARATOR = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.id == newItem.id
            }
            
            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }
        }
    }
}