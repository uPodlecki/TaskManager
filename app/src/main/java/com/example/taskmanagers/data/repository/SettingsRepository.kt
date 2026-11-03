package com.example.taskmanagers.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.taskmanagers.data.local.TaskPriority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        private val DEFAULT_PRIORITY_KEY = stringPreferencesKey("default_priority")
        private val DEFAULT_NOTIFY_BEFORE_KEY = intPreferencesKey("default_notify_before")
    }

    val isDarkMode: Flow<Boolean> = context.settingsDataStore.data
        .map { preferences -> preferences[DARK_MODE_KEY] ?: false }

    suspend fun setDarkMode(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    val notificationsEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { preferences -> preferences[NOTIFICATIONS_ENABLED_KEY] ?: true }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    val defaultPriority: Flow<TaskPriority> = context.settingsDataStore.data
        .map { preferences ->
            val priorityName = preferences[DEFAULT_PRIORITY_KEY] ?: TaskPriority.MEDIUM.name
            TaskPriority.valueOf(priorityName)
        }

    suspend fun setDefaultPriority(priority: TaskPriority) {
        context.settingsDataStore.edit { preferences ->
            preferences[DEFAULT_PRIORITY_KEY] = priority.name
        }
    }

    val defaultNotifyBefore: Flow<Int> = context.settingsDataStore.data
        .map { preferences -> preferences[DEFAULT_NOTIFY_BEFORE_KEY] ?: 15 }

    suspend fun setDefaultNotifyBefore(minutes: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[DEFAULT_NOTIFY_BEFORE_KEY] = minutes
        }
    }
}
