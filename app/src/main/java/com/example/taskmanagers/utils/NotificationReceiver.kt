package com.example.taskmanagers.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.taskmanagers.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("taskId", -1L)
        val title = intent.getStringExtra("title") ?: ""

        if (taskId != -1L) {
            val notificationTitle = context.getString(R.string.notif_task_title)
            NotificationHelper.showNotification(
                context = context,
                taskId = taskId,
                title = notificationTitle,
                message = title
            )
        }
    }
}
