package com.example.circularplanner.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.*
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.example.circularplanner.R
import com.example.circularplanner.data.Task
import com.example.circularplanner.ui.component.ActiveTime
import com.example.circularplanner.ui.state.TaskState
import com.example.circularplanner.ui.component.Calendar
import java.util.UUID
import com.example.circularplanner.ui.component.TaskDial
import com.example.circularplanner.ui.component.TaskList
import com.example.circularplanner.ui.viewmodel.DataViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDisplayScreen(
//    modifier: Modifier = Modifier,
    viewModel: DataViewModel,
    taskState: TaskState,
    onNavigateToTaskEdit: (String?) -> Unit,
    onNavigateToTaskInfo: (String?) -> Unit,
    tasks: List<Task>,
    getTask: (UUID?) -> Task?,
//    removeTask: (UUID) -> Unit
    removeTask: (Task) -> Unit,
//    updateTask: (UUID, String, Time, Time, String, Float?, Float?, Int?) -> Unit,
) {
    val formatter = DateTimeFormatter.ofPattern("d. MMMM yyyy")
    val data = viewModel.uiState.collectAsState()

    var openAlertDialog by remember { mutableStateOf(false) }

    var isList by rememberSaveable { mutableStateOf(false) }

    Scaffold (
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("${data.value.date.format(formatter)}") },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primaryContainer),
            )
        },
        bottomBar = {
            BottomAppBar (
//                contentColor = MaterialTheme.colorScheme.primaryContainer
                actions = {
//                    // Leading icons should typically have a high content alpha
//                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
//                        IconButton(onClick = { /* doSomething() */ }) {
//                            Icon(Icons.Filled.Menu, contentDescription = "Localized description")
//                        }
//                    }
                    // These actions should be at the end of the BottomAppBar. They use the default medium
                    // content alpha provided by BottomAppBar
                    // The Spacer pushes the other icons to the end of the app bar
                    Spacer(Modifier.weight(1f, true))

                    IconButton(
                        onClick = {
                            onNavigateToTaskEdit(null)
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.add_circle_24dp_5f6368_fill0_wght400_grad0_opsz24),
                            contentDescription = "Add task",
                            modifier = Modifier.fillMaxSize(0.8F)
                        )
                    }

                    IconButton(onClick = {
                        isList = !isList
                    }) {
                        if (!isList) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.list_24dp_5f6368_fill0_wght400_grad0_opsz24),
                                contentDescription = "List view",
                                modifier = Modifier.fillMaxSize(0.8F)
                            )
                        }
                        else {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.data_usage_24dp_5f6368_fill0_wght400_grad0_opsz24),
                                contentDescription = "Dial view",
                                modifier = Modifier.fillMaxSize(0.8F)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ActiveTime(
                taskState
            )

            Row(
                modifier = Modifier.fillMaxHeight(0.8f)
            ) {
                AnimatedVisibility(
                    visible = !isList,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    TaskDial(
//                        viewModel = viewModel,
                        taskState = taskState,
                        onNavigateToTaskEdit = onNavigateToTaskEdit,
                        onNavigateToTaskInfo = onNavigateToTaskInfo,
                        tasks = tasks,
                        getTask = getTask,
//                        removeTask = removeTask
//                        updateTask = updateTask
                    )
                }

                AnimatedVisibility(
                    visible = isList,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    TaskList (
//                        viewModel = viewModel,
                        onNavigateToTaskInfo = onNavigateToTaskInfo,
                        tasks = tasks,
                        getTask = getTask,
                        removeTask = removeTask
                    )
                }
            }

            Calendar(
                viewModel = viewModel
            )
        }
    }
}