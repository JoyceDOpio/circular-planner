package com.example.circularplanner.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circularplanner.R
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.component.TimePickerDialog
import com.example.circularplanner.ui.state.TaskState
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    modifier: Modifier = Modifier,
//    viewModel: DataViewModel,
//    data: TaskEdit,
    taskId: String?,
    taskState: TaskState,
    onBack: () -> Unit,
    addTask: (String, Time, Time, String) -> Unit,
    getTask: (UUID) -> Task?,
//    updateTask: (UUID, String, Time, Time, String, Float?, Float?, Int?) -> Unit,
) {
    fun getLabel(taskId: String?): String {
        if (taskId == null) {
            return "Create new task"
        }

        return "Edit task"
    }

//    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

//    val taskId = taskState.taskId
    var label: String = getLabel(taskId)
    var task: Task? = null

    var showTimePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }

    var startTimePickerState: TimePickerState
    var endTimePickerState: TimePickerState

    if (taskId != null) {
        task = getTask(UUID.fromString(taskId))

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
            initialHour = taskState.taskStartTime.hour,
            initialMinute = taskState.taskStartTime.minute
        )
        endTimePickerState = rememberTimePickerState(
            initialHour = taskState.taskEndTime.hour,
            initialMinute = taskState.taskEndTime.minute
        )
    }

    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }

    fun closeTimePicker() {
//        viewModel.setTaskStartTime(Time(startTimePickerState.hour, startTimePickerState.minute))
//        viewModel.setTaskEndTime(Time(endTimePickerState.hour, endTimePickerState.minute))
        //TODO: Validate if task start- and end time are within active time bounds
        taskState.taskStartTime = Time(startTimePickerState.hour, startTimePickerState.minute)
        taskState.taskEndTime = Time(endTimePickerState.hour, endTimePickerState.minute)
//        Log.i("startTimePickerState: ", String.format("%d:%02d", startTimePickerState.hour, startTimePickerState.minute))
//        Log.i("endTimePickerState: ", String.format("%d:%02d", startTimePickerState.hour, startTimePickerState.minute))

        showTimePicker = false
        showStartTimePicker = false
    }

    fun openTimePicker() {
        showTimePicker = true
    }

    Column(
        modifier = modifier
            .padding(10.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row (
            modifier = modifier
                .fillMaxWidth()
                .paddingFromBaseline(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Row (
            modifier = modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title field
            OutlinedTextField(
                value = title,
                onValueChange = { text: String ->
                    title = text
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 20.sp),
                label = { Text("Title") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true
            )
        }

        Row (
            modifier = modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Start time
            Text(
                text = String.format("Start time: %d:%02d", taskState.taskStartTime?.hour, taskState.taskStartTime?.minute),
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
            modifier = modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // End time
            Text(
                text = String.format("End time: %d:%02d", taskState.taskEndTime.hour, taskState.taskEndTime.minute),
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
            modifier = modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Description field
            OutlinedTextField(
                value = description,
                onValueChange = { text: String ->
                    description = text
                },
                modifier = Modifier
//                    .padding(10.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f),
                textStyle = TextStyle(fontSize = 18.sp),
                label = { Text("Description") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                singleLine = false
            )
        }

        Row (
            modifier = modifier
                .fillMaxWidth()
                .paddingFromBaseline(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
//                onClick = onNavigateToTaskDisplay,
                onClick = onBack,
                modifier = modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Cancel")
            }

            Button(
                onClick = {
                    //TODO: Validate start- and end-time input - the values should remain within the active time boundaries
                    if (taskId != null) {
                        if (task != null) {
                            task.title = title
                            task.startTime = Time(startTimePickerState.hour, startTimePickerState.minute)
                            task.endTime = Time(endTimePickerState.hour, endTimePickerState.minute)
                            task.description = description
                        }
//                        updateTask(
//                            taskId,
//                            title,
//                            Time(startTimePickerState.hour, startTimePickerState.minute),
//                            Time(endTimePickerState.hour, endTimePickerState.minute),
//                            description
//                        )
                    } else {
                        addTask(
                            title,
                            Time(startTimePickerState.hour, startTimePickerState.minute),
                            Time(endTimePickerState.hour, endTimePickerState.minute),
                            description
                        )
                    }

//                    onNavigateToTaskDisplay()
                    onBack()
                },
                modifier = modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Save")
            }
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