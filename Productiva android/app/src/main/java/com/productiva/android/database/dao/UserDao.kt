package com.productiva.android.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.model.User

/**
 * DAO para operaciones con usuarios en la base de datos local.
 */
@Dao
interface UserDao {
    
    /**
     * Inserta un usuario en la base de datos.
     * Si ya existe un usuario con el mismo ID, lo reemplaza.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long
    
    /**
     * Inserta varios usuarios en la base de datos.
     * Si ya existen usuarios con los mismos IDs, los reemplaza.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<User>): List<Long>
    
    /**
     * Actualiza la información de un usuario existente.
     */
    @Update
    suspend fun update(user: User)
    
    /**
     * Obtiene un usuario por su ID.
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?
    
    /**
     * Obtiene un usuario por su nombre de usuario.
     */
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?
    
    /**
     * Obtiene todos los usuarios de la base de datos.
     */
    @Query("SELECT * FROM users ORDER BY name")
    fun getAllUsers(): LiveData<List<User>>
    
    /**
     * Obtiene todos los usuarios activos.
     */
    @Query("SELECT * FROM users WHERE is_active = 1 ORDER BY name")
    fun getActiveUsers(): LiveData<List<User>>
    
    /**
     * Busca usuarios por nombre o nombre de usuario.
     */
    @Query("SELECT * FROM users WHERE name LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' ORDER BY name")
    fun searchUsers(query: String): LiveData<List<User>>
    
    /**
     * Elimina un usuario por su ID.
     */
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Int): Int
    
    /**
     * Elimina todos los usuarios.
     */
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
    
    /**
     * Verifica si existe algún usuario en la base de datos.
     */
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUsersCount(): Int
    
    /**
     * Obtiene usuarios por empresa.
     */
    @Query("SELECT * FROM users WHERE company_id = :companyId ORDER BY name")
    fun getUsersByCompany(companyId: Int): LiveData<List<User>>
    
    /**
     * Obtiene usuarios por ubicación.
     */
    @Query("SELECT * FROM users WHERE location_id = :locationId ORDER BY name")
    fun getUsersByLocation(locationId: Int): LiveData<List<User>>
}