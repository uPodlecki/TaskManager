package com.example.taskmanagers

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.taskmanagers.data.local.TaskDatabase
import com.example.taskmanagers.data.repository.SettingsRepository
import com.example.taskmanagers.data.repository.TaskAttachmentRepository
import com.example.taskmanagers.data.repository.TaskLocationRepository
import com.example.taskmanagers.data.repository.TaskRepository
import com.example.taskmanagers.navigation.NavGraph
import com.example.taskmanagers.ui.theme.TaskManagerSTheme
import com.example.taskmanagers.utils.NotificationHelper
import com.example.taskmanagers.viewmodel.SettingsViewModel
import com.example.taskmanagers.viewmodel.SettingsViewModelFactory
import com.example.taskmanagers.viewmodel.TaskViewModel
import com.example.taskmanagers.viewmodel.TaskViewModelFactory


class MainActivity : AppCompatActivity() {
    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        if (fineLocationGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestBackgroundLocationLauncher.launch(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        checkExactAlarmPermission()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        requestLocationPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private lateinit var taskViewModel: TaskViewModel

    private val requestBackgroundLocationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            taskViewModel.registerAllGeofences()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = TaskDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(database.taskDao())
        val taskLocationRepository = TaskLocationRepository(database.taskLocationDao())
        val attachmentRepository = TaskAttachmentRepository(database.taskAttachmentDao())
        val factory = TaskViewModelFactory(repository, taskLocationRepository, attachmentRepository = attachmentRepository, appContext = applicationContext)
        val settingsRepository = SettingsRepository(applicationContext)
        taskViewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]
        val settingsFactory = SettingsViewModelFactory(settingsRepository)
        window.setBackgroundDrawable(null)
        NotificationHelper.createNotificationChannel(applicationContext)

        if (!hasLocationPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                requestLocationPermissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        } else {
            checkExactAlarmPermission()
        }

        val notificationTaskId = intent.getLongExtra("taskId", -1L)
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = settingsFactory)
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
            TaskManagerSTheme(darkTheme = isDarkMode) {
                LaunchedEffect(Unit) {
                    taskViewModel.registerAllGeofences()
                }
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    taskViewModel = taskViewModel,
                    settingsViewModel = settingsViewModel,
                    notificationTaskId = notificationTaskId)
            }
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent().apply {
                    action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }
}
