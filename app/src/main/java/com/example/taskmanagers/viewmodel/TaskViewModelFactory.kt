package com.example.taskmanagers.viewmodel
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.taskmanagers.data.repository.TaskAttachmentRepository
import com.example.taskmanagers.data.repository.TaskLocationRepository
import com.example.taskmanagers.data.repository.TaskRepository


class TaskViewModelFactory(
    private val repository: TaskRepository,
    private val locationRepository: TaskLocationRepository,
    private val attachmentRepository: TaskAttachmentRepository,
    private val appContext: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(
                repository,
                locationRepository,
                attachmentRepository,
                appContext
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}