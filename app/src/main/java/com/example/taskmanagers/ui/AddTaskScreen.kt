package com.example.taskmanagers.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.taskmanagers.R
import com.example.taskmanagers.data.local.AttachmentType
import com.example.taskmanagers.data.local.RecurrenceType
import com.example.taskmanagers.data.local.Task
import com.example.taskmanagers.data.local.TaskCategory
import com.example.taskmanagers.data.local.TaskPriority
import com.example.taskmanagers.navigation.Routes
import com.example.taskmanagers.utils.AudioRecorder
import com.example.taskmanagers.utils.FileUtils
import com.example.taskmanagers.viewmodel.SettingsViewModel
import com.example.taskmanagers.viewmodel.TaskViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddTaskScreen(
    viewModel: TaskViewModel,
    settingsViewModel: SettingsViewModel,
    onTaskAdded: () -> Unit,
    navController: NavHostController
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    val defaultPriority by settingsViewModel.defaultPriority.collectAsState()
    val defaultNotifyBefore by settingsViewModel.defaultNotifyBefore.collectAsState()

    var selectedPriority by rememberSaveable(defaultPriority) { mutableStateOf(defaultPriority) }
    var notifyMinutesBefore by rememberSaveable(defaultNotifyBefore) { mutableStateOf(defaultNotifyBefore) }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }

    var notificationEnabled by rememberSaveable { mutableStateOf(false) }
    var locationNotificationEnabled by rememberSaveable { mutableStateOf(false) }
    var selectedRecurrence by rememberSaveable { mutableStateOf(RecurrenceType.NONE) }
    var customIntervalDays by rememberSaveable { mutableStateOf("") }
    var pendingLocations by rememberSaveable { mutableStateOf<List<Triple<Double, Double, String>>>(emptyList()) }
    var pendingAttachments by remember { mutableStateOf(listOf<Triple<Uri, String, AttachmentType>>()) }
    var selectedCategory by rememberSaveable { mutableStateOf(TaskCategory.OTHER) }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val selectedLocation = savedStateHandle?.get<Triple<Double, Double, String>>("selected_location")
    val recorder = remember { AudioRecorder(navController.context) }
    var isRecording by remember { mutableStateOf(false) }
    val audioPermissionState = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val realName = FileUtils.getFileNameFromUri(navController.context, it)
            pendingAttachments = pendingAttachments + Triple(it, realName, AttachmentType.IMAGE)
        }
    }
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val realName = FileUtils.getFileNameFromUri(navController.context, it)
            pendingAttachments = pendingAttachments + Triple(it, realName, AttachmentType.PDF)
        }
    }
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val realName = FileUtils.getFileNameFromUri(navController.context, it)
            pendingAttachments = pendingAttachments + Triple(it, realName, AttachmentType.AUDIO)
        }
    }
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val realName = FileUtils.getFileNameFromUri(navController.context, it)
            pendingAttachments = pendingAttachments + Triple(it, realName, AttachmentType.DOCUMENT)
        }
    }
    fun handleAudioRecording() {
        if (isRecording) {
            recorder.stop()
            isRecording = false
        } else {
            val file = File(navController.context.cacheDir, "temp_recording.mp3")
            recorder.start(file)
            isRecording = true
            val uri = Uri.fromFile(file)
            pendingAttachments = pendingAttachments + Triple(
                uri, "Voice Note ${
                    LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("HH:mm")
                    )
                }", AttachmentType.AUDIO
            )
        }
    }

    LaunchedEffect(selectedLocation) {
        selectedLocation?.let { (lat, lng, address) ->
            pendingLocations = pendingLocations + Triple(lat, lng, address)
            savedStateHandle.remove<Triple<Double, Double, String>>("selected_location")
        }
    }
    Scaffold { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())) {
            Text(stringResource(R.string.priority), style = MaterialTheme.typography.labelMedium)
            Row(modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())) {
                TaskPriority.entries.forEach { priority ->
                    val priorityText = when(priority) {
                        TaskPriority.LOW -> stringResource(R.string.priority_low)
                        TaskPriority.MEDIUM -> stringResource(R.string.priority_medium)
                        TaskPriority.HIGH -> stringResource(R.string.priority_high)
                    }
                    Button(
                        onClick = { selectedPriority = priority },
                        colors = if (priority == selectedPriority) {
                            ButtonDefaults.buttonColors()
                        } else {
                            ButtonDefaults.outlinedButtonColors()
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(priorityText)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.category), style = MaterialTheme.typography.labelMedium)
            Row(modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())) {
                TaskCategory.entries.forEach { category ->
                    val icon = when(category) {
                        TaskCategory.WORK -> androidx.compose.material.icons.Icons.Default.Work
                        TaskCategory.PERSONAL -> androidx.compose.material.icons.Icons.Default.Person
                        TaskCategory.SHOPPING -> androidx.compose.material.icons.Icons.Default.ShoppingCart
                        else -> androidx.compose.material.icons.Icons.Default.List
                    }
                    val categoryText = when(category) {
                        TaskCategory.WORK -> stringResource(R.string.cat_work)
                        TaskCategory.PERSONAL -> stringResource(R.string.cat_personal)
                        TaskCategory.SHOPPING -> stringResource(R.string.cat_shopping)
                        TaskCategory.OTHER -> stringResource(R.string.cat_other)
                    }
                    Button(
                        onClick = { selectedCategory = category },
                        colors = if (category == selectedCategory) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(categoryText)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.recurrence), style = MaterialTheme.typography.labelMedium)
            Row(modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
            ) {
                RecurrenceType.entries.forEach { recurrence ->
                    val recurrenceText = when(recurrence) {
                        RecurrenceType.NONE -> stringResource(R.string.rec_none)
                        RecurrenceType.DAILY -> stringResource(R.string.rec_daily)
                        RecurrenceType.WEEKLY -> stringResource(R.string.rec_weekly)
                        RecurrenceType.MONTHLY -> stringResource(R.string.rec_monthly)
                        RecurrenceType.CUSTOM -> stringResource(R.string.rec_custom)
                    }
                    Button(
                        onClick = { selectedRecurrence = recurrence },
                        colors = if (recurrence == selectedRecurrence) {
                            ButtonDefaults.buttonColors()
                        } else {
                            ButtonDefaults.outlinedButtonColors()
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(recurrenceText)
                    }
                }
            }
            if (selectedRecurrence == RecurrenceType.CUSTOM) {
                OutlinedTextField(
                    value = customIntervalDays,
                    onValueChange = { customIntervalDays = it },
                    label = { Text(stringResource(R.string.repeat_interval)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.enable_reminder), style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = notificationEnabled,
                    onCheckedChange = { notificationEnabled = it },
                    Modifier.padding(4.dp)
                )
            }
            if (notificationEnabled){
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf(15, 30, 60).forEach { minutes ->
                        Button(
                            onClick = { notifyMinutesBefore = minutes },
                            colors = if (notifyMinutesBefore == minutes) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text("$minutes min")
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.enable_location_reminder), style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = locationNotificationEnabled,
                    onCheckedChange = { locationNotificationEnabled = it },
                    Modifier.padding(4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.locations_label), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
            pendingLocations.forEachIndexed { index, (lat, lng, address) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(address, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Button(
                        onClick = { pendingLocations = pendingLocations.filterIndexed { i, _ -> i != index } },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("X") }
                }
            }
            Button(onClick = { navController.navigate(Routes.LOCATION_PICKER) }) {
                Text(stringResource(R.string.add_location))
            }
            Text(stringResource(R.string.attachments_label), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
            pendingAttachments.forEachIndexed { index, (uri, name, type) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable{
                            val filePath = uri.path ?: ""
                            val file = File(filePath)
                            val contentUri = if (uri.scheme == "file") {
                                androidx.core.content.FileProvider.getUriForFile(
                                    navController.context,
                                    "${navController.context.packageName}.fileprovider",
                                    file
                                )
                            } else {
                                uri
                            }
                            val mimeType = when (type) {
                                AttachmentType.IMAGE -> "image/*"
                                AttachmentType.PDF -> "application/pdf"
                                AttachmentType.AUDIO -> "audio/*"
                                AttachmentType.DOCUMENT -> "*/*"
                            }
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                setDataAndType(contentUri, mimeType)
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            navController.context.startActivity(intent)
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (type == AttachmentType.IMAGE) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(50.dp)
                                .padding(end = 8.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Text(
                        text = name,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp))
                    Button(
                        onClick = {
                            pendingAttachments =
                                pendingAttachments.filterIndexed { i, _ -> i != index }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("X") }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.btn_image), fontSize = 12.sp)
                }
                Button(onClick = { pdfPickerLauncher.launch("application/pdf") }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.btn_pdf), fontSize = 12.sp)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { audioPickerLauncher.launch("audio/*") }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.btn_audio), fontSize = 12.sp)
                }
                Button(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    onClick = {
                        if (audioPermissionState.status.isGranted) {
                            handleAudioRecording()
                        } else {
                            audioPermissionState.launchPermissionRequest()
                        }
                    },
                    colors = if (isRecording) ButtonDefaults.buttonColors(containerColor = Color.Red) else ButtonDefaults.buttonColors()
                ) {
                    Text(if (isRecording) stringResource(R.string.btn_stop) else stringResource(R.string.btn_record), fontSize = 12.sp)
                }
                Button(onClick = { documentPickerLauncher.launch("*/*") }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.btn_file), fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.due_date), style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DueDatePicker(
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    modifier = Modifier.weight(1f))
                DueTimePicker(
                    selectedTime = selectedTime,
                    onTimeSelected = { selectedTime = it },
                    modifier = Modifier.weight(1f))
            }
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.title_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                onClick = {
                    val newTask = Task(
                        title = title,
                        description = description,
                        priority = selectedPriority,
                        dueDate = selectedDate?.atTime(selectedTime ?: LocalTime.of(23, 59)),
                        category = selectedCategory,
                        isTimeNotificationEnabled = notificationEnabled,
                        isLocationNotificationEnabled = locationNotificationEnabled,
                        notifyMinutesBefore = if (notificationEnabled) notifyMinutesBefore else null,
                        recurrenceType = selectedRecurrence,
                        customIntervalDays = if (selectedRecurrence == RecurrenceType.CUSTOM) customIntervalDays.toIntOrNull() else null
                    )
                    viewModel.addTask(newTask) { newTaskId ->
                        pendingLocations.forEach { (lat, lng, address) ->
                            viewModel.addLocationToTask(newTaskId, address, lat, lng)
                        }
                        pendingAttachments.forEach { (uri, name, type) ->
                            viewModel.addAttachment(newTaskId, uri, name, type)
                        }
                        onTaskAdded()
                    }
                }
            ) {
                Text(stringResource(R.string.save_task))
            }
        }
    }
}
