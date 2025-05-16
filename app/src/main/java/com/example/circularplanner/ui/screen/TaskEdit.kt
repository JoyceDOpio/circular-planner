package com.example.circularplanner.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.circularplanner.R
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.common.TimePickerDialog
import com.example.circularplanner.ui.viewmodel.DataViewModel
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Serializable
object TaskEdit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    viewModel: DataViewModel,
    onNavigateToTaskDisplay: () -> Unit,
    startTime: Time = Time(LocalDateTime.now().hour, LocalDateTime.now().minute),
    endTime: Time = Time(LocalDateTime.now().hour + 60, LocalDateTime.now().minute),
    addTask: (String, Time, Time, String) -> Unit,
    getTask: (UUID) -> Task?,
    updateTask: (UUID, String, Time, Time, String) -> Unit
) {
    fun getLabel(taskId: UUID?): String {
        if (taskId == null) {
            return "Create new task"
        }

        return "Edit task"
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val taskId = uiState.taskId
    var label: String = getLabel(taskId)
    var task: Task? = null

    var showTimePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }

    var startTimePickerState: TimePickerState
    var endTimePickerState: TimePickerState

    if (taskId != null) {
        task = getTask(taskId)

        startTimePickerState = rememberTimePickerState(
            initialHour = task!!.startTime.hour!!,
            initialMinute = task!!.startTime.minute!!
        )
        endTimePickerState = rememberTimePickerState(
            initialHour = task!!.endTime.hour!!,
            initialMinute = task!!.endTime.minute!!
        )
    } else {
        startTimePickerState = rememberTimePickerState(
            initialHour = startTime.hour!!,
            initialMinute = startTime.minute!!
        )
        endTimePickerState = rememberTimePickerState(
            initialHour = endTime.hour!!,
            initialMinute = endTime.minute!!
        )
    }

    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }

    fun closeTimePicker() {
        showTimePicker = false
        showStartTimePicker = false
    }

    fun openTimePicker() {
        showTimePicker = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column () {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 20.sp)

                Row () {
                    IconButton(onClick = {
                        //TODO: Validate start- and end-time input - the values should not overlap with other tasks and should remain within the active time boundaries
                        if (taskId != null) {
                            updateTask(taskId, title, Time(startTimePickerState.hour, startTimePickerState.minute), Time(endTimePickerState.hour, endTimePickerState.minute), description)
                        } else {
                            addTask(title, Time(startTimePickerState.hour, startTimePickerState.minute), Time(endTimePickerState.hour, endTimePickerState.minute), description)
                        }

                        onNavigateToTaskDisplay()
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.save_24dp_5f6368_fill0_wght400_grad0_opsz24),
                            contentDescription = "Save task",
                            modifier = Modifier.fillMaxSize(0.8F)
                        )
                    }

                    IconButton(
                        onClick = onNavigateToTaskDisplay
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.cancel_24dp_5f6368_fill0_wght400_grad0_opsz24),
                            contentDescription = "Cancel",
                            modifier = Modifier.fillMaxSize(0.8F)
                        )
                    }
                }
            }
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
//                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = { text: String ->
                        title = text
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Title") },
                    textStyle = TextStyle(fontSize = 20.sp)
                )
            }

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Start time
                Text(
                    text = String.format("Start time: %d:%02d", startTimePickerState.hour, startTimePickerState.minute),
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    showStartTimePicker = true
                    openTimePicker()
                }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.schedule_24dp_5f6368_fill0_wght400_grad0_opsz24),
                        contentDescription = "Open time picker",
                        modifier = Modifier.fillMaxSize(0.8F)
                    )
                }
            }

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // End time
                Text(
                    text = String.format("End time: %d:%02d", endTimePickerState.hour, endTimePickerState.minute),
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    openTimePicker()
                }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.schedule_24dp_5f6368_fill0_wght400_grad0_opsz24),
                        contentDescription = "Open time picker",
                        modifier = Modifier.fillMaxSize(0.8F)
                    )
                }
            }

            Row (
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { text: String ->
                        description = text
                    },
                    singleLine = false,
                    label = { Text("Description") },
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f),
                    textStyle = TextStyle(fontSize = 18.sp)
                )
            }
        }

        if (showTimePicker) {
            TimePickerDialog(
                onDismissRequest = {
                    closeTimePicker()
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            closeTimePicker()
                        }
                    ) { Text("OK") } },
                dismissButton = {
                    TextButton(
                        onClick = {
                            closeTimePicker()
                        }
                    ) { Text("Cancel") }
                }
            )
            {
                if (showStartTimePicker) {
                    TimePicker(state = startTimePickerState)
                } else {
                    TimePicker(state = endTimePickerState)
                }
            }
        }
    }
}