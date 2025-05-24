package com.example.circularplanner.ui.screen

import android.icu.text.SimpleDateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import java.util.Date
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.state.TaskState
import com.example.circularplanner.ui.viewmodel.DataViewModel
import java.util.UUID
import kotlin.math.floor
import com.example.circularplanner.utils.MainAppBar
import com.example.circularplanner.utils.TaskDial
import com.example.circularplanner.utils.TaskList
import kotlinx.serialization.Serializable

@Serializable
object TaskDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDisplayScreen(
//    modifier: Modifier = Modifier,
//    viewModel: DataViewModel,
    taskState: TaskState,
    onNavigateToTaskEdit: () -> Unit,
    tasks: List<Task>,
    setNewTaskStartTime: (Time) -> Unit,
    setNewTaskEndTime: (Time) -> Unit,
    getTask: (UUID) -> Task?,
//    removeTask: (UUID) -> Unit
    removeTask: (Task) -> Unit
) {
    val simpleDateFormat = SimpleDateFormat("d MMMM")
    val date = simpleDateFormat.format(Date())

    var openAlertDialog by remember { mutableStateOf(false) }

    var isList by rememberSaveable { mutableStateOf(false) }

    @OptIn(ExperimentalMaterial3Api::class)
    fun calculateTimeIntervalInMinutes(start: Time?, end: Time?): Int {
        var minutes = 0

        if(start != null && end != null) {
            minutes = (end.hour - start.hour) * 60
            minutes -= start.minute
            minutes += end.minute
        }

        return minutes
    }

    fun formatTime(minutesTotal: Int): String {
        var hours = minutesTotal / 60
        var minutes = 0

        if (minutesTotal % 60 != 0) {
            hours = floor(hours.toDouble()).toInt()
            minutes = minutesTotal - hours * 60
        }

        return "$hours hr $minutes min"
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            MainAppBar(
//                viewModel = viewModel,
                taskState = taskState,
                onNavigateToTaskEdit = onNavigateToTaskEdit,
                isList = isList,
                onLayoutChangeRequested = { isList = !isList },
                title = "$date"
            )
        }

        Column (
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxHeight(0.9f)
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
                        tasks = tasks
//                        setNewTaskStartTime = setNewTaskStartTime,
//                        setNewTaskEndTime = setNewTaskEndTime,
//                        removeTask = removeTask
                    )
                }

                AnimatedVisibility(
                    visible = isList,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    TaskList (
//                        viewModel = viewModel,
                        taskState = taskState,
                        onNavigateToTaskEdit = onNavigateToTaskEdit,
                        tasks = tasks,
                        getTask = getTask,
                        removeTask = removeTask
                    )
                }
            }

            // Active time
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                horizontalArrangement = Arrangement.Center
            ) {
//                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val startTime = taskState.activeTimeStart
                val endTime = taskState.activeTimeEnd

                Text (text = "Active Time: ${formatTime(calculateTimeIntervalInMinutes(startTime, endTime))}", fontWeight = FontWeight.Bold)
            }
        }
    }

//    if (openAlertDialog) {
//        PopupDialog(
//            onDismissRequest = { openAlertDialog = false },
//            content = {
//                Text(
//                    text = "This is a minimal dialog",
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .wrapContentSize(Alignment.Center),
//                    textAlign = TextAlign.Center,
//                )
//            }
//        )
//    }
}