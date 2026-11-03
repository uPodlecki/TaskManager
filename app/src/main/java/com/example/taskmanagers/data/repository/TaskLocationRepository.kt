package com.example.taskmanagers.data.repository

import com.example.taskmanagers.data.local.TaskLocation
import com.example.taskmanagers.data.local.TaskLocationDao
import kotlinx.coroutines.flow.Flow

class TaskLocationRepository(private val dao: TaskLocationDao) {

    suspend fun insertLocation(location: TaskLocation): Long{
        return dao.insertLocation(location)
    }
    suspend fun deleteLocation(location: TaskLocation){
        dao.deleteLocation(location)
    }
    fun getLocationsForTask(taskId: Long): Flow<List<TaskLocation>>{
        return dao.getLocationsForTask(taskId)
    }
    fun getAllLocations(): Flow<List<TaskLocation>>{
        return dao.getAllLocations()
    }
}