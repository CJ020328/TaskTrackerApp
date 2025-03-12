package com.jinghang.android.TaskTracker

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TaskListViewModel : ViewModel() {

    private val taskRepository = TaskRepository.get()

    val showCompletedTasksLiveData = MutableLiveData<Boolean>().apply { value = false }
    val selectedPriority: MutableLiveData<Task.Priority?> = MutableLiveData<Task.Priority?>().apply { value = null }
    val showLowPriorityLiveData = MutableLiveData<Boolean>()
    val showMediumPriorityLiveData = MutableLiveData<Boolean>()
    val showHighPriorityLiveData = MutableLiveData<Boolean>()
    private val searchQuery = MutableLiveData<String>().apply { value = "" }
    val sortByDueDateLiveData = MutableLiveData<Boolean>().apply { value = true }

    val taskListLiveData = MediatorLiveData<List<Task>>().apply {
        var showCompleted: Boolean? = null
        var showLow: Boolean? = null
        var showMedium: Boolean? = null
        var showHigh: Boolean? = null
        var sortByDueDate: Boolean? = null
        var tasks: List<Task>? = null
        var searchQuery: String? = null

        fun update() {
            var filteredTasks = tasks?.filter {
                (showCompleted ?: false || !it.isCompleted) &&
                        ((showLow ?: false && it.priority == Task.Priority.LOW) ||
                                (showMedium ?: false && it.priority == Task.Priority.MEDIUM) ||
                                (showHigh ?: false && it.priority == Task.Priority.HIGH) ||
                                (!(showLow ?: false) && !(showMedium ?: false) && !(showHigh ?: false))) &&
                        it.title.contains(searchQuery ?: "", ignoreCase = true)
            }
            if (sortByDueDate == true) {
                filteredTasks = filteredTasks?.sortedBy { it.dueDate }
            }
            this.value = filteredTasks
        }

        addSource(showCompletedTasksLiveData) {
            showCompleted = it
            update()
        }
        addSource(showLowPriorityLiveData) {
            showLow = it
            update()
        }
        addSource(showMediumPriorityLiveData) {
            showMedium = it
            update()
        }
        addSource(showHighPriorityLiveData) {
            showHigh = it
            update()
        }
        addSource(sortByDueDateLiveData) {
            sortByDueDate = it
            update()
        }
        addSource(taskRepository.getTasks()) {
            tasks = it
            update()
        }
        addSource(this@TaskListViewModel.searchQuery) {
            searchQuery = it
            update()
        }
    }

    fun addTask(task: Task) {
        taskRepository.addTask(task)
    }

    fun updateTask(task: Task) {
        taskRepository.updateTask(task)
    }

    fun filterTasks(query: String) {
        searchQuery.value = query
    }
}
