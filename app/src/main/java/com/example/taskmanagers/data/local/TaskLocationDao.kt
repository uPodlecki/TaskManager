package com.example.taskmanagers.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskLocationDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: TaskLocation): Long

    @Delete
    suspend fun deleteLocation(location: TaskLocation)

    @Query("SELECT * FROM task_locations WHERE taskId = :taskId")
    fun getLocationsForTask(taskId: Long): Flow<List<TaskLocation>>

    @Query("SELECT * FROM task_locations")
    fun getAllLocations(): Flow<List<TaskLocation>>
    @Query("SELECT * FROM task_locations WHERE id = :locationId")
    suspend fun getLocationById(locationId: Long): TaskLocation?
}