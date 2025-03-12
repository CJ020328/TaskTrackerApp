package com.jinghang.android.TaskTracker

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Task(
    @PrimaryKey var id: UUID = UUID.randomUUID(),
    var title: String = "",
    var description: String? = null,
    var dueDate: Date? = null,
    var isCompleted: Boolean = false,
    var reminderDate: Date? = null,
    var priority: Priority = Priority.MEDIUM
) {
    enum class Priority {
        LOW, MEDIUM, HIGH
    }
}
