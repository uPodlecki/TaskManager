package com.example.taskmanagers.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskAttachmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: TaskAttachment): Long
    @Delete
    suspend fun deleteAttachment(attachment: TaskAttachment)
    @Query("SELECT * FROM task_attachments WHERE taskId = :taskId")
    fun getAttachmentsForTask(taskId: Long): Flow<List<TaskAttachment>>
}
