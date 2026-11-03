package com.example.taskmanagers.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.taskmanagers.R
import com.example.taskmanagers.data.local.TaskDatabase
import com.example.taskmanagers.data.local.TaskStatus
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                    val db = TaskDatabase.getDatabase(context)
                    val allTasks = db.taskDao().getAllTasks().first()
                    val locations = db.taskLocationDao().getAllLocations().first()
                    locations.forEach { location ->
                        val task = allTasks.find { it.id == location.taskId }
                        if (task != null && task.status != TaskStatus.COMPLETED && task.isLocationNotificationEnabled) {
                            GeofenceHelper.registerGeofence(context, location)
                        }
                    }
                } else {
                    val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return@launch
                    if (geofencingEvent.hasError()) return@launch

                    val geofenceTransition = geofencingEvent.geofenceTransition
                    if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return@launch

                        val db = TaskDatabase.getDatabase(context)
                        triggeringGeofences.forEach { geofence ->
                            val locationId = geofence.requestId.toLongOrNull() ?: return@forEach
                            val location = db.taskLocationDao().getLocationById(locationId)

                            if (location != null) {
                                val task = db.taskDao().getTaskById(location.taskId).first()
                                if (task != null && task.status != TaskStatus.COMPLETED && task.isLocationNotificationEnabled) {
                                    NotificationHelper.showNotification(
                                        context = context,
                                        taskId = location.taskId,
                                        title = context.getString(R.string.notif_location_title, task.title),
                                        message = context.getString(R.string.notif_near_msg, location.name)
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}