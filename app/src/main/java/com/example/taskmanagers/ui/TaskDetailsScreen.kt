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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.taskmanagers.R
import com.example.taskmanagers.data.local.AttachmentType
import com.example.taskmanagers.data.local.RecurrenceType
import com.example.taskmanagers.data.local.TaskCategory
import com.example.taskmanagers.data.local.TaskPriority
import com.example.taskmanagers.data.local.TaskStatus
import com.example.taskmanagers.navigation.Routes
import com.example.taskmanagers.utils.AudioRecorder
import com.example.taskmanagers.utils.FileUtils
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
fun TaskDetailsScreen(
    viewModel: TaskViewModel,
    taskId: Long,
    onTaskUpdated: () -> Unit,
    onTaskDeleted: () -> Unit,
    navController: NavHostController
) {
    val task by remember(taskId) { viewModel.getTaskById(taskId)}.collectAsState()

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var selectedPriority by rememberSaveable { mutableStateOf(TaskPriority.MEDIUM) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var selectedStatus by rememberSaveable { mutableStateOf(TaskStatus.TODO) }
    var notificationEnabled by rememberSaveable { mutableStateOf(false) }
    var locationNotificationEnabled by rememberSaveable { mutableStateOf(false) }
    var notifyMinutesBefore by rememberSaveable { mutableStateOf(15) }
    var selectedRecurrence by rememberSaveable { mutableStateOf(RecurrenceType.NONE) }
    var customIntervalDays by rememberSaveable { mutableStateOf("") }
    val taskLocations by remember(taskId) { viewModel.getLocationsForTask(taskId) }.collectAsState()
    val taskAttachments by remember(taskId) { viewModel.getAttachmentsForTask(taskId) }.collectAsState()
    var selectedCategory by rememberSaveable { mutableStateOf(TaskCategory.OTHER) }
    
    var isInitialized by rememberSaveable { mutableStateOf(false) }

    val recorder = remember { AudioRecorder(navController.context) }
    var isRecording by remember { mutableStateOf(false) }
    var showDeleteDialog by remember {mutableStateOf(false)}
    val audioPermissionState = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val realName = FileUtils.getFileNameFromUri(navController.context, it)
            viewModel.addAttachment(taskId, it, realName, AttachmentType.IMAGE) }
    }
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val realName = FileUtils.getFileNameFromUri(navController.context, it)
            viewModel.addAttachment(taskId, it, realName, AttachmentType.PDF) }
    }
    val audioPickerLauncher = rememberLauncherForActivityResult(    contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val realName = FileUtils.getFileNameFromUri(navController.context, it)
            viewModel.addAttachment(taskId, it, realName, AttachmentType.AUDIO) }
    }
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val realName = FileUtils.getFileNameFromUri(navController.context, it)
            viewModel.addAttachment(taskId, it, realName, AttachmentType.DOCUMENT) }
    }

    fun handleAudioRecording() {
        if (isRecording) {
            recorder.stop()
            isRecording = false
            val file = File(navController.context.cacheDir, "temp_recording.mp3")
            val uri = Uri.fromFile(file)
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            val name = "Voice Note $timestamp"
            viewModel.addAttachment(taskId, uri, name, AttachmentType.AUDIO)
        } else {
            val file = File(navController.context.cacheDir, "temp_recording.mp3")
            recorder.start(file)
            isRecording = true
        }
    }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val selectedLocation = savedStateHandle?.get<Triple<Double, Double, String>>("selected_location")

    LaunchedEffect(task) {
        if (!isInitialized && task != null) {
            task?.let {
                title = it.title
                description = it.description ?: ""
                selectedPriority = it.priority
                selectedDate = it.dueDate?.toLocalDate()
                selectedTime = it.dueDate?.toLocalTime()
                selectedStatus = it.status
                selectedCategory = it.category
                notificationEnabled = it.isTimeNotificationEnabled
                locationNotificationEnabled = it.isLocationNotificationEnabled
                notifyMinutesBefore = it.notifyMinutesBefore ?: 15
                selectedRecurrence = it.recurrenceType
                customIntervalDays = it.customIntervalDays?.toString() ?: ""
            }
            isInitialized = true
        }
    }

    LaunchedEffect(selectedLocation) {
        selectedLocation?.let { (lat, lng, address) ->
            viewModel.addLocationToTask(taskId, address, lat, lng)
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

            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.status), style = MaterialTheme.typography.labelMedium)
            Row(modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())) {
                TaskStatus.entries.forEach { status ->
                    val statusText = when(status) {
                        TaskStatus.TODO -> stringResource(R.string.status_todo)
                        TaskStatus.IN_PROGRESS -> stringResource(R.string.status_in_progress)
                        TaskStatus.COMPLETED -> stringResource(R.string.status_completed)
                    }
                    Button(
                        onClick = { selectedStatus = status},
                        colors = if (status == selectedStatus) {
                            ButtonDefaults.buttonColors()
                        } else {
                            ButtonDefaults.outlinedButtonColors()
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(statusText)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
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
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(categoryText)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.recurrence), style = MaterialTheme.typography.labelMedium)
            Row(modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())) {
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
            Spacer(modifier = Modifier.height(16.dp))
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
            Spacer(modifier = Modifier.height(16.dp))
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
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.locations_label), style = MaterialTheme.typography.labelMedium)
            taskLocations.forEach { location ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(location.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Row {
                        Button(
                            onClick = {
                                val uri = "google.navigation:q=${location.latitude},${location.longitude}".toUri()
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                navController.context.startActivity(intent)
                            },
                            modifier = Modifier.padding(end = 4.dp)
                        ) { Text("Route") }

                        Button(
                            onClick = { viewModel.removeLocation(location) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) { Text("X") }
                    }
                }
            }
            Button(
                onClick = { navController.navigate(Routes.LOCATION_PICKER) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(stringResource(R.string.add_location))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.attachments_label), style = MaterialTheme.typography.labelMedium)
            taskAttachments.forEach { attachment ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            val file = File(attachment.filePath)
                            val contentUri = androidx.core.content.FileProvider.getUriForFile(
                                navController.context,
                                "${navController.context.packageName}.fileprovider",
                                file
                            )
                            val mimeType = when (attachment.type) {
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (attachment.type == AttachmentType.IMAGE) {
                        AsyncImage(
                            model = attachment.filePath,
                            contentDescription = null,
                            modifier = Modifier
                                .size(50.dp)
                                .padding(end = 8.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Text(text = attachment.fileName, modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp))
                    Button(
                        onClick = { viewModel.removeAttachment(attachment) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("X") }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.btn_image), fontSize = 12.sp)
                }
                Button(
                    onClick = { pdfPickerLauncher.launch("application/pdf") },
                    modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.btn_pdf), fontSize = 12.sp)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { audioPickerLauncher.launch("audio/*") },
                    modifier = Modifier.weight(1f)) {
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
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.title_label)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(stringResource(R.string.description_label)) }, modifier = Modifier.fillMaxWidth())

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                onClick = {
                    task?.let { currentTask ->
                        val updatedTask = currentTask.copy(
                            title = title,
                            description = description,
                            priority = selectedPriority,
                            dueDate = selectedDate?.atTime(selectedTime ?: LocalTime.of(23, 59)),
                            status = selectedStatus,
                            category = selectedCategory,
                            isTimeNotificationEnabled = notificationEnabled,
                            isLocationNotificationEnabled = locationNotificationEnabled,
                            notifyMinutesBefore = if (notificationEnabled) notifyMinutesBefore else null,
                            recurrenceType = selectedRecurrence,
                            customIntervalDays = if (selectedRecurrence == RecurrenceType.CUSTOM) customIntervalDays.toIntOrNull() else null
                        )
                        viewModel.updateExistingTask(updatedTask)
                        onTaskUpdated()
                    }
                }
            ) {
                Text(stringResource(R.string.save_changes))
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                onClick = {
                    showDeleteDialog = true
                }
            ) {
                Text(stringResource(R.string.delete_task))
            }
        }
    }
    if (showDeleteDialog) {
        Dialog(onDismissRequest = { showDeleteDialog = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.delete_dialog_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.delete_dialog_msg),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { showDeleteDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(stringResource(R.string.btn_cancel))
                        }
                        Button(
                            onClick = {
                                task?.let { viewModel.removeTask(it) }
                                showDeleteDialog = false
                                onTaskDeleted()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text(stringResource(R.string.btn_delete))
                        }
                    }
                }
            }
        }
    }
}
