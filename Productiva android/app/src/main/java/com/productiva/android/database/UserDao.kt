package com.productiva.android.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.model.User

/**
 * Interfaz de acceso a datos para la entidad User
 */
@Dao
interface UserDao {
    
    /**
     * Obtiene todos los usuarios
     */
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): LiveData<List<User>>
    
    /**
     * Obtiene un usuario por su ID
     */
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?
    
    /**
     * Obtiene usuarios de una empresa específica
     */
    @Query("SELECT * FROM users WHERE companyId = :companyId ORDER BY name ASC")
    fun getUsersByCompany(companyId: Int): LiveData<List<User>>
    
    /**
     * Busca usuarios por nombre, email o username
     */
    @Query("SELECT * FROM users WHERE name LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%'")
    fun searchUsers(query: String): LiveData<List<User>>
    
    /**
     * Inserta un nuevo usuario
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long
    
    /**
     * Inserta múltiples usuarios
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<User>): List<Long>
    
    /**
     * Actualiza un usuario existente
     */
    @Update
    suspend fun update(user: User): Int
    
    /**
     * Elimina un usuario por su ID
     */
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Int): Int
    
    /**
     * Elimina todos los usuarios
     */
    @Query("DELETE FROM users")
    suspend fun deleteAll(): Int
    
    /**
     * Verifica si existe un usuario con las credenciales proporcionadas
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?
    
    /**
     * Obtiene el número total de usuarios
     */
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}