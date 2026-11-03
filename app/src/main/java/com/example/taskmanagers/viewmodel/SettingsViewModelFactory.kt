package com.example.taskmanagers.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.taskmanagers.data.repository.SettingsRepository

class SettingsViewModelFactory(private val repository: SettingsRepository): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(SettingsViewModel::class.java)){
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown Viewmodel class")
    }
}