package com.productiva.android.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.productiva.android.model.Task
import com.productiva.android.model.TaskCompletion

/**
 * Data Access Object para operaciones de tarea en Room
 */
@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isActive = 1 ORDER BY priority DESC, title ASC")
    fun getAllTasks(): LiveData<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE locationId = :locationId AND isActive = 1 ORDER BY priority DESC, title ASC")
    fun getTasksByLocation(locationId: Int): LiveData<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskById(taskId: Int): LiveData<Task>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(tasks: List<Task>)
    
    @Update
    suspend fun updateTask(task: Task)
    
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Int)
    
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
    
    // Task Completion
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskCompletion(taskCompletion: TaskCompletion): Long
    
    @Query("SELECT * FROM task_completions WHERE syncStatus = :syncStatus")
    suspend fun getPendingSyncTaskCompletions(syncStatus: Int = TaskCompletion.SYNC_PENDING): List<TaskCompletion>
    
    @Update
    suspend fun updateTaskCompletion(taskCompletion: TaskCompletion)
    
    @Query("SELECT * FROM task_completions ORDER BY completionDate DESC")
    fun getAllTaskCompletions(): LiveData<List<TaskCompletion>>
    
    @Query("SELECT * FROM task_completions WHERE taskId = :taskId ORDER BY completionDate DESC")
    fun getTaskCompletionsByTaskId(taskId: Int): LiveData<List<TaskCompletion>>
    
    @Transaction
    suspend fun saveTaskCompletion(taskCompletion: TaskCompletion): Long {
        return insertTaskCompletion(taskCompletion)
    }
    
    @Query("UPDATE task_completions SET syncStatus = :syncStatus, serverId = :serverId WHERE id = :id")
    suspend fun updateTaskCompletionSyncStatus(id: Int, syncStatus: Int, serverId: Int?)
}