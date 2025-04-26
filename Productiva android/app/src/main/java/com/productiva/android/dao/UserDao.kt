package com.productiva.android.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.model.User

/**
 * DAO para la entidad Usuario
 */
@Dao
interface UserDao {
    
    /**
     * Inserta un usuario en la base de datos
     * @param user Usuario a insertar
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    /**
     * Inserta múltiples usuarios en la base de datos
     * @param users Lista de usuarios a insertar
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)
    
    /**
     * Actualiza un usuario existente
     * @param user Usuario con los datos actualizados
     */
    @Update
    suspend fun updateUser(user: User)
    
    /**
     * Obtiene un usuario por su ID
     * @param id ID del usuario
     * @return Usuario encontrado o null
     */
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?
    
    /**
     * Obtiene un usuario por nombre de usuario
     * @param username Nombre de usuario
     * @return Usuario encontrado o null
     */
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?
    
    /**
     * Obtiene todos los usuarios
     * @return LiveData con la lista de todos los usuarios
     */
    @Query("SELECT * FROM users")
    fun getAllUsers(): LiveData<List<User>>
    
    /**
     * Obtiene todos los usuarios activos
     * @return LiveData con la lista de usuarios activos
     */
    @Query("SELECT * FROM users WHERE active = 1")
    fun getActiveUsers(): LiveData<List<User>>
    
    /**
     * Obtiene usuarios por compañía
     * @param companyId ID de la compañía
     * @return LiveData con la lista de usuarios de la compañía
     */
    @Query("SELECT * FROM users WHERE company_id = :companyId AND active = 1")
    fun getUsersByCompany(companyId: Int): LiveData<List<User>>
    
    /**
     * Obtiene usuarios por ubicación
     * @param locationId ID de la ubicación
     * @return LiveData con la lista de usuarios de la ubicación
     */
    @Query("SELECT * FROM users WHERE location_id = :locationId AND active = 1")
    fun getUsersByLocation(locationId: Int): LiveData<List<User>>
    
    /**
     * Elimina todos los usuarios (usado para sincronización)
     */
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}