package com.productiva.android.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.model.Task

/**
 * DAO para interactuar con la tabla de tareas
 */
@Dao
interface TaskDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<Task>)
    
    @Update
    suspend fun update(task: Task)
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?
    
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasks(): LiveData<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE assignedTo = :userId ORDER BY dueDate ASC")
    fun getTasksByUser(userId: Int): LiveData<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY dueDate ASC")
    fun getTasksByStatus(status: String): LiveData<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE assignedTo = :userId AND status = :status ORDER BY dueDate ASC")
    fun getTasksByUserAndStatus(userId: Int, status: String): LiveData<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE location_id = :locationId ORDER BY dueDate ASC")
    fun getTasksByLocation(locationId: Int): LiveData<List<Task>>
    
    @Query("SELECT COUNT(*) FROM tasks WHERE assignedTo = :userId AND status = 'pending'")
    suspend fun getPendingTaskCount(userId: Int): Int
    
    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
    
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)
    
    @Query("SELECT * FROM tasks WHERE last_sync < :timestamp")
    suspend fun getTasksToSync(timestamp: Long): List<Task>
    
    @Query("SELECT * FROM tasks WHERE dueDate BETWEEN :startDate AND :endDate ORDER BY dueDate ASC")
    fun getTasksByDateRange(startDate: Long, endDate: Long): LiveData<List<Task>>
}