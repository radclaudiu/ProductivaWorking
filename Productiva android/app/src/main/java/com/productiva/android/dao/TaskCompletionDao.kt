package com.productiva.android.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.model.TaskCompletion

/**
 * DAO para interactuar con la tabla de completaciones de tareas
 */
@Dao
interface TaskCompletionDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(taskCompletion: TaskCompletion): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(taskCompletions: List<TaskCompletion>)
    
    @Update
    suspend fun update(taskCompletion: TaskCompletion)
    
    @Query("SELECT * FROM task_completions WHERE id = :completionId")
    suspend fun getCompletionById(completionId: Int): TaskCompletion?
    
    @Query("SELECT * FROM task_completions WHERE task_id = :taskId ORDER BY completion_date DESC")
    fun getCompletionsByTaskId(taskId: Int): LiveData<List<TaskCompletion>>
    
    @Query("SELECT * FROM task_completions WHERE user_id = :userId ORDER BY completion_date DESC")
    fun getCompletionsByUserId(userId: Int): LiveData<List<TaskCompletion>>
    
    @Query("SELECT * FROM task_completions WHERE is_synced = 0")
    suspend fun getUnsyncedCompletions(): List<TaskCompletion>
    
    @Query("DELETE FROM task_completions")
    suspend fun deleteAll()
    
    @Query("DELETE FROM task_completions WHERE id = :completionId")
    suspend fun deleteCompletionById(completionId: Int)
    
    @Query("SELECT * FROM task_completions WHERE completion_date BETWEEN :startDate AND :endDate ORDER BY completion_date DESC")
    fun getCompletionsByDateRange(startDate: Long, endDate: Long): LiveData<List<TaskCompletion>>
    
    @Query("SELECT COUNT(*) FROM task_completions WHERE user_id = :userId AND completion_date BETWEEN :startDate AND :endDate")
    suspend fun getCompletionCountInDateRange(userId: Int, startDate: Long, endDate: Long): Int
    
    @Query("SELECT * FROM task_completions WHERE signature_path IS NOT NULL")
    suspend fun getCompletionsWithSignatures(): List<TaskCompletion>
}