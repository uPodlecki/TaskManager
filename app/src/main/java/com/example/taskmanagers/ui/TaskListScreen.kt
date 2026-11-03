package com.example.taskmanagers.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.taskmanagers.R
import com.example.taskmanagers.data.local.Task
import com.example.taskmanagers.data.local.TaskCategory
import com.example.taskmanagers.data.local.TaskPriority
import com.example.taskmanagers.data.local.TaskStatus
import com.example.taskmanagers.viewmodel.TaskView
import com.example.taskmanagers.viewmodel.TaskViewModel


@Composable
fun formatTaskDate(dateTime: java.time.LocalDateTime): String {
    val date = dateTime.toLocalDate()
    val today = java.time.LocalDate.now()
    val tomorrow = today.plusDays(1)
    val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")

    return when (date) {
        today -> stringResource(R.string.today_at, dateTime.format(timeFormatter))
        tomorrow -> stringResource(R.string.tomorrow_at, dateTime.format(timeFormatter))
        else -> dateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
    }
}
@Composable
fun TaskItem(task: Task, onClick:()-> Unit, modifier: Modifier = Modifier) {
    val targetColor = priorityColor(task.priority)
    val animatedPriorityColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 400),
        label = "priorityColorAnimation"
    )
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min)
        ) { Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(6.dp)
                .background(animatedPriorityColor)
        )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                val priorityText = when(task.priority) {
                    TaskPriority.LOW -> stringResource(R.string.priority_low)
                    TaskPriority.MEDIUM -> stringResource(R.string.priority_medium)
                    TaskPriority.HIGH -> stringResource(R.string.priority_high)
                }
                Text(text = stringResource(R.string.priority) + ": " + priorityText)
                Spacer(modifier = Modifier.height(4.dp))
                val statusText = when(task.status) {
                    TaskStatus.TODO -> stringResource(R.string.status_todo)
                    TaskStatus.IN_PROGRESS -> stringResource(R.string.status_in_progress)
                    TaskStatus.COMPLETED -> stringResource(R.string.status_completed)
                }
                Text(text = stringResource(R.string.status) + ": " + statusText)
                Spacer(modifier = Modifier.height(8.dp))
                val categoryIcon = when(task.category) {
                    TaskCategory.WORK -> Icons.Default.Work
                    TaskCategory.PERSONAL -> Icons.Default.Person
                    TaskCategory.SHOPPING -> Icons.Default.ShoppingCart
                    else -> Icons.Default.List
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val categoryText = when(task.category) {
                        TaskCategory.WORK -> stringResource(R.string.cat_work)
                        TaskCategory.PERSONAL -> stringResource(R.string.cat_personal)
                        TaskCategory.SHOPPING -> stringResource(R.string.cat_shopping)
                        TaskCategory.OTHER -> stringResource(R.string.cat_other)
                    }
                    Text(text = categoryText)
                }
                val isOverdue = task.dueDate?.isBefore(java.time.LocalDateTime.now()) == true && task.status != TaskStatus.COMPLETED
                val dateColor = if (isOverdue) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                task.dueDate?.let { dueDate ->
                    Text(
                        text = stringResource(R.string.deadline_label, formatTaskDate(dueDate)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = dateColor,
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTaskItem(
    modifier: Modifier = Modifier,
    task: Task,
    onClick: () -> Unit,
    onMarkCompleted: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onMarkCompleted()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    showDeleteDialog = true
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        backgroundContent = {
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
                SwipeToDismissBoxValue.EndToStart -> Color(0xFFF44336)
                SwipeToDismissBoxValue.Settled -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    else -> Alignment.CenterEnd
                }
            ) {
                val icon = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                    Icons.Default.Check
                } else {
                    Icons.Default.Delete
                }
                Icon(icon, contentDescription = null, tint = Color.White)
            }
        }
    ) {
        TaskItem(task = task, onClick = onClick)
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
                                onDelete()
                                showDeleteDialog = false
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onAddTaskClick: () -> Unit,
    onTaskClick: (Long) -> Unit,
    onSettingsClick: () -> Unit) {
    val tasks by viewModel.sortedAndFilteredTasks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedView by viewModel.selectedView.collectAsState()
    var showQuickAdd by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val listState = rememberLazyListState()
    var previousSize by remember { mutableStateOf(tasks.size) }
    LaunchedEffect(tasks.size) {
        if (tasks.size > previousSize) {
            listState.animateScrollToItem(0)
        }
        previousSize = tasks.size
    }
    if (showQuickAdd) {
        ModalBottomSheet(
            onDismissRequest = { showQuickAdd = false },
            sheetState = sheetState
        ) {
            var quickTitle by remember { mutableStateOf("") }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(stringResource(R.string.quick_add_title), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quickTitle,
                    onValueChange = { quickTitle = it },
                    label = { Text(stringResource(R.string.title_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (quickTitle.isNotBlank()) {
                            viewModel.addTask(
                                Task(
                                    title = quickTitle,
                                    priority = TaskPriority.MEDIUM
                                )
                            )
                            quickTitle = ""
                            showQuickAdd = false
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.save_task))
                }
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_tasks), color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary),
                            actions = {
                    IconButton(onClick = { showQuickAdd = true }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.quick_add_title))
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskClick,
                containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery ?: "",
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text(stringResource(R.string.search_hint)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (!searchQuery.isNullOrEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                        }
                    }
                }
            )
            SortDropdown(currentSort = sortOption, onSortSelected = {viewModel.setSortOption(it)})
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.view), style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)

            ) {
                TaskView.entries.forEach { view ->
                    val viewText = when(view) {
                        TaskView.ALL -> stringResource(R.string.all)
                        TaskView.TODAY -> stringResource(R.string.view_today)
                        TaskView.OVERDUE -> stringResource(R.string.view_overdue)
                    }
                    Button(
                        onClick = {viewModel.setSelectedView(view)},
                        colors = if (selectedView == view) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                        shape = RoundedCornerShape(16.dp)) {
                        Text(viewText)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.status), style = MaterialTheme.typography.labelMedium)
            Row(    modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.setStatus(null) },
                    colors = if (selectedStatus == null) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                    shape = RoundedCornerShape(16.dp)) {
                    Text(stringResource(R.string.all))
                }
                TaskStatus.entries.forEach { status ->
                    val statusText = when(status) {
                        TaskStatus.TODO -> stringResource(R.string.status_todo)
                        TaskStatus.IN_PROGRESS -> stringResource(R.string.status_in_progress)
                        TaskStatus.COMPLETED -> stringResource(R.string.status_completed)
                    }
                    Button(
                        onClick = {viewModel.setStatus(status)},
                        colors = if (selectedStatus == status) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                        shape = RoundedCornerShape(16.dp)) {
                        Text(statusText)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.category), style = MaterialTheme.typography.labelMedium)
            Row(    modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.setCategory(null) },
                    colors = if (selectedCategory == null) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                    shape = RoundedCornerShape(16.dp)) { Text(stringResource(R.string.all)) }

                TaskCategory.entries.forEach { category ->
                    val icon = when(category) {
                        TaskCategory.WORK -> Icons.Default.Work
                        TaskCategory.PERSONAL -> Icons.Default.Person
                        TaskCategory.SHOPPING -> Icons.Default.ShoppingCart
                        else -> Icons.Default.List
                    }
                    val categoryText = when(category) {
                        TaskCategory.WORK -> stringResource(R.string.cat_work)
                        TaskCategory.PERSONAL -> stringResource(R.string.cat_personal)
                        TaskCategory.SHOPPING -> stringResource(R.string.cat_shopping)
                        TaskCategory.OTHER -> stringResource(R.string.cat_other)
                    }
                    Button(
                        onClick = { viewModel.setCategory(category) },
                        colors = if (selectedCategory == category) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                        shape = RoundedCornerShape(16.dp)) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(categoryText)
                    }
                }
            }
        if(tasks.isEmpty()){Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_tasks),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
                LazyColumn(state = listState) {
                    items(tasks, key = { it.id }) { task ->
                        SwipeableTaskItem(
                            modifier = Modifier.animateItem(),
                            task = task,
                            onClick = { onTaskClick(task.id) },
                            onMarkCompleted = { viewModel.markAsCompleted((task)) },
                            onDelete = { viewModel.removeTask(task) }
                        )
                    }
                }
            }
        }
    }
}

fun priorityColor(priority: TaskPriority): Color{
    return when (priority){
        TaskPriority.LOW -> Color(0xFF4CAF50)
        TaskPriority.MEDIUM -> Color(0xFFFF9800)
        TaskPriority.HIGH -> Color(0xFFF44336)
    }
}
