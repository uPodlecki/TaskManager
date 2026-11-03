package com.example.taskmanagers.data.local
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

enum class TaskStatus{
    TODO, IN_PROGRESS, COMPLETED
}
enum class TaskPriority{
    LOW, MEDIUM, HIGH
}
enum class RecurrenceType{
    NONE, DAILY, WEEKLY, MONTHLY, CUSTOM
}
enum class TaskCategory{
    WORK, PERSONAL, SHOPPING, OTHER
}

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val priority: TaskPriority,
    val description: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val dueDate: LocalDateTime? = null,
    val status: TaskStatus = TaskStatus.TODO,
    val category: TaskCategory = TaskCategory.OTHER,
    val isTimeNotificationEnabled: Boolean = false,
    val notifyMinutesBefore: Int? = null,
    val isLocationNotificationEnabled: Boolean = false,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val customIntervalDays: Int? = null,
    val parentTaskId: Long? = null
)