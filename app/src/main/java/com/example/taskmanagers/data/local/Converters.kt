package com.example.taskmanagers.data.local

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.ZoneOffset

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it / 1000, 0, ZoneOffset.UTC) }
    }
    @TypeConverter
    fun dateToTimestamp(value: LocalDateTime?): Long?{
        return value?.let { it.toEpochSecond(  ZoneOffset.UTC) * 1000 }
    }

    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String {
        return status.name
    }
    @TypeConverter
    fun toTaskStatus(status: String): TaskStatus {
        return TaskStatus.valueOf(status)
    }

    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority): String{
        return priority.name
    }
    @TypeConverter
    fun toTaskPriority(priority: String): TaskPriority{
        return TaskPriority.valueOf(priority)
    }

    @TypeConverter
    fun fromRecurrenceType(recurrenceType: RecurrenceType): String{
        return recurrenceType.name
    }
    @TypeConverter
    fun toRecurrenceType(recurrenceType: String): RecurrenceType{
        return RecurrenceType.valueOf(recurrenceType)
    }
    @TypeConverter
    fun fromAttachmentType(type: AttachmentType): String{
        return type.name
    }
    @TypeConverter
    fun toAttachmentType(type: String): AttachmentType{
        return AttachmentType.valueOf(type)
    }
    @TypeConverter
    fun fromTaskCategory(category: TaskCategory): String {
        return category.name}

    @TypeConverter
    fun toTaskCategory(value: String): TaskCategory {
        return TaskCategory.valueOf(value)
    }
}