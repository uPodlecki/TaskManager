package com.example.taskmanagers.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.taskmanagers.data.local.TaskLocation
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

object GeofenceHelper {
    @SuppressLint("MissingPermission")
    fun registerGeofence(context: Context, location: TaskLocation) {
        val geofencingClient = LocationServices.getGeofencingClient(context)

        val geofence = Geofence.Builder()
            .setRequestId(location.id.toString())
            .setCircularRegion(location.latitude, location.longitude, location.radiusMeters)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
        val pendingIntent = createPendingIntent(context, location.taskId)

        geofencingClient.addGeofences(geofencingRequest, pendingIntent).run {
            addOnSuccessListener {
                Log.d("Geofence", "SUKCES: Zarejestrowano punkt ID: ${location.id}")
            }
            addOnFailureListener { e ->
                Log.e("Geofence", "BŁĄD: Nie zarejestrowano! Kod: ${e.message}")
            }
        }
    }

    fun removeGeofence(context: Context, taskId: Long) {
        val geofencingClient = LocationServices.getGeofencingClient(context)
        geofencingClient.removeGeofences(createPendingIntent(context, taskId))
    }

    private fun createPendingIntent(context: Context, taskId: Long): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
}