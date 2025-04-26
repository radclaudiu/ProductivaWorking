package com.productiva.android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.productiva.android.R
import com.productiva.android.model.User

/**
 * Adaptador para mostrar usuarios en un RecyclerView
 */
class UserListAdapter(
    private val onUserClickListener: (User) -> Unit
) : RecyclerView.Adapter<UserListAdapter.UserViewHolder>() {
    
    private val userList = mutableListOf<User>()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }
    
    override fun getItemCount(): Int = userList.size
    
    /**
     * Actualiza la lista de usuarios
     */
    fun updateUserList(users: List<User>) {
        userList.clear()
        userList.addAll(users)
        notifyDataSetChanged()
    }
    
    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewUserIcon: ImageView = itemView.findViewById(R.id.imageViewUserIcon)
        private val textViewUserName: TextView = itemView.findViewById(R.id.textViewUserName)
        private val textViewUserRole: TextView = itemView.findViewById(R.id.textViewUserRole)
        
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onUserClickListener(userList[position])
                }
            }
        }
        
        fun bind(user: User) {
            textViewUserName.text = user.name ?: user.username
            textViewUserRole.text = formatRole(user.role)
            
            // Configurar ícono según el rol
            val iconResId = when (user.role.lowercase()) {
                "administrator", "admin" -> R.drawable.ic_admin
                "manager" -> R.drawable.ic_manager
                "employee" -> R.drawable.ic_employee
                else -> R.drawable.ic_user
            }
            imageViewUserIcon.setImageResource(iconResId)
        }
        
        private fun formatRole(role: String): String {
            return when (role.lowercase()) {
                "administrator", "admin" -> "Administrador"
                "manager" -> "Gerente"
                "employee" -> "Empleado"
                "user" -> "Usuario"
                else -> role.replaceFirstChar { it.uppercase() }
            }
        }
    }
}