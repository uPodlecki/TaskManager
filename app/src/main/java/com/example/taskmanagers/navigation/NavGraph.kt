package com.example.taskmanagers.navigation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.taskmanagers.ui.AddTaskScreen
import com.example.taskmanagers.ui.LocationPickerScreen
import com.example.taskmanagers.ui.SettingsScreen
import com.example.taskmanagers.ui.TaskDetailsScreen
import com.example.taskmanagers.ui.TaskListScreen
import com.example.taskmanagers.viewmodel.SettingsViewModel
import com.example.taskmanagers.viewmodel.TaskViewModel


object Routes {
    const val TASK_LIST = "task_list"
    const val ADD_TASK = "add_task"
    const val TASK_DETAILS = "task_details/{taskId}"
    const val SETTINGS = "settings"
    const val LOCATION_PICKER = "location_picker"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    taskViewModel: TaskViewModel,
    settingsViewModel: SettingsViewModel,
    notificationTaskId: Long) {
    LaunchedEffect(notificationTaskId) {
        if(notificationTaskId != -1L){
            navController.navigate("task_details/$notificationTaskId")
        }
    }
    NavHost(
        navController = navController,
        startDestination = Routes.TASK_LIST
    ) {
        composable(Routes.TASK_LIST) {
            TaskListScreen(
                viewModel = taskViewModel,
                onAddTaskClick = { navController.navigate(Routes.ADD_TASK) },
                onTaskClick = {taskId -> navController.navigate("task_details/$taskId")},
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.ADD_TASK) {
            AddTaskScreen(
                viewModel = taskViewModel,
                settingsViewModel = settingsViewModel,
                navController = navController,
                onTaskAdded = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(viewModel = settingsViewModel)
        }
        composable(
            route = Routes.TASK_DETAILS,
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
            TaskDetailsScreen(
                viewModel = taskViewModel,
                navController = navController,
                taskId = taskId,
                onTaskUpdated = { navController.popBackStack() },
                onTaskDeleted = { navController.popBackStack() }
            )
        }
        composable(Routes.LOCATION_PICKER) {
            LocationPickerScreen(
                onLocationSelected = { lat, lng, address ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("selected_location", Triple(lat, lng, address))
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}