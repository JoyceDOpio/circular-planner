package com.example.circularplanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.circularplanner.ui.screen.TaskDisplayScreen
import com.example.circularplanner.ui.screen.TaskEditScreen
import com.example.circularplanner.ui.screen.TaskInfoScreen
import com.example.circularplanner.ui.viewmodel.TaskDisplayViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun Navigation(
//    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val taskViewModel: TaskDisplayViewModel = viewModel(factory = TaskDisplayViewModel.Factory)
    val uiState by taskViewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = TaskDisplayRoute
    ) {
        composable<TaskDisplayRoute> {
            TaskDisplayScreen(
                uiState = uiState,
                deleteTask = { task ->
                    taskViewModel.deleteTask(task)
                },
                onChangeDisplayForm = taskViewModel::setIsList,
                onNavigateToTaskEdit = { id ->
                    navController.navigate(route = TaskEditRoute(id = id))
                },
                onNavigateToTaskInfo = { id ->
                    navController.navigate(route = TaskInfoRoute(id = id!!))
                },
                onClickSaveActiveTime = {
                    taskViewModel.saveDay()
                    taskViewModel.setIsActiveTimeSetUp(false)
                },
                saveTask = taskViewModel::saveTask,
                selectTask = taskViewModel::selectTask,
                setActiveTimeStart = taskViewModel::setActiveTimeStart,
                setActiveTimeEnd = taskViewModel::setActiveTimeEnd,
                setIsActiveTimeSetUp = taskViewModel::setIsActiveTimeSetUp,
                onSetSelectedDate = { date ->
                    taskViewModel.setSelectedDate(date)
                },
                setTaskStartTime = taskViewModel::setTaskStartTime,
                setTaskEndTime = taskViewModel::setTaskEndTime
            )
        }

        composable<TaskEditRoute> { backStackEntry ->
            val data: TaskEditRoute = backStackEntry.toRoute<TaskEditRoute>()
            val taskId = if (data.id != null) UUID.fromString(data.id) else null

            TaskEditScreen(
//                viewModel = taskEditViewModel,
//                taskId = taskId,
                uiState = uiState,
                onBack = {
                    navController.popBackStack()
                },
                saveTask = {
                    taskViewModel.saveTask()
                },
                setTaskStartTime = taskViewModel::setTaskStartTime,
                setTaskEndTime = taskViewModel::setTaskEndTime,
                setTaskDescription = taskViewModel::setTaskDescription,
                setTaskTitle = taskViewModel::setTaskTitle,
                reset = {
                    coroutineScope.launch {
                        // Wait 1 second before resetting the TaskDetails
                        delay(1000L)
                        taskViewModel.resetTaskDetails()
                    }
                }
            )
        }

        composable<TaskInfoRoute> { backStackEntry ->
            val data: TaskInfoRoute = backStackEntry.toRoute<TaskInfoRoute>()

            TaskInfoScreen(
//                taskId = taskId,
                uiState = uiState,
                onCancel = {
                    navController.popBackStack()
                },
                onNavigateToTaskEdit = {
                    navController.navigate(route = TaskEditRoute(id = data.id))
                },
                reset = {
                    coroutineScope.launch {
                        // Wait 1 second before resetting the TaskDetails
                        delay(1000L)
                        taskViewModel.resetTaskDetails()
                    }
                }
            )
        }
    }
}