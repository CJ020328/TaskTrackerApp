package com.jinghang.android.TaskTracker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.jinghang.android.TaskTracker.database.TaskDao
import com.jinghang.android.TaskTracker.database.TaskDatabase
import com.jinghang.android.TaskTracker.database.migration_2_3
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

private const val DATABASE_NAME = "task-database"

class TaskRepository private constructor(private val context: Context) {
    private val database: TaskDatabase = createDatabase(context)
    private val taskDao: TaskDao = database.taskDao()
    private val executor: Executor = Executors.newSingleThreadExecutor()

    fun getTasks(): LiveData<List<Task>> = taskDao.getTasks()
    fun getTask(id: UUID): LiveData<Task?> = taskDao.getTask(id)

    fun updateTask(task: Task) {
        executor.execute {
            taskDao.updateTask(task)
        }
    }

    fun addTask(task: Task) {
        executor.execute {
            taskDao.addTask(task)
        }
    }

    fun deleteTask(task: Task) {
        executor.execute {
            taskDao.deleteTask(task)
        }
    }

    fun getUncompletedTasks(): LiveData<List<Task>> {
        return taskDao.getUncompletedTasks()
    }

    fun scheduleNotification(task: Task) {
        if (task.isCompleted) {
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, TaskDueReceiver::class.java).apply {
            putExtra("task_id", task.id.toString())
            putExtra("task_title", task.title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val reminderTimeInMillis = task.reminderDate?.time
        val currentTimeInMillis = System.currentTimeMillis()
        if (reminderTimeInMillis != null && reminderTimeInMillis >= currentTimeInMillis) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTimeInMillis, pendingIntent)
        }
    }

    fun cancelNotification(task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskDueReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        private var INSTANCE: TaskRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = TaskRepository(context)
            }
        }

        fun get(): TaskRepository {
            return INSTANCE ?: throw IllegalStateException("TaskRepository must be initialized")
        }

        private fun createDatabase(context: Context): TaskDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                TaskDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(migration_2_3)
                .build()
        }
    }
}
