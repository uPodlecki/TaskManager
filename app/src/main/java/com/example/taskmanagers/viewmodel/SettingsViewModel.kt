package com.example.taskmanagers.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanagers.data.local.TaskPriority
import com.example.taskmanagers.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository): ViewModel(){
    val isDarkMode: StateFlow<Boolean> = repository.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    val notificationsEnabled: StateFlow<Boolean> = repository.notificationsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val defaultPriority: StateFlow<TaskPriority> = repository.defaultPriority
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TaskPriority.MEDIUM
        )

    val defaultNotifyBefore: StateFlow<Int> = repository.defaultNotifyBefore
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 15
        )

    fun setDarkMode(enabled: Boolean){
        viewModelScope.launch {
            repository.setDarkMode(enabled)
        }
    }
    fun setNotifications(enabled: Boolean){
        viewModelScope.launch {
            repository.setNotificationsEnabled(enabled)
        }
    }

    fun setDefaultPriority(priority: TaskPriority) {
        viewModelScope.launch {
            repository.setDefaultPriority(priority)
        }
    }

    fun setDefaultNotifyBefore(minutes: Int) {
        viewModelScope.launch {
            repository.setDefaultNotifyBefore(minutes)
        }
    }
}
