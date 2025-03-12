package com.jinghang.android.TaskTracker

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_TASK_ID = "task_id"

class TaskFragment : Fragment(), DatePickerFragment.Callbacks {
    private lateinit var task: Task
    private lateinit var titleField: EditText
    private lateinit var dueDateButton: Button
    private lateinit var completedCheckBox: CheckBox
    private lateinit var deleteButton: ImageButton
    private lateinit var submitButton: Button
    private lateinit var cancelButton: Button
    private lateinit var descriptionField: EditText
    private lateinit var reminderDateButton: Button

    private val taskDetailViewModel: TaskDetailViewModel by lazy {
        ViewModelProviders.of(this).get(TaskDetailViewModel::class.java)
    }

    private val taskRepository = TaskRepository.get()
    private var taskTitleEntered: Boolean = false
    private var taskDetailsEntered: Boolean = false
    private var savedTaskTitle: String? = null
    private var savedTaskDueDate: Date? = null

    companion object {
        fun newInstance(taskId: UUID): TaskFragment {
            val args = Bundle().apply {
                putSerializable(ARG_TASK_ID, taskId)
            }
            return TaskFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val taskId: UUID = arguments?.getSerializable(ARG_TASK_ID) as UUID
        taskDetailViewModel.loadTask(taskId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_task, container, false)
        titleField = view.findViewById(R.id.task_title)
        dueDateButton = view.findViewById(R.id.task_due_date)
        completedCheckBox = view.findViewById(R.id.task_completed)
        deleteButton = view.findViewById(R.id.task_delete)
        submitButton = view.findViewById(R.id.submit_button)
        cancelButton = view.findViewById(R.id.cancel_button)
        descriptionField = view.findViewById(R.id.task_description)
        reminderDateButton = view.findViewById(R.id.task_reminder_date)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        taskDetailViewModel.taskLiveData.observe(viewLifecycleOwner) { task ->
            task?.let {
                this.task = task
                updateUI()
            }
        }

        descriptionField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                task.description = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        dueDateButton.setOnClickListener {
            showDateTimePicker(task.dueDate) { dateTime ->
                task.dueDate = dateTime
                updateUI()
            }
        }

        reminderDateButton.setOnClickListener {
            showDateTimePicker(task.reminderDate) { dateTime ->
                task.reminderDate = dateTime
                updateUI()
            }
        }

        submitButton.setOnClickListener {
            saveTask()
            activity?.supportFragmentManager?.popBackStack()
        }

        cancelButton.setOnClickListener {
            if (!taskDetailsEntered) {
                task.title = savedTaskTitle ?: ""
                task.dueDate = savedTaskDueDate
                updateUI()
            }
            activity?.supportFragmentManager?.popBackStack()
        }

        deleteButton.setOnClickListener {
            taskDetailViewModel.deleteTask(task)
            activity?.supportFragmentManager?.popBackStack()
        }

        titleField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                taskTitleEntered = !s.isNullOrBlank()
                taskDetailsEntered = taskTitleEntered || task.dueDate != null
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        completedCheckBox.setOnCheckedChangeListener { _, isChecked ->
            task.isCompleted = isChecked
            if (isChecked) {
                taskRepository.cancelNotification(task)
            } else {
                taskRepository.scheduleNotification(task)
            }
        }

        val radioGroup = view.findViewById<RadioGroup>(R.id.radio_group_priority)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            task.priority = when (checkedId) {
                R.id.radio_low_priority -> Task.Priority.LOW
                R.id.radio_medium_priority -> Task.Priority.MEDIUM
                R.id.radio_high_priority -> Task.Priority.HIGH
                else -> Task.Priority.MEDIUM // Default to medium priority if no selection is made
            }
        }
    }

    private fun updateUI() {
        if (::task.isInitialized) {
            if (!taskDetailsEntered) {
                titleField.setText(task.title)
            }
            dueDateButton.text = formatDate(task.dueDate) ?: getString(R.string.no_due_date)
            completedCheckBox.isChecked = task.isCompleted
            reminderDateButton.text = formatDate(task.reminderDate) ?: getString(R.string.no_reminder_date)

            val radioGroup = view?.findViewById<RadioGroup>(R.id.radio_group_priority)
            when (task.priority) {
                Task.Priority.LOW -> radioGroup?.check(R.id.radio_low_priority)
                Task.Priority.MEDIUM -> radioGroup?.check(R.id.radio_medium_priority)
                Task.Priority.HIGH -> radioGroup?.check(R.id.radio_high_priority)
            }
            descriptionField.setText(task.description)
        }
    }

    override fun onDateSelected(date: Date) {
        task.dueDate = date
        taskDetailsEntered = true
        updateUI()
    }

    private fun saveTask() {
        val taskTitle = titleField.text.toString().trim()
        if (taskTitle.isBlank() && task.dueDate == null) {
            taskDetailViewModel.deleteTask(task)
        } else {
            task.title = taskTitle
            task.description = descriptionField.text.toString().trim()
            taskDetailViewModel.saveTask(task)
        }
    }

    override fun onStop() {
        super.onStop()
        if (!taskDetailsEntered) {
            taskDetailViewModel.deleteTask(task)
        }
    }

    private fun showDateTimePicker(dateTime: Date?, onDateTimeSet: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        dateTime?.let { calendar.time = it }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        DatePickerDialog(requireContext(), R.style.PickerDialogTheme, { _, year, month, dayOfMonth ->
            TimePickerDialog(requireContext(), R.style.PickerDialogTheme, { _, hour, minute ->
                calendar.set(year, month, dayOfMonth, hour, minute)
                onDateTimeSet(calendar.time)
            }, hour, minute, true).show()
        }, year, month, day).show()
    }

    private fun formatDate(date: Date?): String? {
        return date?.let {
            SimpleDateFormat("EEE, MM, dd, hh:mm a", Locale.getDefault()).format(it)
        }
    }
}
