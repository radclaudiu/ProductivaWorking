package com.productiva.android.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para los usuarios.
 * Proporciona métodos para acceder y manipular la tabla de usuarios.
 */
@Dao
interface UserDao {
    /**
     * Inserta un nuevo usuario en la base de datos.
     * Si ya existe un usuario con el mismo ID, lo reemplaza.
     *
     * @param user Usuario a insertar.
     * @return ID del usuario insertado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long
    
    /**
     * Actualiza un usuario existente.
     *
     * @param user Usuario a actualizar.
     * @return Número de filas actualizadas.
     */
    @Update
    suspend fun updateUser(user: User): Int
    
    /**
     * Obtiene un usuario por su ID.
     *
     * @param userId ID del usuario.
     * @return Flow con el usuario, o null si no existe.
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: Int): Flow<User?>
    
    /**
     * Obtiene un usuario por su email.
     *
     * @param email Email del usuario.
     * @return Flow con el usuario, o null si no existe.
     */
    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserByEmail(email: String): Flow<User?>
    
    /**
     * Obtiene todos los usuarios.
     *
     * @return Flow con la lista de todos los usuarios.
     */
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>
    
    /**
     * Obtiene un usuario por su ID de forma síncrona.
     *
     * @param userId ID del usuario.
     * @return El usuario, o null si no existe.
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserByIdSync(userId: Int): User?
    
    /**
     * Obtiene un usuario por su email de forma síncrona.
     *
     * @param email Email del usuario.
     * @return El usuario, o null si no existe.
     */
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmailSync(email: String): User?
    
    /**
     * Elimina todos los usuarios.
     */
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
    
    /**
     * Elimina un usuario por su ID.
     *
     * @param userId ID del usuario a eliminar.
     * @return Número de filas eliminadas.
     */
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Int): Int
}