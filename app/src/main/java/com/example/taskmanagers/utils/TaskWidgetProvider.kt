package com.example.taskmanagers.utils

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.taskmanagers.R
import com.example.taskmanagers.data.local.TaskDatabase
import com.example.taskmanagers.data.local.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class TaskWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.task_widget)

        CoroutineScope(Dispatchers.IO).launch {
            val db = TaskDatabase.getDatabase(context)
            val allTasks = db.taskDao().getAllTasks().first()
            val today = LocalDate.now()
            val todayTasksCount = allTasks.count {
                it.dueDate?.toLocalDate() == today && it.status != TaskStatus.COMPLETED
            }

            views.setTextViewText(R.id.widget_task_count, "$todayTasksCount remaining")
            val intent = android.content.Intent(context, com.example.taskmanagers.MainActivity::class.java)
            val pendingIntent = android.app.PendingIntent.getActivity(
                context, 0, intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_task_count, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
fun updateAllWidgets(context: Context) {
    val intent = android.content.Intent(context, TaskWidgetProvider::class.java).apply {
        action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
    }
    val ids = android.appwidget.AppWidgetManager.getInstance(context)
        .getAppWidgetIds(android.content.ComponentName(context, TaskWidgetProvider::class.java))
    intent.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    context.sendBroadcast(intent)
}