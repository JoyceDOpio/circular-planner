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
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.state.TaskState
import java.util.UUID
import kotlin.math.floor
import com.example.circularplanner.utils.MainAppBar
import com.example.circularplanner.utils.TaskDial
import com.example.circularplanner.utils.TaskList


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDisplayScreen(
//    modifier: Modifier = Modifier,
//    viewModel: DataViewModel,
    taskState: TaskState,
    onNavigateToTaskEdit: (String?) -> Unit,
    onNavigateToTaskInfo: (String?) -> Unit,
    tasks: List<Task>,
    getTask: (UUID?) -> Task?,
//    removeTask: (UUID) -> Unit
    removeTask: (Task) -> Unit,
//    updateTask: (UUID, String, Time, Time, String, Float?, Float?, Int?) -> Unit,
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
//                modifier = Modifier,
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
}