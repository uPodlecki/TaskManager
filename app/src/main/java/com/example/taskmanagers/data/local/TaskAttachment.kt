package com.example.taskmanagers.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

enum class AttachmentType {
    IMAGE, DOCUMENT, PDF, AUDIO
}

@Entity(
    tableName = "task_attachments",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TaskAttachment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val fileName: String,
    val filePath: String,
    val type: AttachmentType
)
