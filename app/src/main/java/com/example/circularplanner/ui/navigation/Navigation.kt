package com.example.circularplanner.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.screen.ActiveTimeSetUpScreen
import com.example.circularplanner.ui.screen.TaskDisplayScreen
import com.example.circularplanner.ui.screen.TaskEditScreen
import com.example.circularplanner.ui.screen.TaskInfoScreen
import com.example.circularplanner.ui.state.rememberTaskState
import com.example.circularplanner.ui.viewmodel.DataViewModel
import java.util.UUID
import java.util.function.Predicate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val dataViewModel: DataViewModel = viewModel()
    val taskState = rememberTaskState()

    var tasks = remember { mutableStateListOf<Task>()}
//    var tasks = remember { mutableStateListOf(
//        Task("feed the dog", Time(7,0), Time(7,15), description = "Make sure that the cat doesn't eat the dog's food."),
//        Task("feed the cat", Time(6,30), Time(6,45), description = "He's favourite food is lasagna (although he'll eat pretty much anything you give him)."),
//        Task("read a book", Time(8,0), Time(9,0)),
//        Task("go for a walk", Time(9,30), Time(10,0)),
//        Task("call Jola", Time(10,30), Time(11,0)),
//        Task("go to the sauna", Time(11,5), Time(11,12))
//    ) }

    var sortedTasks = tasks.sortedWith(object : Comparator <Task> {
        override fun compare (first: Task, second: Task) : Int {
            if (first.startTime.hour < second.startTime.hour) {
                return -1
            } else if (first.startTime.hour > second.startTime.hour) {
                return 1
            } else {
                if (first.startTime.minute < second.startTime.minute) {
                    return -1
                } else if (first.startTime.minute > second.startTime.minute) {
                    return 1
                } else {
                    return 0
                }
            }
        }
    })

    fun addTask(title: String, startTime: Time, endTime: Time, description: String) {
        var newTask = Task(title, startTime, endTime, description = description)
        tasks.add(newTask)
    }

    fun getTask(id: UUID?): Task? {
        if (id != null) {
            return tasks.find { task: Task -> task.id == id }
        }

        return null
    }

//    fun removeTask(id: UUID) {
//        val condition = Predicate<Task> {
//            it.id == id
//        }
//
//        tasks.removeIf(condition)
//    }

    fun removeTask(task: Task) {
        val condition = Predicate<Task> {
            it.id == task.id
        }

        tasks.removeIf(condition)
    }

    fun updateTask(
        id: UUID,
        title: String,
        startTime: Time,
        endTime: Time,
        description: String,
        startAngle: Float?,
        endAngle: Float?,
        durationInMinutes: Int?
    ) {
        var task = tasks.find { it.id == id }

        if (task != null) {
            task.title = title
            task.startTime = startTime
            task.endTime = endTime
            task.description = description
        }
    }

    NavHost(
        navController = navController,
        startDestination = ActiveTimeSetUp
    ) {
        composable<ActiveTimeSetUp> {
            ActiveTimeSetUpScreen(
//                viewModel = dataViewModel,
                taskState = taskState,
                onNavigateToTaskDisplay = {
                    navController.navigate(route = TaskDisplayRoute)
                }
            )
        }

        composable<TaskDisplayRoute> {
            TaskDisplayScreen(
//                viewModel = dataViewModel,
                taskState = taskState,
                onNavigateToTaskEdit = { id ->
                    navController.navigate(route = TaskEditRoute(id = id))
                },
                onNavigateToTaskInfo = { id ->
                    navController.navigate(route = TaskInfoRoute(id = id!!))
                },
                tasks = sortedTasks,
//                addTask = ::addTask,
                getTask = ::getTask,
                removeTask = ::removeTask,
//                updateTask = ::updateTask
            )
        }

        composable<TaskEditRoute> { backStackEntry ->
            val data: TaskEditRoute = backStackEntry.toRoute<TaskEditRoute>()

            TaskEditScreen(
//                viewModel = dataViewModel,
//                data = data,
                taskId = data.id,
                taskState = taskState,
                onBack = {
                    navController.popBackStack()
                },
                addTask = ::addTask,
                getTask = ::getTask,
//                updateTask = ::updateTask
            )
        }

        composable<TaskInfoRoute> { backStackEntry ->
            val data: TaskInfoRoute = backStackEntry.toRoute<TaskInfoRoute>()

            TaskInfoScreen(
//                viewModel = dataViewModel,
                taskId = data.id,
                onCancel = {
                    navController.popBackStack()
                },
                onNavigateToTaskEdit = {
                    navController.navigate(route = TaskEditRoute(id = data.id))
                },
                getTask = ::getTask
            )
        }
    }
}