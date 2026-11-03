package com.example.taskmanagers.data.repository

import com.example.taskmanagers.data.local.Task
import com.example.taskmanagers.data.local.TaskCategory
import com.example.taskmanagers.data.local.TaskDao
import com.example.taskmanagers.data.local.TaskPriority
import com.example.taskmanagers.data.local.TaskStatus
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    suspend fun insertTask(task: Task): Long{
        return taskDao.insertTask(task)
    }
    suspend fun updateTask(task: Task){
        taskDao.updateTask(task)
    }
    suspend fun deleteTask(task: Task){
        taskDao.deleteTask(task)
    }
    fun getAllTasks(): Flow<List<Task>>{
        return taskDao.getAllTasks()
    }
    fun getTaskById(taskId: Long): Flow<Task?>{
        return taskDao.getTaskById(taskId)
    }
    fun getFilteredTasks(
        status: TaskStatus? = null,
        priority: TaskPriority? = null,
        category: TaskCategory? = null,
        searchQuery: String? = null
    ): Flow<List<Task>>
    {
        return taskDao.getFilteredTasks(status, priority, category, searchQuery)
    }
}