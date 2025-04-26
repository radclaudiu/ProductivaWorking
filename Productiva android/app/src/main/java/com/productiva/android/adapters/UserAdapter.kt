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
class UserAdapter(private val listener: OnUserClickListener) : ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }
    
    /**
     * ViewHolder para los usuarios
     */
    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.user_name)
        private val emailTextView: TextView = itemView.findViewById(R.id.user_email)
        private val usernameTextView: TextView = itemView.findViewById(R.id.user_username)
        private val companyTextView: TextView = itemView.findViewById(R.id.user_company)
        
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onUserClick(getItem(position))
                }
            }
        }
        
        /**
         * Vincula los datos del usuario con la vista
         */
        fun bind(user: User) {
            nameTextView.text = user.name
            emailTextView.text = user.email
            usernameTextView.text = "@${user.username}"
            companyTextView.text = user.companyName
        }
    }
    
    /**
     * DiffUtil para comparar usuarios eficientemente
     */
    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
    
    /**
     * Interfaz para manejar los clics en los usuarios
     */
    interface OnUserClickListener {
        fun onUserClick(user: User)
    }
}