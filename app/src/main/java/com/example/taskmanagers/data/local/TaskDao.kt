package com.example.taskmanagers.data.local
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface TaskDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskById(taskId: Long): Flow<Task?>

    @Query("SELECT * FROM tasks WHERE " +
            "(:status IS NULL OR status=:status) " +
            "AND (:priority IS NULL OR priority=:priority) " +
            "AND (:category IS NULL OR category=:category) " +
            "AND (:searchQuery IS NULL OR title LIKE '%' || :searchQuery || '%')")
    fun getFilteredTasks(
        status: TaskStatus? = null,
        priority: TaskPriority? = null,
        category: TaskCategory? = null,
        searchQuery: String? = null) : Flow<List<Task>>
}