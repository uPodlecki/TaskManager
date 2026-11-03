package com.example.taskmanagers.data.repository

import com.example.taskmanagers.data.local.TaskAttachment
import com.example.taskmanagers.data.local.TaskAttachmentDao
import kotlinx.coroutines.flow.Flow

class TaskAttachmentRepository(private val taskAttachmentDao: TaskAttachmentDao) {
    suspend fun insertAttachment(attachment: TaskAttachment): Long {
        return taskAttachmentDao.insertAttachment(attachment)
    }
    suspend fun deleteAttachment(attachment: TaskAttachment) {
        taskAttachmentDao.deleteAttachment(attachment)
    }
    fun getAttachmentsForTask(taskId: Long): Flow<List<TaskAttachment>> {
        return taskAttachmentDao.getAttachmentsForTask(taskId)
    }
}