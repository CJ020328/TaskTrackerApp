package com.jinghang.android.TaskTracker

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "TaskListFragment"

class TaskListFragment : Fragment() {

    interface Callbacks {
        fun onTaskSelected(taskId: UUID)
    }

    private var callbacks: Callbacks? = null

    private val taskListViewModel: TaskListViewModel by lazy {
        ViewModelProviders.of(this).get(TaskListViewModel::class.java)
    }

    private val taskDetailViewModel: TaskDetailViewModel by lazy {
        ViewModelProviders.of(this).get(TaskDetailViewModel::class.java)
    }

    private val taskRepository = TaskRepository.get()

    private lateinit var taskRecyclerView: RecyclerView
    private var adapter: TaskAdapter? = null
    private val deletedTasksStack = Stack<Task>()
    private var currentScrollPosition = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPause() {
        super.onPause()
        currentScrollPosition = (taskRecyclerView.layoutManager as LinearLayoutManager)
            .findFirstCompletelyVisibleItemPosition()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_task_list, container, false)
        taskRecyclerView = view.findViewById(R.id.task_recycler_view) as RecyclerView
        taskRecyclerView.layoutManager = LinearLayoutManager(context)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fab: FloatingActionButton = view.findViewById(R.id.fab_add_task)
        fab.setOnClickListener {
            val task = Task()
            taskListViewModel.addTask(task)
            callbacks?.onTaskSelected(task.id)
        }
        taskRecyclerView.scrollToPosition(currentScrollPosition)
        taskListViewModel.taskListLiveData.observe(
            viewLifecycleOwner,
            { tasks ->
                tasks?.let {
                    Log.i(TAG, "Got tasks ${tasks.size}")
                    updateUI()
                }
            })
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_task_list, menu)

        val searchItem = menu.findItem(R.id.menu_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Log.d(TAG, "QueryTextSubmit: $query")
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Log.d(TAG, "QueryTextChange: $newText")
                taskListViewModel.filterTasks(newText)
                return false
            }
        })

        val sortByDueDateItem = menu.findItem(R.id.sort_by_due_date)
        sortByDueDateItem.isCheckable = true
        sortByDueDateItem.isChecked = taskListViewModel.sortByDueDateLiveData.value ?: false

        val showCompletedItem = menu.findItem(R.id.show_completed)
        showCompletedItem.isCheckable = true
        showCompletedItem.isChecked = taskListViewModel.showCompletedTasksLiveData.value ?: false

        val lowPriorityItem = menu.findItem(R.id.low_priority)
        lowPriorityItem.isCheckable = true
        lowPriorityItem.isChecked = taskListViewModel.showLowPriorityLiveData.value ?: false

        val mediumPriorityItem = menu.findItem(R.id.medium_priority)
        mediumPriorityItem.isCheckable = true
        mediumPriorityItem.isChecked = taskListViewModel.showMediumPriorityLiveData.value ?: false

        val highPriorityItem = menu.findItem(R.id.high_priority)
        highPriorityItem.isCheckable = true
        highPriorityItem.isChecked = taskListViewModel.showHighPriorityLiveData.value ?: false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.show_completed -> {
                item.isChecked = !item.isChecked
                taskListViewModel.showCompletedTasksLiveData.value = item.isChecked
                updateUI()
                true
            }
            R.id.low_priority -> {
                item.isChecked = !item.isChecked
                taskListViewModel.showLowPriorityLiveData.value = item.isChecked
                updateUI()
                true
            }
            R.id.medium_priority -> {
                item.isChecked = !item.isChecked
                taskListViewModel.showMediumPriorityLiveData.value = item.isChecked
                updateUI()
                true
            }
            R.id.high_priority -> {
                item.isChecked = !item.isChecked
                taskListViewModel.showHighPriorityLiveData.value = item.isChecked
                updateUI()
                true
            }
            R.id.sort_by_due_date -> {
                item.isChecked = !item.isChecked
                taskListViewModel.sortByDueDateLiveData.value = item.isChecked
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateUI() {
        val tasks = taskListViewModel.taskListLiveData.value?.toMutableList() ?: mutableListOf()
        val layoutManager = taskRecyclerView.layoutManager as LinearLayoutManager
        val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()

        adapter = TaskAdapter(tasks)
        taskRecyclerView.adapter = adapter

        if (firstVisibleItemPosition != RecyclerView.NO_POSITION) {
            layoutManager.scrollToPosition(firstVisibleItemPosition)
        }
    }

    private inner class TaskHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private lateinit var task: Task

        private val titleTextView: TextView = itemView.findViewById(R.id.task_title) as TextView
        private val dueDateTextView: TextView = itemView.findViewById(R.id.task_due_date) as TextView
        private val reminderDateTextView: TextView =
            itemView.findViewById(R.id.task_reminder_date) as TextView
        private val completedCheckBox: CheckBox = itemView.findViewById(R.id.task_completed) as CheckBox
        private val priorityLabelTextView: TextView = itemView.findViewById(R.id.priority_label)
        private val priorityTextView: TextView = itemView.findViewById(R.id.task_priority)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(task: Task) {
            this.task = task
            titleTextView.text = task.title

            if (task.isCompleted) {
                titleTextView.paintFlags = titleTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                titleTextView.paintFlags =
                    titleTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            dueDateTextView.text = task.dueDate?.let {
                val formatter = SimpleDateFormat("EEE, MM, dd, hh:mm a", Locale.getDefault())
                formatter.format(it)
            } ?: getString(R.string.no_due_date)

            reminderDateTextView.text = task.reminderDate?.let {
                val formatter = SimpleDateFormat("EEE, MM, dd, hh:mm a", Locale.getDefault())
                formatter.format(it)
            } ?: getString(R.string.no_reminder_date)

            completedCheckBox.isChecked = task.isCompleted
            completedCheckBox.setOnCheckedChangeListener { _, isChecked ->

                if (isChecked) {
                    titleTextView.paintFlags =
                        titleTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    taskRepository.cancelNotification(task)
                    task.isCompleted = isChecked
                } else {
                    titleTextView.paintFlags =
                        titleTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    taskRepository.scheduleNotification(task)
                }

                if (isChecked && !(taskListViewModel.showCompletedTasksLiveData.value ?: false)) {
                    val animation = TranslateAnimation(
                        Animation.ABSOLUTE, 0f,
                        Animation.ABSOLUTE, itemView.width.toFloat(),
                        Animation.ABSOLUTE, 0f,
                        Animation.ABSOLUTE, 0f
                    )
                    animation.duration = 1000
                    animation.fillAfter = true

                    animation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {}

                        override fun onAnimationEnd(animation: Animation?) {
                            task.isCompleted = isChecked
                            taskListViewModel.updateTask(task)

                            adapter?.tasks?.let { tasks ->
                                val index = tasks.indexOf(task)
                                if (index != -1) {
                                    tasks.removeAt(index)
                                    adapter?.notifyItemRemoved(index)
                                }
                            }

                            view?.let {
                                Snackbar.make(
                                    it,
                                    "Your task has been archived",
                                    Snackbar.LENGTH_LONG
                                )
                                    .setAction("Undo") {
                                        task.isCompleted = !isChecked
                                        taskListViewModel.updateTask(task)
                                        adapter?.tasks?.add(task)
                                        adapter?.notifyDataSetChanged()
                                    }.show()
                            }
                        }

                        override fun onAnimationRepeat(animation: Animation?) {}
                    })

                    itemView.startAnimation(animation)
                } else {
                    task.isCompleted = isChecked
                    taskListViewModel.updateTask(task)
                }
            }
            priorityLabelTextView.text = "Priority:"
            priorityTextView.text = task.priority.toString()
            deleteButton.setOnClickListener {
                val animation = AlphaAnimation(1.0f, 0.0f)
                animation.duration = 1000

                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        itemView.visibility = View.INVISIBLE
                        taskDetailViewModel.deleteTask(task)
                        deletedTasksStack.push(task)
                        adapter?.removeAt(adapterPosition)

                        view?.let {
                            Snackbar.make(it, "Task deleted", Snackbar.LENGTH_LONG)
                                .setAction("Undo") {
                                    val lastDeletedTask = deletedTasksStack.pop()
                                    taskListViewModel.addTask(lastDeletedTask)
                                    itemView.visibility = View.VISIBLE
                                    adapter?.notifyDataSetChanged()
                                }.show()
                        }
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })

                itemView.startAnimation(animation)
            }
        }

        override fun onClick(v: View?) {
            callbacks?.onTaskSelected(task.id)
        }
    }

    private inner class TaskAdapter(var tasks: MutableList<Task>) :
        RecyclerView.Adapter<TaskHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {
            val view = layoutInflater.inflate(R.layout.list_item_task, parent, false)
            return TaskHolder(view)
        }

        override fun onBindViewHolder(holder: TaskHolder, position: Int) {
            val task = tasks[position]
            holder.bind(task)
        }

        override fun getItemCount(): Int = tasks.size

        fun removeAt(position: Int) {
            tasks.removeAt(position)
            notifyItemRemoved(position)
        }

    }

    companion object {
        fun newInstance(): TaskListFragment {
            return TaskListFragment()
        }
    }
}
