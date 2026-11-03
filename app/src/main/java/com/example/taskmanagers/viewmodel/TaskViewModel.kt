package com.example.taskmanagers.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanagers.data.local.AttachmentType
import com.example.taskmanagers.data.local.RecurrenceType
import com.example.taskmanagers.data.local.Task
import com.example.taskmanagers.data.local.TaskAttachment
import com.example.taskmanagers.data.local.TaskCategory
import com.example.taskmanagers.data.local.TaskLocation
import com.example.taskmanagers.data.local.TaskPriority
import com.example.taskmanagers.data.local.TaskStatus
import com.example.taskmanagers.data.repository.TaskAttachmentRepository
import com.example.taskmanagers.data.repository.TaskLocationRepository
import com.example.taskmanagers.data.repository.TaskRepository
import com.example.taskmanagers.utils.FileUtils
import com.example.taskmanagers.utils.GeofenceHelper
import com.example.taskmanagers.utils.NotificationScheduler
import com.example.taskmanagers.utils.updateAllWidgets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime

enum class SortOption() {
    DATE_CREATED,
    DUE_DATE,
    PRIORITY
}
enum class TaskView {
    ALL, TODAY, OVERDUE
}

class TaskViewModel(
    private val repository: TaskRepository,
    private val locationRepository: TaskLocationRepository,
    private val attachmentRepository: TaskAttachmentRepository,
    private val appContext: Context
) : ViewModel() {
    val allTasks: StateFlow<List<Task>> = repository.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery: StateFlow<String?> = _searchQuery.asStateFlow()

    private val _selectedStatus = MutableStateFlow<TaskStatus?>(null)
    val selectedStatus: StateFlow<TaskStatus?> = _selectedStatus.asStateFlow()

    private val _selectedPriority = MutableStateFlow<TaskPriority?>(null)
    val selectedPriority: StateFlow<TaskPriority?> = _selectedPriority.asStateFlow()

    private val _selectedCategory = MutableStateFlow<TaskCategory?>(null)
    val selectedCategory: StateFlow<TaskCategory?> = _selectedCategory.asStateFlow()

    fun setSearchQuery(query: String?) {
        _searchQuery.value = query
    }

    fun setStatus(status: TaskStatus?) {
        _selectedStatus.value = status
    }

    fun setPriority(priority: TaskPriority?) {
        _selectedPriority.value = priority
    }

    fun setCategory(category: TaskCategory?) {
        _selectedCategory.value = category
    }

    val filteredTasks: StateFlow<List<Task>> = combine(
        _selectedStatus,
        _selectedPriority,
        _selectedCategory,
        _searchQuery
    ) { status, priority, category, query ->
        repository.getFilteredTasks(status, priority, category, query)
    }
        .flatMapLatest { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    private val _sortOption = MutableStateFlow(SortOption.DATE_CREATED)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    private val _selectedView = MutableStateFlow(TaskView.ALL)
    val selectedView: StateFlow<TaskView> = _selectedView.asStateFlow()

    fun setSelectedView(view: TaskView) {
        _selectedView.value = view
    }

    val sortedAndFilteredTasks: StateFlow<List<Task>> = combine(
        filteredTasks,
        _sortOption,
        _selectedView
    ) { tasks, sortOption, view ->
        val viewFiltered = when (view) {
            TaskView.ALL -> tasks
            TaskView.TODAY -> tasks.filter { it.dueDate?.toLocalDate() == LocalDate.now() }
            TaskView.OVERDUE -> tasks.filter {
                it.dueDate != null && it.dueDate.isBefore(LocalDateTime.now()) && it.status != TaskStatus.COMPLETED
            }
        }
        when (sortOption) {
            SortOption.DATE_CREATED -> viewFiltered.sortedByDescending { it.createdAt }
            SortOption.DUE_DATE -> viewFiltered.sortedBy { it.dueDate }
            SortOption.PRIORITY -> viewFiltered.sortedByDescending { it.priority }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTask(task: Task, onTaskCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val newId = repository.insertTask(task)
            if (task.isTimeNotificationEnabled) {
                val notifyAt = task.dueDate?.minusMinutes(task.notifyMinutesBefore?.toLong() ?: 0L)
                if (notifyAt != null) {
                    NotificationScheduler.scheduleTaskNotification(
                        appContext,
                        newId,
                        task.title,
                        notifyAt
                    )
                }
            }
            onTaskCreated(newId)
            updateAllWidgets(appContext)
        }
    }

    fun updateExistingTask(task: Task) {
        viewModelScope.launch {
            val oldTask = repository.getTaskById(task.id).first()
            repository.updateTask(task)
            if(task.status == TaskStatus.COMPLETED && oldTask?.status != TaskStatus.COMPLETED){
                NotificationScheduler.cancelTaskNotification(appContext, task.id)
                handleRecurrence(task)
            }
            if (task.status != TaskStatus.COMPLETED && task.isTimeNotificationEnabled) {
                val notifyAt = task.dueDate?.minusMinutes(task.notifyMinutesBefore?.toLong() ?: 0L)
                if (notifyAt != null) {
                    NotificationScheduler.scheduleTaskNotification(
                        appContext,
                        task.id,
                        task.title,
                        notifyAt
                    )
                }
            }
        }
    }

    fun removeTask(task: Task) {
        viewModelScope.launch {
            val attachments = attachmentRepository.getAttachmentsForTask(task.id).first()
            attachments.forEach { attachment ->
                try {
                    File(attachment.filePath).delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            repository.deleteTask(task)
            NotificationScheduler.cancelTaskNotification(appContext, task.id)
            updateAllWidgets(appContext)
        }
    }

    fun calculateNextDueDate(
        currentDueDate: LocalDateTime,
        recurrenceType: RecurrenceType,
        customIntervalDays: Int?
    ): LocalDateTime {
        return when (recurrenceType) {
            RecurrenceType.DAILY -> currentDueDate.plusDays(1)
            RecurrenceType.WEEKLY -> currentDueDate.plusWeeks(1)
            RecurrenceType.MONTHLY -> currentDueDate.plusMonths(1)
            RecurrenceType.CUSTOM -> currentDueDate.plusDays((customIntervalDays ?: 1).toLong())
            RecurrenceType.NONE -> currentDueDate
        }
    }

    private suspend fun handleRecurrence(task: Task) {
        val oldLocations = locationRepository.getLocationsForTask(task.id).first()
        if (task.recurrenceType != RecurrenceType.NONE && task.dueDate != null) {
            val oldAttachments = attachmentRepository.getAttachmentsForTask(task.id).first()
            val nextDueDate =
                calculateNextDueDate(task.dueDate, task.recurrenceType, task.customIntervalDays)
            val nextTask = task.copy(
                id = 0,
                status = TaskStatus.TODO,
                createdAt = LocalDateTime.now(),
                dueDate = nextDueDate,
                parentTaskId = task.id,
                isLocationNotificationEnabled = task.isLocationNotificationEnabled
            )
            val newId = repository.insertTask(nextTask)
            oldLocations.forEach { loc ->
                val newLocId = locationRepository.insertLocation(loc.copy(id = 0, taskId = newId))
                if (nextTask.isLocationNotificationEnabled) {
                    GeofenceHelper.registerGeofence(appContext, loc.copy(id = newLocId, taskId = newId))
                }
            }
            oldAttachments.forEach { att ->
                attachmentRepository.insertAttachment(att.copy(id = 0, taskId = newId))
            }
            if (nextTask.isTimeNotificationEnabled) {
                val notifyAt =
                    nextDueDate.minusMinutes(nextTask.notifyMinutesBefore?.toLong() ?: 0L)
                NotificationScheduler.scheduleTaskNotification(
                    appContext,
                    newId,
                    nextTask.title,
                    notifyAt
                )
            }
        }
    }

    fun markAsCompleted(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(status = TaskStatus.COMPLETED))
            NotificationScheduler.cancelTaskNotification(appContext, task.id)
            handleRecurrence(task)
            updateAllWidgets(appContext)
        }
    }
    fun getTaskById(taskId: Long): StateFlow<Task?> {
        return repository.getTaskById(taskId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    fun addLocationToTask(taskId: Long, name: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val location = TaskLocation(
                taskId = taskId,
                name = name,
                latitude = latitude,
                longitude = longitude
            )
            val generateId = locationRepository.insertLocation(location)
            GeofenceHelper.registerGeofence(appContext, location.copy(id = generateId))
        }
    }

    fun getLocationsForTask(taskId: Long): StateFlow<List<TaskLocation>> {
        return locationRepository.getLocationsForTask(taskId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun removeLocation(location: TaskLocation) {
        viewModelScope.launch {
            locationRepository.deleteLocation(location)
            GeofenceHelper.removeGeofence(appContext, location.taskId)
        }
    }

    fun registerAllGeofences() {
        viewModelScope.launch {
            locationRepository.getAllLocations().first().forEach { location ->
                GeofenceHelper.registerGeofence(appContext, location)
            }
        }
    }

    fun getAttachmentsForTask(taskId: Long): StateFlow<List<TaskAttachment>> {
        return attachmentRepository.getAttachmentsForTask(taskId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun addAttachment(taskId: Long, uri: Uri, filename: String, type: AttachmentType) {
        viewModelScope.launch {
            val filePath = FileUtils.saveUriToInternalStorage(appContext, uri, type)
            if (filePath != null) {
                val attachment = TaskAttachment(
                    taskId = taskId,
                    fileName = filename,
                    filePath = filePath,
                    type = type
                )
                attachmentRepository.insertAttachment(attachment)
            }
        }
    }

    fun removeAttachment(attachment: TaskAttachment) {
        viewModelScope.launch {
            File(attachment.filePath).delete()
            attachmentRepository.deleteAttachment(attachment)
        }
    }
}
