package com.example.circularplanner.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
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
import androidx.compose.ui.unit.dp
import com.example.circularplanner.R
import com.example.circularplanner.ui.ActiveTime
import com.example.circularplanner.ui.ActiveTimePicker
import com.example.circularplanner.ui.common.TimePickerDialog
import kotlinx.serialization.Serializable
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.state.TaskState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveTimeSetUpScreen (
//    viewModel: DataViewModel = viewModel(),
    taskState: TaskState,
    modifier: Modifier = Modifier,
    onNavigateToTaskDisplay: () -> Unit
){
    var startActiveTimePickerState = rememberTimePickerState()
    var endActiveTimePickerState = rememberTimePickerState()
    var showTimePicker by remember { mutableStateOf(false) }
    var showStartActiveTimePicker by remember { mutableStateOf(false) }

    fun closeTimePicker() {
        showTimePicker = false
        showStartActiveTimePicker = false
    }

    fun validateActiveTime(start: Time, end: Time): Boolean {
        if (start.hour == 0 && start.minute == 0) {
            if (end.hour >= start.hour || end.minute >= start.minute) {
                return true
            }
        } else {
            if (end.hour == start.hour) {
                if (end.minute > start.minute) {
                    return true
                }
            } else if (end.hour > start.hour) {
                return true
            }
        }

        return false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        Column(
            modifier = modifier
//                .fillMaxSize()
                .padding(vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceAround,
//            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(5.dp),
//                .weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                )
                {
//                    IconButton(onClick = {
//                        //TODO: open info popup
//                    }) {
//                        Icon(
//                            imageVector = ImageVector.vectorResource(id = R.drawable.info_27dp_5f6368_fill0_wght400_grad0_opsz24),
//                            contentDescription = stringResource(id = R.string.info_content_desc),
//                            //                        modifier = Modifier.fillMaxSize(0.25F)
//                        )
//                    }

                    ActiveTimePicker(
                        startActiveTimePickerState,
                        ActiveTime.START
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        showTimePicker = true
                        showStartActiveTimePicker = true
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.schedule_24dp_5f6368_fill0_wght400_grad0_opsz24),
                            contentDescription = "Open active time start setter",
                            modifier = Modifier.fillMaxSize(0.8f)
                        )
                    }
                }

            }

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(5.dp),
//                .weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,

                    ) {
//                    IconButton(onClick = {
//                        //TODO: open info popup
//                    }) {
//                        Icon(
//                            imageVector = ImageVector.vectorResource(id = R.drawable.info_27dp_5f6368_fill0_wght400_grad0_opsz24),
//                            contentDescription = stringResource(id = R.string.info_content_desc),
//                            //                        modifier = Modifier.fillMaxSize(0.25F)
//                        )
//                    }

                    ActiveTimePicker(
                        endActiveTimePickerState,
                        ActiveTime.END
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        showTimePicker = true
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.schedule_24dp_5f6368_fill0_wght400_grad0_opsz24),
                            contentDescription = "Open active time end setter",
                            modifier = Modifier.fillMaxSize(0.8F)
                        )
                    }
                }
            }
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .paddingFromBaseline(top = 5.dp),
//                .weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically

        ) {
            Button(
                onClick = {
                    val startTime = Time(startActiveTimePickerState.hour, startActiveTimePickerState.minute)
                    val endTime = Time(endActiveTimePickerState.hour, endActiveTimePickerState.minute)
                    val isActiveTimeValid = validateActiveTime(startTime, endTime)

                    if(isActiveTimeValid) {
    //                    viewModel.setActiveTime(startTime, endTime)
                        taskState.activeTimeStart = startTime
                        taskState.activeTimeEnd = endTime
                        onNavigateToTaskDisplay()
                    } else {
                        //TODO: Display notification about invalid input
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
//                shape = Shapes(RectangleShape.createOutline())
            ) {
                Text(text = "Calculate active time")
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
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        closeTimePicker()
                    }
                ) { Text("Cancel") }
            }
        )
        {
            if (showStartActiveTimePicker) {
                TimePicker(state = startActiveTimePickerState)
            } else {
                TimePicker(state = endActiveTimePickerState)
            }
        }
    }
}