package com.productiva.android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.productiva.android.R
import com.productiva.android.models.User

/**
 * Adaptador para la lista de usuarios
 */
class UserAdapter(private val onUserClick: (User) -> Unit) : 
    ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view, onUserClick)
    }
    
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class UserViewHolder(
        itemView: View,
        private val onUserClick: (User) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val nameTextView: TextView = itemView.findViewById(R.id.textViewUserName)
        private val roleTextView: TextView = itemView.findViewById(R.id.textViewUserRole)
        private val userIconImageView: ImageView = itemView.findViewById(R.id.imageViewUserIcon)
        
        fun bind(user: User) {
            nameTextView.text = user.name ?: user.username
            roleTextView.text = formatRole(user.role)
            
            // Ajustar ícono según el rol
            when (user.role.lowercase()) {
                "admin" -> userIconImageView.setImageResource(R.drawable.ic_admin)
                "manager" -> userIconImageView.setImageResource(R.drawable.ic_manager)
                else -> userIconImageView.setImageResource(R.drawable.ic_user)
            }
            
            // Configurar clic para seleccionar el usuario
            itemView.setOnClickListener {
                onUserClick(user)
            }
        }
        
        /**
         * Formatea el rol del usuario para mostrarlo
         */
        private fun formatRole(role: String): String {
            return when (role.lowercase()) {
                "admin" -> "Administrador"
                "manager" -> "Gerente"
                "user" -> "Usuario"
                "local_user" -> "Usuario Local"
                else -> role.replaceFirstChar { it.uppercase() }
            }
        }
    }
    
    /**
     * DiffUtil para optimizar actualizaciones en RecyclerView
     */
    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}