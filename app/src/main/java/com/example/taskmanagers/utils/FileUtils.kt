package com.example.taskmanagers.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.taskmanagers.data.local.AttachmentType
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    fun saveUriToInternalStorage(context: Context, uri: Uri, type: AttachmentType): String? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileName = "attachment_${System.currentTimeMillis()}.${type.name.lowercase()}"
        val destFile = File(context.filesDir, fileName)
        return try {
            val outPutStream = FileOutputStream(destFile)
            inputStream.use { input ->
                outPutStream.use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private fun getExt(type: AttachmentType): String {
        return when(type) {
            AttachmentType.IMAGE -> "jpg"
            AttachmentType.PDF -> "pdf"
            AttachmentType.AUDIO -> "mp3"
            AttachmentType.DOCUMENT -> "doc"
        }
    }
    fun getFileNameFromUri(context: Context, uri: Uri): String {
        var name = "unknown_file"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }
}