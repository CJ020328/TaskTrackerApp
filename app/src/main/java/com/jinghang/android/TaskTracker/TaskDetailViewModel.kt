package com.jinghang.android.TaskTracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*

class TaskDetailViewModel : ViewModel() {
    private val taskRepository: TaskRepository = TaskRepository.get()
    private val taskIdLiveData = MutableLiveData<UUID>()

    var taskLiveData: LiveData<Task?> = Transformations.switchMap(taskIdLiveData) {
        taskRepository.getTask(it)
    }

    fun loadTask(taskId: UUID) {
        taskIdLiveData.value = taskId
    }

    fun saveTask(task: Task) {
        taskRepository.updateTask(task)
        taskRepository.scheduleNotification(task)
    }

    fun deleteTask(task: Task) {
        taskRepository.deleteTask(task)
        taskRepository.cancelNotification(task)
    }

}
