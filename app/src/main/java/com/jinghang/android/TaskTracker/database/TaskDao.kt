package com.jinghang.android.TaskTracker.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.jinghang.android.TaskTracker.Task
import java.util.*

@Dao
interface TaskDao {
    @Query("SELECT * FROM task ORDER BY dueDate ASC")
    fun getTasksSortedByDueDate(): LiveData<List<Task>>

    @Query("SELECT * FROM task WHERE isCompleted = 0")
    fun getUncompletedTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM task")
    fun getTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM task WHERE id=(:id)")
    fun getTask(id: UUID): LiveData<Task?>

    @Query("SELECT * FROM task WHERE priority = :priority")
    fun getTasksByPriority(priority: Task.Priority): LiveData<List<Task>>

    @Insert
    fun addTask(task: Task)

    @Update
    fun updateTask(task: Task)

    @Delete
    fun deleteTask(task: Task)
}
