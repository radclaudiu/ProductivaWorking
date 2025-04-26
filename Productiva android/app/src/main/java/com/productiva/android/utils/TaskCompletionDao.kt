package com.productiva.android.utils

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productiva.android.models.TaskCompletion

@Dao
interface TaskCompletionDao {
    @Query("SELECT * FROM task_completions")
    fun getAllTaskCompletions(): LiveData<List<TaskCompletion>>
    
    @Query("SELECT * FROM task_completions WHERE taskId = :taskId")
    fun getCompletionsByTaskId(taskId: Int): LiveData<List<TaskCompletion>>
    
    @Query("SELECT * FROM task_completions WHERE userId = :userId")
    fun getCompletionsByUserId(userId: Int): LiveData<List<TaskCompletion>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskCompletion(taskCompletion: TaskCompletion): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTaskCompletions(taskCompletions: List<TaskCompletion>)
    
    @Update
    suspend fun updateTaskCompletion(taskCompletion: TaskCompletion)
    
    @Query("DELETE FROM task_completions WHERE localId = :localId")
    suspend fun deleteTaskCompletionById(localId: Int)
    
    @Query("SELECT * FROM task_completions WHERE localSyncStatus != 0")
    fun getUnsyncedTaskCompletions(): LiveData<List<TaskCompletion>>
    
    @Query("UPDATE task_completions SET id = :remoteId, localSyncStatus = 0 WHERE localId = :localId")
    suspend fun updateCompletionAfterSync(localId: Int, remoteId: Int)
}